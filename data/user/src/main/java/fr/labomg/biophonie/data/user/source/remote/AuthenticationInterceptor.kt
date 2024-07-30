package fr.labomg.biophonie.data.user.source.remote

import fr.labomg.biophonie.core.network.addAuthorizationHeader
import fr.labomg.biophonie.data.user.source.UserRepository
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationInterceptor @Inject constructor(private val userRepository: UserRepository) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().addSignInTokenForRestrictedUrl()
        return chain.proceed(request)
    }

    private fun Request.addSignInTokenForRestrictedUrl(): Request {
        val isUrlRestricted = getIsUrlRestricted(url)
        if (!isUrlRestricted) return this
        var newRequest = this
        runBlocking {
            userRepository
                .getUser()
                .onSuccess { user ->
                    user.token?.let { // if token is null, authenticator will fetch another
                        newRequest = newRequest.addAuthorizationHeader(it)
                    }
                }
                .onFailure { Timber.wtf(it.message) }
        }

        return newRequest
    }

    private fun getIsUrlRestricted(url: HttpUrl) = url.toString().contains("/restricted/")
}
