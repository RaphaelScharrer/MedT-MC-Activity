package at.htl.activitiy_android.view.gameplay

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.htl.activitiy_android.domain.model.WordCategory
import at.htl.activitiy_android.view.playfield.GameBoardActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlayScreen(
    gameId: Long,
    onBack: () -> Unit = {},
    vm: GamePlayViewModel = viewModel(
        factory = GamePlayViewModelFactory(gameId)
    )
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var showRedScreen by remember { mutableStateOf(false) }

    // Load game data
    LaunchedEffect(Unit) {
        vm.onEvent(GamePlayEvent.LoadGameData)
    }

    // Show red screen and play sound when time is up
    LaunchedEffect(state.timeUp) {
        if (state.timeUp) {
            showRedScreen = true
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000)
            } catch (e: Exception) {
                // Ignore sound errors
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(state.gameName) },
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
                // Bottom bar shows word + timer when timer is running
                if (state.phase == GamePhase.TIMER_RUNNING || state.phase == GamePhase.TIME_UP) {
                    Surface(
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp,
                        color = if (state.timeUp) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Category label
                            val categoryLabel = when (state.currentCategory) {
                                WordCategory.DRAW -> "ZEICHNEN"
                                WordCategory.ACT -> "PANTOMIME"
                                WordCategory.DESCRIBE -> "ERKLÄREN"
                                null -> ""
                            }
                            Text(
                                text = categoryLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            Spacer(Modifier.height(8.dp))

                            // Word

                            Text(
                                //text = state.currentWord?.word ?: "",
                                text = "Los gehts!",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(16.dp))

                            // Timer
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 32.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = formatTime(state.timerSeconds),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 48.sp
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val intent = Intent(context, GameBoardActivity::class.java)
                                    context.startActivity(intent)
                                },
                                enabled = !state.isLoading,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "Erraten",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Time up message
                            if (state.timeUp) {
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = "ZEIT ABGELAUFEN!",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(Modifier.height(16.dp))

                                /*
                                Button(
                                    onClick = { vm.onEvent(GamePlayEvent.ResetForNextTurn) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFFD32F2F)
                                    )
                                ) {
                                    Text(
                                        text = "Nächste Runde",
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                 */
                                Button(
                                    onClick = {
                                        val intent = Intent(context, GameBoardActivity::class.java)
                                        context.startActivity(intent)
                                    },
                                    enabled = !state.isLoading,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFFD32F2F)
                                    )
                                ) {
                                    Text(
                                        text = "Spielfeld",
                                        //style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
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
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    state.error != null -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.error ?: "",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { vm.onEvent(GamePlayEvent.ClearError) }) {
                                Text("OK")
                            }
                        }
                    }

                    state.phase == GamePhase.WORD_SELECTION -> {
                        // Word selection phase
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Word Card
                            state.currentCategory?.let { category ->
                                WordCard(
                                    category = category,
                                    selectedDifficulty = state.selectedDifficulty,
                                    onSelectDifficulty = { points ->
                                        vm.onEvent(GamePlayEvent.SelectDifficulty(points))
                                    },
                                    onStartTimer = {
                                        vm.onEvent(GamePlayEvent.StartTimer)
                                    }
                                )
                            }
                        }
                    }

                    state.phase == GamePhase.TIMER_RUNNING -> {
                        // Timer running - show encouragement
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val emoji = when (state.currentCategory) {
                                WordCategory.DRAW -> "✏️"
                                WordCategory.ACT -> "🎭"
                                WordCategory.DESCRIBE -> "💬"
                                null -> "🎯"
                            }

                            Text(
                                text = emoji,
                                fontSize = 80.sp
                            )

                            Spacer(Modifier.height(24.dp))

                            Text(
                                //text = "Los geht's!",
                                text = when (state.currentCategory) {
                                    WordCategory.DRAW -> "Zeichne das Wort:"
                                    WordCategory.ACT -> "Stelle das Wort dar:"
                                    WordCategory.DESCRIBE -> "Erkläre das Wort:"
                                    null -> ""
                                },
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )


                            Spacer(Modifier.height(8.dp))


                            Text(
                                /*
                                text = when (state.currentCategory) {
                                    WordCategory.DRAW -> "Zeichne das Wort:"
                                    WordCategory.ACT -> "Stelle das Wort dar:"
                                    WordCategory.DESCRIBE -> "Erkläre das Wort:"
                                    null -> ""
                                },
                                 */
                                text = state.currentWord?.word ?: "",
                                //style = MaterialTheme.typography.titleMedium,
                                style = MaterialTheme.typography.titleLarge ,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )

                        }
                    }

                    else -> {
                        // Time up state - content is in bottom bar
                    }
                }
            }
        }

        // Red overlay when time is up
        if (showRedScreen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = 0.70f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ZEIT ABGELAUFEN!",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = { showRedScreen = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Red
                        )
                    ) {
                        Text(
                            text = "Schließen",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", mins, secs)
}
