package com.example

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.accounts.AccountsScreen
import com.example.ui.accounts.TransferScreen
import com.example.ui.backup.BackupRestoreScreen
import com.example.ui.budgets.BudgetsScreen
import com.example.ui.categories.CategoriesScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.recurring.RecurringScreen
import com.example.ui.reports.ReportsScreen
import com.example.ui.savings.SavingsDetailScreen
import com.example.ui.savings.SavingsGoalsScreen
import com.example.ui.security.PinLockScreen
import com.example.ui.security.SecuritySettingsScreen
import com.example.ui.settings.AboutScreen
import com.example.ui.settings.PrivacyPolicyScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackCard
import com.example.ui.theme.BlackPrimary
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLedgerTheme
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.transactions.AddEditTransactionScreen
import com.example.ui.transactions.TransactionDetailScreen
import com.example.ui.transactions.TransactionsScreen

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val isDarkMode by viewModel.isDarkModeFlow.collectAsState()

            GoldLedgerTheme(darkTheme = isDarkMode) {
                GoldLedgerApp(viewModel = viewModel)
            }
        }
    }
}

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val isCenterAction: Boolean = false,
    val testTag: String
)

@Composable
fun GoldLedgerApp(viewModel: MainViewModel) {
    val isOnboardingCompleted by viewModel.isOnboardingCompletedFlow.collectAsState()
    val isAppUnlocked by viewModel.isAppUnlocked.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    // 1. Show Onboarding if not completed
    if (!isOnboardingCompleted) {
        OnboardingScreen(
            onFinish = { selectedCurrency ->
                viewModel.completeOnboarding(selectedCurrency)
            }
        )
        return
    }

    // 2. Show PIN Lock if app is locked
    if (!isAppUnlocked) {
        PinLockScreen(viewModel = viewModel)
        return
    }

    // 3. Navigation BackHandler for Sub-screens
    if (currentScreen != ScreenDestination.MainNav) {
        BackHandler {
            viewModel.navigateBack()
        }
    }

    val navItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, testTag = "nav_tab_home"),
        BottomNavItem("Transactions", Icons.Default.ReceiptLong, testTag = "nav_tab_transactions"),
        BottomNavItem("Add", Icons.Default.Add, isCenterAction = true, testTag = "nav_tab_add"),
        BottomNavItem("Reports", Icons.Default.BarChart, testTag = "nav_tab_reports"),
        BottomNavItem("Settings", Icons.Default.Settings, testTag = "nav_tab_settings")
    )

    Scaffold(
        containerColor = BlackPrimary,
        bottomBar = {
            if (currentScreen == ScreenDestination.MainNav) {
                NavigationBar(
                    containerColor = BlackCard,
                    contentColor = TextWhite,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BlackBorder)
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index

                        if (item.isCenterAction) {
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    viewModel.selectedTransaction.value = null
                                    viewModel.selectTab(index)
                                },
                                icon = {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(GoldPrimary)
                                            .border(1.dp, GoldLight, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            tint = Color(0xFF141208),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                },
                                label = null,
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        } else {
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.selectTab(index) },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = GoldPrimary,
                                    selectedTextColor = GoldPrimary,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted,
                                    indicatorColor = GoldContainer
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_navigation"
            ) { screen ->
                when (screen) {
                    ScreenDestination.MainNav -> {
                        when (selectedTab) {
                            0 -> DashboardScreen(viewModel = viewModel)
                            1 -> TransactionsScreen(viewModel = viewModel)
                            2 -> AddEditTransactionScreen(viewModel = viewModel)
                            3 -> ReportsScreen(viewModel = viewModel)
                            4 -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                    ScreenDestination.AccountsList -> AccountsScreen(viewModel = viewModel)
                    ScreenDestination.AddEditAccount -> AccountsScreen(viewModel = viewModel)
                    ScreenDestination.TransferMoney -> TransferScreen(viewModel = viewModel)
                    ScreenDestination.BudgetsList -> BudgetsScreen(viewModel = viewModel)
                    ScreenDestination.AddEditBudget -> BudgetsScreen(viewModel = viewModel)
                    ScreenDestination.SavingsList -> SavingsGoalsScreen(viewModel = viewModel)
                    ScreenDestination.AddEditSavingsGoal -> SavingsGoalsScreen(viewModel = viewModel)
                    ScreenDestination.SavingsDetail -> SavingsDetailScreen(viewModel = viewModel)
                    ScreenDestination.RecurringList -> RecurringScreen(viewModel = viewModel)
                    ScreenDestination.AddEditRecurring -> RecurringScreen(viewModel = viewModel)
                    ScreenDestination.CategoriesList -> CategoriesScreen(viewModel = viewModel)
                    ScreenDestination.BackupRestore -> BackupRestoreScreen(viewModel = viewModel)
                    ScreenDestination.SecuritySettings -> SecuritySettingsScreen(viewModel = viewModel)
                    ScreenDestination.PrivacyPolicy -> PrivacyPolicyScreen(viewModel = viewModel)
                    ScreenDestination.AboutApp -> AboutScreen(viewModel = viewModel)
                    ScreenDestination.TransactionDetail -> TransactionDetailScreen(viewModel = viewModel)
                }
            }
        }
    }
}
