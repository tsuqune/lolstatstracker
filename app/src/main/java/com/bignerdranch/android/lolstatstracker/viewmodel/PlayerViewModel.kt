package com.bignerdranch.android.lolstatstracker.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.lolstatstracker.repository.ChampionRepository
import com.bignerdranch.android.lolstatstracker.Constants
import com.bignerdranch.android.lolstatstracker.Screen
import com.bignerdranch.android.lolstatstracker.model.*
import com.bignerdranch.android.lolstatstracker.network.RiotApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException

private const val TAG = "PlayerViewModel"

class PlayerViewModel : ViewModel() {
    private val _playerData = MutableStateFlow<PlayerData?>(null)
    val playerData: StateFlow<PlayerData?> = _playerData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)

    private val _matches = MutableStateFlow<List<MatchData>>(emptyList())
    val matches: StateFlow<List<MatchData>> = _matches

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Input)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun resetNavigation() {
        _currentScreen.value = Screen.Input
    }

    fun clearData() {
        _playerData.value = null
        _matches.value = emptyList()
    }

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

                // Шаг 5: Получение ID normal матчей
                Log.d(TAG, "Step 5: Fetching normal match IDs...")
                val matchIds = RiotApiService.accountInstance.getRankedMatchIds(
                    puuid = account.puuid,
                    count = 20,
                    queue = 420,
                    apiKey = Constants.RIOT_API_KEY
                ).also { logMatchIds(it) }

                val matches = matchIds.map { matchId ->
                    async {
                        val response = RiotApiService.accountInstance.getMatchDetails(
                            matchId,
                            Constants.RIOT_API_KEY
                        )
                        processMatch(response, account.puuid)
                    }
                }.awaitAll().filterNotNull()

                _matches.value = matches

                // Шаг 6:
                Log.d(TAG, "Step 6: Processing data...")
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
                    topChampions = championMasteries
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

    private fun logMatchIds(ids: List<String>) {
        Log.d(TAG, "Retrieved ${ids.size} ranked match IDs:")
        ids.forEachIndexed { index, id ->
            Log.d(TAG, "${index + 1}. $id")
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

    private fun processMatch(response: MatchDetailsResponse, puuid: String): MatchData? {
        val participant = response.info.participants.find { it.puuid == puuid }
        return participant?.let {
            MatchData(
                matchId = response.metadata.matchId,
                championId = it.championId,
                kills = it.kills,
                deaths = it.deaths,
                assists = it.assists,
                win = it.win
            )
        }
    }
}
