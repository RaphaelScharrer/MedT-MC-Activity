package at.htl.activitiy_android.view.playerteamsetup

import at.htl.activitiy_android.domain.model.Player
import at.htl.activitiy_android.domain.model.Team

/**
 * Lokaler Spieler-Eintrag (noch nicht in der DB).
 * Team wird als Position (0-3) gespeichert: Rot, Blau, Grün, Gelb.
 */
data class LocalPlayer(
    val name: String,
    val teamPosition: Int // 0=Rot, 1=Blau, 2=Grün, 3=Gelb
)

data class PlayerTeamSetupState(
    val nameInput: String = "",
    val players: List<LocalPlayer> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val showConfirmDialog: Boolean = false,
    val isPersisting: Boolean = false,
    val persistError: String? = null,
    val persistedSuccessfully: Boolean = false
) {
    val nextTeamPosition: Int
        get() = if (players.isEmpty()) 0 else (players.last().teamPosition + 1) % 4
}
