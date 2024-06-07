package com.example.mesrapp.services

import com.example.mesrapp.models.AcceptedUser
import com.example.mesrapp.models.IdMatch
import com.example.mesrapp.models.Match
import com.example.mesrapp.models.UpdateImageUserDto
import com.example.mesrapp.models.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("users")
    fun addUser(@Body user: User): Call<ResponseBody>

    @GET("users/{spotiId}")
    fun getUser(
        @Path("spotiId") id: String
    ): Call<User>

    @PATCH("users/{spotiId}")
    fun updateUser(
        @Path("spotiId") spotiId: String,
        @Body user: User
    ): Call<ResponseBody>


    @Multipart
    @POST("users/addImage")
    fun updateImage(
        @Part("user") user: UpdateImageUserDto,
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>


    @GET("users/check-spotify/{spotiId}")
    fun checkSpotifyId(
        @Path("spotiId") spotiId: String
    ): Call<ResponseBody>

    @POST("matches/add")
    fun addMatch(
        @Body match: Match
    ): Call<ResponseBody>

    @GET("matches/get/{spotiId}")
    fun getMatches(
        @Path("spotiId") spotiId: String,
        @Query("type") type: String
    ): Call<List<Match>>

    @GET("idMatches/get/{spotiId}")
    fun getIdMatches(
        @Path("spotiId") spotiId: String
    ): Call<List<IdMatch>>

    @GET("acceptedUsers/get/{spotiId}")
    fun getAcceptedMatches(
        @Path("spotiId") spotiId: String,
    ): Call<List<AcceptedUser>>

    @DELETE("idMatches/deleteMatch/{spotiId1}/{spotiId2}")
    fun deleteMatchedUsers(
        @Path("spotiId1") spotiId1: String,
        @Path("spotiId2") spotiId2: String
    ): Call<ResponseBody>

    @POST("acceptedUsers/add")
    fun acceptUser(
        @Query("spotiId") spotiId: String,
        @Query("acceptedSpotiId") acceptedSpotiId: String
    ): Call<ResponseBody>

    @DELETE("users/{spotiId}")
    fun deleteUser(
        @Path("spotiId") spotiId: String
    ): Call<ResponseBody>

    @Multipart
    @POST("chats/{messageId}")
    fun uploadImage(
        @Path("messageId") messageId: String,
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>

}