package com.example.ui.reports

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.export.ExportUtils
import com.example.core.utilities.FinancialUtils
import com.example.data.model.TransactionType
import com.example.ui.MainViewModel
import com.example.ui.components.BarChartItem
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.CategoryPieData
import com.example.ui.components.GoldButton
import com.example.ui.components.GoldOutlinedButton
import com.example.ui.components.IncomeVsExpenseBarChart
import com.example.ui.components.TrendLineChart
import com.example.ui.components.TrendPoint
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
import java.util.Calendar

enum class ReportPeriod(val displayName: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year"),
    ALL("All Time")
}

@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allTransactions by viewModel.allTransactionsFlow.collectAsState()
    val currency by viewModel.currencyFlow.collectAsState()

    var selectedPeriod by remember { mutableStateOf(ReportPeriod.MONTH) }

    val now = System.currentTimeMillis()
    val filteredTransactions = remember(allTransactions, selectedPeriod) {
        val startTimestamp = when (selectedPeriod) {
            ReportPeriod.WEEK -> FinancialUtils.getStartOfWeek(now)
            ReportPeriod.MONTH -> FinancialUtils.getStartOfMonth(now)
            ReportPeriod.YEAR -> FinancialUtils.getStartOfYear(now)
            ReportPeriod.ALL -> 0L
        }
        allTransactions.filter { it.transaction.dateTimestamp >= startTimestamp }
    }

    val totalIncome = filteredTransactions
        .filter { it.transaction.type == TransactionType.INCOME }
        .sumOf { it.transaction.amountMinorUnits }

    val totalExpense = filteredTransactions
        .filter { it.transaction.type == TransactionType.EXPENSE }
        .sumOf { it.transaction.amountMinorUnits }

    val netSavings = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) {
        ((netSavings.toDouble() / totalIncome.toDouble()) * 100).toInt()
    } else 0

    // Category Pie Data
    val categoryPieData = filteredTransactions
        .filter { it.transaction.type == TransactionType.EXPENSE && it.category != null }
        .groupBy { it.category!! }
        .map { (cat, txs) ->
            val color = runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }.getOrDefault(GoldPrimary)
            CategoryPieData(
                categoryName = cat.name,
                amountMinorUnits = txs.sumOf { it.transaction.amountMinorUnits },
                color = color
            )
        }
        .sortedByDescending { it.amountMinorUnits }

    // Trend points (6 days or 6 weeks)
    val trendPoints = remember(filteredTransactions) {
        val points = mutableListOf<TrendPoint>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = now

        for (i in 5 downTo 0) {
            val c = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dayStart = FinancialUtils.getStartOfDay(c.timeInMillis)
            val dayEnd = FinancialUtils.getEndOfDay(c.timeInMillis)
            val daySpend = allTransactions
                .filter { it.transaction.type == TransactionType.EXPENSE && it.transaction.dateTimestamp in dayStart..dayEnd }
                .sumOf { it.transaction.amountMinorUnits }
            points.add(TrendPoint(FinancialUtils.formatDate(c.timeInMillis, "dd MMM"), daySpend))
        }
        points
    }

    val barData = listOf(
        BarChartItem("Period Total", totalIncome, totalExpense)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BlackPrimary)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Financial Analytics & Reports",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Period Filter Selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E1E))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (p in ReportPeriod.values()) {
                    val isSelected = p == selectedPeriod
                    Surface(
                        onClick = { selectedPeriod = p },
                        color = if (isSelected) GoldPrimary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = p.displayName,
                                color = if (isSelected) Color(0xFF141208) else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Summary Metric Cards
        item {
            Surface(
                color = BlackCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FINANCIAL SUMMARY (${selectedPeriod.displayName.uppercase()})",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Income", color = TextMuted, fontSize = 12.sp)
                            Text(
                                text = FinancialUtils.formatCurrency(totalIncome, currency),
                                color = IncomeGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Expenses", color = TextMuted, fontSize = 12.sp)
                            Text(
                                text = FinancialUtils.formatCurrency(totalExpense, currency),
                                color = ExpenseRed,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Net Savings", color = TextMuted, fontSize = 12.sp)
                            Text(
                                text = FinancialUtils.formatCurrency(netSavings, currency),
                                color = if (netSavings >= 0) GoldLight else ExpenseRed,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF191919))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Savings Rate", color = TextMuted, fontSize = 12.sp)
                            Text(
                                text = "$savingsRate%",
                                color = if (savingsRate >= 20) IncomeGreen else GoldLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Category Spending Donut Chart
        item {
            CategoryDonutChart(
                items = categoryPieData,
                currency = currency
            )
        }

        // Spending Trend Line Chart
        item {
            TrendLineChart(
                points = trendPoints,
                title = "6-Day Spending Trend",
                currency = currency,
                lineColor = GoldPrimary
            )
        }

        // Export and Share Section
        item {
            Surface(
                color = BlackCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Export & Share Reports",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Generate printable PDF financial statements or export raw transactions as CSV spreadsheets.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GoldButton(
                            text = "Export PDF",
                            onClick = {
                                try {
                                    val pdfFile = viewModel.exportPdf(context, "Financial Statement - ${selectedPeriod.displayName}")
                                    ExportUtils.shareFile(context, pdfFile, "application/pdf", "GoldLedger Financial Report")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            leadingIcon = Icons.Default.PictureAsPdf,
                            modifier = Modifier.weight(1f),
                            testTagStr = "export_pdf_button"
                        )

                        GoldOutlinedButton(
                            text = "Export CSV",
                            onClick = {
                                try {
                                    val csvFile = viewModel.exportCsv(context)
                                    ExportUtils.shareFile(context, csvFile, "text/csv", "GoldLedger Transactions CSV")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            testTagStr = "export_csv_button"
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
