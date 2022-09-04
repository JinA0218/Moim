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

    @POST("/join-party/taxi-party")
    fun joinTaxiParty(
        @Body joinBody: PartyJoinInformation
    ): Call<Unit>

    @POST("/join-party/meal-party")
    fun joinMealParty(
        @Body joinBody: PartyJoinInformation
    ): Call<Unit>

    @POST("/join-party/night-meal-party")
    fun joinNightMealParty(
        @Body joinBody: PartyJoinInformation
    ): Call<Unit>

    @POST("/join-party/study-party")
    fun joinStudyParty(
        @Body joinBody: PartyJoinInformation
    ): Call<Unit>

    @POST("/join-party/custom-party")
    fun joinCustomParty(
        @Body joinBody: PartyJoinInformation
    ): Call<Unit>

    @POST("/create-party/taxi-party")
    fun createTaxiParty(
        @Body partyBody: TaxiParty,
    ): Call<ResponseCreateParty>

    @POST("/create-party/meal-party")
    fun createMealParty(
        @Body partyBody: MealParty,
    ): Call<ResponseCreateParty>

    @POST("/create-party/night-meal-party")
    fun createNightMealParty(
        @Body partyBody: MealParty,
    ): Call<ResponseCreateParty>

    @POST("/create-party/study-party")
    fun createStudyParty(
        @Body partyBody: StudyParty,
    ): Call<ResponseCreateParty>

    @POST("/create-party/custom-party")
    fun createCustomParty(
        @Body partyBody: CustomParty,
    ): Call<ResponseCreateParty>

    @POST("/modify-party")
    fun modifyParty(
        @Body partyBody: Party,
    ): Call<ResponseModifyParty>

    @GET("/party-user-list/{party_id}")
    fun getPartyUserList(
        @Path("party_id") partyId: Int,
    ): Call<MutableList<UserInformation>>

    @GET("/party-chat-list")
    fun getPartyChatList(
        @Query("party_id") partyId: Int,
        @Query("offset") offset: Int,
    ): Call<MutableList<ChatItem>>

    @GET("/my-party/{userid}")
    fun getMyPartyList(
        @Path("userid") userId: String,
    ): Call<ResponseMixedParty>

    @GET("/liked-party/{userid}")
    fun getLikedPartyList(
        @Path("userid") userId: String,
    ): Call<ResponseMixedParty>

    @POST("/like")
    fun likeParty(
        @Body likeBody: LikeInformation
    ): Call<Unit>

    @POST("/leave-party")
    fun leaveParty(
        @Body LeaveBody: LeaveInformation
    ): Call<Unit>
}