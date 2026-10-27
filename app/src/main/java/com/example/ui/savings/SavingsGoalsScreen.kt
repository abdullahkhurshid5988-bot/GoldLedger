package com.example.ui.savings

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Savings
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
import com.example.data.model.SavingsGoalWithContributions
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GoldButton
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackCard
import com.example.ui.theme.BlackPrimary
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun SavingsGoalsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.savingsGoalsFlow.collectAsState()
    val currency by viewModel.currencyFlow.collectAsState()
    var showAddGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BlackPrimary,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = GoldPrimary,
                contentColor = Color(0xFF141208),
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("fab_add_savings_goal")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Goal",
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
                    text = "Savings Goals",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (goals.isEmpty()) {
                EmptyStateView(
                    title = "No Savings Goals Yet",
                    subtitle = "Define your financial targets like Emergency Fund, Vacation, or New Vehicle.",
                    icon = Icons.Default.Savings,
                    buttonText = "Create Goal",
                    onButtonClick = { showAddGoalDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(goals, key = { it.goal.id }) { item ->
                        SavingsGoalCardItem(
                            goalWithContributions = item,
                            currency = currency,
                            onClick = {
                                viewModel.selectedSavingsGoal.value = item
                                viewModel.navigateTo(ScreenDestination.SavingsDetail)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showAddGoalDialog) {
        AddSavingsGoalDialog(
            currencySymbol = currency.symbol,
            decimalDigits = currency.decimalDigits,
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { name, targetAmountMinorUnits ->
                showAddGoalDialog = false
                viewModel.saveSavingsGoal(
                    name = name,
                    targetAmountMinorUnits = targetAmountMinorUnits,
                    targetDate = null,
                    colorHex = "#D4AF37",
                    iconName = "savings"
                )
            }
        )
    }
}

@Composable
fun SavingsGoalCardItem(
    goalWithContributions: SavingsGoalWithContributions,
    currency: com.example.data.model.SupportedCurrency,
    onClick: () -> Unit
) {
    val goal = goalWithContributions.goal
    val target = goal.targetAmountMinorUnits.coerceAtLeast(1L)
    val percentage = ((goal.savedAmountMinorUnits.toDouble() / target.toDouble()) * 100).toInt()
    val isCompleted = percentage >= 100

    Surface(
        onClick = onClick,
        color = BlackCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isCompleted) IncomeGreen.copy(alpha = 0.6f) else BlackBorder,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) IncomeGreen.copy(alpha = 0.2f) else GoldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Savings,
                            contentDescription = null,
                            tint = if (isCompleted) IncomeGreen else GoldLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = goal.name,
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isCompleted) "Goal Achieved!" else "$percentage% completed",
                            color = if (isCompleted) IncomeGreen else GoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = FinancialUtils.formatCurrency(goal.savedAmountMinorUnits, currency),
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isCompleted) IncomeGreen else GoldPrimary,
                trackColor = Color(0xFF282828)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Target: ${FinancialUtils.formatCurrency(target, currency)}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = "Tap to Deposit / Withdraw",
                    color = GoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AddSavingsGoalDialog(
    currencySymbol: String,
    decimalDigits: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    var goalName by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Savings Goal", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = goalName,
                    onValueChange = { goalName = it },
                    label = { Text("Goal Name (e.g. Emergency Fund, Car)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) targetText = it },
                    label = { Text("Target Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                text = "Save Goal",
                onClick = {
                    if (goalName.isNotBlank()) {
                        val minorUnits = FinancialUtils.parseToMinorUnits(targetText, decimalDigits)
                        if (minorUnits > 0) {
                            onConfirm(goalName, minorUnits)
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
