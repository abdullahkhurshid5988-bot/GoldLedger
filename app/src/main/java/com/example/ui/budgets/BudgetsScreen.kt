package com.example.ui.budgets

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.utilities.FinancialUtils
import com.example.data.model.BudgetWithDetails
import com.example.data.model.CategoryType
import com.example.data.model.TransactionType
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GoldButton
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

@Composable
fun BudgetsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val budgets by viewModel.budgetsFlow.collectAsState()
    val currency by viewModel.currencyFlow.collectAsState()
    val allTransactions by viewModel.allTransactionsFlow.collectAsState()
    val categories by viewModel.categoriesFlow.collectAsState()

    var showCreateBudgetDialog by remember { mutableStateOf(false) }

    val startOfMonth = FinancialUtils.getStartOfMonth()
    val endOfMonth = FinancialUtils.getEndOfMonth()

    Scaffold(
        containerColor = BlackPrimary,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateBudgetDialog = true },
                containerColor = GoldPrimary,
                contentColor = Color(0xFF141208),
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("fab_add_budget")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Budget",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // App Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF222222))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Budgets & Spending Limits",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (budgets.isEmpty()) {
                EmptyStateView(
                    title = "No Active Budgets",
                    subtitle = "Set monthly spending limits for categories to stay within your targets.",
                    icon = Icons.Default.PieChart,
                    buttonText = "Create First Budget",
                    onButtonClick = { showCreateBudgetDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(budgets, key = { it.budget.id }) { item ->
                        val b = item.budget
                        // Compute actual spent this month
                        val spent = allTransactions
                            .filter {
                                it.transaction.type == TransactionType.EXPENSE &&
                                        it.transaction.dateTimestamp in startOfMonth..endOfMonth &&
                                        (b.categoryId == null || it.transaction.categoryId == b.categoryId)
                            }
                            .sumOf { it.transaction.amountMinorUnits }

                        BudgetCardItem(
                            budgetWithDetails = item,
                            spentMinorUnits = spent,
                            currency = currency,
                            onDelete = { viewModel.deleteBudget(b) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showCreateBudgetDialog) {
        CreateBudgetDialog(
            categories = categories.filter { it.type == CategoryType.EXPENSE },
            currencySymbol = currency.symbol,
            decimalDigits = currency.decimalDigits,
            onDismiss = { showCreateBudgetDialog = false },
            onConfirm = { name, categoryId, limitMinorUnits ->
                showCreateBudgetDialog = false
                viewModel.saveBudget(
                    name = name,
                    categoryId = categoryId,
                    limitMinorUnits = limitMinorUnits
                )
            }
        )
    }
}

@Composable
fun BudgetCardItem(
    budgetWithDetails: BudgetWithDetails,
    spentMinorUnits: Long,
    currency: com.example.data.model.SupportedCurrency,
    onDelete: () -> Unit
) {
    val budget = budgetWithDetails.budget
    val limit = budget.limitMinorUnits.coerceAtLeast(1L)
    val percentage = ((spentMinorUnits.toDouble() / limit.toDouble()) * 100).toInt()
    val remaining = limit - spentMinorUnits

    val statusColor = when {
        percentage >= 100 -> ExpenseRed
        percentage >= 80 -> GoldLight
        else -> IncomeGreen
    }

    val statusLabel = when {
        percentage >= 100 -> "Limit Exceeded"
        percentage >= 80 -> "Approaching Limit"
        else -> "On Track"
    }

    Surface(
        color = BlackCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF282828), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = budget.name,
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = budgetWithDetails.category?.name ?: "All Categories (Monthly)",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Budget",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = statusColor,
                trackColor = Color(0xFF282828)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Spent", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = FinancialUtils.formatCurrency(spentMinorUnits, currency),
                        color = statusColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Used", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = "$percentage%",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Limit", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = FinancialUtils.formatCurrency(limit, currency),
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CreateBudgetDialog(
    categories: List<com.example.data.model.CategoryEntity>,
    currencySymbol: String,
    decimalDigits: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, Long?, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var limitText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Spending Budget", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Budget Name (e.g. Dining Out, Monthly Total)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) limitText = it },
                    label = { Text("Monthly Limit ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Target Category (Optional)", color = TextMuted, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Surface(
                            onClick = { selectedCategoryId = null },
                            color = if (selectedCategoryId == null) GoldContainer else Color(0xFF242424),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(
                                1.dp,
                                if (selectedCategoryId == null) GoldPrimary else BlackBorder,
                                RoundedCornerShape(8.dp)
                            )
                        ) {
                            Text(
                                text = "All Categories",
                                color = if (selectedCategoryId == null) GoldLight else TextWhite,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    items(categories) { cat ->
                        val isSelected = cat.id == selectedCategoryId
                        Surface(
                            onClick = { selectedCategoryId = cat.id },
                            color = if (isSelected) GoldContainer else Color(0xFF242424),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(
                                1.dp,
                                if (isSelected) GoldPrimary else BlackBorder,
                                RoundedCornerShape(8.dp)
                            )
                        ) {
                            Text(
                                text = cat.name,
                                color = if (isSelected) GoldLight else TextWhite,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            GoldButton(
                text = "Save Budget",
                onClick = {
                    if (name.isNotBlank()) {
                        val minorUnits = FinancialUtils.parseToMinorUnits(limitText, decimalDigits)
                        if (minorUnits > 0) {
                            onConfirm(name, selectedCategoryId, minorUnits)
                        }
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextWhite)
            }
        },
        containerColor = BlackCard,
        shape = RoundedCornerShape(16.dp)
    )
}
