package com.bignerdranch.android.lolstatstracker.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.lolstatstracker.Screen
import com.bignerdranch.android.lolstatstracker.viewmodel.PlayerViewModel

@Composable
fun InputScreen(
    viewModel: PlayerViewModel, // Добавляем параметр
    onSearch: (String, String) -> Unit
) {
    var gameName by remember { mutableStateOf("") }
    var tagLine by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Текстовые поля в карточках как в MainScreen
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = gameName,
                        onValueChange = { gameName = it },
                        label = { Text("Имя игрока") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Введите игровое имя") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = tagLine,
                        onValueChange = { tagLine = it },
                        label = { Text("Игровой тег") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Например: RU1") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка в стиле MainScreen
            Button(
                onClick = { onSearch(gameName, tagLine)
                    viewModel.navigateTo(Screen.Main)},
                modifier = Modifier.fillMaxWidth(),
                enabled = gameName.isNotBlank() && tagLine.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Поиск", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}