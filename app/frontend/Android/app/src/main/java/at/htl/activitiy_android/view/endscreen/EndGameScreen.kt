package at.htl.activitiy_android.view.endscreen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import at.htl.activitiy_android.MainActivity
import at.htl.activitiy_android.ui.theme.ActivitiyAndroidTheme
import at.htl.activitiy_android.data.repository.GameRepository
import at.htl.activitiy_android.domain.model.Player
import at.htl.activitiy_android.domain.model.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class EndGameState(
    val rankedTeams: List<Pair<Team, Int>> = emptyList(), // Team + Position
    val allPlayers: List<Player> = emptyList()
)

class EndGameViewModel : ViewModel() {
    private val repository = GameRepository

    private val _state = MutableStateFlow(EndGameState())
    val state: StateFlow<EndGameState> = _state

    fun loadFinalRankings() {
        val session = repository.currentSession.value
        val teams = session.teams
        val positions = session.teamBoardPositions

        // Sort teams by their board position (highest first)
        val rankedTeams = teams
            .map { team -> team to (positions[team.id] ?: 0) }
            .sortedByDescending { it.second }

        _state.update {
            EndGameState(
                rankedTeams = rankedTeams,
                allPlayers = session.players
            )
        }
    }
}

class EndGameViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EndGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EndGameViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class EndGameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ActivitiyAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EndGameContent()
                }
            }
        }
    }
}

@Composable
fun EndGameContent(
    vm: EndGameViewModel = viewModel(factory = EndGameViewModelFactory())
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.loadFinalRankings()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Spiel Ende",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Rankings
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Rangliste",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(state.rankedTeams, key = { _, pair -> pair.first.id ?: 0 }) { index, (team, position) ->
                val teamPlayers = state.allPlayers.filter { it.team == team.id }
                val rank = index + 1

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (rank) {
                            1 -> MaterialTheme.colorScheme.primaryContainer
                            2 -> MaterialTheme.colorScheme.secondaryContainer
                            3 -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Team Header with Rank
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Rank Badge
                            Text(
                                text = "$rank.",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(48.dp)
                            )

                            Image(
                                painter = painterResource(id = team.imageRes),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = team.label,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Position: $position | ${teamPlayers.size} Spieler",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Players in this team
                        if (teamPlayers.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(8.dp))

                            teamPlayers.forEach { player ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = team.imageRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        player.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        "${player.pointsEarned} Punkte",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        } else {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Keine Spieler in diesem Team",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Button zum GameModeScreen
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra("RESET_TO_GAME_MODE", true)
                }
                context.startActivity(intent)
                (context as? ComponentActivity)?.finish()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Neues Spiel starten",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}