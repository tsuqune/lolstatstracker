package com.bignerdranch.android.lolstatstracker.model

data class RiotAccountResponse(
    val puuid: String,
    val gameName: String,
    val tagLine: String
)

data class SummonerResponse(
    val id: String,
    val puuid: String,
    val name: String?,
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

//data class ChampionMasteryResponse(
//    val championId: Int,
//    val championLevel: Int,
//    val championPoints: Int,
//    val lastPlayTime: Long,
//    val championPointsSinceLastLevel: Int,
//    val championPointsUntilNextLevel: Int,
//    val chestGranted: Boolean,
//    val tokensEarned: Int,
//    val summonerId: String
//)
//
//data class ChampionDataResponse(
//    val data: Map<String, ChampionDetails>
//)
//
//data class ChampionDetails(
//    val key: String, // ID чемпиона
//    val name: String,
//    val image: ChampionImage
//)
//
//data class ChampionImage(
//    val full: String
//)