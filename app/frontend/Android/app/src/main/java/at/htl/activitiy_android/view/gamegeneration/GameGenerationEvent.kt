package at.htl.activitiy_android.view.gamegeneration

sealed interface GameGenerationEvent {
    data class GameNameChanged(val value: String) : GameGenerationEvent
    data class CreateGame(val onSuccess: (Long) -> Unit) : GameGenerationEvent
    data object LoadRecentGames : GameGenerationEvent
    data object ClearMessages : GameGenerationEvent
    data class LoadGame(val gameId: Long) : GameGenerationEvent
    data class UpdateGame(val gameId: Long, val onSuccess: (Long) -> Unit) : GameGenerationEvent
    data class ResumeGame(val gameId: Long, val onSuccess: (Long) -> Unit) : GameGenerationEvent
    data class DeleteGame(val gameId: Long) : GameGenerationEvent
}