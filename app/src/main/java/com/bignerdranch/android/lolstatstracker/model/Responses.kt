package com.bignerdranch.android.lolstatstracker.model

import com.google.gson.annotations.SerializedName

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

data class ChampionMasteryResponse(
    val championId: Long,
    val championLevel: Int,
    val championPoints: Int,
    val lastPlayTime: Long
)
