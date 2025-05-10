package com.bignerdranch.android.lolstatstracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.bignerdranch.android.lolstatstracker.ui.theme.LolstatstrackerTheme
import com.bignerdranch.android.lolstatstracker.viewmodel.PlayerViewModel
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextOverflow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LolstatstrackerTheme {
                var showStatsScreen by remember { mutableStateOf(false) }
                var gameName by remember { mutableStateOf("") }
                var tagLine by remember { mutableStateOf("") }
                val viewModel: PlayerViewModel = viewModel()

                if (!showStatsScreen) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextField(
                            value = gameName,
                            onValueChange = { gameName = it },
                            label = { Text("Имя игрока") }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TextField(
                            value = tagLine,
                            onValueChange = { tagLine = it },
                            label = { Text("Тег (например: RU1)") }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                viewModel.fetchPlayerData(gameName, tagLine)
                                showStatsScreen = true
                            },
                            enabled = gameName.isNotBlank() && tagLine.isNotBlank()
                        ) {
                            Text("Поиск")
                        }
                    }
                } else {
                    PlayerStatsScreen(viewModel) {
                        showStatsScreen = false
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerStatsScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val playerData by viewModel.playerData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
            Button(onClick = onBack) {
                Text("Назад")
            }
        } else if (playerData != null) {
            AsyncImage(
                model = "${Constants.DDRAGON_BASE_URL}img/profileicon/${playerData!!.profileIconId}.png",
                contentDescription = "Profile Icon",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            Text(playerData!!.summonerName, style = MaterialTheme.typography.headlineMedium)
            Text("Уровень: ${playerData!!.summonerLevel}")

            Spacer(modifier = Modifier.height(16.dp))

            val tierLower = playerData!!.tier?.lowercase() ?: "unranked"
            AsyncImage(
                model = "https://raw.communitydragon.org/latest/plugins/rcp-fe-lol-static-assets/global/default/images/ranked-mini-crests/${tierLower}.png",
                contentDescription = "Rank Icon",
                modifier = Modifier
                    .size(70.dp)
            )

            playerData!!.tier?.let { tier ->
                playerData!!.rank?.let { rank ->
                    Text("Ранг: $tier $rank")
                }
            }

            playerData!!.leaguePoints.let { leaguePoints ->
                Text("LP: $leaguePoints")
            }

            playerData!!.winRate?.let {
                Text("Ranked win Rate: ${"%.1f".format(it)}%")
            }

            playerData!!.wins?.let{ wins ->
                playerData!!.losses?.let{ losses ->
                    Text(
                        "Ranked W/L: $wins/$losses",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally // Центрируем всю колонку
            ) {
                Text(
                    "Топ чемпионы:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center, // Центрируем элементы в ряду
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    playerData!!.topChampions.forEach { champion ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(100.dp) // Немного увеличим ширину для лучшего отображения
                                .padding(horizontal = 8.dp)
                        ) {
                            AsyncImage(
                                model = "${Constants.DDRAGON_BASE_URL}img/champion/${champion.championName?.replace(" ", "")}.png",
                                contentDescription = "Champion Image",
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = champion.championName ?: "Unknown",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                "Ур. ${champion.championLevel}",
                                style = MaterialTheme.typography.labelSmall
                            )

                            Text(
                                "${champion.championPoints} pts",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
            Button(onClick = onBack) {
                Text("Назад")
            }
        }
    }
}