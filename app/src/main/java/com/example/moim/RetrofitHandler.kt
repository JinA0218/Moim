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

    @POST("/create-party")
    fun createParty(
        @Body partyBody: Party,
    ): Call<ResponseCreateParty>

    @POST("/modify-party")
    fun modifyParty(
        @Body partyBody: Party,
    ): Call<ResponseModifyParty>


}