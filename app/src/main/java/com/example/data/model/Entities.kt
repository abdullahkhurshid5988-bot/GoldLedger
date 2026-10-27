package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balanceMinorUnits: Long = 0L,
    val currencyCode: String = "PKR",
    val colorHex: String = "#D4AF37",
    val iconName: String = "wallet",
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: CategoryType,
    val iconName: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false
)

@Entity(
    tableName = "transactions",
    indices = [
        Index("accountId"),
        Index("toAccountId"),
        Index("categoryId"),
        Index("dateTimestamp")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amountMinorUnits: Long,
    val accountId: Long,
    val toAccountId: Long? = null, // for TRANSFER
    val categoryId: Long? = null,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val timeString: String = "",
    val description: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val merchant: String = "",
    val notes: String = "",
    val receiptUri: String? = null,
    val isRecurring: Boolean = false,
    val recurringId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "budgets",
    indices = [Index("categoryId")]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val categoryId: Long? = null, // null means overall budget
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val limitMinorUnits: Long = 0L,
    val alertThresholdPercent: Int = 80,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmountMinorUnits: Long,
    val savedAmountMinorUnits: Long = 0L,
    val targetDate: Long? = null,
    val iconName: String = "savings",
    val colorHex: String = "#D4AF37",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "savings_contributions",
    foreignKeys = [
        ForeignKey(
            entity = SavingsGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("goalId"), Index("accountId")]
)
data class SavingsContributionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val accountId: Long,
    val amountMinorUnits: Long, // positive for deposit, negative for withdrawal
    val dateTimestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(
    tableName = "recurring_transactions",
    indices = [Index("accountId"), Index("categoryId")]
)
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amountMinorUnits: Long,
    val accountId: Long,
    val categoryId: Long?,
    val description: String,
    val frequency: RecurringFrequency,
    val nextExecutionDate: Long,
    val lastExecutionDate: Long? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "BUDGET_ALERT"
)
