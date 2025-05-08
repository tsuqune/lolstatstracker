package com.bignerdranch.android.lolstatstracker

import android.util.Log
import com.bignerdranch.android.lolstatstracker.network.RiotApiService

object ChampionRepository {
    private var championsCache: Map<Long, String>? = null
    private var lastFetchTime: Long = 0
    private const val CACHE_DURATION = 86400000 // 24 часа

    suspend fun getChampionName(key: Long): String? {
        val currentTime = System.currentTimeMillis()

        if (championsCache == null || currentTime - lastFetchTime > CACHE_DURATION) {
            try {
                val response = RiotApiService.ddragonInstance.getAllChampions()
                championsCache = response.data.values.associate {
                    it.key.toLong() to it.name
                }
                lastFetchTime = currentTime
            } catch (e: Exception) {
                Log.e("ChampionRepo", "Error fetching champions", e)
                return null
            }
        }

        return championsCache?.get(key) ?: "Unknown Champion"
    }
}