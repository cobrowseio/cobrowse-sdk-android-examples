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
            _sessionCode.value = if (code.isNullOrEmpty()) {
                ""
            } else {
                // We always assume the code has only 6 digits
                "${code.substring(0, 3)} - ${code.substring(3, 6)}"
            }
        }
    }

    fun clearSessionCode() {
        _sessionCode.value = ""
    }
}
