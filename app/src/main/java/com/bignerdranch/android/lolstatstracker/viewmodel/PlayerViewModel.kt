package com.bignerdranch.android.lolstatstracker.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.lolstatstracker.Constants
import com.bignerdranch.android.lolstatstracker.model.LeagueEntryResponse
import com.bignerdranch.android.lolstatstracker.model.PlayerData
import com.bignerdranch.android.lolstatstracker.model.RiotAccountResponse
import com.bignerdranch.android.lolstatstracker.model.SummonerResponse
import com.bignerdranch.android.lolstatstracker.network.RiotApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException

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

            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "Starting fetch for: $gameName#$tagLine")

            try {
                // 1. Получаем аккаунт (континентальный API)
                Log.d(TAG, "Step 1: Fetching account by Riot ID...")
                val account = try {
                    RiotApiService.accountInstance.getAccountByRiotId(
                        gameName = gameName,
                        tagLine = tagLine,
                        apiKey = Constants.RIOT_API_KEY
                    ).also {
                        logAccountResponse(it)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Account fetch failed", e)
                    throw Exception("Failed to get account: ${e.message}")
                }

                // 2. Получаем summoner (региональный API)
                Log.d(TAG, "Step 2: Fetching summoner by PUUID...")
                val summoner = try {
                    RiotApiService.regionalInstance.getSummonerByPuuid(
                        puuid = account.puuid,
                        apiKey = Constants.RIOT_API_KEY
                    ).also {
                        logSummonerResponse(it)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Summoner fetch failed", e)
                    throw Exception("Failed to get summoner: ${e.message}")
                }

                // 3. Получаем ранговую статистику (региональный API)
                Log.d(TAG, "Step 3: Fetching league entries...")
                val leagueEntries = try {
                    RiotApiService.regionalInstance.getLeagueEntries(
                        summonerId = summoner.id,
                        apiKey = Constants.RIOT_API_KEY
                    ).also {
                        logLeagueEntries(it)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "League entries fetch failed", e)
                    throw Exception("Failed to get league data: ${e.message}")
                }

                // 4. Обрабатываем данные
                Log.d(TAG, "Step 4: Processing data...")
                val soloQueue = leagueEntries.find { it.queueType == "RANKED_SOLO_5x5" }

                val processedData = PlayerData(
                    summonerName = summoner.name ?: account.gameName,
                    summonerLevel = summoner.summonerLevel,
                    profileIconId = summoner.profileIconId,
                    rank = soloQueue?.rank,
                    tier = soloQueue?.tier,
                    leaguePoints = soloQueue?.leaguePoints,
                    wins = soloQueue?.wins,
                    losses = soloQueue?.losses,
                    winRate = soloQueue?.let {
                        (it.wins.toDouble() / (it.wins + it.losses)) * 100
                    }
                ).also {
                    Log.d(TAG, "Processed player data: $it")
                }

                _playerData.value = processedData
                Log.d(TAG, "✔ Successfully fetched all data")

            } catch (e: UnknownHostException) {
                val errorMsg = "No internet connection"
                Log.e(TAG, errorMsg, e)
                _error.value = errorMsg
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("HTTP 401") == true -> "Invalid API key"
                    e.message?.contains("HTTP 403") == true -> "Access denied. Check API key permissions"
                    e.message?.contains("HTTP 404") == true -> "Player not found"
                    e.message?.contains("HTTP 429") == true -> "Too many requests"
                    else -> "Error: ${e.message ?: "Unknown error"}"
                }
                Log.e(TAG, errorMsg, e)
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
                Log.d(TAG, "═══════════════════════════════════════\n")
            }
        }
    }

    private fun logAccountResponse(response: RiotAccountResponse) {
        Log.d(TAG, """
            |Account response:
            |PUUID: ${response.puuid}
            |GameName: ${response.gameName}
            |TagLine: ${response.tagLine}
        """.trimMargin())
    }

    private fun logSummonerResponse(response: SummonerResponse) {
        Log.d(TAG, """
            |Summoner response:
            |ID: ${response.id}
            |Name: ${response.name}
            |Level: ${response.summonerLevel}
            |IconID: ${response.profileIconId}
        """.trimMargin())
    }

    private fun logLeagueEntries(entries: List<LeagueEntryResponse>) {
        Log.d(TAG, "League entries (${entries.size}):")
        entries.forEachIndexed { i, entry ->
            Log.d(TAG, """
                |Entry $i:
                |Queue: ${entry.queueType}
                |Tier: ${entry.tier}
                |Rank: ${entry.rank}
                |LP: ${entry.leaguePoints}
                |W/L: ${entry.wins}/${entry.losses}
            """.trimMargin())
        }
    }
}