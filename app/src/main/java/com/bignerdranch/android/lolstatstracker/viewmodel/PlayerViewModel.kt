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

private const val TAG = "PlayerViewModel" // Тег для логов

// ViewModel для работы с данными игрока
class PlayerViewModel : ViewModel() {
    // Поток данных игрока (MutableStateFlow для внутреннего использования)
    private val _playerData = MutableStateFlow<PlayerData?>(null)
    // Публичная версия потока данных игрока (только для чтения)
    val playerData: StateFlow<PlayerData?> = _playerData

    // Поток состояния загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Поток ошибок
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Основная функция для получения данных игрока
    fun fetchPlayerData(gameName: String, tagLine: String) {
        viewModelScope.launch { // Запуск корутины в scope ViewModel
            _isLoading.value = true // Устанавливаем состояние загрузки
            _error.value = null // Сбрасываем ошибки

            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "Starting fetch for: $gameName#$tagLine")

            try {
                // Шаг 1: Получение аккаунта через континентальный API
                Log.d(TAG, "Step 1: Fetching account by Riot ID...")
                val account = try {
                    RiotApiService.accountInstance.getAccountByRiotId(
                        gameName = gameName,
                        tagLine = tagLine,
                        apiKey = Constants.RIOT_API_KEY
                    ).also { // Логируем полученные данные
                        logAccountResponse(it)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Account fetch failed", e)
                    throw Exception("Failed to get account: ${e.message}")
                }

                // Шаг 2: Получение summoner через региональный API (по PUUID)
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

                // Шаг 3: Получение ранговой статистики через региональный API
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

                // Шаг 4: Обработка и преобразование данных
                Log.d(TAG, "Step 4: Processing data...")
                // Находим данные для одиночной очереди (SOLO)
                val soloQueue = leagueEntries.find { it.queueType == "RANKED_SOLO_5x5" }

                val topChampions = championMasteries
                    .sortedByDescending { it.championPoints }
                    .take(3)
                    .map {
                        ChampionMastery(
                            championId = it.championId,
                            championLevel = it.championLevel,
                            championPoints = it.championPoints,
                            lastPlayTime = it.lastPlayTime
                        )
                    }

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
                Log.d(TAG, "✔ All data fetched successfully")

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
                _isLoading.value = false // Завершаем загрузку в любом случае
                Log.d(TAG, "═══════════════════════════════════════\n")
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
            |Account response:
            |PUUID: ${response.puuid}
            |GameName: ${response.gameName}
            |TagLine: ${response.tagLine}
        """.trimMargin())
    }

    // Вспомогательная функция для логирования данных summoner
    private fun logSummonerResponse(response: SummonerResponse) {
        Log.d(TAG, """
            |Summoner response:
            |ID: ${response.id}
            |Level: ${response.summonerLevel}
            |IconID: ${response.profileIconId}
        """.trimMargin())
    }

    // Вспомогательная функция для логирования данных ранговой статистики
    private fun logLeagueEntries(entries: List<LeagueEntryResponse>) {
        Log.d(TAG, "League entries (${entries.size}):")
        entries.forEachIndexed { i, entry ->
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
                |Champion ID: ${m.championId}
                |Level: ${m.championLevel}
                |Points: ${m.championPoints}
                |Last played: ${Date(m.lastPlayTime)}
            """.trimMargin())
        }
    }
}