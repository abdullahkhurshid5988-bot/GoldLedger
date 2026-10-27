package com.example.core.insights

import com.example.core.utilities.FinancialUtils
import com.example.data.model.BudgetWithDetails
import com.example.data.model.SupportedCurrency
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import java.util.Calendar

data class FinancialInsight(
    val id: String,
    val title: String,
    val description: String,
    val type: InsightType,
    val isPositive: Boolean
)

enum class InsightType {
    SPENDING,
    BUDGET,
    SAVINGS,
    CATEGORY,
    GENERAL
}

object FinancialInsightsEngine {

    fun generateInsights(
        transactions: List<TransactionWithDetails>,
        budgets: List<BudgetWithDetails>,
        currency: SupportedCurrency
    ): List<FinancialInsight> {
        val insights = mutableListOf<FinancialInsight>()
        val now = System.currentTimeMillis()

        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        cal.add(Calendar.MONTH, -1)
        val prevMonth = cal.get(Calendar.MONTH)
        val prevMonthYear = cal.get(Calendar.YEAR)

        // Split transactions by this month vs previous month
        val thisMonthExpenses = transactions.filter {
            it.transaction.type == TransactionType.EXPENSE &&
                    isSameMonthAndYear(it.transaction.dateTimestamp, currentMonth, currentYear)
        }
        val prevMonthExpenses = transactions.filter {
            it.transaction.type == TransactionType.EXPENSE &&
                    isSameMonthAndYear(it.transaction.dateTimestamp, prevMonth, prevMonthYear)
        }

        val thisMonthTotalExpense = thisMonthExpenses.sumOf { it.transaction.amountMinorUnits }
        val prevMonthTotalExpense = prevMonthExpenses.sumOf { it.transaction.amountMinorUnits }

        val thisMonthIncomes = transactions.filter {
            it.transaction.type == TransactionType.INCOME &&
                    isSameMonthAndYear(it.transaction.dateTimestamp, currentMonth, currentYear)
        }
        val thisMonthTotalIncome = thisMonthIncomes.sumOf { it.transaction.amountMinorUnits }

        // 1. Overall monthly spending comparison
        if (prevMonthTotalExpense > 0 && thisMonthTotalExpense > 0) {
            val diff = thisMonthTotalExpense - prevMonthTotalExpense
            val pct = (diff.toDouble() / prevMonthTotalExpense.toDouble()) * 100
            val absPct = kotlin.math.abs(pct).toInt()
            if (diff > 0) {
                insights.add(
                    FinancialInsight(
                        id = "monthly_expense_increase",
                        title = "Higher Spending This Month",
                        description = "Your total expenses are $absPct% higher than last month. Consider reviewing non-essential purchases.",
                        type = InsightType.SPENDING,
                        isPositive = false
                    )
                )
            } else if (diff < 0) {
                insights.add(
                    FinancialInsight(
                        id = "monthly_expense_decrease",
                        title = "Reduced Spending",
                        description = "Great job! Your spending is $absPct% lower compared to the same period last month.",
                        type = InsightType.SPENDING,
                        isPositive = true
                    )
                )
            }
        }

        // 2. Savings rate insight
        if (thisMonthTotalIncome > 0) {
            val netSavings = thisMonthTotalIncome - thisMonthTotalExpense
            val savingsRate = ((netSavings.toDouble() / thisMonthTotalIncome.toDouble()) * 100).toInt()
            if (savingsRate >= 20) {
                insights.add(
                    FinancialInsight(
                        id = "savings_rate_healthy",
                        title = "Strong Savings Rate",
                        description = "You have preserved $savingsRate% of your income this month, beating the standard 20% financial benchmark.",
                        type = InsightType.SAVINGS,
                        isPositive = true
                    )
                )
            } else if (savingsRate < 0) {
                val deficit = FinancialUtils.formatCurrency(kotlin.math.abs(netSavings), currency)
                insights.add(
                    FinancialInsight(
                        id = "savings_rate_negative",
                        title = "Deficit Warning",
                        description = "Expenses exceed income by $deficit this month. Tap into reserves or curb discretionary outflow.",
                        type = InsightType.SAVINGS,
                        isPositive = false
                    )
                )
            }
        }

        // 3. Category spending insight
        val topCategory = thisMonthExpenses
            .groupBy { it.category?.name ?: "Uncategorized" }
            .mapValues { entry -> entry.value.sumOf { it.transaction.amountMinorUnits } }
            .maxByOrNull { it.value }

        if (topCategory != null && thisMonthTotalExpense > 0) {
            val catPct = ((topCategory.value.toDouble() / thisMonthTotalExpense.toDouble()) * 100).toInt()
            if (catPct >= 30) {
                val formattedCatTotal = FinancialUtils.formatCurrency(topCategory.value, currency)
                insights.add(
                    FinancialInsight(
                        id = "top_category_spend",
                        title = "Dominant Category: ${topCategory.key}",
                        description = "${topCategory.key} accounts for $catPct% ($formattedCatTotal) of your spending this month.",
                        type = InsightType.CATEGORY,
                        isPositive = catPct < 40
                    )
                )
            }
        }

        // 4. Budget health insight
        for (budgetWithCat in budgets) {
            val budget = budgetWithCat.budget
            if (budget.limitMinorUnits > 0) {
                val catExpenses = if (budget.categoryId != null) {
                    thisMonthExpenses.filter { it.transaction.categoryId == budget.categoryId }
                } else {
                    thisMonthExpenses
                }
                val spent = catExpenses.sumOf { it.transaction.amountMinorUnits }
                val pct = ((spent.toDouble() / budget.limitMinorUnits.toDouble()) * 100).toInt()

                if (pct >= 100) {
                    insights.add(
                        FinancialInsight(
                            id = "budget_over_${budget.id}",
                            title = "Budget Exceeded: ${budget.name}",
                            description = "You have reached $pct% of your ${budget.name} limit. Additional expenses will exceed target limits.",
                            type = InsightType.BUDGET,
                            isPositive = false
                        )
                    )
                } else if (pct >= 80) {
                    insights.add(
                        FinancialInsight(
                            id = "budget_warning_${budget.id}",
                            title = "Budget Alert: ${budget.name}",
                            description = "You have used $pct% of your ${budget.name} budget. Watch upcoming transactions closely.",
                            type = InsightType.BUDGET,
                            isPositive = false
                        )
                    )
                }
            }
        }

        if (insights.isEmpty()) {
            insights.add(
                FinancialInsight(
                    id = "general_welcome",
                    title = "Smart Tracking Active",
                    description = "Continue logging daily transactions to unlock deep personalized spending trends and offline analytics.",
                    type = InsightType.GENERAL,
                    isPositive = true
                )
            )
        }

        return insights
    }

    private fun isSameMonthAndYear(timestamp: Long, month: Int, year: Int): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
    }
}
