package com.example.data.model

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

enum class AccountType(val displayName: String, val defaultIcon: String) {
    CASH_WALLET("Cash Wallet", "wallet"),
    BANK_ACCOUNT("Bank Account", "account_balance"),
    MOBILE_WALLET("Mobile Wallet", "smartphone"),
    SAVINGS_ACCOUNT("Savings Account", "savings"),
    CUSTOM("Custom Account", "credit_card")
}

enum class CategoryType {
    EXPENSE,
    INCOME
}

enum class BudgetPeriod(val displayName: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    CUSTOM("Custom Range")
}

enum class RecurringFrequency(val displayName: String, val daysInterval: Int) {
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30),
    YEARLY("Yearly", 365)
}

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    DEBIT_CARD("Debit Card"),
    CREDIT_CARD("Credit Card"),
    BANK_TRANSFER("Bank Transfer"),
    MOBILE_PAY("Mobile Payment"),
    CHECK("Check"),
    OTHER("Other")
}

data class SupportedCurrency(
    val code: String,
    val symbol: String,
    val name: String,
    val decimalDigits: Int = 2
)

object Currencies {
    val ALL = listOf(
        SupportedCurrency("PKR", "Rs", "Pakistani Rupee"),
        SupportedCurrency("USD", "$", "US Dollar"),
        SupportedCurrency("EUR", "€", "Euro"),
        SupportedCurrency("GBP", "£", "British Pound"),
        SupportedCurrency("AED", "AED", "UAE Dirham"),
        SupportedCurrency("SAR", "SAR", "Saudi Riyal"),
        SupportedCurrency("INR", "₹", "Indian Rupee"),
        SupportedCurrency("CAD", "CA$", "Canadian Dollar"),
        SupportedCurrency("AUD", "AU$", "Australian Dollar"),
        SupportedCurrency("JPY", "¥", "Japanese Yen", decimalDigits = 0),
        SupportedCurrency("TRY", "₺", "Turkish Lira"),
        SupportedCurrency("QAR", "QAR", "Qatari Riyal")
    )

    fun getByCode(code: String): SupportedCurrency {
        return ALL.find { it.code.equals(code, ignoreCase = true) } ?: ALL.first()
    }
}
