package at.htl.activitiy_android.view.playerteamsetup

sealed interface PlayerTeamSetupEvent {
    data class NameChanged(val value: String) : PlayerTeamSetupEvent
    data object AddPlayer : PlayerTeamSetupEvent
    data class RemovePlayer(val name: String) : PlayerTeamSetupEvent
    data class CycleTeam(val name: String) : PlayerTeamSetupEvent
    data object FinishClicked : PlayerTeamSetupEvent
    data object ConfirmTeams : PlayerTeamSetupEvent
    data object DismissConfirmDialog : PlayerTeamSetupEvent
    data object ClearMessages : PlayerTeamSetupEvent
}
