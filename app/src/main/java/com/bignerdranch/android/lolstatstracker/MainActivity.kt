package com.bignerdranch.android.lolstatstracker

import PlayerRepository
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bignerdranch.android.lolstatstracker.screens.InputScreen
import com.bignerdranch.android.lolstatstracker.screens.MainScreen
import com.bignerdranch.android.lolstatstracker.screens.LastGamesScreen
import com.bignerdranch.android.lolstatstracker.ui.theme.LolstatstrackerTheme
import com.bignerdranch.android.lolstatstracker.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { PlayerRepository(database.playerDao()) }
    @SuppressLint("StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LolstatstrackerTheme {
                val viewModel: PlayerViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return PlayerViewModel(repository) as T
                        }
                    }
                )
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

                when (currentScreen) {
                    Screen.Input -> InputScreen(
                        viewModel = viewModel,
                        onSearch = { gameName, tagLine ->
                            viewModel.fetchPlayerData(gameName, tagLine)
                        }
                    )

                    Screen.Main -> MainScreen(
                        viewModel = viewModel,
                        onLastGamesClick = { viewModel.navigateTo(Screen.LastGames) },
                        onLogout = {
                            viewModel.resetNavigation()

                        }
                    )

                    Screen.LastGames -> LastGamesScreen(
                        matches = viewModel.matches.value,
                        onBack = { viewModel.navigateTo(Screen.Main) }
                    )
                }
            }
        }
    }
}

sealed class Screen {
    object Input : Screen()
    object Main : Screen()
    object LastGames : Screen()
}