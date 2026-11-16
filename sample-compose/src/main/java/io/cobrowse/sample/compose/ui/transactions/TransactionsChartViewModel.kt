package io.cobrowse.sample.compose.ui.transactions

import androidx.lifecycle.ViewModel
import io.cobrowse.sample.data.TransactionsRepository
import io.cobrowse.sample.data.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TransactionsChartUiState(
    val recentTransactions: List<Transaction> = emptyList(),
    val balance: Double = 0.0
)

class TransactionsChartViewModel(repository: TransactionsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TransactionsChartUiState(
            recentTransactions = repository.recentTransactions(),
            balance = (1000..3000).random() + (0..99).random() / 100.0
        )
    )
    val uiState: StateFlow<TransactionsChartUiState> = _uiState.asStateFlow()
}
