package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Currencies
import com.example.data.model.SupportedCurrency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gold_ledger_prefs", Context.MODE_PRIVATE)

    private val _currencyFlow = MutableStateFlow(getCurrency())
    val currencyFlow: StateFlow<SupportedCurrency> = _currencyFlow.asStateFlow()

    private val _hideBalanceFlow = MutableStateFlow(isBalanceHidden())
    val hideBalanceFlow: StateFlow<Boolean> = _hideBalanceFlow.asStateFlow()

    private val _darkModeFlow = MutableStateFlow(isDarkMode())
    val darkModeFlow: StateFlow<Boolean> = _darkModeFlow.asStateFlow()

    private val _isOnboardingCompletedFlow = MutableStateFlow(isOnboardingCompleted())
    val isOnboardingCompletedFlow: StateFlow<Boolean> = _isOnboardingCompletedFlow.asStateFlow()

    private val _isPinLockEnabledFlow = MutableStateFlow(isPinLockEnabled())
    val isPinLockEnabledFlow: StateFlow<Boolean> = _isPinLockEnabledFlow.asStateFlow()

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean("onboarding_completed", false)

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
        _isOnboardingCompletedFlow.value = completed
    }

    fun getCurrency(): SupportedCurrency {
        val code = prefs.getString("currency_code", "PKR") ?: "PKR"
        return Currencies.getByCode(code)
    }

    fun setCurrency(currency: SupportedCurrency) {
        prefs.edit()
            .putString("currency_code", currency.code)
            .putString("currency_symbol", currency.symbol)
            .apply()
        _currencyFlow.value = currency
    }

    fun isBalanceHidden(): Boolean = prefs.getBoolean("hide_balance", false)

    fun setHideBalance(hidden: Boolean) {
        prefs.edit().putBoolean("hide_balance", hidden).apply()
        _hideBalanceFlow.value = hidden
    }

    fun isDarkMode(): Boolean = prefs.getBoolean("dark_mode", true)

    fun setDarkMode(darkMode: Boolean) {
        prefs.edit().putBoolean("dark_mode", darkMode).apply()
        _darkModeFlow.value = darkMode
    }

    fun isPinLockEnabled(): Boolean = prefs.getBoolean("pin_lock_enabled", false)

    fun setPinLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pin_lock_enabled", enabled).apply()
        _isPinLockEnabledFlow.value = enabled
    }

    fun getPinHash(): String = prefs.getString("pin_hash", "") ?: ""

    fun setPinHash(hash: String) {
        prefs.edit().putString("pin_hash", hash).apply()
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean("biometric_enabled", false)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    fun isBudgetAlert80Enabled(): Boolean = prefs.getBoolean("alert_80_enabled", true)
    fun setBudgetAlert80Enabled(enabled: Boolean) = prefs.edit().putBoolean("alert_80_enabled", enabled).apply()

    fun isBudgetAlert100Enabled(): Boolean = prefs.getBoolean("alert_100_enabled", true)
    fun setBudgetAlert100Enabled(enabled: Boolean) = prefs.edit().putBoolean("alert_100_enabled", enabled).apply()

    fun getLastBackupTimestamp(): Long = prefs.getLong("last_backup_time", 0L)
    fun setLastBackupTimestamp(time: Long) = prefs.edit().putLong("last_backup_time", time).apply()

    fun clearAllData() {
        prefs.edit().clear().apply()
        _currencyFlow.value = Currencies.getByCode("PKR")
        _hideBalanceFlow.value = false
        _darkModeFlow.value = true
        _isOnboardingCompletedFlow.value = false
        _isPinLockEnabledFlow.value = false
    }
}
