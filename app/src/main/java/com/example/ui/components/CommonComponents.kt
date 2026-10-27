package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.utilities.FinancialUtils
import com.example.data.model.SupportedCurrency
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackCard
import com.example.ui.theme.BlackSecondary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedBg
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TransferBlue
import com.example.ui.theme.TransferBlueBg

@Composable
fun GoldBalanceCard(
    totalBalanceMinorUnits: Long,
    monthlyIncomeMinorUnits: Long,
    monthlyExpenseMinorUnits: Long,
    currency: SupportedCurrency,
    isBalanceHidden: Boolean,
    onToggleHideBalance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayBalance = if (isBalanceHidden) "••••••••" else FinancialUtils.formatCurrency(totalBalanceMinorUnits, currency)
    val displayIncome = if (isBalanceHidden) "••••" else FinancialUtils.formatCurrency(monthlyIncomeMinorUnits, currency)
    val displayExpense = if (isBalanceHidden) "••••" else FinancialUtils.formatCurrency(monthlyExpenseMinorUnits, currency)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF221F18),
                        Color(0xFF141414),
                        Color(0xFF0F0F0F)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        GoldPrimary.copy(alpha = 0.6f),
                        Color(0xFF40361C),
                        Color(0xFF202020)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
            .testTag("main_balance_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TOTAL BALANCE",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
                IconButton(
                    onClick = onToggleHideBalance,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("toggle_balance_visibility")
                ) {
                    Icon(
                        imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Balance Visibility",
                        tint = GoldLight.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = displayBalance,
                color = TextWhite,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Income & Expense Breakdown Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF191919))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Income
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(IncomeGreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Income",
                            tint = IncomeGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Income", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = displayIncome,
                            color = IncomeGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(Color(0xFF333333))
                )

                // Expense
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ExpenseRedBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Expense",
                            tint = ExpenseRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Expenses", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = displayExpense,
                            color = ExpenseRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    testTagStr: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag(testTagStr)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isPrimary) GoldPrimary else Color(0xFF222222))
                .border(
                    width = 1.dp,
                    color = if (isPrimary) GoldLight else BlackBorder,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPrimary) Color(0xFF141208) else GoldLight,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = if (isPrimary) GoldLight else TextMuted,
            fontSize = 11.sp,
            fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun TransactionRowItem(
    item: TransactionWithDetails,
    currency: SupportedCurrency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tx = item.transaction
    val isIncome = tx.type == TransactionType.INCOME
    val isTransfer = tx.type == TransactionType.TRANSFER

    val amountColor = when (tx.type) {
        TransactionType.INCOME -> IncomeGreen
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.TRANSFER -> TransferBlue
    }

    val iconBg = when (tx.type) {
        TransactionType.INCOME -> IncomeGreenBg
        TransactionType.EXPENSE -> ExpenseRedBg
        TransactionType.TRANSFER -> TransferBlueBg
    }

    val iconTint = when (tx.type) {
        TransactionType.INCOME -> IncomeGreen
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.TRANSFER -> TransferBlue
    }

    val iconVector = when {
        isTransfer -> Icons.Default.SwapHoriz
        item.category != null -> CategoryIconHelper.getIcon(item.category.iconName)
        isIncome -> Icons.Default.ArrowDownward
        else -> Icons.Default.ArrowUpward
    }

    val title = tx.description.ifBlank {
        if (isTransfer) "Transfer" else item.category?.name ?: "Expense"
    }

    val subtitle = when {
        isTransfer -> "${item.account?.name ?: "Account"} → ${item.toAccount?.name ?: "Account"}"
        else -> "${item.account?.name ?: "Wallet"} • ${FinancialUtils.formatDate(tx.dateTimestamp, "dd MMM")}"
    }

    Surface(
        onClick = onClick,
        color = BlackCard,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF282828), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = FinancialUtils.formatCurrency(
                    tx.amountMinorUnits,
                    currency,
                    includeSign = !isTransfer
                ),
                color = amountColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    testTagStr: String = ""
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = Color(0xFF141208),
            disabledContainerColor = Color(0xFF333333),
            disabledContentColor = Color(0xFF777777)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(50.dp)
            .testTag(testTagStr)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun GoldOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTagStr: String = ""
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = GoldLight
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(listOf(GoldPrimary, GoldLight))
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(50.dp)
            .testTag(testTagStr)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextWhite,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        if (actionText != null && onActionClick != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onActionClick)
                    .padding(4.dp)
            ) {
                Text(
                    text = actionText,
                    color = GoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = actionText,
                    tint = GoldLight,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(GoldContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldLight,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            color = TextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        if (buttonText != null && onButtonClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            GoldButton(
                text = buttonText,
                onClick = onButtonClick,
                leadingIcon = Icons.Default.Add
            )
        }
    }
}
