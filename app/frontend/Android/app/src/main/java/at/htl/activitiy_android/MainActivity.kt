package at.htl.activitiy_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import at.htl.activitiy_android.data.repository.GameRepository
import at.htl.activitiy_android.view.gamegeneration.GameGenerationScreen
import at.htl.activitiy_android.view.gamegeneration.GameGenerationViewModel
import at.htl.activitiy_android.view.gamemode.GameModeScreen
import at.htl.activitiy_android.view.gameplay.GamePlayScreen
import at.htl.activitiy_android.view.gamesummary.GameSummaryScreen
import at.htl.activitiy_android.view.playerteamsetup.PlayerTeamSetupScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.GameMode) }
    var currentGameName by remember { mutableStateOf("") }
    var currentGameId by remember { mutableStateOf<Long?>(null) }

    when (currentScreen) {
        Screen.GameMode -> {
            GameModeScreen(
                onLocalSelected = {
                    currentScreen = Screen.GameGeneration
                }
            )
        }

        Screen.GameGeneration -> {
            val gameVm: GameGenerationViewModel = viewModel()

            GameGenerationScreen(
                onGameCreated = { _ -> },
                onNameConfirmed = { name ->
                    // Spielname nur lokal merken, NICHT in DB speichern
                    // Persistierung passiert erst im PlayerTeamSetup nach Bestätigung
                    currentGameName = name
                    currentScreen = Screen.PlayerTeamSetup
                },
                vm = gameVm
            )
        }

        Screen.PlayerTeamSetup -> {
            PlayerTeamSetupScreen(
                gameName = currentGameName,
                onBack = {
                    currentScreen = Screen.GameGeneration
                },
                onConfirmed = {
                    // gameId aus Repository holen (wurde beim Persistieren gesetzt)
                    currentGameId = GameRepository.getCurrentGameId()
                    currentScreen = Screen.GameSummary
                }
            )
        }

        Screen.GameSummary -> {
            currentGameId?.let { gameId ->
                GameSummaryScreen(
                    gameId = gameId,
                    onBack = {
                        // Ab hier KEIN Zurück zur Teamerstellung möglich
                        // onBack bleibt leer - kein Zurücknavigieren
                    },
                    onStartGame = {
                        currentScreen = Screen.GamePlay
                    }
                )
            }
        }

        Screen.GamePlay -> {
            currentGameId?.let { gameId ->
                GamePlayScreen(
                    gameId = gameId,
                    onBack = {
                        currentScreen = Screen.GameSummary
                    }
                )
            }
        }
    }
}

sealed class Screen {
    data object GameMode : Screen()
    data object GameGeneration : Screen()
    data object PlayerTeamSetup : Screen()
    data object GameSummary : Screen()
    data object GamePlay : Screen()
}
