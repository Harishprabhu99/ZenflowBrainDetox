package com.zenflow.brain.detox.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface ZenflowApi {
    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): UserResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
}

data class SignupRequest(
    val email: String,
    val password: String,
    val displayName: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserResponse(
    val id: String,
    val email: String,
    val displayName: String?
)

data class AuthResponse(
    val token: String
)
