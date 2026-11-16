package io.cobrowse.sample.data

import io.cobrowse.sample.data.model.Transaction
import java.time.LocalDate

/**
 * Class that loads (actually randomly generates) user financial information from
 * the provided data source.
 */
class TransactionsRepository(private val dataSource: TransactionsDataSource) {
    fun recentTransactions(): List<Transaction> {
        val currentDate = LocalDate.now()
        val startOfMonth = currentDate.withDayOfMonth(1)

        return arrayOf(
            dataSource.generate(1, arrayOf(Transaction.Category.Childcare), startOfMonth.rangeTo(currentDate)),
            dataSource.generate(2, arrayOf(Transaction.Category.Groceries), startOfMonth.rangeTo(currentDate)),
            dataSource.generate(1, arrayOf(Transaction.Category.Utilities), startOfMonth.rangeTo(currentDate)))
            .filter {it is Result.Success }
            .flatMap { (it as Result.Success).data }
            .sortedBy { it.date }
    }

    fun allTransactions(): List<Transaction> {
        val currentDate = LocalDate.now()
        val minDate = currentDate.minusMonths(3).withDayOfMonth(1)

        return dataSource.generate(30, minDate.rangeTo(currentDate)).let {
            if (it is Result.Success) (it as Result.Success).data else listOf()
        }
    }
}