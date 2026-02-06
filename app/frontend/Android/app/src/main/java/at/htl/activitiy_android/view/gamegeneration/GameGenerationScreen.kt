package at.htl.activitiy_android.view.gamegeneration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameGenerationScreen(
    onGameCreated: (Long) -> Unit,
    onNameConfirmed: ((String) -> Unit)? = null,
    vm: GameGenerationViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    Scaffold(

    )
    { padding ->
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
                // Neues Spiel erstellen
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
                            "Neues Spiel erstellen",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Gib deinem Spiel einen Namen",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )

                        Spacer(Modifier.height(20.dp))

                        OutlinedTextField(
                            value = state.gameNameInput,
                            onValueChange = {
                                vm.onEvent(GameGenerationEvent.GameNameChanged(it))
                            },
                            label = { Text("Spielname") },
                            placeholder = { Text("z.B. Activity Abend 2026") },
                            singleLine = true,
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (onNameConfirmed != null) {
                                    // Nur Name lokal weitergeben, NICHT in DB speichern
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
                            Text("Weiter")
                        }
                    }
                }

                if (state.successMessage != null) {
                    Spacer(Modifier.height(16.dp))
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