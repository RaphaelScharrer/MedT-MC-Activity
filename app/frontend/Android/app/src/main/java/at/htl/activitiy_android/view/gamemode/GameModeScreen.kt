package at.htl.activitiy_android.view.gamemode

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GameModeScreen(
    onLocalSelected: () -> Unit
) {
    var showOnlineDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Activity",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Spielmodus wählen",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(48.dp))

        // Lokal Button
        Button(
            onClick = onLocalSelected,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Filled.Person, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                "Lokal",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(Modifier.height(16.dp))

        // Online Button
        Button(
            onClick = { showOnlineDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Spacer(Modifier.width(8.dp))
            Text(
                "Online",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    if (showOnlineDialog) {
        AlertDialog(
            onDismissRequest = { showOnlineDialog = false },
            title = { Text("Nicht verfügbar") },
            text = { Text("Dieser Modus ist noch nicht verfügbar.") },
            confirmButton = {
                TextButton(onClick = { showOnlineDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
