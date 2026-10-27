package com.example.ui.categories

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryType
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun CategoriesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categoriesFlow.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Expense, 1: Income
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val activeType = if (selectedTab == 0) CategoryType.EXPENSE else CategoryType.INCOME
    val displayedCategories = categories.filter { it.type == activeType }

    Scaffold(
        containerColor = BlackPrimary,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCategoryDialog = true },
                containerColor = GoldPrimary,
                contentColor = Color(0xFF141208),
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("fab_add_category")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Category",
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
                    text = "Category Management",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs: Expense vs Income
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BlackCard,
                contentColor = GoldLight,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GoldPrimary,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BlackBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Expenses (${categories.count { it.type == CategoryType.EXPENSE }})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Income (${categories.count { it.type == CategoryType.INCOME }})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedCategories, key = { it.id }) { cat ->
                    val color = runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }.getOrDefault(GoldPrimary)

                    Surface(
                        color = BlackCard,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF282828), RoundedCornerShape(14.dp))
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
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(color.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CategoryIconHelper.getIcon(cat.iconName),
                                        contentDescription = cat.name,
                                        tint = color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = cat.name,
                                        color = TextWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (cat.isDefault) {
                                        Text(
                                            text = "Default Category",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            if (!cat.isDefault) {
                                IconButton(
                                    onClick = { viewModel.deleteCategory(cat) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Archive Category",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            type = activeType,
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, iconName, colorHex ->
                showAddCategoryDialog = false
                viewModel.saveCategory(
                    name = name,
                    type = activeType,
                    iconName = iconName,
                    colorHex = colorHex
                )
            }
        )
    }
}

@Composable
fun AddCategoryDialog(
    type: CategoryType,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("restaurant") }
    var selectedColor by remember { mutableStateOf("#D4AF37") }

    val icons = listOf(
        "restaurant", "shopping_bag", "directions_bus", "local_gas_station",
        "receipt_long", "home", "school", "medical_services", "movie",
        "flight", "wifi", "shopping_cart", "family_restroom", "payments",
        "storefront", "laptop", "trending_up", "card_giftcard", "savings"
    )

    val colors = listOf("#D4AF37", "#28B67A", "#4D96FF", "#E65A5A", "#9C27B0", "#FF9800", "#00BCD4", "#E91E63")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New ${type.name.lowercase().replaceFirstChar { it.uppercase() }} Category", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Select Icon", color = TextMuted, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(icons) { iconName ->
                        val isSelected = iconName == selectedIcon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) GoldPrimary else Color(0xFF262626))
                                .clickable { selectedIcon = iconName },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CategoryIconHelper.getIcon(iconName),
                                contentDescription = iconName,
                                tint = if (isSelected) Color(0xFF141208) else TextWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Text("Select Color", color = TextMuted, fontSize = 12.sp)
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
                text = "Add Category",
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedIcon, selectedColor)
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
