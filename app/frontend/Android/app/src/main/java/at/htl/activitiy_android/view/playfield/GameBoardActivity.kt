package at.htl.activitiy_android.view.playfield

import android.content.Intent
import at.htl.activitiy_android.R
import android.os.Bundle
import android.widget.GridLayout
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import at.htl.activitiy_android.view.gameplay.GamePlayActivity

class GameBoardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameBoardScreen()
                }
            }
        }
    }
}

@Composable
fun GameBoardScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // GridLayout mit AndroidView einbinden
        AndroidView(
            factory = { ctx ->
                GridLayout(ctx).apply {
                    columnCount = 3
                    rowCount = 6

                    // Start-Feld
                    val startField = android.widget.TextView(ctx).apply {
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = 0
                            height = 0
                            columnSpec = GridLayout.spec(0, 3, 1f)
                            rowSpec = GridLayout.spec(0, 1f)
                            setMargins(8, 8, 8, 8)
                        }
                        text = "START"
                        textSize = 24f
                        gravity = android.view.Gravity.CENTER
                        setTextColor(android.graphics.Color.parseColor("#1C1B1F"))  // onPrimaryContainer
                        setBackgroundColor(android.graphics.Color.parseColor("#E8DEF8"))  // primaryContainer
                        setPadding(16, 16, 16, 16)
                    }
                    addView(startField)

                    // 15 Felder erstellen
                    for (i in 0 until 15) {
                        val imageView = ImageView(ctx).apply {
                            layoutParams = GridLayout.LayoutParams().apply {
                                width = 0
                                height = 0
                                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                                setMargins(8, 8, 8, 8)
                            }
                            scaleType = ImageView.ScaleType.CENTER_INSIDE
                            setPadding(16, 16, 16, 16)

                            // Wechselnde Hintergrundfarben
                            val backgroundColor = when (i % 3) {
                                0 -> android.graphics.Color.parseColor("#F09BAA")  // Rot für Zeichnen
                                1 -> android.graphics.Color.parseColor("#99B4F2")  // Blau für Erklären
                                else -> android.graphics.Color.parseColor("#B8F599")  // Grün für Pantomime
                            }
                            setBackgroundColor(backgroundColor)

                            // Wechselnde Icons
                            val drawableResId = when (i % 3) {
                                0 -> R.drawable.ic_1  // Zeichnen
                                1 -> R.drawable.ic_2  // Erklären
                                else -> R.drawable.ic_3  // Pantomime
                            }
                            setImageResource(drawableResId)
                        }
                        addView(imageView)
                    }

                    // Ziel-Feld
                    val goalField = android.widget.TextView(ctx).apply {
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = 0
                            height = 0
                            columnSpec = GridLayout.spec(0, 3, 1f)
                            rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                            setMargins(8, 8, 8, 8)
                        }
                        text = "ZIEL"
                        textSize = 24f
                        gravity = android.view.Gravity.CENTER
                        setTextColor(android.graphics.Color.parseColor("#1C1B1F"))  // onPrimaryContainer
                        setBackgroundColor(android.graphics.Color.parseColor("#E8DEF8"))  // primaryContainer
                        setPadding(16, 16, 16, 16)
                    }
                    addView(goalField)
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

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
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        val intent = Intent(context, GamePlayActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Zum Gameplay", // TODO: SUche nutze text resourcen
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}