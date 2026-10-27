package com.example.ui.transactions

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.utilities.FinancialUtils
import com.example.data.model.TransactionType
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.CategoryIconHelper
import com.example.ui.components.GoldButton
import com.example.ui.components.GoldOutlinedButton
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackCard
import com.example.ui.theme.BlackPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TransferBlue

@Composable
fun TransactionDetailScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val txDetails = viewModel.selectedTransaction.value
    val currency by viewModel.currencyFlow.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (txDetails == null) {
        viewModel.navigateBack()
        return
    }

    val tx = txDetails.transaction
    val isIncome = tx.type == TransactionType.INCOME
    val isTransfer = tx.type == TransactionType.TRANSFER

    val amountColor = when (tx.type) {
        TransactionType.INCOME -> IncomeGreen
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.TRANSFER -> TransferBlue
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Transaction?", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete this transaction? The account balance will be automatically restored.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteTransaction(tx)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextWhite)
                }
            },
            containerColor = BlackCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.selectTab(2) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF222222))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = GoldLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E1717))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ExpenseRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Amount & Type Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BlackCard)
                    .border(1.dp, Color(0xFF2D2D2D), RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(amountColor.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tx.type.name,
                            color = amountColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = FinancialUtils.formatCurrency(tx.amountMinorUnits, currency, includeSign = !isTransfer),
                        color = amountColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = tx.description.ifBlank { tx.type.name },
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Details Section
        item {
            Surface(
                color = BlackCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DetailRow("Date", FinancialUtils.formatDate(tx.dateTimestamp, "EEEE, dd MMMM yyyy"))
                    DetailRow("Time", tx.timeString.ifBlank { FinancialUtils.formatTime(tx.dateTimestamp) })

                    if (txDetails.category != null) {
                        DetailRow("Category", txDetails.category.name)
                    }

                    DetailRow("Account", txDetails.account?.name ?: "Unknown")

                    if (isTransfer && txDetails.toAccount != null) {
                        DetailRow("Transferred To", txDetails.toAccount.name)
                    }

                    DetailRow("Payment Method", tx.paymentMethod.displayName)

                    if (tx.merchant.isNotBlank()) {
                        DetailRow("Merchant / Payee", tx.merchant)
                    }

                    if (tx.notes.isNotBlank()) {
                        DetailRow("Notes", tx.notes)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 13.sp)
        Text(
            text = value,
            color = TextWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
