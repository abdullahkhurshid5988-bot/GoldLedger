package com.example.core.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.core.utilities.FinancialUtils
import com.example.data.model.SupportedCurrency
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithDetails
import java.io.File
import java.io.FileOutputStream

object ExportUtils {

    /**
     * Generates a CSV file from transaction records
     */
    fun exportToCsv(
        context: Context,
        transactions: List<TransactionWithDetails>,
        currency: SupportedCurrency
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "GoldLedger_Transactions_${System.currentTimeMillis()}.csv")

        file.printWriter().use { out ->
            out.println("ID,Date,Time,Type,Amount,Currency,Account,To Account,Category,Payment Method,Description,Notes")
            for (item in transactions) {
                val tx = item.transaction
                val dateStr = FinancialUtils.formatDate(tx.dateTimestamp)
                val timeStr = tx.timeString.ifBlank { FinancialUtils.formatTime(tx.dateTimestamp) }
                val amountStr = FinancialUtils.formatCurrency(tx.amountMinorUnits, currency, includeSymbol = false)
                val accountStr = item.account?.name ?: "Unknown"
                val toAccountStr = item.toAccount?.name ?: ""
                val categoryStr = item.category?.name ?: "Uncategorized"
                val methodStr = tx.paymentMethod.displayName
                val desc = sanitizeCsv(tx.description)
                val notes = sanitizeCsv(tx.notes)

                out.println("${tx.id},\"$dateStr\",\"$timeStr\",\"${tx.type}\",\"$amountStr\",\"${currency.code}\",\"$accountStr\",\"$toAccountStr\",\"$categoryStr\",\"$methodStr\",\"$desc\",\"$notes\"")
            }
        }
        return file
    }

    /**
     * Generates a PDF Report with header, summary cards, and transaction records table.
     */
    fun exportToPdf(
        context: Context,
        title: String,
        dateRangeStr: String,
        transactions: List<TransactionWithDetails>,
        currency: SupportedCurrency
    ): File {
        val reportDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(reportDir, "GoldLedger_Report_${System.currentTimeMillis()}.pdf")

        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 portrait
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Header Background: Luxury dark header
        paint.color = Color.rgb(18, 18, 18)
        canvas.drawRect(0f, 0f, 595f, 110f, paint)

        // Accent gold line
        paint.color = Color.rgb(212, 175, 55)
        paint.strokeWidth = 3f
        canvas.drawLine(0f, 110f, 595f, 110f, paint)

        // GoldLedger Title
        paint.color = Color.rgb(212, 175, 55)
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("GOLDLEDGER", 36f, 48f, paint)

        // Subtitle & Date
        paint.color = Color.WHITE
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(title, 36f, 72f, paint)

        paint.color = Color.rgb(170, 170, 170)
        paint.textSize = 10f
        canvas.drawText("Period: $dateRangeStr  |  Currency: ${currency.code}", 36f, 92f, paint)

        // Summary Calculations
        val totalIncome = transactions.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amountMinorUnits }
        val totalExpense = transactions.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amountMinorUnits }
        val netSavings = totalIncome - totalExpense

        var currentY = 140f

        // Draw Summary Box
        paint.color = Color.rgb(245, 245, 245)
        canvas.drawRoundRect(36f, currentY, 559f, currentY + 60f, 8f, 8f, paint)

        // Income, Expense, Net values
        paint.color = Color.rgb(40, 182, 122)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Income: ${FinancialUtils.formatCurrency(totalIncome, currency)}", 50f, currentY + 36f, paint)

        paint.color = Color.rgb(230, 90, 90)
        canvas.drawText("Expenses: ${FinancialUtils.formatCurrency(totalExpense, currency)}", 220f, currentY + 36f, paint)

        paint.color = if (netSavings >= 0) Color.rgb(212, 175, 55) else Color.rgb(230, 90, 90)
        canvas.drawText("Net: ${FinancialUtils.formatCurrency(netSavings, currency)}", 410f, currentY + 36f, paint)

        currentY += 80f

        // Table Header
        paint.color = Color.rgb(230, 230, 230)
        canvas.drawRect(36f, currentY, 559f, currentY + 24f, paint)

        paint.color = Color.BLACK
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Date", 42f, currentY + 16f, paint)
        canvas.drawText("Description", 110f, currentY + 16f, paint)
        canvas.drawText("Category", 260f, currentY + 16f, paint)
        canvas.drawText("Account", 380f, currentY + 16f, paint)
        canvas.drawText("Amount", 480f, currentY + 16f, paint)

        currentY += 32f

        // Table Rows (up to 25 items for one page)
        val rowLimit = 25
        paint.typeface = Typeface.DEFAULT
        for ((idx, item) in transactions.take(rowLimit).withIndex()) {
            val tx = item.transaction
            val isEven = idx % 2 == 0
            if (isEven) {
                paint.color = Color.rgb(250, 250, 250)
                canvas.drawRect(36f, currentY - 12f, 559f, currentY + 8f, paint)
            }

            paint.color = Color.DKGRAY
            paint.textSize = 9f
            canvas.drawText(FinancialUtils.formatDate(tx.dateTimestamp, "dd/MM/yy"), 42f, currentY, paint)

            val descText = if (tx.description.length > 22) tx.description.take(20) + ".." else tx.description.ifBlank { tx.type.name }
            canvas.drawText(descText, 110f, currentY, paint)

            val catText = item.category?.name ?: if (tx.type == TransactionType.TRANSFER) "Transfer" else "Other"
            canvas.drawText(catText.take(18), 260f, currentY, paint)

            val accText = item.account?.name ?: "-"
            canvas.drawText(accText.take(16), 380f, currentY, paint)

            paint.color = when (tx.type) {
                TransactionType.INCOME -> Color.rgb(40, 182, 122)
                TransactionType.EXPENSE -> Color.rgb(215, 60, 60)
                TransactionType.TRANSFER -> Color.rgb(77, 150, 255)
            }
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val amtText = FinancialUtils.formatCurrency(tx.amountMinorUnits, currency, includeSign = true)
            canvas.drawText(amtText, 480f, currentY, paint)
            paint.typeface = Typeface.DEFAULT

            currentY += 20f
        }

        // Footer
        paint.color = Color.rgb(150, 150, 150)
        paint.textSize = 8f
        canvas.drawText("Generated by GoldLedger Smart Expense & Budget Manager • Confidential", 36f, 810f, paint)

        pdfDoc.finishPage(page)

        FileOutputStream(file).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()
        return file
    }

    /**
     * Shares an exported file via the Android Share Sheet using FileProvider
     */
    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share $title").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun sanitizeCsv(text: String): String {
        return text.replace("\"", "\"\"")
    }
}
