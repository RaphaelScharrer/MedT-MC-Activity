package at.htl.activitiy_android.view.playfield

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.htl.activitiy_android.R
import at.htl.activitiy_android.domain.model.Team
import at.htl.activitiy_android.view.gameplay.GamePlayActivity

class GameBoardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameId = intent.getLongExtra(EXTRA_GAME_ID, -1L)

        setContent {
            MaterialTheme {
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

    LaunchedEffect(Unit) {
        vm.loadBoardState()
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
                    enabled = activeTeams.isNotEmpty(),
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
            }
        }
    }
}

@Composable
fun StartField(
    teamsOnStart: List<Team>,
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
                            modifier = Modifier.size(28.dp),
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
                            modifier = Modifier.size(28.dp),
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
                        modifier = Modifier.size(20.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(2.dp))
                }
            }
        }
    }
}
