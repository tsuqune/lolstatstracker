package com.bignerdranch.android.lolstatstracker.model

data class RiotAccountResponse(
    val puuid: String,
    val gameName: String,
    val tagLine: String
)

data class SummonerResponse(
    val id: String,
    val puuid: String,

    val summonerLevel: Int,
    val profileIconId: Int
)

data class LeagueEntryResponse(
    val queueType: String,
    val tier: String?,
    val rank: String?,
    val leaguePoints: Int,
    val wins: Int,
    val losses: Int
)
