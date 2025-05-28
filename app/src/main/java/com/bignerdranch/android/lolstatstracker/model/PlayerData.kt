package com.bignerdranch.android.lolstatstracker.model

data class PlayerData(
    val summonerName: String,
    val summonerLevel: Int,
    val profileIconId: Int,
    val rank: String?,
    val tier: String?,
    val leaguePoints: Int?,
    val wins: Int?,
    val losses: Int?,
    val winRate: Double?,
    val topChampions: List<ChampionMastery>,
    val matches: List<MatchData> = emptyList()
)

