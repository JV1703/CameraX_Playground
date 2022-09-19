package com.example.intermediatesubmission.data.network

import com.example.intermediatesubmission.data.data_source.DicodingLocalDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HeaderInterceptor @Inject constructor(private val localDataSource: DicodingLocalDataSource) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        var token: String? = null
        return runBlocking {
            token = localDataSource.getAuthTokenFromPreferencesStore().first()
            if (!token.isNullOrEmpty()) {
                val authorized = original.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(authorized)
            } else {
                chain.proceed(original)
            }
        }
    }
}