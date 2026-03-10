package at.htl.activitiy_android.view.gameplay

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

    // Navigate back to board when word is guessed
    LaunchedEffect(state.navigateToBoard) {
        if (state.navigateToBoard) {
            val intent = Intent(context, GameBoardActivity::class.java)
            intent.putExtra(GameBoardActivity.EXTRA_GAME_ID, gameId)
            context.startActivity(intent)
            (context as? ComponentActivity)?.finish()
        }
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
                // Bottom bar shows timer + action buttons when timer is running
                if (state.phase == GamePhase.TIMER_RUNNING || state.phase == GamePhase.TIME_UP) {
                    val barColor = if (state.timeUp) Color(0xFFB71C1C) else MaterialTheme.colorScheme.surfaceVariant
                    Surface(
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp,
                        color = barColor
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Timer display
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = if (state.timeUp) 0.15f else 0.12f))
                                    .padding(horizontal = 36.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = formatTime(state.timerSeconds),
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.timeUp) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 48.sp
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            if (!state.timeUp) {
                                // "Erraten" button – green
                                Button(
                                    onClick = { vm.onEvent(GamePlayEvent.WordGuessed) },
                                    enabled = !state.isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                        contentColor = MaterialTheme.colorScheme.onTertiary
                                    )
                                ) {
                                    Text(
                                        text = "✓  Erraten!",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                // Time-up state
                                Text(
                                    text = "ZEIT ABGELAUFEN!",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        vm.onEvent(GamePlayEvent.ResetForNextTurn)
                                        val intent = Intent(context, GameBoardActivity::class.java)
                                        intent.putExtra(GameBoardActivity.EXTRA_GAME_ID, gameId)
                                        context.startActivity(intent)
                                        (context as? ComponentActivity)?.finish()
                                    },
                                    enabled = !state.isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFFB71C1C)
                                    )
                                ) {
                                    Text(
                                        text = "Zum Spielfeld",
                                        fontSize = 17.sp,
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
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Team + player info
                            state.currentTeam?.let { team ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = team.imageRes),
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "${team.label} ist dran",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            state.currentPlayer?.let { player ->
                                                Text(
                                                    text = "Spieler: ${player.name}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(20.dp))
                            }

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
                        // Category colors matching board
                        val categoryColor = when (state.currentCategory) {
                            WordCategory.ACT -> Color(0xFFE8526A)
                            WordCategory.DESCRIBE -> Color(0xFF4A6ED4)
                            WordCategory.DRAW -> Color(0xFF3AA65A)
                            null -> MaterialTheme.colorScheme.primary
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Category badge
                            val categoryLabel = when (state.currentCategory) {
                                WordCategory.DRAW -> "ZEICHNEN"
                                WordCategory.ACT -> "PANTOMIME"
                                WordCategory.DESCRIBE -> "ERKLÄREN"
                                null -> ""
                            }
                            val emoji = when (state.currentCategory) {
                                WordCategory.DRAW -> "✏️"
                                WordCategory.ACT -> "🎭"
                                WordCategory.DESCRIBE -> "💬"
                                null -> "🎯"
                            }

                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = categoryColor
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                    Text(
                                        text = categoryLabel,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 1.5.sp
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Instruction
                            Text(
                                text = when (state.currentCategory) {
                                    WordCategory.DRAW -> "Zeichne das Wort:"
                                    WordCategory.ACT -> "Stelle das Wort dar:"
                                    WordCategory.DESCRIBE -> "Erkläre das Wort:"
                                    null -> ""
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(16.dp))

                            // THE WORD – large, high contrast, on a colored card
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = categoryColor.copy(alpha = 0.18f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = state.currentWord?.word ?: "",
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                                )
                            }

                            // Player name
                            state.currentPlayer?.let { player ->
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = player.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
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
                    .background(Color(0xFFB71C1C).copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "⏰",
                        fontSize = 72.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "ZEIT\nABGELAUFEN!",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 44.sp
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = { showRedScreen = false },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFFB71C1C)
                        )
                    ) {
                        Text(
                            text = "Schließen",
                            fontSize = 17.sp,
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
