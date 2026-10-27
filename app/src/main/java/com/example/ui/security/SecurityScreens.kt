package com.example.ui.security

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.core.security.SecurityManager
import com.example.ui.MainViewModel
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
fun PinLockScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val canUseBiometric = remember { SecurityManager.canAuthenticateBiometric(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlackPrimary)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(GoldContainer)
                .border(1.dp, GoldPrimary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = GoldLight,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "GoldLedger Locked",
            color = TextWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Enter your 4-digit security PIN",
            color = TextMuted,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 4 PIN Dots
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (i in 0 until 4) {
                val isFilled = i < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isFilled) GoldPrimary else Color(0xFF2B2B2B))
                        .border(1.dp, if (isFilled) GoldLight else BlackBorder, CircleShape)
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = errorMessage!!, color = ExpenseRed, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Keypad Grid 3x4
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("BIO", "0", "DEL")
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (row in keys) {
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    for (key in row) {
                        PinKeyButton(
                            text = key,
                            onClick = {
                                when (key) {
                                    "DEL" -> {
                                        if (enteredPin.isNotEmpty()) {
                                            enteredPin = enteredPin.dropLast(1)
                                            errorMessage = null
                                        }
                                    }
                                    "BIO" -> {
                                        if (canUseBiometric && context is FragmentActivity) {
                                            SecurityManager.showBiometricPrompt(
                                                activity = context,
                                                onSuccess = { viewModel.unlockAppWithBiometrics() },
                                                onError = { err -> errorMessage = err }
                                            )
                                        }
                                    }
                                    else -> {
                                        if (enteredPin.length < 4) {
                                            enteredPin += key
                                            if (enteredPin.length == 4) {
                                                val success = viewModel.unlockApp(enteredPin)
                                                if (!success) {
                                                    errorMessage = "Incorrect PIN. Try again."
                                                    enteredPin = ""
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PinKeyButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color(0xFF1C1C1C),
        shape = CircleShape,
        modifier = Modifier
            .size(68.dp)
            .border(1.dp, Color(0xFF2C2C2C), CircleShape)
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (text) {
                "DEL" -> Icon(imageVector = Icons.Default.Backspace, contentDescription = "Delete", tint = TextWhite, modifier = Modifier.size(20.dp))
                "BIO" -> Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Biometric", tint = GoldLight, modifier = Modifier.size(24.dp))
                else -> Text(text = text, color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SecuritySettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isPinEnabled by viewModel.isPinLockEnabledFlow.collectAsState()
    var showSetPinDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlackPrimary)
            .padding(16.dp)
    ) {
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
                text = "Security & App Lock",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            color = BlackCard,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BlackBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("App PIN Lock", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Protect your financial data with a 4-digit PIN lock when opening the app.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Switch(
                        checked = isPinEnabled,
                        onCheckedChange = { enable ->
                            if (enable) {
                                showSetPinDialog = true
                            } else {
                                viewModel.disablePin()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF141208),
                            checkedTrackColor = GoldPrimary
                        )
                    )
                }

                if (isPinEnabled) {
                    Spacer(modifier = Modifier.height(14.dp))
                    GoldButton(
                        text = "Change PIN",
                        onClick = { showSetPinDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showSetPinDialog) {
        SetPinDialog(
            onDismiss = { showSetPinDialog = false },
            onConfirm = { pin ->
                showSetPinDialog = false
                viewModel.setPin(pin)
            }
        )
    }
}

@Composable
fun SetPinDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Security PIN", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { str -> if (str.length <= 4 && str.all { it.isDigit() }) pin = str },
                    label = { Text("4-digit PIN") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { str -> if (str.length <= 4 && str.all { it.isDigit() }) confirmPin = str },
                    label = { Text("Confirm PIN") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(text = error!!, color = ExpenseRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            GoldButton(
                text = "Save PIN",
                onClick = {
                    if (pin.length != 4) {
                        error = "PIN must be exactly 4 digits"
                    } else if (pin != confirmPin) {
                        error = "PINs do not match"
                    } else {
                        onConfirm(pin)
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
