package io.cobrowse.sample.compose.ui.account

import androidx.lifecycle.ViewModel
import io.cobrowse.sample.data.LoginRepository

class AccountViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    val user = loginRepository.user

    fun logout() {
        loginRepository.logout()
    }

    fun getSessionCode(): String {
        return (100000..999999).random().toString()
    }
}

