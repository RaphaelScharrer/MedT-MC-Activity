package at.htl.activitiy_android.domain.model

import androidx.compose.ui.graphics.Color
import androidx.annotation.DrawableRes
import at.htl.activitiy_android.R

data class Team(
    val id: Long? = null,
    val position: Int = 0,    // Board-Position (0-16), wird im Backend gespeichert
    val gameId: Long? = null, //Fk to Game
    val playerIds: List<Long>? = null,
    val colorIndex: Int = 0,  // Farb-Index (0-3), wird vom Repository nach dem Laden gesetzt
) {

    @get:DrawableRes
    val imageRes: Int
        get() = when (colorIndex % 4) {
            0 -> R.drawable.p1
            1 -> R.drawable.p2
            2 -> R.drawable.p3
            else -> R.drawable.p4
        }

    val color: Color
        get() = when (colorIndex % 4) {
            0 -> Color(0xFFE53935) // Rot
            1 -> Color(0xFF1E88E5) // Blau
            2 -> Color(0xFF43A047) // Grün
            else -> Color(0xFFFDD835) // Gelb
        }

    val colorName: String
        get() = when (colorIndex % 4) {
            0 -> "Rot"
            1 -> "Blau"
            2 -> "Grün"
            else -> "Gelb"
        }

    val label: String
        get() = "Team - $colorName"
}