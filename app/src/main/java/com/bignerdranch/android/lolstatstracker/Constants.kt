package com.bignerdranch.android.lolstatstracker

object Constants {
    const val RIOT_API_KEY = "RGAPI-1abfa8aa-89c0-4328-a4eb-f17d39452073"

    // Для Account API (континентальный)
    const val ACCOUNT_BASE_URL = "https://europe.api.riotgames.com/"

    // Для Summoner и League API (региональный)
    const val REGIONAL_BASE_URL = "https://ru.api.riotgames.com/" // Для RU региона

    private const val DDRAGON_VERSION = "15.9.1" // Добавляем константу для версии
    const val DDRAGON_BASE_URL = "https://ddragon.leagueoflegends.com/cdn/${DDRAGON_VERSION}/"
}