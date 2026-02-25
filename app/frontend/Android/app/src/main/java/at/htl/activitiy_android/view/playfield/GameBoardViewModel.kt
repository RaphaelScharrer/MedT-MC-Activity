package at.htl.activitiy_android.view.playfield

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import at.htl.activitiy_android.data.repository.GameRepository
import at.htl.activitiy_android.domain.model.Player
import at.htl.activitiy_android.domain.model.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameBoardState(
    val teams: List<Team> = emptyList(),
    val players: List<Player> = emptyList(),
    val teamBoardPositions: Map<Long, Int> = emptyMap(),
    val finishedTeamIds: Set<Long> = emptySet(),
    val currentTeamIndex: Int = 0,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveCompleted: Boolean = false
)

class GameBoardViewModel(
    private val gameId: Long
) : ViewModel() {

    private val repository = GameRepository

    private val _state = MutableStateFlow(GameBoardState())
    val state: StateFlow<GameBoardState> = _state

    fun saveAndExit() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            repository.saveGameState()
            repository.clearSession()
            _state.update { it.copy(isSaving = false, saveCompleted = true) }
        }
    }

    fun loadBoardState() {
        viewModelScope.launch {
            val session = repository.currentSession.value
            val teams = session.teams.filter { it.gameId == gameId }

            // Initialize board positions if empty (first time entering board)
            if (session.teamBoardPositions.isEmpty() && teams.isNotEmpty()) {
                repository.initializeBoardPositions(teams)
            }

            // Load players from backend if not yet in session
            val players = if (session.players.isEmpty()) {
                repository.loadPlayersForGame(gameId).getOrDefault(emptyList())
            } else {
                session.players
            }

            val updatedSession = repository.currentSession.value
            _state.update {
                GameBoardState(
                    teams = teams,
                    players = players,
                    teamBoardPositions = updatedSession.teamBoardPositions,
                    finishedTeamIds = updatedSession.finishedTeamIds,
                    currentTeamIndex = updatedSession.currentTeamIndex,
                    isLoading = false
                )
            }
        }
    }
}

class GameBoardViewModelFactory(
    private val gameId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameBoardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameBoardViewModel(gameId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
