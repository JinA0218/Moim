package com.example.moim

import java.io.Serializable

// TODO: IP 주소 계속 바꿔주기
const val ipAddress = "143.248.195.111"

data class Party (
    val id: Int,
    val name: String,
    val curPeopleCount: Int,
    val maxPeopleCount: Int,
): Serializable

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