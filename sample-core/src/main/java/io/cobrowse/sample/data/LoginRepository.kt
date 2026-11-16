package io.cobrowse.sample.data

import io.cobrowse.sample.data.model.LoggedInUser

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        user = null
    }

    fun logout(): Boolean {
        user = null
        dataSource.logout()
        return true
    }

    fun login(username: String, password: String): Result<LoggedInUser> {
        // handle login
        val result = dataSource.login(username, password)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }

        return result
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
    }

    companion object {
        @Volatile
        private var INSTANCE: LoginRepository? = null

        fun getInstance(): LoginRepository {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = LoginRepository(dataSource = LoginDataSource())
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}