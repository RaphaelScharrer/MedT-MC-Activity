package at.htl.activitiy_android.view.gameplay

sealed interface GamePlayEvent {
    data object LoadGameData : GamePlayEvent
    data class SelectDifficulty(val points: Int) : GamePlayEvent
    data object StartTimer : GamePlayEvent
    data object TimerTick : GamePlayEvent
    data object ResetForNextTurn : GamePlayEvent
    data object ClearError : GamePlayEvent
}
