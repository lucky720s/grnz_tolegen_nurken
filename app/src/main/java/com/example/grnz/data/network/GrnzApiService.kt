package com.example.grnz.data.network

import com.example.grnz.data.network.model.Photo
import com.example.grnz.data.network.model.SearchResponse
import com.example.grnz.data.network.model.UserPhotosResponse
import com.example.grnz.data.network.model.FeedResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface GrnzApiService {

    @GET("/api/photos")
    suspend fun searchPhotos(@Query("grnz") grnz: String): Response<SearchResponse>

    @GET("/api/search-history")
    suspend fun getSearchHistory(): Response<List<String>>

    @Multipart
    @POST("/api/photos")
    suspend fun uploadPhoto(
        @Part("grnz") grnz: RequestBody,
        @Part photoFile: MultipartBody.Part
    ): Response<Photo>

    @GET("/api/users/{userId}/photos")
    suspend fun getUserPhotos(
        @Path("userId") userId: String
    ): Response<UserPhotosResponse>

    @GET("/api/feed")
    suspend fun getFeed(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("numberPart") numberPart: String?,
        @Query("letterPart") letterPart: String?,
        @Query("regionCode") regionCode: String?
    ): Response<FeedResponse>

    @DELETE("/api/photos/{id}")
    suspend fun deletePhoto(
        @Path("id") photoId: String
    ): Response<Unit>

    @DELETE("/api/search-history")
    suspend fun clearSearchHistory(): Response<Unit>
}
