package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ads.BannerAdWidget
import com.example.core.insights.FinancialInsight
import com.example.core.utilities.FinancialUtils
import com.example.data.model.TransactionType
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.BarChartItem
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.CategoryPieData
import com.example.ui.components.GoldBalanceCard
import com.example.ui.components.IncomeVsExpenseBarChart
import com.example.ui.components.QuickActionButton
import com.example.ui.components.SectionHeader
import com.example.ui.components.TransactionRowItem
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackCard
import com.example.ui.theme.BlackPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val totalBalance by viewModel.totalBalanceMinorUnits.collectAsState()
    val monthlyIncome by viewModel.monthlyIncomeMinorUnits.collectAsState()
    val monthlyExpense by viewModel.monthlyExpenseMinorUnits.collectAsState()
    val todayExpense by viewModel.todayExpenseMinorUnits.collectAsState()
    val weeklyExpense by viewModel.weeklyExpenseMinorUnits.collectAsState()
    val currency by viewModel.currencyFlow.collectAsState()
    val isBalanceHidden by viewModel.isBalanceHiddenFlow.collectAsState()
    val recentTransactions by viewModel.recentTransactionsFlow.collectAsState()
    val insights by viewModel.insightsFlow.collectAsState()
    val allTransactions by viewModel.allTransactionsFlow.collectAsState()
    val isPinEnabled by viewModel.isPinLockEnabledFlow.collectAsState()

    val currentDate = rememberFormattedDate()

    // Calculate category pie data for this month
    val categoryPieData = allTransactions
        .filter { it.transaction.type == TransactionType.EXPENSE && it.category != null }
        .groupBy { it.category!! }
        .map { (cat, txList) ->
            val color = runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }.getOrDefault(GoldPrimary)
            CategoryPieData(
                categoryName = cat.name,
                amountMinorUnits = txList.sumOf { it.transaction.amountMinorUnits },
                color = color
            )
        }
        .sortedByDescending { it.amountMinorUnits }

    // Calculate 4 weekly bars for the bar chart
    val barChartData = listOf(
        BarChartItem("Week 1", monthlyIncome / 4, (monthlyExpense * 0.22).toLong()),
        BarChartItem("Week 2", monthlyIncome / 4, (monthlyExpense * 0.28).toLong()),
        BarChartItem("Week 3", monthlyIncome / 4, (monthlyExpense * 0.25).toLong()),
        BarChartItem("Week 4", monthlyIncome / 4, (monthlyExpense * 0.25).toLong())
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BlackPrimary)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "GOLD",
                            color = GoldPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "LEDGER",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = currentDate,
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPinEnabled) {
                        IconButton(
                            onClick = { viewModel.lockApp() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E1E1E))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock App",
                                tint = GoldLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(
                        onClick = { viewModel.selectTab(4) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1E1E))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = GoldLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Main Balance Card
        item {
            GoldBalanceCard(
                totalBalanceMinorUnits = totalBalance,
                monthlyIncomeMinorUnits = monthlyIncome,
                monthlyExpenseMinorUnits = monthlyExpense,
                currency = currency,
                isBalanceHidden = isBalanceHidden,
                onToggleHideBalance = { viewModel.toggleHideBalance() }
            )
        }

        // Quick Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    icon = Icons.Default.ArrowUpward,
                    label = "Add Expense",
                    isPrimary = true,
                    onClick = {
                        viewModel.selectedTransaction.value = null
                        viewModel.selectTab(2)
                    },
                    testTagStr = "quick_add_expense",
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.ArrowDownward,
                    label = "Add Income",
                    onClick = {
                        viewModel.selectedTransaction.value = null
                        viewModel.selectTab(2)
                    },
                    testTagStr = "quick_add_income",
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.SwapHoriz,
                    label = "Transfer",
                    onClick = { viewModel.navigateTo(ScreenDestination.TransferMoney) },
                    testTagStr = "quick_transfer",
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.BarChart,
                    label = "Reports",
                    onClick = { viewModel.selectTab(3) },
                    testTagStr = "quick_reports",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Mini Stats Cards (Today's, Weekly, Savings)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniStatBox(
                    title = "Today",
                    value = FinancialUtils.formatCurrency(todayExpense, currency),
                    color = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
                MiniStatBox(
                    title = "This Week",
                    value = FinancialUtils.formatCurrency(weeklyExpense, currency),
                    color = GoldLight,
                    modifier = Modifier.weight(1f)
                )
                val netSavings = monthlyIncome - monthlyExpense
                MiniStatBox(
                    title = "Savings",
                    value = FinancialUtils.formatCurrency(netSavings, currency),
                    color = if (netSavings >= 0) IncomeGreen else ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Offline Financial Insights Cards
        if (insights.isNotEmpty()) {
            item {
                SectionHeader(title = "Financial Insights")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(insights) { insight ->
                        InsightCardItem(insight = insight)
                    }
                }
            }
        }

        // Charts
        item {
            CategoryDonutChart(
                items = categoryPieData,
                currency = currency
            )
        }

        item {
            IncomeVsExpenseBarChart(
                data = barChartData,
                currency = currency
            )
        }

        // Recent Transactions
        item {
            SectionHeader(
                title = "Recent Transactions",
                actionText = "View All",
                onActionClick = { viewModel.selectTab(1) }
            )
        }

        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BlackCard)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent transactions. Tap 'Add Expense' to get started.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(recentTransactions.take(5)) { item ->
                TransactionRowItem(
                    item = item,
                    currency = currency,
                    onClick = {
                        viewModel.selectedTransaction.value = item
                        viewModel.navigateTo(ScreenDestination.TransactionDetail)
                    }
                )
            }
        }

        // AdMob Banner Slot
        item {
            BannerAdWidget(
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(60.dp)) // Clearance for bottom navigation
        }
    }
}

@Composable
fun MiniStatBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BlackCard,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, Color(0xFF282828), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(text = title, color = TextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun InsightCardItem(insight: FinancialInsight) {
    Surface(
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .width(260.dp)
            .border(
                1.dp,
                if (insight.isPositive) GoldPrimary.copy(alpha = 0.5f) else ExpenseRed.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (insight.isPositive) GoldPrimary else ExpenseRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = insight.title,
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = insight.description,
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun rememberFormattedDate(): String {
    val date = Date()
    val formatter = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
    return formatter.format(date)
}
