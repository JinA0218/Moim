package com.example.moim

import java.io.Serializable

// TODO: 뭐가 더 필요할까?

data class Party (
    val id: Int,
    val name: String,
    val curPeopleCount: Int,
    val maxPeopleCount: Int,
): Serializable