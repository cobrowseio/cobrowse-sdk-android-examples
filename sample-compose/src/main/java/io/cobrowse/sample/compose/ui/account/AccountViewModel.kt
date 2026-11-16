package io.cobrowse.sample.compose.ui.account

import androidx.lifecycle.ViewModel
import io.cobrowse.CobrowseIO
import io.cobrowse.sample.data.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccountViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    val user = loginRepository.user

    private val _sessionCode = MutableStateFlow("")
    val sessionCode: StateFlow<String> = _sessionCode.asStateFlow()

    fun logout() {
        loginRepository.logout()
    }

    fun requestSessionCode() {
        CobrowseIO.instance().createSession { _, session ->
            val code = session?.code()
            // Codes have 6 digits, displayed in groups of 3
            _sessionCode.value = code.orEmpty().chunked(3).joinToString(" - ")
        }
    }

    fun clearSessionCode() {
        _sessionCode.value = ""
    }
}
