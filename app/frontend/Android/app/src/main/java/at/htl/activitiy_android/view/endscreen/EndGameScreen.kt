package at.htl.activitiy_android.view.endscreen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🏆", fontSize = 56.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Spiel beendet!",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Rangliste",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Rankings
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(state.rankedTeams, key = { _, pair -> pair.first.id ?: 0 }) { index, (team, position) ->
                val teamPlayers = state.allPlayers.filter { it.team == team.id }
                val rank = index + 1

                // Medal colors
                val medalEmoji = when (rank) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> null }
                val cardColor = when (rank) {
                    1 -> Color(0xFF7A5C00)   // gold tint
                    2 -> Color(0xFF4A5060)   // silver tint
                    3 -> Color(0xFF5A3820)   // bronze tint
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val borderColor = when (rank) {
                    1 -> Color(0xFFFFD700)
                    2 -> Color(0xFFB0BEC5)
                    3 -> Color(0xFFCD7F32)
                    else -> Color.Transparent
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (rank == 1) 8.dp else 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Team Header with Rank
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Rank badge
                            if (medalEmoji != null) {
                                Text(
                                    text = medalEmoji,
                                    fontSize = if (rank == 1) 32.sp else 26.sp,
                                    modifier = Modifier.width(44.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$rank",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                            }

                            Image(
                                painter = painterResource(id = team.imageRes),
                                contentDescription = null,
                                modifier = Modifier.size(if (rank == 1) 40.dp else 32.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = team.label,
                                    fontSize = if (rank == 1) 20.sp else 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Feld $position  •  ${teamPlayers.size} Spieler",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Players in this team
                        if (teamPlayers.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Spacer(Modifier.height(8.dp))

                            teamPlayers.forEach { player ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = team.imageRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        player.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${player.pointsEarned ?: 0} Pkt.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (rank == 1) Color(0xFFFFD700) else Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Button zum GameModeScreen
        Spacer(Modifier.height(12.dp))
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
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(
                text = "Neues Spiel starten",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}