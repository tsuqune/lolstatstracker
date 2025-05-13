package com.bignerdranch.android.lolstatstracker

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bignerdranch.android.lolstatstracker.screens.InputScreen
import com.bignerdranch.android.lolstatstracker.screens.MainScreen
import com.bignerdranch.android.lolstatstracker.screens.LastGamesScreen
import com.bignerdranch.android.lolstatstracker.ui.theme.LolstatstrackerTheme
import com.bignerdranch.android.lolstatstracker.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {
    @SuppressLint("StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LolstatstrackerTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Input) }
                val viewModel: PlayerViewModel = viewModel()

                when (currentScreen) {
                    Screen.Input -> InputScreen(
                        onSearch = { gameName, tagLine ->
                            viewModel.fetchPlayerData(gameName, tagLine)
                            currentScreen = Screen.Main
                        }
                    )

                    Screen.Main -> MainScreen(
                        viewModel = viewModel,
                        onLastGamesClick = { currentScreen = Screen.LastGames },
                        onLogout = { currentScreen = Screen.Input }
                    )

                    Screen.LastGames -> LastGamesScreen(
                        matches = viewModel.matches.value,
                        onBack = { currentScreen = Screen.Main }
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