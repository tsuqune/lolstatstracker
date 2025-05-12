package com.bignerdranch.android.lolstatstracker.model

data class MatchData(
    val matchId: String,
    val championId: Long,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val win: Boolean
)