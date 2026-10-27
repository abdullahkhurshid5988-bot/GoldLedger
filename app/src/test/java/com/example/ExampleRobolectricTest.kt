package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.GoldLedgerDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinancialRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: GoldLedgerDatabase
    private lateinit var repository: FinancialRepository
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, GoldLedgerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FinancialRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun readStringFromContext() {
        val appName = context.getString(R.string.app_name)
        assertEquals("GoldLedger", appName)
    }

    @Test
    fun testCreateExpenseAndBalanceDeduction() = runBlocking {
        // 1. Create Account with 10,000.00 minor units (1000000L)
        val account = AccountEntity(
            name = "Cash Wallet",
            type = AccountType.CASH_WALLET,
            balanceMinorUnits = 1000000L,
            currencyCode = "PKR"
        )
        val accId = repository.insertAccount(account)

        // 2. Add Expense of 250.00 (25000L)
        val expense = TransactionEntity(
            type = TransactionType.EXPENSE,
            amountMinorUnits = 25000L,
            accountId = accId,
            description = "Groceries"
        )
        val txId = repository.addTransaction(expense)
        assertNotNull(txId)

        // 3. Verify Account balance is now 975,000L (9,750.00)
        val updatedAcc = repository.getAccountById(accId)
        assertNotNull(updatedAcc)
        assertEquals(975000L, updatedAcc?.balanceMinorUnits)
    }

    @Test
    fun testCreateIncomeAndBalanceAddition() = runBlocking {
        val account = AccountEntity(
            name = "Bank Account",
            type = AccountType.BANK_ACCOUNT,
            balanceMinorUnits = 500000L,
            currencyCode = "PKR"
        )
        val accId = repository.insertAccount(account)

        val income = TransactionEntity(
            type = TransactionType.INCOME,
            amountMinorUnits = 100000L, // 1,000.00
            accountId = accId,
            description = "Freelancing"
        )
        repository.addTransaction(income)

        val updatedAcc = repository.getAccountById(accId)
        assertEquals(600000L, updatedAcc?.balanceMinorUnits)
    }

    @Test
    fun testAccountTransfer() = runBlocking {
        val fromAcc = AccountEntity(name = "Bank", type = AccountType.BANK_ACCOUNT, balanceMinorUnits = 500000L)
        val toAcc = AccountEntity(name = "Cash", type = AccountType.CASH_WALLET, balanceMinorUnits = 100000L)

        val fromId = repository.insertAccount(fromAcc)
        val toId = repository.insertAccount(toAcc)

        val transfer = TransactionEntity(
            type = TransactionType.TRANSFER,
            amountMinorUnits = 200000L,
            accountId = fromId,
            toAccountId = toId,
            description = "ATM Withdrawal"
        )
        repository.addTransaction(transfer)

        val updatedFrom = repository.getAccountById(fromId)
        val updatedTo = repository.getAccountById(toId)

        assertEquals(300000L, updatedFrom?.balanceMinorUnits)
        assertEquals(300000L, updatedTo?.balanceMinorUnits)
    }

    @Test
    fun testDeleteTransactionBalanceRollback() = runBlocking {
        val acc = AccountEntity(name = "Wallet", type = AccountType.CASH_WALLET, balanceMinorUnits = 100000L)
        val accId = repository.insertAccount(acc)

        val expense = TransactionEntity(
            type = TransactionType.EXPENSE,
            amountMinorUnits = 30000L,
            accountId = accId,
            description = "Coffee"
        )
        val txId = repository.addTransaction(expense)
        val txEntity = expense.copy(id = txId)

        // Balance should be 70000L
        assertEquals(70000L, repository.getAccountById(accId)?.balanceMinorUnits)

        // Delete transaction -> balance should rollback to 100000L
        repository.deleteTransaction(txEntity)
        assertEquals(100000L, repository.getAccountById(accId)?.balanceMinorUnits)
    }
}
