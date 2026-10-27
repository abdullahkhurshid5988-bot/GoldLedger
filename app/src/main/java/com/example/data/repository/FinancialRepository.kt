package com.example.data.repository

import com.example.data.local.AccountDao
import com.example.data.local.BudgetDao
import com.example.data.local.CategoryDao
import com.example.data.local.CategorySpendingSum
import com.example.data.local.GoldLedgerDatabase
import com.example.data.local.NotificationDao
import com.example.data.local.RecurringTransactionDao
import com.example.data.local.SavingsGoalDao
import com.example.data.local.TransactionDao
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FinancialRepository(
    private val database: GoldLedgerDatabase
) {
    private val accountDao: AccountDao = database.accountDao()
    private val categoryDao: CategoryDao = database.categoryDao()
    private val transactionDao: TransactionDao = database.transactionDao()
    private val budgetDao: BudgetDao = database.budgetDao()
    private val savingsGoalDao: SavingsGoalDao = database.savingsGoalDao()
    private val recurringDao: RecurringTransactionDao = database.recurringTransactionDao()
    private val notificationDao: NotificationDao = database.notificationDao()

    // Accounts
    fun getAllAccounts(): Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    suspend fun getAccountById(id: Long): AccountEntity? = accountDao.getAccountById(id)
    suspend fun insertAccount(account: AccountEntity): Long = accountDao.insertAccount(account)
    suspend fun updateAccount(account: AccountEntity) = accountDao.updateAccount(account)
    suspend fun deleteAccount(account: AccountEntity) = accountDao.deleteAccount(account)
    suspend fun setDefaultAccount(id: Long) = accountDao.setDefaultAccount(id)

    // Categories
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    fun getCategoriesByType(type: CategoryType): Flow<List<CategoryEntity>> = categoryDao.getCategoriesByType(type)
    suspend fun getCategoryById(id: Long): CategoryEntity? = categoryDao.getCategoryById(id)
    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)
    suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)
    suspend fun archiveCategory(id: Long) = categoryDao.archiveCategory(id)

    // Transactions
    fun getAllTransactions(): Flow<List<TransactionWithDetails>> = transactionDao.getAllTransactionsWithDetails()
    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionWithDetails>> =
        transactionDao.getRecentTransactionsWithDetails(limit)
    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<TransactionWithDetails>> =
        transactionDao.getTransactionsByDateRange(start, end)
    fun getTransactionsForAccount(accountId: Long): Flow<List<TransactionWithDetails>> =
        transactionDao.getTransactionsForAccount(accountId)
    suspend fun getTransactionById(id: Long): TransactionWithDetails? = transactionDao.getTransactionById(id)

    /**
     * Records a transaction with atomic account balance updates
     */
    suspend fun addTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        val txId = transactionDao.insertTransaction(transaction)
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                accountDao.adjustAccountBalance(transaction.accountId, -transaction.amountMinorUnits)
            }
            TransactionType.INCOME -> {
                accountDao.adjustAccountBalance(transaction.accountId, transaction.amountMinorUnits)
            }
            TransactionType.TRANSFER -> {
                accountDao.adjustAccountBalance(transaction.accountId, -transaction.amountMinorUnits)
                transaction.toAccountId?.let { toAccId ->
                    accountDao.adjustAccountBalance(toAccId, transaction.amountMinorUnits)
                }
            }
        }
        txId
    }

    /**
     * Updates an existing transaction, reverting the old balance impact and applying the new one.
     */
    suspend fun updateTransaction(newTransaction: TransactionEntity) = withContext(Dispatchers.IO) {
        val old = transactionDao.getRawTransactionById(newTransaction.id) ?: return@withContext
        // Revert old impact
        when (old.type) {
            TransactionType.EXPENSE -> {
                accountDao.adjustAccountBalance(old.accountId, old.amountMinorUnits)
            }
            TransactionType.INCOME -> {
                accountDao.adjustAccountBalance(old.accountId, -old.amountMinorUnits)
            }
            TransactionType.TRANSFER -> {
                accountDao.adjustAccountBalance(old.accountId, old.amountMinorUnits)
                old.toAccountId?.let { toAccId ->
                    accountDao.adjustAccountBalance(toAccId, -old.amountMinorUnits)
                }
            }
        }
        // Apply new impact
        when (newTransaction.type) {
            TransactionType.EXPENSE -> {
                accountDao.adjustAccountBalance(newTransaction.accountId, -newTransaction.amountMinorUnits)
            }
            TransactionType.INCOME -> {
                accountDao.adjustAccountBalance(newTransaction.accountId, newTransaction.amountMinorUnits)
            }
            TransactionType.TRANSFER -> {
                accountDao.adjustAccountBalance(newTransaction.accountId, -newTransaction.amountMinorUnits)
                newTransaction.toAccountId?.let { toAccId ->
                    accountDao.adjustAccountBalance(toAccId, newTransaction.amountMinorUnits)
                }
            }
        }
        transactionDao.updateTransaction(newTransaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        // Revert balance impact
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                accountDao.adjustAccountBalance(transaction.accountId, transaction.amountMinorUnits)
            }
            TransactionType.INCOME -> {
                accountDao.adjustAccountBalance(transaction.accountId, -transaction.amountMinorUnits)
            }
            TransactionType.TRANSFER -> {
                accountDao.adjustAccountBalance(transaction.accountId, transaction.amountMinorUnits)
                transaction.toAccountId?.let { toAccId ->
                    accountDao.adjustAccountBalance(toAccId, -transaction.amountMinorUnits)
                }
            }
        }
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun getTotalByTypeAndRange(type: TransactionType, start: Long, end: Long): Long {
        return transactionDao.getTotalAmountByTypeAndRange(type, start, end) ?: 0L
    }

    suspend fun getTotalExpenseForCategoryAndRange(categoryId: Long, start: Long, end: Long): Long {
        return transactionDao.getTotalExpenseForCategoryAndRange(categoryId, start, end) ?: 0L
    }

    suspend fun getCategorySpendingSummary(start: Long, end: Long): List<CategorySpendingSum> {
        return transactionDao.getCategorySpendingSummary(start, end)
    }

    // Budgets
    fun getAllBudgets(): Flow<List<BudgetWithDetails>> = budgetDao.getAllBudgetsWithDetails()
    suspend fun getBudgetById(id: Long): BudgetEntity? = budgetDao.getBudgetById(id)
    suspend fun insertBudget(budget: BudgetEntity): Long = budgetDao.insertBudget(budget)
    suspend fun updateBudget(budget: BudgetEntity) = budgetDao.updateBudget(budget)
    suspend fun deleteBudget(budget: BudgetEntity) = budgetDao.deleteBudget(budget)

    // Savings Goals
    fun getAllGoals(): Flow<List<SavingsGoalWithContributions>> = savingsGoalDao.getAllGoalsWithContributions()
    suspend fun getGoalById(id: Long): SavingsGoalEntity? = savingsGoalDao.getGoalById(id)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long = savingsGoalDao.insertGoal(goal)
    suspend fun updateGoal(goal: SavingsGoalEntity) = savingsGoalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: SavingsGoalEntity) = savingsGoalDao.deleteGoal(goal)
    fun getContributionsForGoal(goalId: Long): Flow<List<SavingsContributionEntity>> =
        savingsGoalDao.getContributionsForGoal(goalId)

    suspend fun addSavingsContribution(
        goalId: Long,
        accountId: Long,
        amountMinorUnits: Long,
        isDeposit: Boolean,
        note: String
    ) = withContext(Dispatchers.IO) {
        val contribution = SavingsContributionEntity(
            goalId = goalId,
            accountId = accountId,
            amountMinorUnits = if (isDeposit) amountMinorUnits else -amountMinorUnits,
            note = note
        )
        savingsGoalDao.insertContribution(contribution)
        if (isDeposit) {
            savingsGoalDao.adjustGoalSavedAmount(goalId, amountMinorUnits)
            accountDao.adjustAccountBalance(accountId, -amountMinorUnits)
        } else {
            savingsGoalDao.adjustGoalSavedAmount(goalId, -amountMinorUnits)
            accountDao.adjustAccountBalance(accountId, amountMinorUnits)
        }
    }

    // Recurring Transactions
    fun getActiveRecurring(): Flow<List<RecurringWithDetails>> = recurringDao.getActiveRecurringWithDetails()
    fun getAllRecurring(): Flow<List<RecurringWithDetails>> = recurringDao.getAllRecurringWithDetails()
    suspend fun insertRecurring(recurring: RecurringTransactionEntity): Long = recurringDao.insertRecurring(recurring)
    suspend fun updateRecurring(recurring: RecurringTransactionEntity) = recurringDao.updateRecurring(recurring)
    suspend fun deleteRecurring(recurring: RecurringTransactionEntity) = recurringDao.deleteRecurring(recurring)
    suspend fun getDueRecurring(currentTime: Long): List<RecurringTransactionEntity> =
        recurringDao.getDueRecurring(currentTime)

    // Notifications
    fun getAllNotifications(): Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    suspend fun insertNotification(notification: NotificationEntity): Long =
        notificationDao.insertNotification(notification)
    suspend fun markNotificationAsRead(id: Long) = notificationDao.markAsRead(id)
    suspend fun clearNotifications() = notificationDao.clearAll()
}
