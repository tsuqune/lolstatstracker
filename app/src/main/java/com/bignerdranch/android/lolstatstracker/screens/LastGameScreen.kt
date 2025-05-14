package com.bignerdranch.android.lolstatstracker.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bignerdranch.android.lolstatstracker.repository.ChampionRepository
import com.bignerdranch.android.lolstatstracker.Constants
import com.bignerdranch.android.lolstatstracker.model.MatchData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LastGamesScreen(
    matches: List<MatchData>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Последние игры") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(matches) { match ->
                MatchItem(match = match)
            }
        }
    }
}

@Composable
private fun MatchItem(match: MatchData) {
    val backgroundColor = if (match.win) Color(0xFF27AE60) else Color(0xFFEB5757)

    var championName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(match.championId) {
        championName = ChampionRepository.getChampionName(match.championId)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "${Constants.DDRAGON_BASE_URL}img/champion/${championName?.replace(" ", "")}.png",
                contentDescription = "Чемпион",
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "KDA: ${match.kills}/${match.deaths}/${match.assists}",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = if (match.win) "ПОБЕДА" else "ПОРАЖЕНИЕ",
                color = Color.White
            )
        }
    }
}