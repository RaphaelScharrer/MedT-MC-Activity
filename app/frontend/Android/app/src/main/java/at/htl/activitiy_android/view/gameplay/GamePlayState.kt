package at.htl.activitiy_android.view.gameplay

import at.htl.activitiy_android.domain.model.Team
import at.htl.activitiy_android.domain.model.Word
import at.htl.activitiy_android.domain.model.WordCategory

data class GamePlayState(
    val currentTeam: Team? = null,
    val currentTeamIndex: Int = 0,
    val teams: List<Team> = emptyList(),
    val availableWords: List<Word> = emptyList(),
    val currentWord: Word? = null,
    val selectedDifficulty: Int? = null,
    val currentCategory: WordCategory? = null,
    val timerSeconds: Int = 10,  // 10 seconds for testing
    val timerRunning: Boolean = false,
    val timeUp: Boolean = false,
    val phase: GamePhase = GamePhase.WORD_SELECTION,
    val isLoading: Boolean = false,
    val error: String? = null,
    val gameName: String = ""
)

enum class GamePhase {
    WORD_SELECTION,  // Wort auswählen
    TIMER_RUNNING,   // Timer läuft
    TIME_UP          // Zeit abgelaufen
}
