package at.htl.activitiy_android.view.gamemode

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htl.activitiy_android.R

@Composable
fun GameModeScreen(
    onLocalSelected: () -> Unit
) {
    var showOnlineDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        // App Icon / Emoji
        Text(
            text = "🎉",
            fontSize = 72.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        // "Activity" Titel
        Text(
            text = "Activity",
            fontSize = 56.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Das Partyspiel",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))

        // Lokal Button – primäre Aktion → orange (secondary)
        Button(
            onClick = onLocalSelected,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(
                text = stringResource(R.string.gamemode_local),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        // Online Button – sekundäre Aktion → outlined
        OutlinedButton(
            onClick = { showOnlineDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text(
                text = "Online",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.weight(1.5f))
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
