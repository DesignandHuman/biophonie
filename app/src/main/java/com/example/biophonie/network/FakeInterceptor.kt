package com.example.biophonie.network

import android.os.SystemClock
import okhttp3.*
import java.io.IOException
import java.net.URI

// FAKE RESPONSES
private const val SoundId1 = "{\"id\":1,\"sounds\":[{\"title\":\"Hibou en soirée\",\"date\":\"2017-06-24T09:32:55Z\",\"amplitudes\":[0,1,2,3,4,5,6],\"url_photo\":\"https://biophonie.fr/photos/1\",\"url_audio\":\"https://biophonie.fr/audios/1\"}]}"
private const val SoundId2 = "{\"id\":2,\"sounds\":[{\"title\":\"Chouette le matin\",\"date\":\"2018-09-22T11:21:32Z\",\"amplitudes\":[0,1,2,3,4,5,6],\"url_photo\":\"https://biophonie.fr/photos/2\",\"url_audio\":\"https://biophonie.fr/audios/2\"}," +
        "{\"title\":\"Chouette le soir\",\"date\":\"2018-12-22T23:15:30Z\",\"amplitudes\":[0,1,2,3,4,5,6],\"url_photo\":\"https://biophonie.fr/photos/3\",\"url_audio\":\"https://biophonie.fr/audios/3\"}]}"

// Only for testing purpose. Use a MockWebServer for more complete tests
class FakeInterceptor : Interceptor {
    
    private val TAG = FakeInterceptor::class.java.simpleName

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response? {
        val responseString: String
        // Get Request URI.
        val uri: URI = chain.request().url().uri()
        // Get Query String.
        val query: String? = uri.query
        // Parse the Query String.
        SystemClock.sleep(1000)
        if (query != null){
            val parsedQuery = query.split("=".toRegex()).toTypedArray()
            responseString = if (parsedQuery[0].equals("id", ignoreCase = true) && parsedQuery[1]
                    .equals("1", ignoreCase = true)
            ) {
                SoundId1
            } else if (parsedQuery[0].equals("id", ignoreCase = true) && parsedQuery[1]
                    .equals("2", ignoreCase = true)
            ) {
                SoundId2
            } else {
                ""
            }
            return Response.Builder()
                .code(200)
                .message(responseString)
                .request(chain.request())
                .protocol(Protocol.HTTP_2)
                .body(
                    ResponseBody.create(
                        MediaType.parse("application/json"),
                        responseString.toByteArray()
                    )
                )
                .addHeader("content-type", "application/json")
                .build()
        } else {
            return Response.Builder()
                .code(200)
                .message("{\"message\":\"OK\"}")
                .request(chain.request())
                .protocol(Protocol.HTTP_2)
                .body(
                    ResponseBody.create(
                        MediaType.parse("application/json"),
                        "{\"message\":\"OK\"}".toByteArray()
                    )
                )
                .addHeader("content-type", "application/json")
                .build()
        }
    }

}