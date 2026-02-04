package at.htl.activitiy_android.view.gameplay

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Category Header
            Text(
                text = "$categoryIcon $categoryLabel",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            // Difficulty Selection
            Text(
                text = "Schwierigkeit wählen",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            // Simple Difficulty Buttons (3, 4, 5 points)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(3, 4, 5).forEach { points ->
                    TextButton(
                        onClick = { onSelectDifficulty(points) }
                    ) {
                        Text(
                            text = "$points Punkte",
                            fontWeight = if (selectedDifficulty == points) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedDifficulty == points)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Start Timer Button
            Button(
                onClick = onStartTimer,
                enabled = selectedDifficulty != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Timer starten")
            }
        }
    }
}
