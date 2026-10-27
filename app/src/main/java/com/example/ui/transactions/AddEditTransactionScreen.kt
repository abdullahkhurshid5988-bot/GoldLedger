package com.example.ui.transactions

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.utilities.FinancialUtils
import com.example.data.model.CategoryType
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionType
import com.example.ui.MainViewModel
import com.example.ui.components.CategoryIconHelper
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
import com.example.ui.theme.TransferBlue
import java.util.Calendar

@Composable
fun AddEditTransactionScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currency by viewModel.currencyFlow.collectAsState()
    val accounts by viewModel.accountsFlow.collectAsState()
    val categories by viewModel.categoriesFlow.collectAsState()
    val existingTx = viewModel.selectedTransaction.value

    var transactionType by remember {
        mutableStateOf(existingTx?.transaction?.type ?: TransactionType.EXPENSE)
    }

    var amountText by remember {
        mutableStateOf(
            if (existingTx != null) {
                FinancialUtils.minorUnitsToInputString(existingTx.transaction.amountMinorUnits, currency.decimalDigits)
            } else ""
        )
    }

    var description by remember {
        mutableStateOf(existingTx?.transaction?.description ?: "")
    }

    var merchant by remember {
        mutableStateOf(existingTx?.transaction?.merchant ?: "")
    }

    var notes by remember {
        mutableStateOf(existingTx?.transaction?.notes ?: "")
    }

    var selectedAccountId by remember {
        mutableStateOf(existingTx?.transaction?.accountId ?: accounts.firstOrNull { it.isDefault }?.id ?: accounts.firstOrNull()?.id ?: 1L)
    }

    var selectedToAccountId by remember {
        mutableStateOf(existingTx?.transaction?.toAccountId ?: accounts.getOrNull(1)?.id)
    }

    var selectedCategoryId by remember {
        mutableStateOf(existingTx?.transaction?.categoryId)
    }

    var paymentMethod by remember {
        mutableStateOf(existingTx?.transaction?.paymentMethod ?: PaymentMethod.CASH)
    }

    var dateTimestamp by remember {
        mutableStateOf(existingTx?.transaction?.dateTimestamp ?: System.currentTimeMillis())
    }

    var timeString by remember {
        mutableStateOf(existingTx?.transaction?.timeString ?: FinancialUtils.formatTime(System.currentTimeMillis()))
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Filter categories by selected transaction type
    val relevantCategories = categories.filter {
        if (transactionType == TransactionType.EXPENSE) it.type == CategoryType.EXPENSE
        else it.type == CategoryType.INCOME
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BlackPrimary)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Navigation Header
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingTx != null) "Edit Transaction" else "New Transaction",
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF222222))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Transaction Type Segment Selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1A1A1A))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TypeSegmentButton(
                    label = "Expense",
                    isSelected = transactionType == TransactionType.EXPENSE,
                    activeColor = ExpenseRed,
                    onClick = {
                        transactionType = TransactionType.EXPENSE
                        selectedCategoryId = null
                    },
                    modifier = Modifier.weight(1f),
                    testTagStr = "type_expense_tab"
                )
                TypeSegmentButton(
                    label = "Income",
                    isSelected = transactionType == TransactionType.INCOME,
                    activeColor = IncomeGreen,
                    onClick = {
                        transactionType = TransactionType.INCOME
                        selectedCategoryId = null
                    },
                    modifier = Modifier.weight(1f),
                    testTagStr = "type_income_tab"
                )
                TypeSegmentButton(
                    label = "Transfer",
                    isSelected = transactionType == TransactionType.TRANSFER,
                    activeColor = TransferBlue,
                    onClick = {
                        transactionType = TransactionType.TRANSFER
                        selectedCategoryId = null
                    },
                    modifier = Modifier.weight(1f),
                    testTagStr = "type_transfer_tab"
                )
            }
        }

        // Large Amount Input Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BlackCard)
                    .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AMOUNT (${currency.symbol})",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { input ->
                            // Allow digits and single decimal dot
                            if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) {
                                amountText = input
                                errorMessage = null
                            }
                        },
                        placeholder = {
                            Text(
                                "0.00",
                                color = TextMuted,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when (transactionType) {
                                TransactionType.EXPENSE -> ExpenseRed
                                TransactionType.INCOME -> IncomeGreen
                                TransactionType.TRANSFER -> TransferBlue
                            },
                            textAlign = TextAlign.Center
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_amount_input")
                    )
                }
            }
        }

        // Category Selector (if Expense or Income)
        if (transactionType != TransactionType.TRANSFER) {
            item {
                Column {
                    Text(
                        text = "Category",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(relevantCategories) { cat ->
                            val isSelected = cat.id == selectedCategoryId
                            val catColor = runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }.getOrDefault(GoldPrimary)

                            Surface(
                                onClick = { selectedCategoryId = cat.id },
                                color = if (isSelected) GoldContainer else BlackCard,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.border(
                                    1.dp,
                                    if (isSelected) GoldPrimary else BlackBorder,
                                    RoundedCornerShape(12.dp)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(catColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = CategoryIconHelper.getIcon(cat.iconName),
                                            contentDescription = cat.name,
                                            tint = catColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cat.name,
                                        color = if (isSelected) GoldLight else TextWhite,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Account Selector
        item {
            Column {
                Text(
                    text = if (transactionType == TransactionType.TRANSFER) "From Account" else "Account / Wallet",
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(accounts) { acc ->
                        val isSelected = acc.id == selectedAccountId
                        Surface(
                            onClick = { selectedAccountId = acc.id },
                            color = if (isSelected) GoldContainer else BlackCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.border(
                                1.dp,
                                if (isSelected) GoldPrimary else BlackBorder,
                                RoundedCornerShape(12.dp)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = acc.name,
                                    color = if (isSelected) GoldLight else TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${FinancialUtils.formatCurrency(acc.balanceMinorUnits, currency)})",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Destination Account (for Transfer)
        if (transactionType == TransactionType.TRANSFER) {
            item {
                Column {
                    Text(
                        text = "To Account",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(accounts.filter { it.id != selectedAccountId }) { acc ->
                            val isSelected = acc.id == selectedToAccountId
                            Surface(
                                onClick = { selectedToAccountId = acc.id },
                                color = if (isSelected) GoldContainer else BlackCard,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.border(
                                    1.dp,
                                    if (isSelected) GoldPrimary else BlackBorder,
                                    RoundedCornerShape(12.dp)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = acc.name,
                                        color = if (isSelected) GoldLight else TextWhite,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Date and Time Pickers
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date Picker Chip
                Surface(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = dateTimestamp }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                cal.set(y, m, d)
                                dateTimestamp = cal.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    color = BlackCard,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, BlackBorder, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = GoldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = FinancialUtils.formatDate(dateTimestamp),
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Time Picker Chip
                Surface(
                    onClick = {
                        val cal = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                cal.set(Calendar.HOUR_OF_DAY, hour)
                                cal.set(Calendar.MINUTE, minute)
                                timeString = FinancialUtils.formatTime(cal.timeInMillis)
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            false
                        ).show()
                    },
                    color = BlackCard,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, BlackBorder, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Time",
                            tint = GoldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timeString,
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Description Input
        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (e.g. Dinner with team)", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = BlackBorder,
                    focusedContainerColor = BlackCard,
                    unfocusedContainerColor = BlackCard,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_description_input")
            )
        }

        // Merchant Input
        item {
            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant / Payee (Optional)", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = BlackBorder,
                    focusedContainerColor = BlackCard,
                    unfocusedContainerColor = BlackCard,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Notes Input
        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = BlackBorder,
                    focusedContainerColor = BlackCard,
                    unfocusedContainerColor = BlackCard,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Error message if any
        if (errorMessage != null) {
            item {
                Text(
                    text = errorMessage!!,
                    color = ExpenseRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Save Button
        item {
            GoldButton(
                text = if (existingTx != null) "Update Transaction" else "Save Transaction",
                onClick = {
                    val minorUnits = FinancialUtils.parseToMinorUnits(amountText, currency.decimalDigits)
                    if (minorUnits <= 0) {
                        errorMessage = "Please enter an amount greater than 0"
                        return@GoldButton
                    }
                    if (transactionType == TransactionType.TRANSFER && selectedAccountId == selectedToAccountId) {
                        errorMessage = "From and To accounts must be different"
                        return@GoldButton
                    }
                    if (transactionType == TransactionType.TRANSFER && selectedToAccountId == null) {
                        errorMessage = "Please select a destination account"
                        return@GoldButton
                    }

                    viewModel.saveTransaction(
                        id = existingTx?.transaction?.id ?: 0L,
                        type = transactionType,
                        amountMinorUnits = minorUnits,
                        accountId = selectedAccountId,
                        toAccountId = if (transactionType == TransactionType.TRANSFER) selectedToAccountId else null,
                        categoryId = if (transactionType != TransactionType.TRANSFER) selectedCategoryId else null,
                        dateTimestamp = dateTimestamp,
                        timeString = timeString,
                        description = description.ifBlank { transactionType.name },
                        paymentMethod = paymentMethod,
                        merchant = merchant,
                        notes = notes
                    )
                },
                leadingIcon = Icons.Default.Check,
                modifier = Modifier.fillMaxWidth(),
                testTagStr = "save_transaction_button"
            )
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun TypeSegmentButton(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTagStr: String = ""
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) activeColor else Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.height(40.dp).testTag(testTagStr)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (isSelected) Color(0xFF141208) else TextMuted,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}
