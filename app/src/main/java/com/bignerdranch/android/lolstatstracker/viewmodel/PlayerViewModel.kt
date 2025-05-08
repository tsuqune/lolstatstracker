package com.bignerdranch.android.lolstatstracker.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.lolstatstracker.ChampionRepository
import com.bignerdranch.android.lolstatstracker.Constants
import com.bignerdranch.android.lolstatstracker.model.*
import com.bignerdranch.android.lolstatstracker.network.RiotApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.util.Date

private const val TAG = "PlayerViewModel"

class PlayerViewModel : ViewModel() {
    private val _playerData = MutableStateFlow<PlayerData?>(null)
    val playerData: StateFlow<PlayerData?> = _playerData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchPlayerData(gameName: String, tagLine: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            Log.d(TAG, "============================================================")
            Log.d(TAG, "Starting fetch for: $gameName#$tagLine")

            try {
                // Шаг 1: Получение аккаунта
                Log.d(TAG, "Step 1: Fetching account...")
                val account = RiotApiService.accountInstance.getAccountByRiotId(
                    gameName, tagLine, Constants.RIOT_API_KEY
                ).also { logAccountResponse(it) }

                // Шаг 2: Получение summoner
                Log.d(TAG, "Step 2: Fetching summoner...")
                val summoner = RiotApiService.regionalInstance.getSummonerByPuuid(
                    account.puuid, Constants.RIOT_API_KEY
                ).also { logSummonerResponse(it) }

                // Шаг 3: Получение ранговой статистики
                Log.d(TAG, "Step 3: Fetching league entries...")
                val leagueEntries = RiotApiService.regionalInstance.getLeagueEntries(
                    summoner.id, Constants.RIOT_API_KEY
                ).also { logLeagueEntries(it) }

                // Шаг 4: Получение мастерства чемпионов
                Log.d(TAG, "Step 4: Fetching champion masteries...")
                val championMasteries = RiotApiService.regionalInstance.getChampionMasteries(
                    account.puuid, Constants.RIOT_API_KEY
                ).also { logChampionMasteries(it) }

                val topChampions = championMasteries
                    .sortedByDescending { it.championPoints }
                    .take(3)
                    .map { mastery ->
                        val name = ChampionRepository.getChampionName(mastery.championId)
                        ChampionMastery(
                            championId = mastery.championId,
                            championLevel = mastery.championLevel,
                            championPoints = mastery.championPoints,
                            championName = name
                        )
                    }

                // Обработка данных
                Log.d(TAG, "Step 5: Processing data...")
                val soloQueue = leagueEntries.find { it.queueType == "RANKED_SOLO_5x5" }



                val processedData = PlayerData(
                    summonerName = account.gameName,
                    summonerLevel = summoner.summonerLevel,
                    profileIconId = summoner.profileIconId,
                    rank = soloQueue?.rank,
                    tier = soloQueue?.tier,
                    leaguePoints = soloQueue?.leaguePoints,
                    wins = soloQueue?.wins,
                    losses = soloQueue?.losses,
                    winRate = soloQueue?.let {
                        (it.wins.toDouble() / (it.wins + it.losses)) * 100
                    },
                    topChampions = topChampions
                ).also { Log.d(TAG, "Processed data: $it") }

                _playerData.value = processedData
                Log.d(TAG, "All data fetched successfully")

            } catch (e: UnknownHostException) {
                handleError("No internet connection", e)
            } catch (e: Exception) {
                handleError(
                    when {
                        e.message?.contains("HTTP 401") == true -> "Invalid API key"
                        e.message?.contains("HTTP 403") == true -> "API key denied"
                        e.message?.contains("HTTP 404") == true -> "Player not found"
                        e.message?.contains("HTTP 429") == true -> "Too many requests"
                        else -> "Error: ${e.message ?: "Unknown"}"
                    },
                    e
                )
            } finally {
                _isLoading.value = false
                Log.d(TAG, "============================================================\n")
            }
        }
    }

    private fun handleError(message: String, e: Exception) {
        Log.e(TAG, message, e)
        _error.value = message
    }

    // Логирование
    private fun logAccountResponse(response: RiotAccountResponse) {
        Log.d(TAG, """
            |Account:
            |PUUID: ${response.puuid}
            |Name: ${response.gameName}#${response.tagLine}
        """.trimMargin())
    }

    private fun logSummonerResponse(response: SummonerResponse) {
        Log.d(TAG, """
            |Summoner:
            |ID: ${response.id}
            |Level: ${response.summonerLevel}
            |Icon: ${response.profileIconId}
        """.trimMargin())
    }

    private fun logLeagueEntries(entries: List<LeagueEntryResponse>) {
        Log.d(TAG, "League entries (${entries.size}):")
        entries.forEach {
            Log.d(TAG, """
                |Queue: ${it.queueType}
                |Rank: ${it.tier} ${it.rank}
                |LP: ${it.leaguePoints}
                |W/L: ${it.wins}/${it.losses}
            """.trimMargin())
        }
    }

    private fun logChampionMasteries(masteries: List<ChampionMasteryResponse>) {
        Log.d(TAG, "Champion masteries (${masteries.size}):")
        masteries.take(3).forEachIndexed { i, m ->
            Log.d(TAG, """
                |Top ${i + 1}:
                |Champion Key: ${m.championId}
                |Level: ${m.championLevel}
                |Points: ${m.championPoints}
            """.trimMargin())
        }
    }
}