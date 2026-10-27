package com.example.ui.transactions

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.EmptyStateView
import com.example.ui.components.TransactionRowItem
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

enum class SortOrder {
    NEWEST,
    OLDEST,
    AMOUNT_HIGH,
    AMOUNT_LOW
}

@Composable
fun TransactionsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allTransactions by viewModel.allTransactionsFlow.collectAsState()
    val currency by viewModel.currencyFlow.collectAsState()
    val categories by viewModel.categoriesFlow.collectAsState()
    val accounts by viewModel.accountsFlow.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var currentSort by remember { mutableStateOf(SortOrder.NEWEST) }

    // Filter and Sort logic
    val filteredTransactions = remember(
        allTransactions,
        searchQuery,
        selectedTypeFilter,
        selectedCategoryId,
        selectedAccountId,
        currentSort
    ) {
        var list = allTransactions

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.transaction.description.lowercase().contains(q) ||
                        it.transaction.merchant.lowercase().contains(q) ||
                        (it.category?.name?.lowercase()?.contains(q) == true) ||
                        (it.account?.name?.lowercase()?.contains(q) == true)
            }
        }

        if (selectedTypeFilter != null) {
            list = list.filter { it.transaction.type == selectedTypeFilter }
        }

        if (selectedCategoryId != null) {
            list = list.filter { it.transaction.categoryId == selectedCategoryId }
        }

        if (selectedAccountId != null) {
            list = list.filter { it.transaction.accountId == selectedAccountId || it.transaction.toAccountId == selectedAccountId }
        }

        when (currentSort) {
            SortOrder.NEWEST -> list.sortedByDescending { it.transaction.dateTimestamp }
            SortOrder.OLDEST -> list.sortedBy { it.transaction.dateTimestamp }
            SortOrder.AMOUNT_HIGH -> list.sortedByDescending { it.transaction.amountMinorUnits }
            SortOrder.AMOUNT_LOW -> list.sortedBy { it.transaction.amountMinorUnits }
        }
    }

    Scaffold(
        containerColor = BlackPrimary,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.selectedTransaction.value = null
                    viewModel.selectTab(2)
                },
                containerColor = GoldPrimary,
                contentColor = Color(0xFF141208),
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("fab_add_transaction")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
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

            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transactions",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                // Sort toggle chip
                Surface(
                    onClick = {
                        currentSort = when (currentSort) {
                            SortOrder.NEWEST -> SortOrder.AMOUNT_HIGH
                            SortOrder.AMOUNT_HIGH -> SortOrder.AMOUNT_LOW
                            SortOrder.AMOUNT_LOW -> SortOrder.OLDEST
                            SortOrder.OLDEST -> SortOrder.NEWEST
                        }
                    },
                    color = Color(0xFF1F1F1F),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.border(1.dp, BlackBorder, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = GoldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentSort) {
                                SortOrder.NEWEST -> "Newest"
                                SortOrder.OLDEST -> "Oldest"
                                SortOrder.AMOUNT_HIGH -> "Highest"
                                SortOrder.AMOUNT_LOW -> "Lowest"
                            },
                            color = GoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search description, merchant...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
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
                    .testTag("transactions_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Type Filter Chips (All, Expense, Income, Transfer)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    TypeChip(
                        label = "All",
                        isSelected = selectedTypeFilter == null,
                        color = GoldPrimary,
                        onClick = { selectedTypeFilter = null }
                    )
                }
                item {
                    TypeChip(
                        label = "Expenses",
                        isSelected = selectedTypeFilter == TransactionType.EXPENSE,
                        color = ExpenseRed,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == TransactionType.EXPENSE) null else TransactionType.EXPENSE
                        }
                    )
                }
                item {
                    TypeChip(
                        label = "Income",
                        isSelected = selectedTypeFilter == TransactionType.INCOME,
                        color = IncomeGreen,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == TransactionType.INCOME) null else TransactionType.INCOME
                        }
                    )
                }
                item {
                    TypeChip(
                        label = "Transfers",
                        isSelected = selectedTypeFilter == TransactionType.TRANSFER,
                        color = TransferBlue,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == TransactionType.TRANSFER) null else TransactionType.TRANSFER
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transaction List
            if (filteredTransactions.isEmpty()) {
                EmptyStateView(
                    title = "No Transactions Found",
                    subtitle = if (searchQuery.isNotBlank() || selectedTypeFilter != null) {
                        "Try adjusting your filters or search terms."
                    } else {
                        "Start logging your daily expenses and income."
                    },
                    icon = Icons.Default.ReceiptLong,
                    buttonText = "Add Transaction",
                    onButtonClick = {
                        viewModel.selectedTransaction.value = null
                        viewModel.selectTab(2)
                    }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTransactions, key = { it.transaction.id }) { item ->
                        TransactionRowItem(
                            item = item,
                            currency = currency,
                            onClick = {
                                viewModel.selectedTransaction.value = item
                                viewModel.navigateTo(ScreenDestination.TransactionDetail)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(100.dp)) // Clearance for Bottom Navigation & FAB
                    }
                }
            }
        }
    }
}

@Composable
fun TypeChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) color.copy(alpha = 0.2f) else BlackCard,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.border(
            width = 1.dp,
            color = if (isSelected) color else BlackBorder,
            shape = RoundedCornerShape(20.dp)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                color = if (isSelected) color else TextMuted,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
