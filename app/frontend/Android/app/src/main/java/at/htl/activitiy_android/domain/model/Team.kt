package at.htl.activitiy_android.domain.model

import androidx.compose.ui.graphics.Color
import androidx.annotation.DrawableRes
import at.htl.activitiy_android.R

data class Team(
    val id: Long? = null,
    val position: Int = 0,
    val gameId: Long? = null, //Fk to Game
    val playerIds: List<Long>? = null,


) {

    @get:DrawableRes
    val imageRes: Int
        get() = when (position % 4) {
            0 -> R.drawable.p1
            1 -> R.drawable.p2
            2 -> R.drawable.p3
            else -> R.drawable.p4
        }
    // UI-Helper für Farben basierend auf Position

    val color: Color
        get() = when (position % 4) {
            0 -> Color(0xFFE53935) // Rot
            1 -> Color(0xFF1E88E5) // Blau
            2 -> Color(0xFF43A047) // Grün
            else -> Color(0xFFFDD835) // Gelb
        }



    val colorName: String
        get() = when (position % 4) {
            0 -> "Rot"
            1 -> "Blau"
            2 -> "Grün"
            else -> "Gelb"
        }

    val label: String
        get() = "Team - $colorName"
}