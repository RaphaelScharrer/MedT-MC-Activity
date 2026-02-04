package at.htl.activitiy_android.view.teamgeneration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import at.htl.activitiy_android.data.repository.GameRepository
import at.htl.activitiy_android.domain.model.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TeamGenerationViewModel(
    private val gameId: Long
) : ViewModel() {

    private val repository = GameRepository

    private val _state = MutableStateFlow(TeamGenerationState())
    val state: StateFlow<TeamGenerationState> = _state

    private var lastSavedTeamCount: Int? = null

    init {
        loadExistingTeams()
    }

    fun onEvent(event: TeamGenerationEvent) {
        when (event) {
            is TeamGenerationEvent.TeamCountChanged -> _state.update {
                it.copy(teamCountInput = event.value, error = null)
            }

            TeamGenerationEvent.GenerateTeams -> generateTeams()

            is TeamGenerationEvent.SaveTeams -> validateGenerateAndSave(event.onSuccess)

            TeamGenerationEvent.ClearMessages -> _state.update {
                it.copy(error = null, successMessage = null)
            }
        }
    }

    private fun loadExistingTeams() {
        viewModelScope.launch {
            repository.loadTeamsForGame(gameId)
                .onSuccess { existingTeams ->
                    if (existingTeams.isNotEmpty()) {
                        lastSavedTeamCount = existingTeams.size
                        _state.update {
                            it.copy(
                                teamCountInput = existingTeams.size.toString(),
                                teams = existingTeams
                            )
                        }
                    }
                }
                .onFailure { e ->
                    println("Error loading existing teams: ${e.message}")
                }
        }
    }

    private fun generateTeams() {
        val count = _state.value.teamCountInput.toIntOrNull()

        if (count == null) {
            _state.update {
                it.copy(error = "Bitte eine gültige Zahl eingeben")
            }
            return
        }

        if (count < 1 || count > 4) {
            _state.update {
                it.copy(error = "Bitte eine Zahl zwischen 1 und 4 eingeben")
            }
            return
        }

        val newTeams = (0 until count).map { i ->
            Team(
                id = null,
                position = i,
                gameId = gameId,
                playerIds = null
            )
        }

        _state.update {
            it.copy(
                teams = newTeams,
                hasChanges = true,
                error = null
            )
        }
    }

    private fun validateGenerateAndSave(onSuccess: () -> Unit) {
        val count = _state.value.teamCountInput.toIntOrNull()

        if (count == null) {
            _state.update {
                it.copy(error = "Bitte eine gültige Zahl eingeben")
            }
            return
        }

        if (count < 1 || count > 4) {
            _state.update {
                it.copy(error = "Bitte eine Zahl zwischen 1 und 4 eingeben")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                // Get current teams from repository
                val currentTeams = repository.getTeams().filter { it.gameId == gameId }

                // Check: Did team count actually change?
                val teamCountChanged = lastSavedTeamCount != null && lastSavedTeamCount != count

                if (teamCountChanged) {
                    // Team count changed - delete everything and start fresh
                    repository.deleteAllTeamsForGame(gameId)
                        .onFailure { throw it }

                    // Create new teams
                    val savedTeams = mutableListOf<Team>()
                    for (i in 0 until count) {
                        val team = Team(
                            id = null,
                            position = i,
                            gameId = gameId,
                            playerIds = null
                        )
                        repository.createTeam(team)
                            .onSuccess { savedTeams.add(it) }
                            .onFailure { throw it }
                    }

                    lastSavedTeamCount = count

                    _state.update {
                        it.copy(
                            teams = savedTeams,
                            isLoading = false,
                            hasChanges = false
                        )
                    }
                } else if (lastSavedTeamCount == null || currentTeams.isEmpty()) {
                    // First time creating teams
                    val savedTeams = mutableListOf<Team>()
                    for (i in 0 until count) {
                        val team = Team(
                            id = null,
                            position = i,
                            gameId = gameId,
                            playerIds = null
                        )
                        repository.createTeam(team)
                            .onSuccess { savedTeams.add(it) }
                            .onFailure { throw it }
                    }

                    lastSavedTeamCount = count

                    _state.update {
                        it.copy(
                            teams = savedTeams,
                            isLoading = false,
                            hasChanges = false
                        )
                    }
                } else {
                    // Same count - just navigate forward without touching data
                    _state.update {
                        it.copy(
                            isLoading = false,
                            hasChanges = false
                        )
                    }
                }

                onSuccess()

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Fehler beim Speichern: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
}

class TeamGenerationViewModelFactory(
    private val gameId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamGenerationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeamGenerationViewModel(gameId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
