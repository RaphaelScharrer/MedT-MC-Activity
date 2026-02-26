package at.htl.activitiy_android.view.gamegeneration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.htl.activitiy_android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameGenerationScreen(
    onGameCreated: (Long) -> Unit,
    onNameConfirmed: ((String) -> Unit)? = null,
    onGameResumed: ((Long) -> Unit)? = null,
    vm: GameGenerationViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    // Lokaler State: welches Spiel soll gelöscht werden (null = kein Dialog)
    var gameToDelete by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        vm.onEvent(GameGenerationEvent.LoadRecentGames)
    }

    // Lösch-Bestätigungs-Dialog
    if (gameToDelete != null) {
        AlertDialog(
            onDismissRequest = { gameToDelete = null },
            title = {
                Text(
                    text = stringResource(R.string.gamegeneration_delete_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = stringResource(R.string.gamegeneration_delete_message))
            },
            confirmButton = {
                Button(
                    onClick = {
                        gameToDelete?.let { id ->
                            vm.onEvent(GameGenerationEvent.DeleteGame(id))
                        }
                        gameToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = stringResource(R.string.gamegeneration_delete_confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { gameToDelete = null }) {
                    Text(text = stringResource(R.string.gamegeneration_delete_cancel))
                }
            }
        )
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Neues Spiel erstellen
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = stringResource(R.string.gamegeneration_new_game),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = stringResource(R.string.gamegeneration_name_game),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )

                            Spacer(Modifier.height(20.dp))

                            OutlinedTextField(
                                value = state.gameNameInput,
                                onValueChange = {
                                    vm.onEvent(GameGenerationEvent.GameNameChanged(it))
                                },
                                label = { Text(text = stringResource(R.string.input_game_name)) },
                                placeholder = { Text(text = stringResource(R.string.input_game_name)) },
                                singleLine = true,
                                enabled = !state.isLoading,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (onNameConfirmed != null) {
                                        val name = state.gameNameInput.trim()
                                        if (name.isNotBlank()) {
                                            onNameConfirmed(name)
                                        }
                                    } else {
                                        vm.onEvent(GameGenerationEvent.CreateGame(onGameCreated))
                                    }
                                },
                                enabled = !state.isLoading && state.gameNameInput.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = stringResource(R.string.gamegeneration_button_next))
                            }
                        }
                    }
                }

                // Success-Meldung
                if (state.successMessage != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Text(
                                state.successMessage ?: "",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                // Fehler-Meldung
                if (state.error != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                state.error ?: "",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Gespeicherte Spiele
                if (state.recentGames.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.gamegeneration_saved_games),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(state.recentGames, key = { it.id ?: 0L }) { game ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = game.name ?: "Unbenanntes Spiel",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!game.createdOn.isNullOrBlank()) {
                                        Text(
                                            text = game.createdOn,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                Spacer(Modifier.width(8.dp))

                                // Fortsetzen-Button
                                Button(
                                    onClick = {
                                        game.id?.let { gameId ->
                                            vm.onEvent(
                                                GameGenerationEvent.ResumeGame(gameId) { id ->
                                                    onGameResumed?.invoke(id)
                                                }
                                            )
                                        }
                                    },
                                    enabled = !state.isLoading
                                ) {
                                    Text(text = stringResource(R.string.gamegeneration_resume_button))
                                }

                                Spacer(Modifier.width(4.dp))

                                // Löschen-Button
                                IconButton(
                                    onClick = { gameToDelete = game.id },
                                    enabled = !state.isLoading
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.gamegeneration_delete_confirm),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
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
