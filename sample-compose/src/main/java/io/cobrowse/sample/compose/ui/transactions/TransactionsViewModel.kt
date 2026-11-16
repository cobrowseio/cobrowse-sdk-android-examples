package io.cobrowse.sample.compose.ui.transactions

import androidx.lifecycle.ViewModel
import io.cobrowse.sample.data.TransactionsRepository
import io.cobrowse.sample.data.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TransactionsViewModel(repository: TransactionsRepository) : ViewModel() {

    private val _transactions = MutableStateFlow(repository.allTransactions())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()
}
