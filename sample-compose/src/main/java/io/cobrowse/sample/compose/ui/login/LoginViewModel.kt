package io.cobrowse.sample.compose.ui.login

import androidx.lifecycle.ViewModel
import io.cobrowse.sample.data.LoginRepository
import io.cobrowse.sample.data.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoggedIn: Boolean = false,
    val errorMessage: Int? = null,
    val usernameError: Int? = null,
    val passwordError: Int? = null
)

class LoginViewModel(
    private val loginRepository: LoginRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            usernameError = if (username.isBlank()) io.cobrowse.sample.core.R.string.invalid_username else null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = if (password.isEmpty()) io.cobrowse.sample.core.R.string.invalid_password else null
        )
    }

    fun login() {
        val username = _uiState.value.username
        val password = _uiState.value.password

        if (username.isBlank() || password.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                usernameError = if (username.isBlank()) io.cobrowse.sample.core.R.string.invalid_username else null,
                passwordError = if (password.isEmpty()) io.cobrowse.sample.core.R.string.invalid_password else null
            )
            return
        }

        _uiState.value = when (loginRepository.login(username, password)) {
            is Result.Success -> _uiState.value.copy(isLoggedIn = true, errorMessage = null)
            is Result.Error -> _uiState.value.copy(errorMessage = io.cobrowse.sample.core.R.string.login_failed)
        }
    }
}
