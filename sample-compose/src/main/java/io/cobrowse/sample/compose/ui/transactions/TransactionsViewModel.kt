package io.cobrowse.sample.compose.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.cobrowse.sample.data.TransactionsRepository
import io.cobrowse.sample.data.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransactionsUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class TransactionsViewModel(private val repository: TransactionsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

             _uiState.value = _uiState.value.copy(
                transactions = repository.allTransactions(),
                isLoading = false,
                errorMessage = null
            )
        }
    }
}

