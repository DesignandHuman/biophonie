package fr.labomg.biophonie.data.source.remote

import fr.labomg.biophonie.util.AppPrefs
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

@Singleton
class AuthenticationInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().addSignInTokenForRestrictedUrl()
        return chain.proceed(request)
    }

    private fun Request.addSignInTokenForRestrictedUrl(): Request {
        val isUrlRestricted = getIsUrlRestricted(url)
        if (isUrlRestricted) {
            AppPrefs.token?.let { token ->
                return this.addAuthorizationHeader(token)
            }
        }

        return this
    }

    private fun getIsUrlRestricted(url: HttpUrl) = url.toString().contains("/restricted/")
}
