package com.example.ui.accounts

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
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.utilities.FinancialUtils
import com.example.ui.MainViewModel
import com.example.ui.components.GoldButton
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackCard
import com.example.ui.theme.BlackPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TransferBlue

@Composable
fun TransferScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accountsFlow.collectAsState()
    val currency by viewModel.currencyFlow.collectAsState()

    var fromAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var toAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: 2L) }
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val fromAccount = accounts.find { it.id == fromAccountId }
    val toAccount = accounts.find { it.id == toAccountId }

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
                    text = "Transfer Funds",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Amount Box
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BlackCard)
                    .border(1.dp, TransferBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TRANSFER AMOUNT (${currency.symbol})",
                        color = TransferBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() || c == '.' } && it.count { c -> c == '.' } <= 1) {
                                amountText = it
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
                            color = TransferBlue,
                            textAlign = TextAlign.Center
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("transfer_amount_input")
                    )
                }
            }
        }

        // Source Account (From)
        item {
            Column {
                Text("From Account (Deduct)", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(accounts) { acc ->
                        val isSelected = acc.id == fromAccountId
                        Surface(
                            onClick = {
                                fromAccountId = acc.id
                                if (toAccountId == acc.id) {
                                    toAccountId = accounts.firstOrNull { it.id != acc.id }?.id ?: 0L
                                }
                            },
                            color = if (isSelected) Color(0xFF1E2838) else BlackCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.border(
                                1.dp,
                                if (isSelected) TransferBlue else BlackBorder,
                                RoundedCornerShape(12.dp)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = acc.name,
                                    color = if (isSelected) TransferBlue else TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = FinancialUtils.formatCurrency(acc.balanceMinorUnits, currency),
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Swap Icon Center Divider
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        val temp = fromAccountId
                        fromAccountId = toAccountId
                        toAccountId = temp
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF262626))
                        .border(1.dp, BlackBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap Accounts",
                        tint = GoldLight,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Destination Account (To)
        item {
            Column {
                Text("To Account (Credit)", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(accounts.filter { it.id != fromAccountId }) { acc ->
                        val isSelected = acc.id == toAccountId
                        Surface(
                            onClick = { toAccountId = acc.id },
                            color = if (isSelected) Color(0xFF1E2838) else BlackCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.border(
                                1.dp,
                                if (isSelected) TransferBlue else BlackBorder,
                                RoundedCornerShape(12.dp)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = acc.name,
                                    color = if (isSelected) TransferBlue else TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = FinancialUtils.formatCurrency(acc.balanceMinorUnits, currency),
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Notes Input
        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Transfer Reason / Reference (Optional)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (errorMessage != null) {
            item {
                Text(text = errorMessage!!, color = ExpenseRed, fontSize = 13.sp)
            }
        }

        item {
            GoldButton(
                text = "Complete Transfer",
                onClick = {
                    val minorUnits = FinancialUtils.parseToMinorUnits(amountText, currency.decimalDigits)
                    if (minorUnits <= 0) {
                        errorMessage = "Please enter a valid transfer amount"
                        return@GoldButton
                    }
                    if (fromAccountId == toAccountId) {
                        errorMessage = "Source and destination accounts must be distinct"
                        return@GoldButton
                    }
                    viewModel.transferMoney(
                        fromAccountId = fromAccountId,
                        toAccountId = toAccountId,
                        amountMinorUnits = minorUnits,
                        notes = notes
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTagStr = "confirm_transfer_button"
            )
        }
    }
}
