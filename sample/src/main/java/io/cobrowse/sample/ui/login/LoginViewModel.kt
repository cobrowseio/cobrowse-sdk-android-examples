package io.cobrowse.sample.ui.login

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.cobrowse.sample.core.R
import io.cobrowse.sample.data.CobrowseDeepLinks
import io.cobrowse.sample.data.LoginRepository
import io.cobrowse.sample.data.Result
import io.cobrowse.sample.ui.BaseViewModel

/**
 * View-Model for the login form.
 */
class LoginViewModel(private val loginRepository: LoginRepository) : BaseViewModel() {

    //<editor-fold desc="Authorization">

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    val isLoggedIn: Boolean
        get() = loginRepository.isLoggedIn

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(username, password)

        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank()
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.isNotEmpty()
    }

    //</editor-fold>

    //<editor-fold desc="Deep links">

    fun parseDeepLink(uri: Uri): Boolean {
        val handled = CobrowseDeepLinks.handle(uri)
        if (handled && CobrowseDeepLinks.isDemo(uri) && !isLoggedIn) {
            login("demo", "demo")
        }
        return handled
    }

    //</editor-fold>
}
