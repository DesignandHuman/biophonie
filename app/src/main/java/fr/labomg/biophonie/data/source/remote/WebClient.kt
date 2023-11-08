package fr.labomg.biophonie.data.source.remote

import fr.labomg.biophonie.BASE_URL
import fr.labomg.biophonie.data.source.ResultCallAdapterFactory
import fr.labomg.biophonie.util.AppPrefs
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class WebClient {

    inner class AuthenticationInterceptor: Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            return if (chain.request().url.toString().contains("/restricted/")) {
                val token = AppPrefs.token
                if (token != null) {
                    val newRequest = chain.request().signWithToken(token)
                    chain.proceed(newRequest)
                } else {
                    chain.proceed(chain.request())
                }
            } else {
                chain.proceed(chain.request())
            }
        }
    }

    inner class AccessTokenAuthenticator: Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            return if (response.retryCount > 2) null
            else response.createSignedRequest()
        }

        private fun Response.createSignedRequest(): Request? {
            var signedRequest: Request? = null
            runBlocking {
                fetchAccessToken()
                    .onSuccess { signedRequest = request.signWithToken(it.token) }
                    .onFailure { signedRequest = null }
            }
            return signedRequest
        }


        private suspend fun fetchAccessToken(): Result<AccessToken> {
            val user = buildAuthUser()
            return if (user != null) {
                withContext(Dispatchers.IO) {
                    this@WebClient.webService.refreshToken(user)
                        .onSuccess { AppPrefs.token = it.token }
                }
            } else {
                Result.failure(RuntimeException("user not in shared pref"))
            }
        }

        private fun buildAuthUser(): NetworkAuthUser? {
            val username = AppPrefs.userName
            val password = AppPrefs.password
            return if (username != null && password != null)
                NetworkAuthUser(username,password)
            else null
        }
    }


    val webService: WebService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .addInterceptor(AuthenticationInterceptor())
            .authenticator(AccessTokenAuthenticator())
            .build()

        val moshi = Moshi.Builder()
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .client(client)
            .build()

        retrofit.create(WebService::class.java)
    }

    fun Request.signWithToken(token: String) =
        newBuilder()
            .header("Authorization", token)
            .build()

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
}