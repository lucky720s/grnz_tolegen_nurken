package com.example.grnz.data.network

import com.example.grnz.data.network.model.LoginRequest
import com.example.grnz.data.network.model.LoginResponse
import com.example.grnz.data.network.model.ProfileResponse
import com.example.grnz.data.network.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("/api/profile")
    suspend fun getProfile(): Response<ProfileResponse>
}
