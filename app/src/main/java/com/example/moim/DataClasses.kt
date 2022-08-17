package com.example.moim

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// TODO: IP 주소 계속 바꿔주기
const val ipAddress = "143.248.195.111"

data class Place(
    val place1: String,
    val place2: String?,
    val place3: String,
): Serializable

data class CommonPartyAttributes(
    val partyId: Int,
    val partyName: String,
    val startPlace: Place,
    val currentPeopleCount: Int,
    val maximumPeopleCount: Int,
    val detailedDescription: String,
): Serializable

data class TaxiParty(
    val detailedStartPlace: String,
    val destination: String,
    val date: Int,
    val time: Int,
    val currentPeopleCount: Int,
    val maximumPeopleCount: Int,
): Serializable

data class MealParty(
    val partyId: Int,
    val partyName: String,
    val outside: Boolean,
    val date: Int,
    val time: Int,
    val currentPeopleCount: Int,
    val maximumPeopleCount: Int,
)

data class LoginInformation (
    val userid: String,
    val pw: String,
): Serializable

data class RegisterInformation (
    val userid: String,
    val pw: String,
    val age: Int?,
    val username: String,
    val place1: String,         // 광역시 특별시 도
    val place2: String?,        // 시, 군 (광역시 특별시의 경우 NULL)
    val place3: String,         // 구
): Serializable


// Retrofit2 API 와 연결해서 사용할 것
data class ResponseLogin(
    val username: String,
): Serializable

data class ResponseCreateParty(
    @SerializedName("party_id")
    val partyId: Int,
): Serializable