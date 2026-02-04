package at.htl.activitiy_android.view.gamesummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import at.htl.activitiy_android.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class GameSummaryViewModel(
    private val gameId: Long
) : ViewModel() {

    private val repository = GameRepository

    private val _state = MutableStateFlow(GameSummaryState())
    val state: StateFlow<GameSummaryState> = _state

    fun loadGameData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // Load data from repository (uses cached data if available)
                repository.loadGame(gameId).onFailure { throw it }
                repository.loadTeamsForGame(gameId).onFailure { throw it }
                repository.loadPlayersForGame(gameId).onFailure { throw it }

                // Get data from repository's single source of truth
                val session = repository.currentSession.value
                val teams = repository.getTeams().filter { it.gameId == gameId }
                val players = repository.getPlayers()

                _state.update {
                    it.copy(
                        gameName = session.game?.name ?: "Unbenanntes Spiel",
                        teams = teams,
                        allPlayers = players,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Fehler beim Laden: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
}

class GameSummaryViewModelFactory(
    private val gameId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameSummaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameSummaryViewModel(gameId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
