package at.htl.activitiy_android.view.gameplay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htl.activitiy_android.domain.model.WordCategory

@Composable
fun WordCard(
    category: WordCategory,
    selectedDifficulty: Int?,
    onSelectDifficulty: (Int) -> Unit,
    onStartTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryLabel = when (category) {
        WordCategory.DRAW -> "ZEICHNEN"
        WordCategory.ACT -> "PANTOMIME"
        WordCategory.DESCRIBE -> "ERKLÄREN"
    }

    val categoryIcon = when (category) {
        WordCategory.DRAW -> "✏️"
        WordCategory.ACT -> "🎭"
        WordCategory.DESCRIBE -> "💬"
    }

    // Matching board field colors for each category
    val categoryColor = when (category) {
        WordCategory.ACT -> Color(0xFFE8526A)       // Rot – Pantomime
        WordCategory.DESCRIBE -> Color(0xFF4A6ED4)  // Blau – Erklären
        WordCategory.DRAW -> Color(0xFF3AA65A)       // Grün – Zeichnen
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Kategorie-Header ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = categoryColor,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = categoryIcon,
                        fontSize = 48.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = categoryLabel,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ── Schwierigkeit auswählen ──────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Schwierigkeit wählen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        3 to "Leicht",
                        4 to "Mittel",
                        5 to "Schwer"
                    ).forEach { (points, label) ->
                        val isSelected = selectedDifficulty == points
                        if (isSelected) {
                            Button(
                                onClick = { onSelectDifficulty(points) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$points",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        lineHeight = 20.sp
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSelectDifficulty(points) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$points",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 20.sp
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Timer starten ────────────────────────────────────────────
                Button(
                    onClick = onStartTimer,
                    enabled = selectedDifficulty != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = "▶  Timer starten",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
