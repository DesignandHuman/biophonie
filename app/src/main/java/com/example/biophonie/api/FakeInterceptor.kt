package com.example.biophonie.api

import android.os.SystemClock
import okhttp3.*
import java.io.IOException
import java.net.URI

// FAKE RESPONSES
private const val SoundId1 = "{\"id\":1,\"name\":\"Son de lac\",\"urlAudio\":\"https://blabla.com/1\",\"amplitudes\":[0,1,2,3,4,5,6]}"
private const val SoundId2 = "{\"id\":2,\"name\":\"Son de forêt\",\"urlAudio\":\"https://blabla.com/2\",\"amplitudes\":[6,5,4,3,2,1,0]}"

// Only for testing purpose. Use a MockWebServer for more complete tests
class FakeInterceptor : Interceptor {
    
    private val TAG = FakeInterceptor::class.java.simpleName

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response? {
        val response: Response?
        val responseString: String
        // Get Request URI.
        val uri: URI = chain.request().url().uri()
        // Get Query String.
        val query: String = uri.query
        // Parse the Query String.
        SystemClock.sleep(1000);
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
        response = Response.Builder()
            .code(200)
            .message(responseString)
            .request(chain.request())
            .protocol(Protocol.HTTP_1_0)
            .body(
                ResponseBody.create(
                    MediaType.parse("application/json"),
                    responseString.toByteArray()
                )
            )
            .addHeader("content-type", "application/json")
            .build()
        return response
    }

}