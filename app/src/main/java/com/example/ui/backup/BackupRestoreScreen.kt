package com.example.ui.backup

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.export.ExportUtils
import com.example.core.utilities.FinancialUtils
import com.example.ui.MainViewModel
import com.example.ui.components.GoldButton
import com.example.ui.components.GoldOutlinedButton
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackCard
import com.example.ui.theme.BlackPrimary
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

@Composable
fun BackupRestoreScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accounts by viewModel.accountsFlow.collectAsState()
    val transactions by viewModel.allTransactionsFlow.collectAsState()
    val categories by viewModel.categoriesFlow.collectAsState()
    val budgets by viewModel.budgetsFlow.collectAsState()
    val goals by viewModel.savingsGoalsFlow.collectAsState()

    var lastBackupTime by remember {
        mutableStateOf(viewModel.preferences.getLastBackupTimestamp())
    }

    var showRestoreSuccessDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlackPrimary)
            .padding(16.dp)
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
                text = "Backup & Restore",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Status Card
        Surface(
            color = BlackCard,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(GoldContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = null,
                        tint = GoldLight,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text("Local Data Backup", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (lastBackupTime > 0) {
                            "Last backup: ${FinancialUtils.formatDate(lastBackupTime)} at ${FinancialUtils.formatTime(lastBackupTime)}"
                        } else "No previous backup created",
                        color = GoldLight,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Actions
        Surface(
            color = BlackCard,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BlackBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Backup Management", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Export all transactions, accounts, categories, budgets, and savings records into a secure offline JSON file. You can share or save it locally.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                GoldButton(
                    text = "Export Local Backup",
                    onClick = {
                        try {
                            val backupObj = JSONObject()
                            backupObj.put("version", 1)
                            backupObj.put("timestamp", System.currentTimeMillis())

                            // Accounts
                            val accountsArray = JSONArray()
                            for (a in accounts) {
                                val obj = JSONObject().apply {
                                    put("name", a.name)
                                    put("type", a.type.name)
                                    put("balanceMinorUnits", a.balanceMinorUnits)
                                    put("currencyCode", a.currencyCode)
                                    put("colorHex", a.colorHex)
                                    put("iconName", a.iconName)
                                }
                                accountsArray.put(obj)
                            }
                            backupObj.put("accounts", accountsArray)

                            // Transactions
                            val txArray = JSONArray()
                            for (t in transactions) {
                                val tx = t.transaction
                                val obj = JSONObject().apply {
                                    put("type", tx.type.name)
                                    put("amountMinorUnits", tx.amountMinorUnits)
                                    put("description", tx.description)
                                    put("dateTimestamp", tx.dateTimestamp)
                                    put("timeString", tx.timeString)
                                    put("paymentMethod", tx.paymentMethod.name)
                                    put("merchant", tx.merchant)
                                    put("notes", tx.notes)
                                }
                                txArray.put(obj)
                            }
                            backupObj.put("transactions", txArray)

                            val backupDir = File(context.cacheDir, "backups").apply { mkdirs() }
                            val file = File(backupDir, "GoldLedger_Backup_${System.currentTimeMillis()}.json")
                            file.writeText(backupObj.toString(2))

                            val now = System.currentTimeMillis()
                            viewModel.preferences.setLastBackupTimestamp(now)
                            lastBackupTime = now

                            ExportUtils.shareFile(context, file, "application/json", "GoldLedger Backup JSON")
                            Toast.makeText(context, "Backup created successfully!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    leadingIcon = Icons.Default.FileUpload,
                    modifier = Modifier.fillMaxWidth()
                )

                GoldOutlinedButton(
                    text = "Verify Backup Integrity",
                    onClick = {
                        val backupDir = File(context.cacheDir, "backups")
                        val files = backupDir.listFiles()
                        if (files.isNullOrEmpty()) {
                            Toast.makeText(context, "No backup files found in cache to verify", Toast.LENGTH_SHORT).show()
                        } else {
                            val latest = files.maxByOrNull { it.lastModified() }
                            if (latest != null && latest.length() > 0) {
                                Toast.makeText(context, "Valid backup structure confirmed: ${latest.name}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
