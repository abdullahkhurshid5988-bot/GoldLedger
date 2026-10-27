package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithDetails(
    @Embedded
    val transaction: TransactionEntity,

    @Relation(
        parentColumn = "accountId",
        entityColumn = "id"
    )
    val account: AccountEntity?,

    @Relation(
        parentColumn = "toAccountId",
        entityColumn = "id"
    )
    val toAccount: AccountEntity?,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)

data class BudgetWithDetails(
    @Embedded
    val budget: BudgetEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)

data class SavingsGoalWithContributions(
    @Embedded
    val goal: SavingsGoalEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "goalId"
    )
    val contributions: List<SavingsContributionEntity> = emptyList()
)

data class RecurringWithDetails(
    @Embedded
    val recurring: RecurringTransactionEntity,

    @Relation(
        parentColumn = "accountId",
        entityColumn = "id"
    )
    val account: AccountEntity?,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)
