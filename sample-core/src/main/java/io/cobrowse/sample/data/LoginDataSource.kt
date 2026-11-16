package io.cobrowse.sample.data

import io.cobrowse.sample.data.model.LoggedInUser
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(username: String, password: String): Result<LoggedInUser> {
        try {
            val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(),
                                        "Frank Spenser",
                                        "f.spencer@example.com")
            return Result.Success(fakeUser)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // Revoke authentication here
        // Empty as there is no real authentication
    }
}