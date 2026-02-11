package at.htl.activitiy_android.view.playerteamsetup

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerTeamSetupScreen(
    gameName: String,
    onBack: () -> Unit = {},
    onConfirmed: () -> Unit = {},
    vm: PlayerTeamSetupViewModel = viewModel(
        factory = PlayerTeamSetupViewModelFactory(gameName)
    )
) {
    val state by vm.state.collectAsState()

    // Auto-fade Erfolgsmeldung
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            delay(2000)
            vm.onEvent(PlayerTeamSetupEvent.ClearMessages)
        }
    }

    // Navigation nach erfolgreichem Persistieren
    LaunchedEffect(state.persistedSuccessfully) {
        if (state.persistedSuccessfully) {
            onConfirmed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spieler & Teams") },
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
                        onClick = { vm.onEvent(PlayerTeamSetupEvent.FinishClicked) },
                        enabled = !state.isPersisting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isPersisting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Wird gespeichert...")
                        } else {
                            Text(
                                "Fertig",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                // Eingabe-Bereich
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Spieler hinzufügen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = state.nameInput,
                                onValueChange = {
                                    vm.onEvent(PlayerTeamSetupEvent.NameChanged(it))
                                },
                                label = { Text("Spielername") },
                                placeholder = { Text("z.B. Max") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                enabled = !state.isPersisting,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { vm.onEvent(PlayerTeamSetupEvent.AddPlayer) },
                                enabled = !state.isPersisting && state.nameInput.isNotBlank()
                            ) {
                                Text("Hinzufügen")
                            }
                        }
                    }
                }

                // Fehlermeldung
                AnimatedVisibility(
                    visible = state.error != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
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
                                TextButton(onClick = {
                                    vm.onEvent(PlayerTeamSetupEvent.ClearMessages)
                                }) {
                                    Text("OK")
                                }
                            }
                        }
                    }
                }

                // Persist-Fehlermeldung
                AnimatedVisibility(
                    visible = state.persistError != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
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
                                    state.persistError ?: "",
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                TextButton(onClick = {
                                    vm.onEvent(PlayerTeamSetupEvent.ClearMessages)
                                }) {
                                    Text("OK")
                                }
                            }
                        }
                    }
                }

                // Erfolgsmeldung
                AnimatedVisibility(
                    visible = state.successMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                state.successMessage ?: "",
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Spielerliste Header
                Text(
                    "Spieler (${state.players.size})",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    "Tippe auf einen Spieler, um das Team zu wechseln",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                // Spielerliste
                if (state.players.isEmpty()) {
                    Text(
                        "Noch keine Spieler hinzugefügt.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(
                            state.players,
                            key = { _, player -> player.name }
                        ) { _, player ->
                            PlayerTeamRow(
                                player = player,
                                onTap = {
                                    vm.onEvent(PlayerTeamSetupEvent.CycleTeam(player.name))
                                },
                                onRemove = {
                                    vm.onEvent(PlayerTeamSetupEvent.RemovePlayer(player.name))
                                },
                                enabled = !state.isPersisting
                            )
                        }
                    }
                }
            }

            // Loading overlay
            if (state.isPersisting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Daten werden gespeichert...")
                    }
                }
            }
        }
    }

    // Bestätigungsdialog
    if (state.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { vm.onEvent(PlayerTeamSetupEvent.DismissConfirmDialog) },
            title = { Text("Teams bestätigen") },
            text = {
                Text("Bist du zufrieden mit den Teams? Im Spiel können sie nicht mehr geändert werden.")
            },
            confirmButton = {
                Button(onClick = { vm.onEvent(PlayerTeamSetupEvent.ConfirmTeams) }) {
                    Text("Ja")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.onEvent(PlayerTeamSetupEvent.DismissConfirmDialog) }) {
                    Text("Nein")
                }
            }
        )
    }
}

@Composable
private fun PlayerTeamRow(
    player: LocalPlayer,
    onTap: () -> Unit,
    onRemove: () -> Unit,
    enabled: Boolean
) {
    val teamColor = when (player.teamPosition % 4) {
        0 -> Color(0xFFE53935) // Rot
        1 -> Color(0xFF1E88E5) // Blau
        2 -> Color(0xFF43A047) // Grün
        else -> Color(0xFFFDD835) // Gelb
    }

    val teamName = when (player.teamPosition % 4) {
        0 -> "Rot"
        1 -> "Blau"
        2 -> "Grün"
        else -> "Gelb"
    }

    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable(enabled = enabled) { onTap() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Teamfarbe als Kreis
            
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(teamColor)
            )


            Spacer(Modifier.width(12.dp))

            // Name
            Text(
                player.name,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            // Teamname
            Text(
                teamName,
                style = MaterialTheme.typography.bodySmall,
                color = teamColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.width(8.dp))

            // Entfernen-Button
            IconButton(
                onClick = onRemove,
                enabled = enabled,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Entfernen",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
