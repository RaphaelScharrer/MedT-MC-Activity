package at.htl.activitiy_android.view.playfield

import android.content.Intent
import android.content.res.Configuration
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import at.htl.activitiy_android.domain.model.Player
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

    val selectedTeamPlayers = remember(selectedTeam, state.players) {
        val teamId = selectedTeam?.id ?: return@remember emptyList()
        state.players.filter { it.team == teamId }
    }

    LaunchedEffect(Unit) {
        vm.loadBoardState()
    }

    LaunchedEffect(state.finishedTeamIds.size) {
        val activeTeams = state.teams.filter { it.id !in state.finishedTeamIds }
        if (activeTeams.size <= 1 && state.teams.isNotEmpty()) {
            val intent = Intent(context, EndGameActivity::class.java)
            context.startActivity(intent)
        }
    }

    LaunchedEffect(state.saveCompleted) {
        if (state.saveCompleted) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("RESET_TO_GAME_MODE", true)
            }
            context.startActivity(intent)
        }
    }

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
                    Text("Beenden")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirmDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    selectedTeam?.let { team ->
        TeamInfoDialog(
            team = team,
            players = selectedTeamPlayers,
            onDismiss = { selectedTeam = null }
        )
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val gridCols = if (isLandscape) 5 else 3
    val gridRows = if (isLandscape) 3 else 5

    Column(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                StartField(
                    teamsOnStart = state.teams.filter {
                        (state.teamBoardPositions[it.id] ?: 0) == 0
                    },
                    onTeamClick = { selectedTeam = it },
                    isLandscape = isLandscape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(4.dp)
                )

                for (row in 0 until gridRows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        for (col in 0 until gridCols) {
                            val fieldIndex = row * gridCols + col
                            val boardPosition = fieldIndex + 1
                            val teamsOnField = state.teams.filter {
                                (state.teamBoardPositions[it.id] ?: 0) == boardPosition
                            }
                            GameField(
                                fieldIndex = fieldIndex,
                                teamsOnField = teamsOnField,
                                onTeamClick = { selectedTeam = it },
                                isLandscape = isLandscape,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                GoalField(
                    teamsAtGoal = state.teams.filter {
                        (state.teamBoardPositions[it.id] ?: 0) >= 16
                    },
                    onTeamClick = { selectedTeam = it },
                    isLandscape = isLandscape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(4.dp)
                )
            }

        }

        // ── Bottom Bar ───────────────────────────────────────────────────────
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
                            text = "Beenden",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ── Spielfeld-Composables ────────────────────────────────────────────────────

@Composable
fun TeamInfoDialog(
    team: Team,
    players: List<Player>,
    onDismiss: () -> Unit
) {
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
                Image(
                    painter = painterResource(id = team.imageRes),
                    contentDescription = team.label,
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = team.label,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider()
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
    isLandscape: Boolean = false,
    modifier: Modifier = Modifier
) {
    val iconRotation = 0f
    Box(
        modifier = modifier
            .background(color = Color(0xFFE8DEF8), shape = RoundedCornerShape(8.dp)),
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
                Row(horizontalArrangement = Arrangement.Center) {
                    teamsOnStart.forEach { team ->
                        Image(
                            painter = painterResource(id = team.imageRes),
                            contentDescription = team.label,
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer { rotationZ = iconRotation }
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
    isLandscape: Boolean = false,
    modifier: Modifier = Modifier
) {
    val iconRotation = 0f
    Box(
        modifier = modifier
            .background(color = Color(0xFFE8DEF8), shape = RoundedCornerShape(8.dp)),
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
                Row(horizontalArrangement = Arrangement.Center) {
                    teamsAtGoal.forEach { team ->
                        Image(
                            painter = painterResource(id = team.imageRes),
                            contentDescription = team.label,
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer { rotationZ = iconRotation }
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
    isLandscape: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgColor = when (fieldIndex % 3) {
        0    -> Color(0xFFF09BAA)   // Rot  – Pantomime
        1    -> Color(0xFF99B4F2)   // Blau – Erklären
        else -> Color(0xFFB8F599)   // Grün – Zeichnen
    }
    val iconRes = when (fieldIndex % 3) {
        0    -> R.drawable.ic_1
        1    -> R.drawable.ic_2
        else -> R.drawable.ic_3
    }
    val fieldNumber = fieldIndex + 1
    val iconRotation = 0f

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Kategorie-Icon (rotiert im Landscape-Modus)
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .graphicsLayer { rotationZ = iconRotation },
            contentScale = ContentScale.Fit
        )
        // Feldnummer oben links
        Text(
            text = fieldNumber.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.45f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(3.dp)
        )
        // Spielfiguren unten (rotiert im Landscape-Modus)
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
                            .size(28.dp)
                            .graphicsLayer { rotationZ = iconRotation }
                            .clickable { onTeamClick(team) },
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(2.dp))
                }
            }
        }
    }
}
