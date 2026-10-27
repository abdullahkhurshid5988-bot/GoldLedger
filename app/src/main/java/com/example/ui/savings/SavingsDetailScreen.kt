package com.example.ui.savings

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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.MainViewModel
import com.example.ui.components.GoldButton
import com.example.ui.components.GoldOutlinedButton
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
fun SavingsDetailScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val goalWithContributions = viewModel.selectedSavingsGoal.value
    val currency by viewModel.currencyFlow.collectAsState()
    val accounts by viewModel.accountsFlow.collectAsState()

    var showContributionDialog by remember { mutableStateOf(false) }
    var isDepositMode by remember { mutableStateOf(true) }

    if (goalWithContributions == null) {
        viewModel.navigateBack()
        return
    }

    val goal = goalWithContributions.goal
    val target = goal.targetAmountMinorUnits.coerceAtLeast(1L)
    val percentage = ((goal.savedAmountMinorUnits.toDouble() / target.toDouble()) * 100).toInt()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BlackPrimary)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Bar
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
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

                IconButton(
                    onClick = { viewModel.deleteSavingsGoal(goal) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E1717))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Goal",
                        tint = ExpenseRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Main Goal Card
        item {
            Surface(
                color = BlackCard,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(GoldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = GoldLight,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = goal.name,
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = FinancialUtils.formatCurrency(goal.savedAmountMinorUnits, currency),
                        color = GoldLight,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Target: ${FinancialUtils.formatCurrency(target, currency)} ($percentage%)",
                        color = TextMuted,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { (percentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (percentage >= 100) IncomeGreen else GoldPrimary,
                        trackColor = Color(0xFF282828)
                    )
                }
            }
        }

        // Action Buttons: Deposit & Withdraw
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoldButton(
                    text = "Deposit",
                    onClick = {
                        isDepositMode = true
                        showContributionDialog = true
                    },
                    leadingIcon = Icons.Default.Add,
                    modifier = Modifier.weight(1f),
                    testTagStr = "deposit_savings_button"
                )

                GoldOutlinedButton(
                    text = "Withdraw",
                    onClick = {
                        isDepositMode = false
                        showContributionDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    testTagStr = "withdraw_savings_button"
                )
            }
        }

        // Contribution History
        item {
            Text(
                text = "Contribution History",
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (goalWithContributions.contributions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BlackCard)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No contributions yet. Tap 'Deposit' to add funds.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(goalWithContributions.contributions) { cont ->
                val isDeposit = cont.amountMinorUnits >= 0
                Surface(
                    color = BlackCard,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF282828), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isDeposit) Color(0xFF142E1F) else Color(0xFF331A1A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDeposit) Icons.Default.Add else Icons.Default.Remove,
                                    contentDescription = null,
                                    tint = if (isDeposit) IncomeGreen else ExpenseRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isDeposit) "Deposit" else "Withdrawal",
                                    color = TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = FinancialUtils.formatDate(cont.dateTimestamp),
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = FinancialUtils.formatCurrency(
                                kotlin.math.abs(cont.amountMinorUnits),
                                currency,
                                includeSign = true
                            ),
                            color = if (isDeposit) IncomeGreen else ExpenseRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showContributionDialog) {
        SavingsContributionDialog(
            isDeposit = isDepositMode,
            accounts = accounts,
            currencySymbol = currency.symbol,
            decimalDigits = currency.decimalDigits,
            onDismiss = { showContributionDialog = false },
            onConfirm = { accountId, amountMinorUnits, note ->
                showContributionDialog = false
                viewModel.contributeToSavingsGoal(
                    goalId = goal.id,
                    accountId = accountId,
                    amountMinorUnits = amountMinorUnits,
                    isDeposit = isDepositMode,
                    note = note
                )
            }
        )
    }
}

@Composable
fun SavingsContributionDialog(
    isDeposit: Boolean,
    accounts: List<com.example.data.model.AccountEntity>,
    currencySymbol: String,
    decimalDigits: Int,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long, String) -> Unit
) {
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isDeposit) "Deposit to Goal" else "Withdraw from Goal",
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isDeposit) "Select Account to Deduct From:" else "Select Account to Credit:",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts) { acc ->
                        val isSelected = acc.id == selectedAccountId
                        Surface(
                            onClick = { selectedAccountId = acc.id },
                            color = if (isSelected) GoldContainer else Color(0xFF242424),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(
                                1.dp,
                                if (isSelected) GoldPrimary else BlackBorder,
                                RoundedCornerShape(8.dp)
                            )
                        ) {
                            Text(
                                text = acc.name,
                                color = if (isSelected) GoldLight else TextWhite,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

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

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GoldButton(
                text = if (isDeposit) "Deposit" else "Withdraw",
                onClick = {
                    val minorUnits = FinancialUtils.parseToMinorUnits(amountText, decimalDigits)
                    if (minorUnits > 0) {
                        onConfirm(selectedAccountId, minorUnits, note)
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
