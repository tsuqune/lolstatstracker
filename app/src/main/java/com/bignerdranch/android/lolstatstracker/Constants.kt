package com.bignerdranch.android.lolstatstracker

object Constants {
    const val RIOT_API_KEY = "RGAPI-50688e87-dab8-4c13-965e-325b5ed6decd"

    // Для Account API (континентальный)
    const val ACCOUNT_BASE_URL = "https://europe.api.riotgames.com/"

    // Для Summoner и League API (региональный)
    const val REGIONAL_BASE_URL = "https://ru.api.riotgames.com/" // Для RU региона

    private const val DDRAGON_VERSION = "15.9.1" // Добавляем константу для версии
    const val DDRAGON_BASE_URL = "https://ddragon.leagueoflegends.com/cdn/${DDRAGON_VERSION}/"
}