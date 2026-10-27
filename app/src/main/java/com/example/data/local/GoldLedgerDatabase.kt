package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryType
import com.example.data.model.NotificationEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.SavingsContributionEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        SavingsContributionEntity::class,
        RecurringTransactionEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GoldLedgerDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: GoldLedgerDatabase? = null

        fun getDatabase(context: Context): GoldLedgerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GoldLedgerDatabase::class.java,
                    "gold_ledger_database.db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val appContext: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(appContext)
                    populateInitialData(database)
                }
            }
        }

        suspend fun populateInitialData(database: GoldLedgerDatabase) {
            val categoryDao = database.categoryDao()
            val accountDao = database.accountDao()

            // Default Expense Categories
            val expenseCategories = listOf(
                CategoryEntity(name = "Food", type = CategoryType.EXPENSE, iconName = "restaurant", colorHex = "#FF9800", isDefault = true),
                CategoryEntity(name = "Shopping", type = CategoryType.EXPENSE, iconName = "shopping_bag", colorHex = "#E91E63", isDefault = true),
                CategoryEntity(name = "Transport", type = CategoryType.EXPENSE, iconName = "directions_bus", colorHex = "#2196F3", isDefault = true),
                CategoryEntity(name = "Fuel", type = CategoryType.EXPENSE, iconName = "local_gas_station", colorHex = "#FF5722", isDefault = true),
                CategoryEntity(name = "Bills", type = CategoryType.EXPENSE, iconName = "receipt_long", colorHex = "#9C27B0", isDefault = true),
                CategoryEntity(name = "Rent", type = CategoryType.EXPENSE, iconName = "home", colorHex = "#795548", isDefault = true),
                CategoryEntity(name = "Education", type = CategoryType.EXPENSE, iconName = "school", colorHex = "#3F51B5", isDefault = true),
                CategoryEntity(name = "Health", type = CategoryType.EXPENSE, iconName = "medical_services", colorHex = "#F44336", isDefault = true),
                CategoryEntity(name = "Entertainment", type = CategoryType.EXPENSE, iconName = "movie", colorHex = "#00BCD4", isDefault = true),
                CategoryEntity(name = "Travel", type = CategoryType.EXPENSE, iconName = "flight", colorHex = "#009688", isDefault = true),
                CategoryEntity(name = "Mobile & Internet", type = CategoryType.EXPENSE, iconName = "wifi", colorHex = "#673AB7", isDefault = true),
                CategoryEntity(name = "Groceries", type = CategoryType.EXPENSE, iconName = "shopping_cart", colorHex = "#4CAF50", isDefault = true),
                CategoryEntity(name = "Family", type = CategoryType.EXPENSE, iconName = "family_restroom", colorHex = "#FF4081", isDefault = true),
                CategoryEntity(name = "Other", type = CategoryType.EXPENSE, iconName = "more_horiz", colorHex = "#9E9E9E", isDefault = true)
            )

            // Default Income Categories
            val incomeCategories = listOf(
                CategoryEntity(name = "Salary", type = CategoryType.INCOME, iconName = "payments", colorHex = "#28B67A", isDefault = true),
                CategoryEntity(name = "Business", type = CategoryType.INCOME, iconName = "storefront", colorHex = "#D4AF37", isDefault = true),
                CategoryEntity(name = "Freelancing", type = CategoryType.INCOME, iconName = "laptop", colorHex = "#4D96FF", isDefault = true),
                CategoryEntity(name = "Investment", type = CategoryType.INCOME, iconName = "trending_up", colorHex = "#00B0FF", isDefault = true),
                CategoryEntity(name = "Gift", type = CategoryType.INCOME, iconName = "card_giftcard", colorHex = "#FF80AB", isDefault = true),
                CategoryEntity(name = "Bonus", type = CategoryType.INCOME, iconName = "star", colorHex = "#FFD700", isDefault = true),
                CategoryEntity(name = "Commission", type = CategoryType.INCOME, iconName = "percent", colorHex = "#FFAB00", isDefault = true),
                CategoryEntity(name = "Rental Income", type = CategoryType.INCOME, iconName = "apartment", colorHex = "#8D6E63", isDefault = true),
                CategoryEntity(name = "Other", type = CategoryType.INCOME, iconName = "savings", colorHex = "#78909C", isDefault = true)
            )

            categoryDao.insertCategories(expenseCategories)
            categoryDao.insertCategories(incomeCategories)

            // Default Accounts: Cash Wallet and Bank Account
            val cashWallet = AccountEntity(
                name = "Cash Wallet",
                type = AccountType.CASH_WALLET,
                balanceMinorUnits = 0L,
                currencyCode = "PKR",
                colorHex = "#28B67A",
                iconName = "wallet",
                isDefault = true
            )
            val bankAccount = AccountEntity(
                name = "Bank Account",
                type = AccountType.BANK_ACCOUNT,
                balanceMinorUnits = 0L,
                currencyCode = "PKR",
                colorHex = "#D4AF37",
                iconName = "account_balance",
                isDefault = false
            )
            accountDao.insertAccount(cashWallet)
            accountDao.insertAccount(bankAccount)
        }
    }
}
