package com.example.grnz.data.network

import android.content.Context
import com.example.grnz.util.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()
        val token = tokenManager.getToken()
        val requestBuilder = originalRequest.newBuilder()
        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}

object ApiClient {
    const val BASE_URL = "http://10.0.2.2:3000/"
    private lateinit var appContext: Context
    private lateinit var tokenManager: TokenManager
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var retrofitInstance: Retrofit
    fun initialize(context: Context) {
        if (::retrofitInstance.isInitialized) {
            return
        }
        appContext = context.applicationContext
        tokenManager = TokenManager(appContext)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(tokenManager))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        retrofitInstance = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthApiService by lazy {
        if (!::retrofitInstance.isInitialized) {
            throw IllegalStateException("ApiClient must be initialized by calling initialize(context) first.")
        }
        retrofitInstance.create(AuthApiService::class.java)
    }

    val grnzService: GrnzApiService by lazy {
        if (!::retrofitInstance.isInitialized) {
            throw IllegalStateException("ApiClient must be initialized by calling initialize(context) first.")
        }
        retrofitInstance.create(GrnzApiService::class.java)
    }

}
