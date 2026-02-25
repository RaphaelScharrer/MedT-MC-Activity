package at.htl.activitiy_android.view.playfield

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import at.htl.activitiy_android.MainActivity
import at.htl.activitiy_android.R
import at.htl.activitiy_android.ui.theme.ActivitiyAndroidTheme
import at.htl.activitiy_android.domain.model.Team
import at.htl.activitiy_android.view.endscreen.EndGameActivity
import at.htl.activitiy_android.view.gameplay.GamePlayActivity

class GameBoardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameId = intent.getLongExtra(EXTRA_GAME_ID, -1L)

        setContent {
            ActivitiyAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameBoardScreen(gameId = gameId)
                }
            }
        }
    }

    companion object {
        const val EXTRA_GAME_ID = "extra_game_id"
    }
}

@Composable
fun GameBoardScreen(
    gameId: Long,
    vm: GameBoardViewModel = viewModel(
        factory = GameBoardViewModelFactory(gameId)
    )
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var selectedTeam by remember { mutableStateOf<Team?>(null) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.loadBoardState()
    }

    // Navigate to EndGameScreen when only 1 team is left
    LaunchedEffect(state.finishedTeamIds.size) {
        val activeTeams = state.teams.filter { it.id !in state.finishedTeamIds }
        if (activeTeams.size <= 1 && state.teams.isNotEmpty()) {
            val intent = Intent(context, EndGameActivity::class.java)
            context.startActivity(intent)
        }
    }

    // Navigate to start screen after save completed
    LaunchedEffect(state.saveCompleted) {
        if (state.saveCompleted) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("RESET_TO_GAME_MODE", true)
            }
            context.startActivity(intent)
        }
    }

    // Bestätigungs-Dialog für Beenden & Speichern
    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            title = {
                Text(
                    text = "Spiel beenden?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Der aktuelle Spielstand wird gespeichert und du kehrst zum Startbildschirm zurück.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveConfirmDialog = false
                        vm.saveAndExit()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Beenden & Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirmDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Team Info Dialog
    selectedTeam?.let { team ->
        TeamInfoDialog(
            team = team,
            onDismiss = { selectedTeam = null }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Game Board Grid
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // START field
            StartField(
                teamsOnStart = state.teams.filter {
                    (state.teamBoardPositions[it.id] ?: 0) == 0
                },
                onTeamClick = { selectedTeam = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(4.dp)
            )

            // 15 game fields in 5 rows of 3
            for (row in 0 until 5) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (col in 0 until 3) {
                        val fieldIndex = row * 3 + col
                        val boardPosition = fieldIndex + 1 // board positions 1-15

                        val teamsOnField = state.teams.filter {
                            val pos = state.teamBoardPositions[it.id] ?: 0
                            pos == boardPosition
                        }

                        GameField(
                            fieldIndex = fieldIndex,
                            teamsOnField = teamsOnField,
                            onTeamClick = { selectedTeam = it },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                        )
                    }
                }
            }

            // GOAL field
            GoalField(
                teamsAtGoal = state.teams.filter {
                    (state.teamBoardPositions[it.id] ?: 0) >= 16
                },
                onTeamClick = { selectedTeam = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(4.dp)
            )
        }

        // Bottom Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val activeTeams = state.teams.filter { it.id !in state.finishedTeamIds }

                Button(
                    onClick = {
                        val intent = Intent(context, GamePlayActivity::class.java)
                        intent.putExtra(GamePlayActivity.EXTRA_GAME_ID, gameId)
                        context.startActivity(intent)
                    },
                    enabled = activeTeams.isNotEmpty() && !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Runde starten",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { showSaveConfirmDialog = true },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Speichern...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Beenden & Speichern",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeamInfoDialog(
    team: Team,
    onDismiss: () -> Unit
) {
    val repository = at.htl.activitiy_android.data.repository.GameRepository

    // Load players based on playerIds from team
    val players = remember(team.id) {
        val playerIds = team.playerIds ?: emptyList()
        playerIds.mapNotNull { playerId ->
            repository.currentSession.value.players.find { it.id == playerId }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Team Icon
                Image(
                    painter = painterResource(id = team.imageRes),
                    contentDescription = team.label,
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit
                )

                // Team Name
                Text(
                    text = team.label,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider()

                // Team Members
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Teammitglieder:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (players.isEmpty()) {
                        Text(
                            text = "Keine Spieler im Team",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        players.forEach { player ->
                            PlayerItem(playerName = player.name)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Close Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Schließen")
                }
            }
        }
    }
}

@Composable
fun PlayerItem(playerName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = playerName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun StartField(
    teamsOnStart: List<Team>,
    onTeamClick: (Team) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Color(0xFFE8DEF8),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "START",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )
            if (teamsOnStart.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    teamsOnStart.forEach { team ->
                        Image(
                            painter = painterResource(id = team.imageRes),
                            contentDescription = team.label,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onTeamClick(team) },
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GoalField(
    teamsAtGoal: List<Team>,
    onTeamClick: (Team) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Color(0xFFE8DEF8),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ZIEL",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )
            if (teamsAtGoal.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    teamsAtGoal.forEach { team ->
                        Image(
                            painter = painterResource(id = team.imageRes),
                            contentDescription = team.label,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onTeamClick(team) },
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GameField(
    fieldIndex: Int,
    teamsOnField: List<Team>,
    onTeamClick: (Team) -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when (fieldIndex % 3) {
        0 -> Color(0xFFF09BAA)  // Rot - Zeichnen
        1 -> Color(0xFF99B4F2)  // Blau - Erklaeren
        else -> Color(0xFFB8F599)  // Gruen - Pantomime
    }
    val iconRes = when (fieldIndex % 3) {
        0 -> R.drawable.ic_1  // Zeichnen
        1 -> R.drawable.ic_2  // Erklaeren
        else -> R.drawable.ic_3  // Pantomime
    }

    Box(
        modifier = modifier
            .background(
                color = bgColor,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Category icon
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentScale = ContentScale.Fit
        )

        // Team icons overlay at bottom
        if (teamsOnField.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                teamsOnField.forEach { team ->
                    Image(
                        painter = painterResource(id = team.imageRes),
                        contentDescription = team.label,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onTeamClick(team) },
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(2.dp))
                }
            }
        }
    }
}