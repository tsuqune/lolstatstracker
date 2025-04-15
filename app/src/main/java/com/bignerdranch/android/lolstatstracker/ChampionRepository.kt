//package com.bignerdranch.android.lolstatstracker.repository
//
//import android.util.Log
//import com.bignerdranch.android.lolstatstracker.Constants
//import com.bignerdranch.android.lolstatstracker.model.ChampionDataResponse
//import com.bignerdranch.android.lolstatstracker.network.RiotApiService
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class ChampionRepository @Inject constructor() {
//    private val _championMap = MutableStateFlow<Map<Int, String>?>(null)
//    val championMap: StateFlow<Map<Int, String>?> = _championMap
//
//    suspend fun loadChampionData() {
//        try {
//            val response = RiotApiService.accountInstance.getChampionData(
//                version = Constants.DDRAGON_VERSION
//            )
//
//            val map = mutableMapOf<Int, String>()
//            response.data.forEach { (_, champion) ->
//                map[champion.key.toInt()] = champion.name
//            }
//
//            _championMap.value = map
//            Log.d("ChampionRepository", "Loaded ${map.size} champions")
//        } catch (e: Exception) {
//            Log.e("ChampionRepository", "Failed to load champion data", e)
//            _championMap.value = emptyMap()
//        }
//    }
//
//    fun getChampionNameById(id: Int): String {
//        return championMap.value?.get(id) ?: "Unknown"
//    }
//}