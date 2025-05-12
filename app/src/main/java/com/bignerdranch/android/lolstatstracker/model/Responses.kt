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
    val championPoints: Int
)

data class DDragonChampionResponse(
    val data: Map<String, DDragonChampion>
)

data class DDragonChampion(
    val key: String,
    val name: String,
    val id: String
)

data class MatchDetailsResponse(
    val info: MatchInfo,
    val metadata: MatchMetadata
)

data class MatchInfo(
    val participants: List<Participant>
)

data class MatchMetadata(
    val matchId: String
)

data class Participant(
    val puuid: String,
    val championId: Long,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val win: Boolean
)
