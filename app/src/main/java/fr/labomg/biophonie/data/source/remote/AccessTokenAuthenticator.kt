package fr.labomg.biophonie.data.source.remote

import fr.labomg.biophonie.util.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AccessTokenAuthenticator @Inject constructor(
    private val webService: Provider<WebService>,
) : Authenticator {
    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        return if (response.retryCount > 2) null else response.createSignedRequest()
    }

    private val Response.retryCount: Int
        get() {
            var currentResponse = priorResponse
            var result = 0
            while (currentResponse != null) {
                result++
                currentResponse = currentResponse.priorResponse
            }
            return result
        }

    private fun Response.createSignedRequest(): Request? {
        var signedRequest: Request? = null
        runBlocking {
            fetchAccessToken()
                .onSuccess { signedRequest = request.addAuthorizationHeader(it.token) }
                .onFailure { signedRequest = null }
        }
        return signedRequest
    }

    private suspend fun fetchAccessToken(): Result<AccessToken> {
        val user = buildAuthUser()
        return if (user != null) {
            withContext(Dispatchers.IO) {
                webService.get().refreshToken(user).onSuccess {
                    AppPrefs.token = it.token
                }
            }
        } else {
            Result.failure(RuntimeException("user not in shared pref"))
        }
    }

    private fun buildAuthUser(): NetworkAuthUser? {
        val username = AppPrefs.userName
        val password = AppPrefs.password
        return if (username != null && password != null) {
            NetworkAuthUser(username, password)
        } else {
            null
        }
    }
}
