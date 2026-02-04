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
import at.htl.activitiy_android.view.gamegeneration.GameGenerationScreen
import at.htl.activitiy_android.view.gamegeneration.GameGenerationViewModel
import at.htl.activitiy_android.view.gameplay.GamePlayScreen
import at.htl.activitiy_android.view.gamesummary.GameSummaryScreen
import at.htl.activitiy_android.view.teamgeneration.TeamGenerationEvent
import at.htl.activitiy_android.view.teamgeneration.TeamGenerationScreen
import at.htl.activitiy_android.view.teamgeneration.TeamGenerationViewModel
import at.htl.activitiy_android.view.teamgeneration.TeamGenerationViewModelFactory
import at.htl.activitiy_android.view.teamselect.PlayerCreationScreen

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
    var currentScreen by remember { mutableStateOf<Screen>(Screen.GameGeneration) }
    var currentGameId by remember { mutableStateOf<Long?>(null) }
    var currentTeamCount by remember { mutableStateOf<Int?>(null) }

    when (currentScreen) {
        Screen.GameGeneration -> {
            val gameVm: GameGenerationViewModel = viewModel()

            // Only load if gameId exists and screen is visible
            LaunchedEffect(currentGameId, currentScreen) {
                if (currentScreen == Screen.GameGeneration && currentGameId != null) {
                    gameVm.loadGame(currentGameId!!)
                }
            }

            GameGenerationScreen(
                onGameCreated = { gameId ->
                    currentGameId = gameId
                    currentScreen = Screen.TeamGeneration
                },
                vm = gameVm
            )
        }

        Screen.TeamGeneration -> {
            currentGameId?.let { gameId ->
                val teamVm: TeamGenerationViewModel = viewModel(
                    key = "team_$gameId",  // Unique key per game
                    factory = TeamGenerationViewModelFactory(gameId)
                )

                // Only load if teamCount exists
                LaunchedEffect(currentTeamCount, currentScreen) {
                    if (currentScreen == Screen.TeamGeneration && currentTeamCount != null) {
                        teamVm.onEvent(TeamGenerationEvent.TeamCountChanged(currentTeamCount.toString()))
                    }
                }

                TeamGenerationScreen(
                    gameId = gameId,
                    onTeamsCreated = {
                        currentTeamCount = teamVm.state.value.teamCountInput.toIntOrNull()
                        currentScreen = Screen.PlayerCreation
                    },
                    onBack = {
                        currentScreen = Screen.GameGeneration
                    },
                    vm = teamVm
                )
            }
        }

        Screen.PlayerCreation -> {
            currentGameId?.let { gameId ->
                PlayerCreationScreen(
                    gameId = gameId,
                    onBack = {
                        currentScreen = Screen.TeamGeneration
                    },
                    onFinish = {
                        currentScreen = Screen.GameSummary
                    }
                )
            }
        }

        Screen.GameSummary -> {
            currentGameId?.let { gameId ->
                GameSummaryScreen(
                    gameId = gameId,
                    onBack = {
                        currentScreen = Screen.PlayerCreation
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
    data object GameGeneration : Screen()
    data object TeamGeneration : Screen()
    data object PlayerCreation : Screen()
    data object GameSummary : Screen()
    data object GamePlay : Screen()
}