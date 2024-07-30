package fr.labomg.biophonie.data.user.source.remote

import fr.labomg.biophonie.core.network.addAuthorizationHeader
import fr.labomg.biophonie.data.user.source.UserRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber

@Singleton
class AccessTokenAuthenticator @Inject constructor(private val userRepository: UserRepository) :
    Authenticator {
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
            userRepository
                .refreshAccessToken()
                .onSuccess { signedRequest = request.addAuthorizationHeader(it) }
                .onFailure { Timber.wtf(it.message) }
        }
        return signedRequest
    }
}
