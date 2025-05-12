package com.bignerdranch.android.lolstatstracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.bignerdranch.android.lolstatstracker.model.ChampionMastery
import com.bignerdranch.android.lolstatstracker.model.MatchData
import com.bignerdranch.android.lolstatstracker.model.PlayerData
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
                    InputScreen(
                        gameName = gameName,
                        tagLine = tagLine,
                        onGameNameChange = { gameName = it },
                        onTagLineChange = { tagLine = it },
                        onSearch = {
                            viewModel.fetchPlayerData(gameName, tagLine)
                            showStatsScreen = true
                        }
                    )
                } else {
                    PlayerStatsScreen(
                        viewModel = viewModel,
                        onBack = { showStatsScreen = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun InputScreen(
    gameName: String,
    tagLine: String,
    onGameNameChange: (String) -> Unit,
    onTagLineChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = gameName,
            onValueChange = onGameNameChange,
            label = { Text("Имя игрока") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Введите игровое имя") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = tagLine,
            onValueChange = onTagLineChange,
            label = { Text("Игровой тег") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Например: RU1") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSearch,
            modifier = Modifier.fillMaxWidth(),
            enabled = gameName.isNotBlank() && tagLine.isNotBlank()
        ) {
            Text("Поиск")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerStatsScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val playerData by viewModel.playerData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val matches by viewModel.matches.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Статистика игрока") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> LoadingScreen()
            error != null -> ErrorScreen(error!!, onBack)
            playerData != null -> PlayerContent(
                playerData = playerData!!,
                matches = matches,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun PlayerContent(
    playerData: PlayerData,
    matches: List<MatchData>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { PlayerHeader(playerData) }
        item { RankInfo(playerData) }
        item { TopChampions(playerData.topChampions) }
        item { MatchListHeader() }
        items(matches) { MatchCard(match = it) }
    }
}

@Composable
private fun PlayerHeader(playerData: PlayerData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = "${Constants.DDRAGON_BASE_URL}img/profileicon/${playerData.profileIconId}.png",
            contentDescription = "Аватар",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )

        Text(
            text = playerData.summonerName,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = "Уровень: ${playerData.summonerLevel}",
            style = MaterialTheme.typography.bodyMedium)

    }
}

@Composable
private fun RankInfo(playerData: PlayerData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val tier = playerData.tier?.lowercase() ?: "unranked"
        AsyncImage(
            model = "https://raw.communitydragon.org/latest/plugins/rcp-fe-lol-static-assets/global/default/images/ranked-mini-crests/$tier.png",
            contentDescription = "Ранг",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        playerData.tier?.let { tier ->
            Text(
                text = tier.replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.titleLarge)
        }

        Text(
            text = buildString {
                playerData.rank?.let { append("$it ") }
                append("${playerData.leaguePoints} LP")
            },
            style = MaterialTheme.typography.bodyLarge)


        playerData.winRate?.let { winRate ->
            Text(
                text = "Винрейт: ${"%.1f".format(winRate)}%",
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TopChampions(champions: List<ChampionMastery>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Топ чемпионы",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            champions.forEach { champion ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(100.dp)
                ) {
                    AsyncImage(
                        model = "${Constants.DDRAGON_BASE_URL}img/champion/${champion.championName?.replace(" ", "")}.png",
                        contentDescription = "Чемпион",
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                    )

                    Text(
                        text = champion.championName ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Ур. ${champion.championLevel}",
                        style = MaterialTheme.typography.labelSmall)

                }
            }
        }
    }
}

@Composable
private fun MatchListHeader() {
    Text(
        text = "Последние матчи",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
fun MatchCard(match: MatchData) {
    val backgroundColor = if (match.win) Color(0xFF27AE60) else Color(0xFFEB5757)

    var championName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(match.championId) {
        championName = ChampionRepository.getChampionName(match.championId)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(80.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "${Constants.DDRAGON_BASE_URL}img/champion/${championName?.replace(" ", "")}.png",
                contentDescription = "Чемпион",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = championName ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium)

                Text(
                    text = "KDA: ${match.kills}/${match.deaths}/${match.assists}",
                    style = MaterialTheme.typography.bodyMedium)

            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = if (match.win) "ПОБЕДА" else "ПОРАЖЕНИЕ",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge)

        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(error: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(error, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Повторить попытку")
        }
    }
}