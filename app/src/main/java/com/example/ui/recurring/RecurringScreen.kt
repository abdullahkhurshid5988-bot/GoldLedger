package com.example.ui.recurring

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
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.CategoryType
import com.example.data.model.RecurringFrequency
import com.example.data.model.RecurringWithDetails
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
import java.util.Calendar

@Composable
fun RecurringScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val recurringList by viewModel.recurringFlow.collectAsState()
    val currency by viewModel.currencyFlow.collectAsState()
    val accounts by viewModel.accountsFlow.collectAsState()
    val categories by viewModel.categoriesFlow.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BlackPrimary,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GoldPrimary,
                contentColor = Color(0xFF141208),
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("fab_add_recurring")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Recurring Payment",
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
                    text = "Recurring & Subscriptions",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (recurringList.isEmpty()) {
                EmptyStateView(
                    title = "No Recurring Payments",
                    subtitle = "Schedule automated payments for bills, salaries, rent, and subscriptions.",
                    icon = Icons.Default.Repeat,
                    buttonText = "Schedule Payment",
                    onButtonClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(recurringList, key = { it.recurring.id }) { item ->
                        RecurringItemCard(
                            item = item,
                            currency = currency,
                            onDelete = { viewModel.deleteRecurring(item.recurring) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddRecurringDialog(
            accounts = accounts,
            categories = categories,
            currencySymbol = currency.symbol,
            decimalDigits = currency.decimalDigits,
            onDismiss = { showAddDialog = false },
            onConfirm = { type, desc, amountMinorUnits, accountId, categoryId, freq, nextDate ->
                showAddDialog = false
                viewModel.saveRecurring(
                    type = type,
                    amountMinorUnits = amountMinorUnits,
                    accountId = accountId,
                    categoryId = categoryId,
                    description = desc,
                    frequency = freq,
                    nextExecutionDate = nextDate
                )
            }
        )
    }
}

@Composable
fun RecurringItemCard(
    item: RecurringWithDetails,
    currency: com.example.data.model.SupportedCurrency,
    onDelete: () -> Unit
) {
    val rec = item.recurring
    val isIncome = rec.type == TransactionType.INCOME

    Surface(
        color = BlackCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF282828), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isIncome) Color(0xFF142E1F) else Color(0xFF331515)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    tint = if (isIncome) IncomeGreen else ExpenseRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rec.description,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${rec.frequency.displayName} • Next: ${FinancialUtils.formatDate(rec.nextExecutionDate)}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FinancialUtils.formatCurrency(rec.amountMinorUnits, currency, includeSign = true),
                    color = if (isIncome) IncomeGreen else ExpenseRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddRecurringDialog(
    accounts: List<com.example.data.model.AccountEntity>,
    categories: List<com.example.data.model.CategoryEntity>,
    currencySymbol: String,
    decimalDigits: Int,
    onDismiss: () -> Unit,
    onConfirm: (TransactionType, String, Long, Long, Long?, RecurringFrequency, Long) -> Unit
) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedFrequency by remember { mutableStateOf(RecurringFrequency.MONTHLY) }

    val relevantCategories = categories.filter {
        if (type == TransactionType.EXPENSE) it.type == CategoryType.EXPENSE else it.type == CategoryType.INCOME
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Recurring Payment", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Title (e.g. WiFi Bill, Monthly Salary)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amountText = it },
                    label = { Text("Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Frequency", color = TextMuted, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(RecurringFrequency.values().toList()) { freq ->
                        val isSelected = freq == selectedFrequency
                        Surface(
                            onClick = { selectedFrequency = freq },
                            color = if (isSelected) GoldContainer else Color(0xFF242424),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(
                                1.dp,
                                if (isSelected) GoldPrimary else BlackBorder,
                                RoundedCornerShape(8.dp)
                            )
                        ) {
                            Text(
                                text = freq.displayName,
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
                text = "Schedule",
                onClick = {
                    if (description.isNotBlank()) {
                        val minorUnits = FinancialUtils.parseToMinorUnits(amountText, decimalDigits)
                        if (minorUnits > 0) {
                            val cal = Calendar.getInstance()
                            when (selectedFrequency) {
                                RecurringFrequency.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                                RecurringFrequency.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                                RecurringFrequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
                                RecurringFrequency.YEARLY -> cal.add(Calendar.YEAR, 1)
                            }
                            onConfirm(
                                type,
                                description,
                                minorUnits,
                                selectedAccountId,
                                selectedCategoryId,
                                selectedFrequency,
                                cal.timeInMillis
                            )
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
