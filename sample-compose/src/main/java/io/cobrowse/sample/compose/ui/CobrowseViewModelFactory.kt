package io.cobrowse.sample.compose.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.cobrowse.sample.data.LoginDataSource
import io.cobrowse.sample.data.LoginRepository
import io.cobrowse.sample.data.TransactionsDataSource
import io.cobrowse.sample.data.TransactionsRepository
import io.cobrowse.sample.compose.ui.account.AccountViewModel
import io.cobrowse.sample.compose.ui.login.LoginViewModel
import io.cobrowse.sample.compose.ui.transactions.TransactionsChartViewModel
import io.cobrowse.sample.compose.ui.transactions.TransactionsViewModel

/**
 * ViewModel provider factory to instantiate LoginViewModel and other ViewModels.
 * Required given LoginViewModel has a non-empty constructor
 */
class CobrowseViewModelFactory : ViewModelProvider.Factory {

    private val loginRepository = LoginRepository.getInstance()
    private val transactionsRepository =
        TransactionsRepository(dataSource = TransactionsDataSource())

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(loginRepository) as T
        }
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            return AccountViewModel(loginRepository) as T
        }
        if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
            return TransactionsViewModel(transactionsRepository) as T
        }
        if (modelClass.isAssignableFrom(TransactionsChartViewModel::class.java)) {
            return TransactionsChartViewModel(transactionsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}