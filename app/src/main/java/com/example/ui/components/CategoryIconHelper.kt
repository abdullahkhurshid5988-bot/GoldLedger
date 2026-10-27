package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
    fun getIcon(name: String): ImageVector {
        return when (name.lowercase()) {
            "restaurant", "food" -> Icons.Default.Restaurant
            "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
            "directions_bus", "transport" -> Icons.Default.DirectionsBus
            "local_gas_station", "fuel" -> Icons.Default.LocalGasStation
            "receipt_long", "bills" -> Icons.Default.ReceiptLong
            "home", "rent" -> Icons.Default.Home
            "school", "education" -> Icons.Default.School
            "medical_services", "health" -> Icons.Default.MedicalServices
            "movie", "entertainment" -> Icons.Default.Movie
            "flight", "travel" -> Icons.Default.Flight
            "wifi", "internet" -> Icons.Default.Wifi
            "shopping_cart", "groceries" -> Icons.Default.ShoppingCart
            "family_restroom", "family" -> Icons.Default.FamilyRestroom
            "payments", "salary" -> Icons.Default.Payments
            "storefront", "business" -> Icons.Default.Storefront
            "laptop", "freelancing" -> Icons.Default.Laptop
            "trending_up", "investment" -> Icons.Default.TrendingUp
            "card_giftcard", "gift" -> Icons.Default.CardGiftcard
            "star", "bonus" -> Icons.Default.Star
            "percent", "commission" -> Icons.Default.Percent
            "apartment", "rental" -> Icons.Default.Apartment
            "wallet", "cash" -> Icons.Default.Wallet
            "account_balance", "bank" -> Icons.Default.AccountBalance
            "smartphone", "mobile" -> Icons.Default.Smartphone
            "credit_card" -> Icons.Default.CreditCard
            "savings" -> Icons.Default.Savings
            else -> Icons.Default.MoreHoriz
        }
    }
}
