package com.example.ui.settings

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.ads.AdManager
import com.example.data.model.Currencies
import com.example.data.model.SupportedCurrency
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
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
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currency by viewModel.currencyFlow.collectAsState()
    val isDarkMode by viewModel.isDarkModeFlow.collectAsState()
    val isPinEnabled by viewModel.isPinLockEnabledFlow.collectAsState()

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var hasVipBadge by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BlackPrimary)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Settings & Preferences",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // VIP Cosmetic Banner (Rewarded Ad perk)
        item {
            Surface(
                color = GoldContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFF141208),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (hasVipBadge) "VIP Gold Member Active" else "Unlock VIP Gold Badge",
                                color = GoldLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (hasVipBadge) "Cosmetic reward unlocked" else "Watch a rewarded test ad to unlock",
                                color = TextWhite.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (!hasVipBadge) {
                        Surface(
                            onClick = {
                                if (context is Activity) {
                                    AdManager.showRewardedAd(
                                        activity = context,
                                        onRewardEarned = {
                                            hasVipBadge = true
                                            Toast.makeText(context, "VIP Gold Badge Unlocked!", Toast.LENGTH_SHORT).show()
                                        },
                                        onDismissed = {},
                                        onNotAvailable = {
                                            Toast.makeText(context, "Rewarded ad currently loading, try again shortly.", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                            color = GoldPrimary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Unlock",
                                color = Color(0xFF141208),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section: Finance Management
        item {
            SettingsSectionHeader(title = "Financial Management")
        }

        item {
            Surface(
                color = BlackCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackBorder, RoundedCornerShape(16.dp))
            ) {
                Column {
                    SettingsItemRow(
                        icon = Icons.Default.Payments,
                        title = "Base Currency",
                        subtitle = "${currency.code} (${currency.symbol} - ${currency.name})",
                        onClick = { showCurrencyDialog = true }
                    )
                    SettingsDivider()
                    SettingsItemRow(
                        icon = Icons.Default.AccountBalance,
                        title = "Accounts & Wallets",
                        subtitle = "Manage bank accounts, cash wallets & balances",
                        onClick = { viewModel.navigateTo(ScreenDestination.AccountsList) }
                    )
                    SettingsDivider()
                    SettingsItemRow(
                        icon = Icons.Default.PieChart,
                        title = "Budgets & Spending Limits",
                        subtitle = "Configure monthly threshold notifications",
                        onClick = { viewModel.navigateTo(ScreenDestination.BudgetsList) }
                    )
                    SettingsDivider()
                    SettingsItemRow(
                        icon = Icons.Default.Savings,
                        title = "Savings Goals",
                        subtitle = "Track savings deposits and target dates",
                        onClick = { viewModel.navigateTo(ScreenDestination.SavingsList) }
                    )
                    SettingsDivider()
                    SettingsItemRow(
                        icon = Icons.Default.Repeat,
                        title = "Recurring Transactions",
                        subtitle = "Scheduled salaries, subscriptions & bills",
                        onClick = { viewModel.navigateTo(ScreenDestination.RecurringList) }
                    )
                    SettingsDivider()
                    SettingsItemRow(
                        icon = Icons.Default.Category,
                        title = "Categories",
                        subtitle = "Customize expense & income category badges",
                        onClick = { viewModel.navigateTo(ScreenDestination.CategoriesList) }
                    )
                }
            }
        }

        // Section: Security & System
        item {
            SettingsSectionHeader(title = "Security & System")
        }

        item {
            Surface(
                color = BlackCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackBorder, RoundedCornerShape(16.dp))
            ) {
                Column {
                    SettingsItemRow(
                        icon = Icons.Default.Lock,
                        title = "Security & PIN Lock",
                        subtitle = if (isPinEnabled) "PIN Lock Active" else "Disabled",
                        onClick = { viewModel.navigateTo(ScreenDestination.SecuritySettings) }
                    )
                    SettingsDivider()
                    SettingsItemRow(
                        icon = Icons.Default.Backup,
                        title = "Backup & Restore",
                        subtitle = "Export offline JSON backup & restore data",
                        onClick = { viewModel.navigateTo(ScreenDestination.BackupRestore) }
                    )
                }
            }
        }

        // Section: About & Compliance
        item {
            SettingsSectionHeader(title = "About & Information")
        }

        item {
            Surface(
                color = BlackCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackBorder, RoundedCornerShape(16.dp))
            ) {
                Column {
                    SettingsItemRow(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy Policy",
                        subtitle = "Offline-first policy & AdMob details",
                        onClick = { viewModel.navigateTo(ScreenDestination.PrivacyPolicy) }
                    )
                    SettingsDivider()
                    SettingsItemRow(
                        icon = Icons.Default.Info,
                        title = "About GoldLedger",
                        subtitle = "Version 1.0 (Production Build)",
                        onClick = { viewModel.navigateTo(ScreenDestination.AboutApp) }
                    )
                    SettingsDivider()
                    SettingsItemRow(
                        icon = Icons.Default.Share,
                        title = "Share Application",
                        subtitle = "Recommend GoldLedger to friends and colleagues",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "GoldLedger — Smart Expense & Budget Manager")
                                putExtra(Intent.EXTRA_TEXT, "Take control of your personal finances with GoldLedger, a luxury offline-first expense manager for Android!")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share GoldLedger"))
                        }
                    )
                    SettingsDivider()
                    SettingsItemRow(
                        icon = Icons.Default.DeleteForever,
                        title = "Delete All Data",
                        subtitle = "Wipe all accounts, transactions & reset app",
                        titleColor = ExpenseRed,
                        onClick = { showDeleteConfirmDialog = true }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Currency Picker Dialog
    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Base Currency", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(Currencies.ALL) { curr ->
                        val isSelected = curr.code == currency.code
                        Surface(
                            onClick = {
                                viewModel.setCurrency(curr)
                                showCurrencyDialog = false
                            },
                            color = if (isSelected) GoldContainer else Color(0xFF222222),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (isSelected) GoldPrimary else BlackBorder, RoundedCornerShape(10.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = curr.symbol,
                                        color = GoldLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${curr.name} (${curr.code})",
                                        color = TextWhite,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Close", color = TextWhite)
                }
            },
            containerColor = BlackCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Clear Data Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Wipe All Financial Data?", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This action is permanent and cannot be undone. All recorded transactions, custom accounts, budgets, savings goals, and preferences will be permanently erased.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.clearAllData()
                        Toast.makeText(context, "All data has been reset", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                ) {
                    Text("Confirm Wipe", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextWhite)
                }
            },
            containerColor = BlackCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = GoldLight,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = TextWhite
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222222)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (titleColor == ExpenseRed) ExpenseRed else GoldLight,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, color = titleColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, color = TextMuted, fontSize = 12.sp)
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(Color(0xFF242424))
    )
}

@Composable
fun PrivacyPolicyScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BlackPrimary)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                    text = "Privacy Policy",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            Surface(
                color = BlackCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("GoldLedger Privacy Policy", color = GoldLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Effective Date: October 2026", color = TextMuted, fontSize = 11.sp)

                    PolicyParagraph(
                        title = "1. Offline-First Architecture & Data Ownership",
                        content = "GoldLedger is built on an offline-first architecture using a private local SQLite Room database. All financial transactions, account balances, budgets, savings goals, and category configurations entered into GoldLedger are stored exclusively on your device. We do not transmit or store your personal financial data on remote servers."
                    )

                    PolicyParagraph(
                        title = "2. No Banking Credentials Required",
                        content = "GoldLedger does not connect to external banking institutions, nor does it ever request bank account passwords, PINs, or confidential banking credentials. All records are managed manually or via offline recurring schedules."
                    )

                    PolicyParagraph(
                        title = "3. Advertising & Google AdMob",
                        content = "GoldLedger integrates the Google Mobile Ads SDK (AdMob) to display non-intrusive advertisements. AdMob may collect and process pseudonymous identifiers (such as the Google Advertising ID), device telemetry, and coarse network information in compliance with Google's Play Developer Policies. Financial transaction amounts and ledger entries are never shared with advertisers."
                    )

                    PolicyParagraph(
                        title = "4. Local Backup & Storage Access",
                        content = "When you use the Backup & Restore feature, GoldLedger uses the Android Storage Access Framework (SAF) to save your encrypted or plain JSON backup file to a location chosen by you. We never access private storage outside the designated application directories."
                    )

                    PolicyParagraph(
                        title = "5. Biometric and Cryptographic Security",
                        content = "If you enable PIN Lock or Biometric Unlock, your PIN is securely hashed using SHA-256 with a unique salt. Biometric authentication is handled by the Android Jetpack Biometric framework and your biometric templates never leave your device's hardware-backed Trusted Execution Environment (TEE)."
                    )

                    PolicyParagraph(
                        title = "6. Contact & Support",
                        content = "For questions regarding this Privacy Policy or application support, contact the GoldLedger development team at: support@goldledger.app."
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PolicyParagraph(title: String, content: String) {
    Column {
        Text(text = title, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = content, color = TextMuted, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
fun AboutScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BlackPrimary)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                    text = "About GoldLedger",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                painter = painterResource(id = R.drawable.goldledger_logo_512),
                contentDescription = "GoldLedger Logo",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, GoldPrimary, RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "GoldLedger",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Smart Expense & Budget Manager",
                color = GoldLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Version 1.0 (Build 100) • Softonic & Play Ready",
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        item {
            Surface(
                color = BlackCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BlackBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Key Highlights", color = GoldLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    HighlightRow("Offline-First Architecture", "Full functionality with local Room SQLite database without requiring internet.")
                    HighlightRow("Exact Financial Precision", "Integer minor units arithmetic eliminating floating-point errors.")
                    HighlightRow("Luxury Black & Gold Styling", "Engineered with Material 3 fintech aesthetic and high contrast.")
                    HighlightRow("Smart Budget Monitoring", "Real-time alerts at 80% and 100% threshold consumption.")
                    HighlightRow("Savings Targets & Tracking", "Dedicated goal reserves with contribution logs.")
                    HighlightRow("Automated Recurring Payments", "Battery-friendly Android WorkManager background scheduler.")
                    HighlightRow("PDF & CSV Exporting", "Instant report generation with Android Share Sheet integration.")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun HighlightRow(title: String, desc: String) {
    Column {
        Text(text = "• $title", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(text = desc, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(start = 12.dp))
    }
}
