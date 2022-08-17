package com.example.moim

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

val retrofit: Retrofit = Retrofit.Builder().baseUrl("http://$ipAddress/")
    .addConverterFactory(GsonConverterFactory.create()).build()
val retrofitHandler: RetrofitHandler = retrofit.create(RetrofitHandler::class.java)

interface RetrofitHandler {
    @GET("/register/duplicate_id")
    fun checkDuplicateId(
        @Query("userid") userId: String
    ): Call<Unit>

    @POST("/login")
    fun tryLogin(
        @Body loginBody: LoginInformation,
    ): Call<ResponseLogin>

    @POST("/register")
    fun tryRegister(
        @Body registerBody: RegisterInformation,
    ): Call<Unit>

    @GET("/party_list")
    fun getPartyList(
        @Query("party_type") partyType: String
    ): Call<Party>
}