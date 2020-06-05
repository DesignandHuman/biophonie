package com.example.biophonie.api

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException


class ErrorUtils {

    fun parseError(response: Response<*>): ApiError? {
        val converter: Converter<ResponseBody, ApiError> = ApiClient().retrofit
            .responseBodyConverter(ApiError::class.java, arrayOfNulls<Annotation>(0))
        return try {
            converter.convert(response.errorBody()!!)
        } catch (e: IOException) {
            return ApiError()
        }
    }

}