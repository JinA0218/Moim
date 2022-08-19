package com.example.moim

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// TODO: IP 주소 계속 바꿔주기
const val ipAddress = "143.248.195.105"

object PartyTypeNumber {
    const val Taxi = 0
    const val Meal = 1
    const val NightMeal = 2
    const val Study = 3
    const val Custom = 4
}

fun partyTypeString(partyTypeNumber: Int): String {
    return when (partyTypeNumber) {
        PartyTypeNumber.Taxi -> "taxi-party"
        PartyTypeNumber.Meal -> "meal-party"
        PartyTypeNumber.NightMeal -> "night-meal-party"
        PartyTypeNumber.Study -> "study-party"
        PartyTypeNumber.Custom -> "custom-party"
        else -> throw Error("?!")
    }
}

interface Party: Serializable {
    val common: CommonPartyAttributes
}

data class Place(
    @SerializedName("has_place")
    val hasPlace: Int,
    val place1: String?,
    val place2: String?,
    val place3: String?,
): Serializable {
    fun toWrittenString(): String {
        var output = ""

        if (place1 != null) {
            output = place1
        }
        if (place2 != null) {
            output = "$output $place2"
        }
        if (place3 != null) {
            output = "$output $place3"
        }

        return output.ifEmpty {
            "지역 상관없음"
        }
    }
}

data class CommonPartyAttributes(
    @SerializedName("party_id")
    val partyId: Int,
    @SerializedName("party_name")
    val partyName: String,
    @SerializedName("party_head")
    val partyHead: String,
    val place: Place,
    @SerializedName("current_count")
    val currentCount: Int,
    @SerializedName("maximum_count")
    val maximumCount: Int,
    @SerializedName("detailed_description")
    val detailedDescription: String,
    @SerializedName("count_difference")
    val countDifference: Int
): Serializable

data class TaxiParty(
    override val common: CommonPartyAttributes,
    val extra: TaxiExtra,
): Party

data class TaxiExtra(
    @SerializedName("detailed_start_place")
    val detailedStartPlace: String,
    val destination: String,
    @SerializedName("party_date")
    val partyDate: String,
    @SerializedName("party_time")
    val partyTime: String,
): Serializable

//data class TaxiParty(
//    val common: CommonPartyAttributes,
//    val detailedStartPlace: String,
//    val destination: String,
//    val date: Int,
//    val time: Int,
//): Serializable

data class MealParty(
    override val common: CommonPartyAttributes,
    val extra: MealExtra,
): Party, Serializable

data class MealExtra(
    @SerializedName("meal_type")
    val mealType: String,
    val outside: Boolean,
    @SerializedName("party_date")
    val partyDate: String,
    @SerializedName("party_time")
    val partyTime: String,
): Serializable

//data class MealParty(
//    val common: CommonPartyAttributes,
//    val mealType: String,
//    val outside: Boolean,
//    val date: Int,
//    val time: Int,
//): Serializable

data class StudyParty(
    override val common: CommonPartyAttributes
): Party, Serializable

data class CustomParty(
    override val common: CommonPartyAttributes,
): Party, Serializable

//data class StudyParty(
//    val common: CommonPartyAttributes,
//): Serializable
//
//data class CustomParty(
//    val common: CommonPartyAttributes,
//): Serializable

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

data class PartyJoinInformation(
    val userid: String,
    @SerializedName("party_id")
    val partyId: Int,
): Serializable

// Retrofit2 API 와 연결해서 사용할 것
data class ResponseLogin(
    val username: String,
): Serializable

data class ResponseCreateParty(
    @SerializedName("party_id")
    val partyId: Int,
): Serializable

data class ResponseModifyParty(
    val placeHolder: Int,
): Serializable

data class ResponseUsername(
    val username: String,
): Serializable