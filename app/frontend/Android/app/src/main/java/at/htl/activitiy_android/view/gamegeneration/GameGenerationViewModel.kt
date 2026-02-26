package at.htl.activitiy_android.view.gamegeneration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.htl.activitiy_android.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameGenerationViewModel : ViewModel() {

    private val repository = GameRepository

    private val _state = MutableStateFlow(GameGenerationState())
    val state: StateFlow<GameGenerationState> = _state

    fun onEvent(event: GameGenerationEvent) {
        when (event) {
            is GameGenerationEvent.GameNameChanged -> _state.update {
                it.copy(gameNameInput = event.value, error = null)
            }

            is GameGenerationEvent.CreateGame -> createGame(event.onSuccess)

            GameGenerationEvent.LoadRecentGames -> loadRecentGames()

            GameGenerationEvent.ClearMessages -> _state.update {
                it.copy(error = null, successMessage = null)
            }

            is GameGenerationEvent.LoadGame -> loadGame(event.gameId)

            is GameGenerationEvent.UpdateGame -> updateGame(event.gameId, event.onSuccess)

            is GameGenerationEvent.ResumeGame -> resumeGame(event.gameId, event.onSuccess)

            is GameGenerationEvent.DeleteGame -> deleteGame(event.gameId)
        }
    }

    private fun loadRecentGames() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getAllGames()
                .onSuccess { games ->
                    _state.update {
                        it.copy(
                            recentGames = games,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            error = "Fehler beim Laden der Spiele: ${e.message}",
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun resumeGame(gameId: Long, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repository.startNewSession(gameId)
                repository.loadGame(gameId).onFailure { throw it }
                val teams = repository.loadTeamsForGame(gameId).getOrThrow()
                repository.restoreBoardPositionsFromBackend(teams)
                _state.update { it.copy(isLoading = false) }
                onSuccess(gameId)
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

    fun loadGame(gameId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.loadGame(gameId)
                .onSuccess { game ->
                    // Start new session for this game
                    repository.startNewSession(gameId)
                    _state.update {
                        it.copy(
                            currentGame = game,
                            gameNameInput = game.name ?: "",
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            error = "Fehler beim Laden: ${e.message}",
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun updateGame(gameId: Long, onSuccess: (Long) -> Unit) {
        val gameName = _state.value.gameNameInput.trim()

        if (gameName.isEmpty()) {
            _state.update {
                it.copy(error = "Bitte einen Spielnamen eingeben")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.updateGame(gameId, gameName)
                .onSuccess { savedGame ->
                    _state.update {
                        it.copy(
                            currentGame = savedGame,
                            isLoading = false,
                            successMessage = "Spiel aktualisiert!"
                        )
                    }
                    onSuccess(gameId)
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            error = "Fehler beim Aktualisieren: ${e.message}",
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun createGame(onSuccess: (Long) -> Unit) {
        val gameName = _state.value.gameNameInput.trim()

        if (gameName.isEmpty()) {
            _state.update {
                it.copy(error = "Bitte einen Spielnamen eingeben")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.createGame(gameName)
                .onSuccess { createdGame ->
                    // Start new session for this game
                    createdGame.id?.let { id ->
                        repository.startNewSession(id)
                    }

                    _state.update {
                        it.copy(
                            currentGame = createdGame,
                            isLoading = false,
                            gameNameInput = ""
                        )
                    }

                    createdGame.id?.let { gameId ->
                        onSuccess(gameId)
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            error = "Fehler beim Erstellen: ${e.message}",
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun deleteGame(gameId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.deleteGameWithAll(gameId)
                .onSuccess {
                    val games = repository.getAllGames().getOrElse { emptyList() }
                    _state.update { it.copy(recentGames = games, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            error = "Fehler beim Löschen: ${e.message}",
                            isLoading = false
                        )
                    }
                }
        }
    }
}
