package io.cobrowse.sample.data

import io.cobrowse.sample.data.model.Transaction
import java.io.IOException
import java.time.LocalDateTime

/**
 * Class that loads (actually randomly generates) user financial transactions.
 */
class TransactionsDataSource {
    fun generate(count: Int,
                 between: LocalDateRange): Result<List<Transaction>> {
        return generate(count, Transaction.Category.values(), between)
    }

    fun generate(count: Int,
                 categories: Array<Transaction.Category>,
                 between: LocalDateRange): Result<List<Transaction>> {
        try {
            val allPossibleDates = between.iterator().asSequence().shuffled().toList()

            val transactions = (0 ..< count).map { i ->
                val category = categories.random()
                val randomDate = allPossibleDates.random()
                val date = LocalDateTime.of(randomDate.year,
                                            randomDate.month,
                                            randomDate.dayOfMonth,
                                            (0..23).random(),
                                            (0..59).random())
                val title = category.randomName()
                val amount = category.amountRange.random() + (0..99).random() / 100.0

                Transaction(title = title,
                            amount = amount,
                            date = date,
                            category = category)
            }
            return Result.Success(transactions.sortedBy { it.date }.toList())
        } catch (e: Throwable) {
            return Result.Error(IOException("Error while loading transaction data in", e))
        }
    }
}