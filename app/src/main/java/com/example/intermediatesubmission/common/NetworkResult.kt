package com.example.intermediatesubmission.common

import android.util.Log
import org.json.JSONObject
import retrofit2.Response

sealed class NetworkResult<T>(
    val data: T? = null, val message: String? = null
) {

    class Success<T>(data: T) : NetworkResult<T>(data)
    class Error<T>(message: String?, data: T? = null) : NetworkResult<T>(data, message)
    class Loading<T> : NetworkResult<T>()

}

fun <T : Any?> networkResultHandler(response: Response<T>): NetworkResult<T> {

    return when {
        response.message().toString().contains("timeout") -> {
            Log.i("network_result", "timeout")
            NetworkResult.Error("Timeout")
        }
        response.errorBody() != null -> {
            val jObjError = JSONObject(response.errorBody()?.string()!!)
            val errorBody = jObjError.getString("message")
            Log.i("network_result", "$errorBody")
            NetworkResult.Error(
                "${response.code()}, error: $errorBody"
            )
        }
        (response.isSuccessful && response.body() != null) -> {
            val response = response.body()
            Log.i("network_result", "isSuccessful")
            NetworkResult.Success(response!!)
        }
        else -> {
            Log.i(
                "network_result",
                "else - error code: ${response.code()}, msg: ${response.message()}"
            )
            NetworkResult.Error("error code: ${response.code()}, msg: $response")
        }
    }

}