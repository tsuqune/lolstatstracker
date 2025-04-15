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
                model = "${Constants.DDRAGON_BASE_URL}15.7.1/img/profileicon/${playerData!!.profileIconId}.png",
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
                Text("Win Rate: ${"%.1f".format(it)}%")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onBack) {
                Text("Назад")
            }
        }
    }
}