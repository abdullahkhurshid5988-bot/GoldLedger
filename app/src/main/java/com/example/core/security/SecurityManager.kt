package com.example.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.MessageDigest

object SecurityManager {

    fun hashPin(pin: String): String {
        val salt = "GoldLedger_Local_Salt_Secured"
        val bytes = MessageDigest.getInstance("SHA-256").digest((salt + pin).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(enteredPin: String, storedHash: String): Boolean {
        if (storedHash.isBlank()) return false
        return hashPin(enteredPin) == storedHash
    }

    fun canAuthenticateBiometric(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Unlock GoldLedger",
        subtitle: String = "Confirm biometric credentials to proceed",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Biometric authentication failed. Try again.")
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Use PIN")
            .build()

        prompt.authenticate(promptInfo)
    }
}
