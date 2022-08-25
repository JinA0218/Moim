package com.example.moim

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.Size
import android.view.WindowManager
import android.view.WindowMetrics
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Exception

// TODO: IP 주소 계속 바꿔주기
const val ipAddress = "143.248.195.165"

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

fun partyTypeKorean(partyTypeNumber: Int): String {
    return when (partyTypeNumber) {
        PartyTypeNumber.Taxi -> "택시팟"
        PartyTypeNumber.Meal -> "밥약팟"
        PartyTypeNumber.NightMeal -> "야식팟"
        PartyTypeNumber.Study -> "공부/프로젝트팟"
        PartyTypeNumber.Custom -> "나만의팟"
        else -> throw Error("?!")
    }
}

fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(format, locale)
    return formatter.format(this)
}

fun getCurrentDateTime(): Date {
    return Calendar.getInstance().time
}

// Parses date string and transform it to user-friendly string.
fun parseDateString(dateString: String): String {
    val errorString = ""
    val datePart = dateString.split("T")[0]
    val dateValues = datePart.split("-")

    if (dateValues.size != 3) {
        return errorString
    }
    else {
        try {
            var year = dateValues[0].toInt()
            var month = dateValues[1].toInt()
            var day = dateValues[2].toInt()

            day++

            val threshold = when (month) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> 28
                else -> return errorString
            }

            if (day > threshold) {
                day = 1
                month++

                if (month > 12) {
                    month = 1
                    year++
                }
            }

            return String.format("%d년 %d월 %d일", year, month, day)
        }
        catch (e: Exception) {
            return errorString
        }
    }
}

// Parses time string and transform it to user-friendly string.
fun parseTimeString(timeString: String): String {
    val errorString = ""
    val timeList = timeString.split(":")

    if (timeList.size != 3) {
        return errorString
    }

    val hour = timeList[0].toIntOrNull()

    if (hour == null) {
        return errorString
    }
    else {
        return if (hour > 12) {
            String.format("오후 %d시 %s분", hour - 12, timeList[1])
        }
        else if (hour == 12) {
            String.format("오후 12시 %s분", timeList[1])
        }
        else if (hour == 0) {
            String.format("오전 12시 %s분", hour, timeList[1])
        }
        else {
            String.format("오전 %d시 %s분", hour, timeList[1])
        }
    }
}


fun mixedToList(mixedParties: ResponseMixedParty): MutableList<Party> {
    val result = mutableListOf<Party>()

    for (taxiParty in mixedParties.taxiParty) {
        result.add(taxiParty)
    }

    for (mealParty in mixedParties.mealParty) {
        result.add(mealParty)
    }

    for (nightMealParty in mixedParties.nightMealParty) {
        result.add(nightMealParty)
    }

    for (studyParty in mixedParties.studyParty) {
        result.add(studyParty)
    }

    for (customParty in mixedParties.customParty) {
        result.add(customParty)
    }

    return result
}

fun partyTypeToNumber(partyType: String?): Int {
    return when (partyType) {
        "taxi_party" -> PartyTypeNumber.Taxi
        "meal_party" -> PartyTypeNumber.Meal
        "night_meal_party" -> PartyTypeNumber.NightMeal
        "study_party" -> PartyTypeNumber.Study
        "custom_party" -> PartyTypeNumber.Custom
        null -> throw Error("I should have handled all NULL party types.")
        else -> throw Error("Nothing else should encounter.")
    }
}


interface Party: Serializable {
    val common: CommonPartyAttributes
    val partyType: String?
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
    var partyId: Int,
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
    var countDifference: Int
): Serializable

data class TaxiParty(
    override val common: CommonPartyAttributes,
    val extra: TaxiExtra,
    @SerializedName("party_type")
    override val partyType: String?,
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

data class MealParty(
    override val common: CommonPartyAttributes,
    val extra: MealExtra,
    @SerializedName("party_type")
    override val partyType: String?,
): Party, Serializable

data class MealExtra(
    @SerializedName("meal_type")
    val mealType: String,
    val outside: Int,
    @SerializedName("party_date")
    val partyDate: String,
    @SerializedName("party_time")
    val partyTime: String,
): Serializable

data class StudyParty(
    override val common: CommonPartyAttributes,
    @SerializedName("party_type")
    override val partyType: String?,
): Party, Serializable

data class CustomParty(
    override val common: CommonPartyAttributes,
    @SerializedName("party_type")
    override val partyType: String?
): Party, Serializable

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

data class UserInformation (
    @SerializedName("party_id")
    val partyId: Int,
    val userid: String,
    val username: String,
): Serializable

data class ChatItem (
    @SerializedName("chat_id")
    val chatId: Int,
    @SerializedName("chat_type")
    val chatType: String,
    @SerializedName("party_id")
    val partyId: Int,
    val userid: String,
    val username: String,
    @SerializedName("chat_content")
    val chatContent: String,
    @SerializedName("chat_time")
    val chatTime: String,
    @SerializedName("chat_date")
    val chatDate: String,
): Serializable


data class PartyJoinInformation(
    val userid: String,
    @SerializedName("party_id")
    val partyId: Int,
    val username: String,
): Serializable

data class LikeInformation(
    val userid: String,
    val username: String,
    @SerializedName("party_id")
    val partyId: Int,
    val liked: Int,
    @SerializedName("party_type")
    val partyType: String,
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
    // TODO
    val placeHolder: Int,
): Serializable

data class ResponseUsername(
    val username: String,
): Serializable

data class ResponseJoinParty(
    @SerializedName("current_count")
    val currentCount: Int
): Serializable

data class ResponseMixedParty(
    @SerializedName("taxi_party")
    val taxiParty: MutableList<TaxiParty>,
    @SerializedName("meal_party")
    val mealParty: MutableList<MealParty>,
    @SerializedName("night_meal_party")
    val nightMealParty: MutableList<MealParty>,
    @SerializedName("study_party")
    val studyParty: MutableList<StudyParty>,
    @SerializedName("custom_party")
    val customParty: MutableList<CustomParty>
): Serializable
