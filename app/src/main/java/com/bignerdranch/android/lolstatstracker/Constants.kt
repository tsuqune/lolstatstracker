package com.bignerdranch.android.lolstatstracker

object Constants {
    const val RIOT_API_KEY = "RGAPI-a5651738-d8c5-4f34-adbf-78869bb2377e"

    // Для Account API (континентальный)
    const val ACCOUNT_BASE_URL = "https://europe.api.riotgames.com/"

    // Для Summoner и League API (региональный)
    const val REGIONAL_BASE_URL = "https://ru.api.riotgames.com/" // Для RU региона

    const val DDRAGON_VERSION = "15.9.1" // Добавляем константу для версии
    const val DDRAGON_BASE_URL = "https://ddragon.leagueoflegends.com/cdn/${DDRAGON_VERSION}/"
}