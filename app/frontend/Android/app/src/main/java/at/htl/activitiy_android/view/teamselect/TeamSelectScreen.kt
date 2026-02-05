package at.htl.activitiy_android.view.teamselect

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.htl.activitiy_android.domain.model.PlayerWithTeam
import at.htl.activitiy_android.domain.model.Team
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerCreationScreen(
    gameId: Long,
    onBack: () -> Unit = {},
    onFinish: () -> Unit = {},
    vm: TeamSelectViewModel = viewModel(
        factory = TeamSelectViewModelFactory(gameId)
    )
) {
    val state by vm.state.collectAsState()

    // Auto-fade success messages after 3 seconds
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            delay(3000)
            vm.onEvent(TeamSelectEvent.ClearMessages)
        }
    }

    // Load data when screen starts
    LaunchedEffect(Unit) {
        vm.onEvent(TeamSelectEvent.LoadData)

    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teams") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = onFinish,
                        enabled = !state.isLoading && state.teams.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Weiter")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // Team Selection Dropdown
                if (state.teams.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    val selectedTeam = state.teams.find { it.id == state.selectedTeamId }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedTeam?.label ?: "Team wählen",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Standard-Team") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            state.teams.forEach { team ->
                                DropdownMenuItem(
                                    text = { Text(team.label) },
                                    onClick = {
                                        team.id?.let {
                                            vm.onEvent(TeamSelectEvent.SelectTeam(it))
                                        }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Input row - Spieler hinzufügen
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.nameInput,
                        onValueChange = { vm.onEvent(TeamSelectEvent.NameChanged(it)) },
                        label = { Text("Spielername") },
                        placeholder = { Text("z.B. Max Mustermann") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading && state.teams.isNotEmpty()
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { vm.onEvent(TeamSelectEvent.AddPlayer) },
                        enabled = !state.isLoading &&
                                state.nameInput.isNotBlank() &&
                                state.teams.isNotEmpty()
                    ) {
                        Text("Hinzufügen")
                    }
                }

                // Success Message with animation and color coding
                AnimatedVisibility(
                    visible = state.successMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when (state.messageType) {
                                    MessageType.INFO -> MaterialTheme.colorScheme.primaryContainer      // Blue
                                    MessageType.WARNING -> MaterialTheme.colorScheme.errorContainer    // Red
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    state.successMessage ?: "",
                                    modifier = Modifier.weight(1f),
                                    color = when (state.messageType) {
                                        MessageType.INFO -> MaterialTheme.colorScheme.onPrimaryContainer
                                        MessageType.WARNING -> MaterialTheme.colorScheme.onErrorContainer
                                    }
                                )
                                TextButton(onClick = { vm.onEvent(TeamSelectEvent.ClearMessages) }) {
                                    Text("OK")
                                }
                            }
                        }
                    }
                }

                // Error Message (stays until dismissed)
                if (state.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                state.error ?: "",
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            TextButton(onClick = { vm.onEvent(TeamSelectEvent.ClearMessages) }) {
                                Text("OK")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // List header
                Text("Spieler (${state.players.size})", fontWeight = FontWeight.SemiBold)

                Spacer(Modifier.height(8.dp))

                // Player list
                if (state.players.isEmpty() && !state.isLoading) {
                    Text(
                        "Noch keine Spieler hinzugefügt.",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.players, key = { it.player.id ?: it.player.name }) { playerWithTeam ->
                            PlayerRow(
                                playerWithTeam = playerWithTeam,
                                teams = state.teams,
                                onChangeTeam = { teamId ->
                                    playerWithTeam.player.id?.let { playerId ->
                                        vm.onEvent(TeamSelectEvent.ChangeTeam(playerId, teamId))
                                    } ?: vm.onEvent(TeamSelectEvent.ChangeTeamByName(playerWithTeam.player.name, teamId))
                                },
                                onRemove = {
                                    playerWithTeam.player.id?.let { playerId ->
                                        vm.onEvent(TeamSelectEvent.RemovePlayer(playerId))
                                    } ?: vm.onEvent(TeamSelectEvent.RemovePlayerByName(playerWithTeam.player.name))
                                },
                                enabled = !state.isLoading
                            )
                        }
                    }
                }
            }

            // Loading overlay
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun PlayerRow(
    playerWithTeam: PlayerWithTeam,
    teams: List<Team>,
    onChangeTeam: (Long) -> Unit,
    onRemove: () -> Unit,
    enabled: Boolean
) {
    var showTeamDialog by remember { mutableStateOf(false) }

    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable(enabled = enabled) { showTeamDialog = true }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Team image instead of colored circle
            if (playerWithTeam.team != null) {
                Image(
                    painter = painterResource(id = playerWithTeam.team.imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(playerWithTeam.player.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "${playerWithTeam.team?.label ?: "Kein Team"} • ${playerWithTeam.player.pointsEarned} Punkte",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            TextButton(onClick = onRemove, enabled = enabled) {
                Text("Entfernen")
            }
        }
    }

    // Team Selection Dialog
    if (showTeamDialog) {
        AlertDialog(
            onDismissRequest = { showTeamDialog = false },
            title = { Text("Team wählen") },
            text = {
                Column {
                    teams.forEach { team ->
                        TextButton(
                            onClick = {
                                team.id?.let { onChangeTeam(it) }
                                showTeamDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Team image instead of colored circle
                                Image(
                                    painter = painterResource(id = team.imageRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(team.label)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTeamDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}