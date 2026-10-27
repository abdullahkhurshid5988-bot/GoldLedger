package com.example.ui.accounts

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.CategoryIconHelper
import com.example.ui.components.GoldButton
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.BlackCard
import com.example.ui.theme.BlackPrimary
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun AccountsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accountsFlow.collectAsState()
    val currency by viewModel.currencyFlow.collectAsState()
    var showAddAccountDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BlackPrimary,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAccountDialog = true },
                containerColor = GoldPrimary,
                contentColor = Color(0xFF141208),
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("fab_add_account")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Account",
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        text = "Accounts & Wallets",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    onClick = { viewModel.navigateTo(ScreenDestination.TransferMoney) },
                    color = GoldContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Transfer",
                            tint = GoldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Transfer",
                            color = GoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(accounts, key = { it.id }) { acc ->
                    AccountCardItem(
                        account = acc,
                        currency = currency,
                        onSetDefault = { viewModel.setDefaultAccount(acc.id) },
                        onDelete = { viewModel.deleteAccount(acc) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddAccountDialog) {
        AddAccountDialog(
            currencySymbol = currency.symbol,
            decimalDigits = currency.decimalDigits,
            onDismiss = { showAddAccountDialog = false },
            onConfirm = { name, type, balanceMinorUnits, colorHex, iconName ->
                showAddAccountDialog = false
                viewModel.saveAccount(
                    name = name,
                    type = type,
                    initialBalanceMinorUnits = balanceMinorUnits,
                    colorHex = colorHex,
                    iconName = iconName
                )
            }
        )
    }
}

@Composable
fun AccountCardItem(
    account: AccountEntity,
    currency: com.example.data.model.SupportedCurrency,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val color = runCatching { Color(android.graphics.Color.parseColor(account.colorHex)) }.getOrDefault(GoldPrimary)

    Surface(
        color = BlackCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (account.isDefault) GoldPrimary.copy(alpha = 0.6f) else BlackBorder,
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CategoryIconHelper.getIcon(account.iconName),
                    contentDescription = account.name,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = account.name,
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (account.isDefault) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GoldContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("DEFAULT", color = GoldLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = account.type.displayName,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FinancialUtils.formatCurrency(account.balanceMinorUnits, currency),
                    color = if (account.balanceMinorUnits >= 0) IncomeGreen else ExpenseRed,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(BlackCard)
                    ) {
                        if (!account.isDefault) {
                            DropdownMenuItem(
                                text = { Text("Set as Default", color = TextWhite) },
                                onClick = {
                                    menuExpanded = false
                                    onSetDefault()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete Account", color = ExpenseRed) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddAccountDialog(
    currencySymbol: String,
    decimalDigits: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, AccountType, Long, String, String) -> Unit
) {
    var accountName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.BANK_ACCOUNT) }
    var balanceText by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#D4AF37") }

    val colors = listOf("#D4AF37", "#28B67A", "#4D96FF", "#9C27B0", "#FF9800", "#E91E63")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Account / Wallet", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Account Name (e.g. HBL Bank, Easypaisa)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Account Type Chips
                Text("Account Type", color = TextMuted, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AccountType.values().toList()) { type ->
                        val isSelected = type == selectedType
                        Surface(
                            onClick = { selectedType = type },
                            color = if (isSelected) GoldContainer else Color(0xFF242424),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(
                                1.dp,
                                if (isSelected) GoldPrimary else BlackBorder,
                                RoundedCornerShape(8.dp)
                            )
                        ) {
                            Text(
                                text = type.displayName,
                                color = if (isSelected) GoldLight else TextWhite,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) balanceText = it },
                    label = { Text("Opening Balance ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Color Selection
                Text("Accent Color", color = TextMuted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (hex in colors) {
                        val c = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(c)
                                .clickable { selectedColor = hex }
                                .border(
                                    width = if (selectedColor == hex) 2.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            GoldButton(
                text = "Create",
                onClick = {
                    if (accountName.isNotBlank()) {
                        val minorUnits = FinancialUtils.parseToMinorUnits(balanceText, decimalDigits)
                        onConfirm(accountName, selectedType, minorUnits, selectedColor, selectedType.defaultIcon)
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
