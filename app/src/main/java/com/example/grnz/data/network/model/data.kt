package com.example.grnz.data.network.model

data class UserPhotosResponse(
    val userEmail: String?,
    val photos: List<Photo>
)
data class VehicleData(
    val car: String?,
    val year: String?
)
data class Photo(
    val id: String,
    val grnz: String,
    val imageUrl: String?,
    val userId: String,
    val timestamp: Long,
    val originalFilename: String?,
    val filesize: Long?,
    val uploaderEmail: String?
)
data class SearchResponse(
    val vehicle: VehicleData?,
    val photos: List<Photo>
)

data class RegisterRequest(
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String
)

data class ErrorResponse(
    val message: String
)
data class FeedResponse(
    val photos: List<Photo>,
    val currentPage: Int,
    val totalPages: Int,
    val totalPhotos: Int
)
data class ProfileResponse(
    val id: String,
    val email: String
)