package com.bignerdranch.android.lolstatstracker.model

data class ChampionMastery(
    val championId: Long,
    val championLevel: Int,
    val championPoints: Int,
    var championName: String? = null
)