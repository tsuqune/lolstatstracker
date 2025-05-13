package com.bignerdranch.android.lolstatstracker.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bignerdranch.android.lolstatstracker.Constants
import com.bignerdranch.android.lolstatstracker.model.ChampionMastery
import com.bignerdranch.android.lolstatstracker.model.PlayerData
import com.bignerdranch.android.lolstatstracker.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PlayerViewModel,
    onLastGamesClick: () -> Unit,
    onLogout: () -> Unit
) {
    val playerData by viewModel.playerData.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Главный экран") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Выйти",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> LoadingScreen()
            playerData != null -> MainContent(
                playerData = playerData!!,
                onLastGamesClick = onLastGamesClick,
                onLogout = onLogout,
                modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun MainContent(
    playerData: PlayerData,
    onLastGamesClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
        ){
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = "${Constants.DDRAGON_BASE_URL}img/profileicon/${playerData.profileIconId}.png",
                    contentDescription = "Аватар",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = playerData.summonerName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Уровень: ${playerData.summonerLevel}",
                        fontSize = 14.sp
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(24.dp))

        // Ранг
        RankSection(playerData)

        Spacer(modifier = Modifier.height(24.dp))

        // Топ чемпионы
        ChampionMasterySection(playerData.topChampions)

        Spacer(modifier = Modifier.height(24.dp))

        // Кнопки
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onLastGamesClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Последние игры")
            }
        }
    }
}

@Composable
private fun RankSection(playerData: PlayerData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tier = playerData.tier?.lowercase() ?: "unranked"
            AsyncImage(
                model = "https://raw.communitydragon.org/latest/plugins/rcp-fe-lol-static-assets/global/default/images/ranked-mini-crests/$tier.png",
                contentDescription = "Ранг",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column {
                playerData.tier?.let {
                    Text(
                        text = "${it.replaceFirstChar { char -> char.titlecase() }} ${playerData.rank}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${playerData.leaguePoints} LP",
                    fontSize = 14.sp
                )

                playerData.winRate?.let {
                    Text(
                        text = "Винрейт: ${"%.1f".format(it)}%",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ChampionMasterySection(champions: List<ChampionMastery>) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ){
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Топ мастерство",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                champions.take(3).forEach { champion ->
                    ChampionItem(champion)
                }
            }
        }
    }

}

@Composable
private fun ChampionItem(champion: ChampionMastery) {


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        AsyncImage(
            model = "${Constants.DDRAGON_BASE_URL}img/champion/${champion.championName?.replace(" ", "")}.png",
            contentDescription = "Чемпион",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
        )
        Text(
            text = champion.championName ?: "Unknown",
            fontSize = 12.sp,
            maxLines = 1
        )
        Text(
            text = "Ур. ${champion.championLevel}",
            fontSize = 10.sp
        )
    }
}