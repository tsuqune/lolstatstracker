package com.bignerdranch.android.lolstatstracker.network

import android.util.Log
import com.bignerdranch.android.lolstatstracker.Constants
import com.bignerdranch.android.lolstatstracker.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

private const val TAG = "RiotApiService"

private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request()
        Log.d(TAG, "Sending request to: ${request.url}")
        chain.proceed(request)
    }
    .addInterceptor(loggingInterceptor)
    .build()

private val accountRetrofit = Retrofit.Builder()
    .baseUrl(Constants.ACCOUNT_BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val regionalRetrofit = Retrofit.Builder()
    .baseUrl(Constants.REGIONAL_BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val ddragonRetrofit = Retrofit.Builder()
    .baseUrl(Constants.DDRAGON_BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface DDragonService {
    @GET("data/en_US/champion.json")
    suspend fun getAllChampions(): DDragonChampionResponse
}

interface RiotApiService {
    @GET("riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}")
    suspend fun getAccountByRiotId(
        @Path("gameName") gameName: String,
        @Path("tagLine") tagLine: String,
        @Header("X-Riot-Token") apiKey: String
    ): RiotAccountResponse

    @GET("lol/summoner/v4/summoners/by-puuid/{puuid}")
    suspend fun getSummonerByPuuid(
        @Path("puuid") puuid: String,
        @Header("X-Riot-Token") apiKey: String
    ): SummonerResponse

    @GET("lol/league/v4/entries/by-summoner/{summonerId}")
    suspend fun getLeagueEntries(
        @Path("summonerId") summonerId: String,
        @Header("X-Riot-Token") apiKey: String
    ): List<LeagueEntryResponse>

    @GET("lol/champion-mastery/v4/champion-masteries/by-puuid/{puuid}")
    suspend fun getChampionMasteries(
        @Path("puuid") puuid: String,
        @Header("X-Riot-Token") apiKey: String
    ): List<ChampionMasteryResponse>

    @GET("lol/match/v5/matches/by-puuid/{puuid}/ids")
    suspend fun getRankedMatchIds(
        @Path("puuid") puuid: String,
        @Query("count") count: Int,
        @Query("queue") queue: Int,
        @Header("X-Riot-Token") apiKey: String
    ): List<String>

    @GET("lol/match/v5/matches/{matchId}")
    suspend fun getMatchDetails(
        @Path("matchId") matchId: String,
        @Header("X-Riot-Token") apiKey: String
    ): MatchDetailsResponse

    companion object {
        val accountInstance: RiotApiService by lazy {
            accountRetrofit.create(RiotApiService::class.java)
        }

        val regionalInstance: RiotApiService by lazy {
            regionalRetrofit.create(RiotApiService::class.java)
        }

        val ddragonInstance: DDragonService by lazy {
            ddragonRetrofit.create(DDragonService::class.java)
        }
    }
}