package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.BudgetWithDetails
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryType
import com.example.data.model.NotificationEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.RecurringWithDetails
import com.example.data.model.SavingsContributionEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.SavingsGoalWithContributions
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE isArchived = 0 ORDER BY isDefault DESC, createdAt ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("UPDATE accounts SET balanceMinorUnits = balanceMinorUnits + :deltaMinorUnits WHERE id = :accountId")
    suspend fun adjustAccountBalance(accountId: Long, deltaMinorUnits: Long)

    @Query("UPDATE accounts SET isDefault = CASE WHEN id = :defaultId THEN 1 ELSE 0 END")
    suspend fun setDefaultAccount(defaultId: Long)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type AND isArchived = 0 ORDER BY name ASC")
    fun getCategoriesByType(type: CategoryType): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("UPDATE categories SET isArchived = 1 WHERE id = :id")
    suspend fun archiveCategory(id: Long)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}

@Dao
interface TransactionDao {
    @Transaction
    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC, id DESC")
    fun getAllTransactionsWithDetails(): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC, id DESC LIMIT :limit")
    fun getRecentTransactionsWithDetails(limit: Int = 10): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE dateTimestamp BETWEEN :startDate AND :endDate ORDER BY dateTimestamp DESC, id DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE accountId = :accountId OR toAccountId = :accountId ORDER BY dateTimestamp DESC, id DESC")
    fun getTransactionsForAccount(accountId: Long): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionWithDetails?

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getRawTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT SUM(amountMinorUnits) FROM transactions WHERE type = :type AND dateTimestamp BETWEEN :startDate AND :endDate")
    suspend fun getTotalAmountByTypeAndRange(type: TransactionType, startDate: Long, endDate: Long): Long?

    @Query("SELECT SUM(amountMinorUnits) FROM transactions WHERE type = 'EXPENSE' AND categoryId = :categoryId AND dateTimestamp BETWEEN :startDate AND :endDate")
    suspend fun getTotalExpenseForCategoryAndRange(categoryId: Long, startDate: Long, endDate: Long): Long?

    @Query("SELECT categoryId, SUM(amountMinorUnits) as total FROM transactions WHERE type = 'EXPENSE' AND dateTimestamp BETWEEN :startDate AND :endDate GROUP BY categoryId")
    suspend fun getCategorySpendingSummary(startDate: Long, endDate: Long): List<CategorySpendingSum>
}

data class CategorySpendingSum(
    val categoryId: Long?,
    val total: Long
)

@Dao
interface BudgetDao {
    @Transaction
    @Query("SELECT * FROM budgets WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllBudgetsWithDetails(): Flow<List<BudgetWithDetails>>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)
}

@Dao
interface SavingsGoalDao {
    @Transaction
    @Query("SELECT * FROM savings_goals ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllGoalsWithContributions(): Flow<List<SavingsGoalWithContributions>>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getGoalById(id: Long): SavingsGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: SavingsContributionEntity): Long

    @Query("UPDATE savings_goals SET savedAmountMinorUnits = savedAmountMinorUnits + :deltaMinorUnits WHERE id = :goalId")
    suspend fun adjustGoalSavedAmount(goalId: Long, deltaMinorUnits: Long)

    @Query("SELECT * FROM savings_contributions WHERE goalId = :goalId ORDER BY dateTimestamp DESC")
    fun getContributionsForGoal(goalId: Long): Flow<List<SavingsContributionEntity>>
}

@Dao
interface RecurringTransactionDao {
    @Transaction
    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 ORDER BY nextExecutionDate ASC")
    fun getActiveRecurringWithDetails(): Flow<List<RecurringWithDetails>>

    @Transaction
    @Query("SELECT * FROM recurring_transactions ORDER BY createdAt DESC")
    fun getAllRecurringWithDetails(): Flow<List<RecurringWithDetails>>

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 AND nextExecutionDate <= :currentTime")
    suspend fun getDueRecurring(currentTime: Long): List<RecurringTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(recurring: RecurringTransactionEntity): Long

    @Update
    suspend fun updateRecurring(recurring: RecurringTransactionEntity)

    @Delete
    suspend fun deleteRecurring(recurring: RecurringTransactionEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}
