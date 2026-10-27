package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.export.ExportUtils
import com.example.core.insights.FinancialInsight
import com.example.core.insights.FinancialInsightsEngine
import com.example.core.notifications.NotificationHelper
import com.example.core.security.SecurityManager
import com.example.core.utilities.FinancialUtils
import com.example.data.local.GoldLedgerDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.BudgetEntity
import com.example.data.model.BudgetWithDetails
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryType
import com.example.data.model.Currencies
import com.example.data.model.NotificationEntity
import com.example.data.model.RecurringFrequency
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.RecurringWithDetails
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.SavingsGoalWithContributions
import com.example.data.model.SupportedCurrency
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.data.preferences.AppPreferencesManager
import com.example.data.repository.FinancialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

sealed class ScreenDestination {
    object MainNav : ScreenDestination()
    object AccountsList : ScreenDestination()
    object AddEditAccount : ScreenDestination()
    object TransferMoney : ScreenDestination()
    object BudgetsList : ScreenDestination()
    object AddEditBudget : ScreenDestination()
    object SavingsList : ScreenDestination()
    object AddEditSavingsGoal : ScreenDestination()
    object SavingsDetail : ScreenDestination()
    object RecurringList : ScreenDestination()
    object AddEditRecurring : ScreenDestination()
    object CategoriesList : ScreenDestination()
    object BackupRestore : ScreenDestination()
    object SecuritySettings : ScreenDestination()
    object PrivacyPolicy : ScreenDestination()
    object AboutApp : ScreenDestination()
    object TransactionDetail : ScreenDestination()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GoldLedgerDatabase.getDatabase(application)
    val repository = FinancialRepository(database)
    val preferences = AppPreferencesManager(application)

    // Navigation State
    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.MainNav)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // App Lock State
    private val _isAppUnlocked = MutableStateFlow(!preferences.isPinLockEnabled())
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    // Preferences Flows
    val currencyFlow: StateFlow<SupportedCurrency> = preferences.currencyFlow
    val isBalanceHiddenFlow: StateFlow<Boolean> = preferences.hideBalanceFlow
    val isDarkModeFlow: StateFlow<Boolean> = preferences.darkModeFlow
    val isOnboardingCompletedFlow: StateFlow<Boolean> = preferences.isOnboardingCompletedFlow
    val isPinLockEnabledFlow: StateFlow<Boolean> = preferences.isPinLockEnabledFlow

    // Database Flows
    val accountsFlow: StateFlow<List<AccountEntity>> = repository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactionsFlow: StateFlow<List<TransactionWithDetails>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactionsFlow: StateFlow<List<TransactionWithDetails>> = repository.getRecentTransactions(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoriesFlow: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetsFlow: StateFlow<List<BudgetWithDetails>> = repository.getAllBudgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoalsFlow: StateFlow<List<SavingsGoalWithContributions>> = repository.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringFlow: StateFlow<List<RecurringWithDetails>> = repository.getAllRecurring()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationsFlow: StateFlow<List<NotificationEntity>> = repository.getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected items for editing / details
    val selectedTransaction = MutableStateFlow<TransactionWithDetails?>(null)
    val selectedAccount = MutableStateFlow<AccountEntity?>(null)
    val selectedBudget = MutableStateFlow<BudgetWithDetails?>(null)
    val selectedSavingsGoal = MutableStateFlow<SavingsGoalWithContributions?>(null)
    val selectedRecurring = MutableStateFlow<RecurringWithDetails?>(null)

    // Financial Insights
    val insightsFlow: StateFlow<List<FinancialInsight>> = combine(
        allTransactionsFlow,
        budgetsFlow,
        currencyFlow
    ) { txs, bgts, curr ->
        FinancialInsightsEngine.generateInsights(txs, bgts, curr)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculations
    val totalBalanceMinorUnits: StateFlow<Long> = accountsFlow.combine(currencyFlow) { accounts, _ ->
        accounts.sumOf { it.balanceMinorUnits }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val todayExpenseMinorUnits: StateFlow<Long> = allTransactionsFlow.combine(currencyFlow) { txs, _ ->
        val startOfDay = FinancialUtils.getStartOfDay()
        val endOfDay = FinancialUtils.getEndOfDay()
        txs.filter {
            it.transaction.type == TransactionType.EXPENSE &&
                    it.transaction.dateTimestamp in startOfDay..endOfDay
        }.sumOf { it.transaction.amountMinorUnits }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val monthlyExpenseMinorUnits: StateFlow<Long> = allTransactionsFlow.combine(currencyFlow) { txs, _ ->
        val startOfMonth = FinancialUtils.getStartOfMonth()
        val endOfMonth = FinancialUtils.getEndOfMonth()
        txs.filter {
            it.transaction.type == TransactionType.EXPENSE &&
                    it.transaction.dateTimestamp in startOfMonth..endOfMonth
        }.sumOf { it.transaction.amountMinorUnits }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val monthlyIncomeMinorUnits: StateFlow<Long> = allTransactionsFlow.combine(currencyFlow) { txs, _ ->
        val startOfMonth = FinancialUtils.getStartOfMonth()
        val endOfMonth = FinancialUtils.getEndOfMonth()
        txs.filter {
            it.transaction.type == TransactionType.INCOME &&
                    it.transaction.dateTimestamp in startOfMonth..endOfMonth
        }.sumOf { it.transaction.amountMinorUnits }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val weeklyExpenseMinorUnits: StateFlow<Long> = allTransactionsFlow.combine(currencyFlow) { txs, _ ->
        val startOfWeek = FinancialUtils.getStartOfWeek()
        val now = System.currentTimeMillis()
        txs.filter {
            it.transaction.type == TransactionType.EXPENSE &&
                    it.transaction.dateTimestamp in startOfWeek..now
        }.sumOf { it.transaction.amountMinorUnits }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    init {
        // Ensure default initial categories & accounts exist if db was empty
        viewModelScope.launch {
            if (repository.getAllAccounts().stateIn(viewModelScope).value.isEmpty()) {
                GoldLedgerDatabase.populateInitialData(database)
            }
        }
    }

    // Navigation Methods
    fun navigateTo(destination: ScreenDestination) {
        _currentScreen.value = destination
    }

    fun navigateBack() {
        _currentScreen.value = ScreenDestination.MainNav
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
        _currentScreen.value = ScreenDestination.MainNav
    }

    // App Lock Methods
    fun unlockApp(pin: String): Boolean {
        val storedHash = preferences.getPinHash()
        val isValid = SecurityManager.verifyPin(pin, storedHash)
        if (isValid) {
            _isAppUnlocked.value = true
        }
        return isValid
    }

    fun unlockAppWithBiometrics() {
        _isAppUnlocked.value = true
    }

    fun lockApp() {
        if (preferences.isPinLockEnabled()) {
            _isAppUnlocked.value = false
        }
    }

    fun setPin(pin: String) {
        val hash = SecurityManager.hashPin(pin)
        preferences.setPinHash(hash)
        preferences.setPinLockEnabled(true)
        _isAppUnlocked.value = true
    }

    fun disablePin() {
        preferences.setPinHash("")
        preferences.setPinLockEnabled(false)
        preferences.setBiometricEnabled(false)
        _isAppUnlocked.value = true
    }

    // Preferences Actions
    fun toggleHideBalance() {
        preferences.setHideBalance(!preferences.isBalanceHidden())
    }

    fun setCurrency(currency: SupportedCurrency) {
        preferences.setCurrency(currency)
    }

    fun setDarkMode(isDark: Boolean) {
        preferences.setDarkMode(isDark)
    }

    fun completeOnboarding(currency: SupportedCurrency) {
        preferences.setCurrency(currency)
        preferences.setOnboardingCompleted(true)
    }

    // Transaction Actions
    fun saveTransaction(
        id: Long = 0,
        type: TransactionType,
        amountMinorUnits: Long,
        accountId: Long,
        toAccountId: Long? = null,
        categoryId: Long? = null,
        dateTimestamp: Long = System.currentTimeMillis(),
        timeString: String = "",
        description: String = "",
        paymentMethod: com.example.data.model.PaymentMethod = com.example.data.model.PaymentMethod.CASH,
        merchant: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                id = id,
                type = type,
                amountMinorUnits = amountMinorUnits,
                accountId = accountId,
                toAccountId = toAccountId,
                categoryId = categoryId,
                dateTimestamp = dateTimestamp,
                timeString = timeString.ifBlank { FinancialUtils.formatTime(dateTimestamp) },
                description = description,
                paymentMethod = paymentMethod,
                merchant = merchant,
                notes = notes
            )
            if (id == 0L) {
                repository.addTransaction(tx)
                // Check if any budgets are affected and notify
                checkBudgetAlerts(tx)
            } else {
                repository.updateTransaction(tx)
            }
            navigateBack()
        }
    }

    private suspend fun checkBudgetAlerts(tx: TransactionEntity) {
        if (tx.type != TransactionType.EXPENSE) return
        val activeBudgets = budgetsFlow.value
        val startOfMonth = FinancialUtils.getStartOfMonth()
        val endOfMonth = FinancialUtils.getEndOfMonth()

        for (budgetWithDetails in activeBudgets) {
            val budget = budgetWithDetails.budget
            if (budget.categoryId == null || budget.categoryId == tx.categoryId) {
                val totalSpent = if (budget.categoryId == null) {
                    monthlyExpenseMinorUnits.value
                } else {
                    repository.getTotalExpenseForCategoryAndRange(budget.categoryId, startOfMonth, endOfMonth)
                }

                if (budget.limitMinorUnits > 0) {
                    val pct = ((totalSpent.toDouble() / budget.limitMinorUnits.toDouble()) * 100).toInt()
                    if (pct >= 100 && preferences.isBudgetAlert100Enabled()) {
                        val title = "Budget Limit Exceeded: ${budget.name}"
                        val msg = "You have spent 100% or more of your budget limit."
                        repository.insertNotification(NotificationEntity(title = title, message = msg, type = "BUDGET_100"))
                        NotificationHelper.showBudgetNotification(getApplication(), budget.id.toInt(), title, msg)
                    } else if (pct >= budget.alertThresholdPercent && preferences.isBudgetAlert80Enabled()) {
                        val title = "Budget Warning: ${budget.name}"
                        val msg = "You have reached $pct% of your ${budget.name} budget."
                        repository.insertNotification(NotificationEntity(title = title, message = msg, type = "BUDGET_80"))
                        NotificationHelper.showBudgetNotification(getApplication(), budget.id.toInt(), title, msg)
                    }
                }
            }
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
            navigateBack()
        }
    }

    // Account Actions
    fun saveAccount(
        id: Long = 0,
        name: String,
        type: AccountType,
        initialBalanceMinorUnits: Long,
        colorHex: String,
        iconName: String
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                val account = AccountEntity(
                    name = name,
                    type = type,
                    balanceMinorUnits = initialBalanceMinorUnits,
                    currencyCode = currencyFlow.value.code,
                    colorHex = colorHex,
                    iconName = iconName
                )
                repository.insertAccount(account)
            } else {
                val existing = repository.getAccountById(id)
                if (existing != null) {
                    val updated = existing.copy(
                        name = name,
                        type = type,
                        colorHex = colorHex,
                        iconName = iconName
                    )
                    repository.updateAccount(updated)
                }
            }
            navigateTo(ScreenDestination.AccountsList)
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    fun setDefaultAccount(accountId: Long) {
        viewModelScope.launch {
            repository.setDefaultAccount(accountId)
        }
    }

    // Transfer Money
    fun transferMoney(
        fromAccountId: Long,
        toAccountId: Long,
        amountMinorUnits: Long,
        notes: String
    ) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                type = TransactionType.TRANSFER,
                amountMinorUnits = amountMinorUnits,
                accountId = fromAccountId,
                toAccountId = toAccountId,
                description = "Account Transfer",
                notes = notes,
                dateTimestamp = System.currentTimeMillis(),
                timeString = FinancialUtils.formatTime(System.currentTimeMillis())
            )
            repository.addTransaction(tx)
            navigateTo(ScreenDestination.AccountsList)
        }
    }

    // Budget Actions
    fun saveBudget(
        id: Long = 0,
        name: String,
        categoryId: Long?,
        limitMinorUnits: Long,
        alertThresholdPercent: Int = 80
    ) {
        viewModelScope.launch {
            val budget = BudgetEntity(
                id = id,
                name = name,
                categoryId = categoryId,
                limitMinorUnits = limitMinorUnits,
                alertThresholdPercent = alertThresholdPercent
            )
            if (id == 0L) {
                repository.insertBudget(budget)
            } else {
                repository.updateBudget(budget)
            }
            navigateTo(ScreenDestination.BudgetsList)
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    // Savings Actions
    fun saveSavingsGoal(
        id: Long = 0,
        name: String,
        targetAmountMinorUnits: Long,
        targetDate: Long?,
        colorHex: String,
        iconName: String
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                val goal = SavingsGoalEntity(
                    name = name,
                    targetAmountMinorUnits = targetAmountMinorUnits,
                    targetDate = targetDate,
                    colorHex = colorHex,
                    iconName = iconName
                )
                repository.insertGoal(goal)
            } else {
                val existing = repository.getGoalById(id)
                if (existing != null) {
                    val updated = existing.copy(
                        name = name,
                        targetAmountMinorUnits = targetAmountMinorUnits,
                        targetDate = targetDate,
                        colorHex = colorHex,
                        iconName = iconName
                    )
                    repository.updateGoal(updated)
                }
            }
            navigateTo(ScreenDestination.SavingsList)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
            navigateTo(ScreenDestination.SavingsList)
        }
    }

    fun contributeToSavingsGoal(
        goalId: Long,
        accountId: Long,
        amountMinorUnits: Long,
        isDeposit: Boolean,
        note: String
    ) {
        viewModelScope.launch {
            repository.addSavingsContribution(goalId, accountId, amountMinorUnits, isDeposit, note)
            // Refresh goal details
            val updated = repository.getAllGoals().stateIn(viewModelScope).value.find { it.goal.id == goalId }
            selectedSavingsGoal.value = updated
        }
    }

    // Recurring Actions
    fun saveRecurring(
        id: Long = 0,
        type: TransactionType,
        amountMinorUnits: Long,
        accountId: Long,
        categoryId: Long?,
        description: String,
        frequency: RecurringFrequency,
        nextExecutionDate: Long
    ) {
        viewModelScope.launch {
            val item = RecurringTransactionEntity(
                id = id,
                type = type,
                amountMinorUnits = amountMinorUnits,
                accountId = accountId,
                categoryId = categoryId,
                description = description,
                frequency = frequency,
                nextExecutionDate = nextExecutionDate
            )
            if (id == 0L) {
                repository.insertRecurring(item)
            } else {
                repository.updateRecurring(item)
            }
            navigateTo(ScreenDestination.RecurringList)
        }
    }

    fun deleteRecurring(item: RecurringTransactionEntity) {
        viewModelScope.launch {
            repository.deleteRecurring(item)
        }
    }

    // Category Actions
    fun saveCategory(
        id: Long = 0,
        name: String,
        type: CategoryType,
        iconName: String,
        colorHex: String
    ) {
        viewModelScope.launch {
            val category = CategoryEntity(
                id = id,
                name = name,
                type = type,
                iconName = iconName,
                colorHex = colorHex
            )
            if (id == 0L) {
                repository.insertCategory(category)
            } else {
                repository.updateCategory(category)
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.archiveCategory(category.id)
        }
    }

    // Export Reports
    fun exportCsv(context: Context): File {
        val transactions = allTransactionsFlow.value
        val currency = currencyFlow.value
        return ExportUtils.exportToCsv(context, transactions, currency)
    }

    fun exportPdf(context: Context, title: String = "Financial Statement"): File {
        val transactions = allTransactionsFlow.value
        val currency = currencyFlow.value
        val startOfMonth = FinancialUtils.getStartOfMonth()
        val endOfMonth = FinancialUtils.getEndOfMonth()
        val rangeStr = "${FinancialUtils.formatDate(startOfMonth, "dd MMM")} - ${FinancialUtils.formatDate(endOfMonth, "dd MMM yyyy")}"
        return ExportUtils.exportToPdf(context, title, rangeStr, transactions, currency)
    }

    fun clearAllData() {
        viewModelScope.launch {
            database.clearAllTables()
            preferences.clearAllData()
            GoldLedgerDatabase.populateInitialData(database)
            _selectedTab.value = 0
            _currentScreen.value = ScreenDestination.MainNav
            _isAppUnlocked.value = true
        }
    }
}
