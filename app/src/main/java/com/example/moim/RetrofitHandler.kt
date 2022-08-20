package com.example.moim

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

val retrofit: Retrofit = Retrofit.Builder().baseUrl("http://$ipAddress/")
    .addConverterFactory(GsonConverterFactory.create()).build()
val retrofitHandler: RetrofitHandler = retrofit.create(RetrofitHandler::class.java)

interface RetrofitHandler {
    @GET("/register/duplicate-id")
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

    @GET("/party-list/taxi-party")
    fun getTaxiPartyList(): Call<MutableList<TaxiParty>>

    @GET("/party-list/meal-party")
    fun getMealPartyList(): Call<MutableList<MealParty>>

    @GET("/party-list/night-meal-party")
    fun getNightMealPartyList(): Call<MutableList<MealParty>>

    @GET("/party-list/study-party")
    fun getStudyPartyList(): Call<MutableList<StudyParty>>

    @GET("/party-list/custom-party")
    fun getCustomPartyList(): Call<MutableList<CustomParty>>

    @GET("/username")
    fun getUsername(
        @Query("userid") userId: String
    ): Call<ResponseUsername>

    @GET("/party")
    fun getParty(
        @Query("party_id") partyId: Int,
        @Query("type") partyType: String,
    ): Call<Party>

    @POST("/join-party/{party_type}")
    fun joinParty(
        @Path("party_type") partyType: String,
        @Body joinBody: PartyJoinInformation
    ): Call<Unit>

    @POST("/create-party/{party_type}")
    fun createParty(
        @Path("party_type") partyType: String,
        @Body partyBody: Party,
    ): Call<ResponseCreateParty>

    @POST("/modify-party")
    fun modifyParty(
        @Body partyBody: Party,
    ): Call<ResponseModifyParty>

    @GET("/liked-party")
    fun getLikedPartyList(): Call<MutableList<Party>>

    @GET("party-user-list/{party_id}")
    fun getPartyUserList(
        @Path("party_id") partyId: Int,
    ): Call<MutableList<UserInformation>>
}