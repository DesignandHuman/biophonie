package fr.labomg.biophonie.core.network

import fr.labomg.biophonie.core.utils.TokenProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber

@Singleton
class AccessTokenAuthenticator @Inject constructor(private val tokenProvider: TokenProvider) :
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
            tokenProvider
                .refreshToken()
                .onSuccess { signedRequest = request.addAuthorizationHeader(it) }
                .onFailure { Timber.wtf(it.message) }
        }
        return signedRequest
    }
}

@Singleton
class AuthenticationInterceptor @Inject constructor(private val tokenProvider: TokenProvider) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().addSignInTokenForRestrictedUrl()
        return chain.proceed(request)
    }

    private fun Request.addSignInTokenForRestrictedUrl(): Request {
        val isUrlRestricted = isUrlRestricted(url)
        if (!isUrlRestricted) return this
        var newRequest = this
        runBlocking {
            tokenProvider
                .getToken()
                .onSuccess { token ->
                    token?.let { // if token is null, authenticator will fetch another
                        newRequest = newRequest.addAuthorizationHeader(it)
                    }
                }
                .onFailure { Timber.wtf(it.message) }
        }

        return newRequest
    }

    private fun isUrlRestricted(url: HttpUrl) = url.toString().contains("/restricted/")
}
