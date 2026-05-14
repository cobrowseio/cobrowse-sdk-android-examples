package io.cobrowse.sample.ui.login

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.cobrowse.CobrowseIO
import io.cobrowse.sample.R
import io.cobrowse.sample.data.LoginRepository
import io.cobrowse.sample.data.Result
import io.cobrowse.sample.ui.BaseViewModel
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.net.URLDecoder

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
        return when (uri.pathSegments.firstOrNull()) {
            "api" -> updateApi(uri)
            "license" -> updateLicense(uri)
            "data" -> updateCustomData(uri)
            "s" -> startSession(uri)
            "code" -> startSession(uri)
            "demo" -> setupForDemo(uri)
            else -> false
        }
    }

    /**
     * Accepts URLs like `https://cbrws.io/api/https%3A%2F%2Fcobrowse.io`
     */
    private fun updateApi(uri: Uri): Boolean {
        uri.pathSegments.drop(1).firstOrNull()?.let {
            with(CobrowseIO.instance()) {
                stop()
                api(decode(it))
                start()
                return true
            }
        }
        return false
    }

    /**
     * Accepts URLs like `https://cbrws.io/license/trial`
     */
    private fun updateLicense(uri: Uri): Boolean {
        uri.pathSegments.drop(1).firstOrNull()?.let {
            with(CobrowseIO.instance()) {
                stop()
                license(decode(it))
                start()
                return true
            }
        }
        return false
    }

    /**
     * Accepts URLs like `https://cbrws.io/data?firstKey=firstValue&secondKey=secondValue`
     */
    private fun updateCustomData(uri: Uri): Boolean {
        uri.toString().toHttpUrlOrNull()?.let { url ->
            val data = url.queryParameterNames.associateWith { url.queryParameter(decode(it)) }
            with(CobrowseIO.instance()) {
                customData(data)
                return true
            }
        }
        return false
    }

    /**
     * Accepts URLs like `https://cbrws.io/s?id=abc` or `https://cbrws.io/code/000000`
     */
    private fun startSession(uri: Uri): Boolean {
        when (uri.pathSegments.firstOrNull()) {
            "s" -> {
                uri.toString().toHttpUrlOrNull()?.let { url ->
                    url.queryParameter("id")?.let { sessionId ->
                        return startSession(decode(sessionId))
                    }
                }
            }
            "code" -> {
                uri.pathSegments.drop(1).firstOrNull()?.let { code ->
                    return startSession(code)
                }
            }
            else -> {}
        }
        return false
    }

    /**
     * Accepts URLs like `https://cbrws.io/demo/someDemoId?api=https%3A%2F%2Fcobrowse.io&license=trial`
     */
    private fun setupForDemo(uri: Uri): Boolean {
        with(CobrowseIO.instance()) {
            stop()
            uri.toString().toHttpUrlOrNull()?.let { url ->
                url.queryParameter("api")?.let { api(decode(it)) }
                url.queryParameter("license")?.let { license(decode(it)) }
                url.queryParameterNames
                    .filter { it != "api" && it != "license" }
                    .associateWith { url.queryParameter(decode(it)) }
                    .toMutableMap()
                    .let {
                        uri.pathSegments.drop(1).firstOrNull()?.let { demoId ->
                            it["demo_id"] = decode(demoId)
                        }
                        customData(it.toMap())
                    }
            }
            start()
        }
        if (!isLoggedIn) {
            login("demo", "demo")
        }
        return true
    }

    private fun decode(it: String?): String = URLDecoder.decode(it, "UTF-8")

    private fun startSession(idOrCode: String): Boolean {
        with(CobrowseIO.instance()) {
            stop()
            start()
            getSession(idOrCode, null)
        }
        return true
    }

    //</editor-fold>
}
