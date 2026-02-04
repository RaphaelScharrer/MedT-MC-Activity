package at.htl.activitiy_android.view.teamselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import at.htl.activitiy_android.data.repository.GameRepository
import at.htl.activitiy_android.domain.model.Player
import at.htl.activitiy_android.domain.model.PlayerWithTeam
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TeamSelectViewModel(
    private val gameId: Long
) : ViewModel() {

    private val repository = GameRepository

    private val _state = MutableStateFlow(TeamSelectState())
    val state: StateFlow<TeamSelectState> = _state

    fun onEvent(event: TeamSelectEvent) {
        when (event) {
            is TeamSelectEvent.NameChanged -> _state.update {
                it.copy(nameInput = event.value, error = null)
            }

            TeamSelectEvent.AddPlayer -> addPlayer()
            is TeamSelectEvent.RemovePlayer -> removePlayer(event.playerId)
            is TeamSelectEvent.RemovePlayerByName -> removePlayerByName(event.name)
            is TeamSelectEvent.ChangeTeam -> changeTeam(event.playerId, event.teamId)
            is TeamSelectEvent.ChangeTeamByName -> changeTeamByName(event.playerName, event.teamId)

            TeamSelectEvent.ClearMessages -> _state.update {
                it.copy(error = null, successMessage = null)
            }
            TeamSelectEvent.LoadData -> loadData()
            is TeamSelectEvent.SelectTeam -> _state.update {
                it.copy(selectedTeamId = event.teamId)
            }
            TeamSelectEvent.SaveTeamsAndPlayers -> { /* No-op, auto-save on add */ }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // Load teams from repository
                repository.loadTeamsForGame(gameId)
                    .onFailure { throw it }

                // Load players from repository
                repository.loadPlayersForGame(gameId)
                    .onFailure { throw it }

                val teams = repository.getTeams().filter { it.gameId == gameId }
                val players = repository.getPlayers()

                val playersWithTeams = players.map { player ->
                    PlayerWithTeam(
                        player = player,
                        team = teams.find { it.id == player.team }
                    )
                }

                _state.update {
                    it.copy(
                        teams = teams,
                        players = playersWithTeams,
                        isLoading = false,
                        selectedTeamId = it.selectedTeamId ?: teams.firstOrNull()?.id
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

    private fun addPlayer() {
        val s = _state.value
        val name = s.nameInput.trim()

        if (name.isEmpty()) {
            _state.update { it.copy(error = "Bitte einen Spielernamen eingeben.") }
            return
        }

        if (s.teams.isEmpty()) {
            _state.update { it.copy(error = "Bitte zuerst Teams generieren!") }
            return
        }

        // Check duplicate locally first
        if (s.players.any { it.player.name.equals(name, ignoreCase = true) }) {
            _state.update { it.copy(error = "Spieler existiert bereits.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val newPlayer = Player(
                id = null,
                name = name,
                team = s.selectedTeamId,
                pointsEarned = 0
            )

            repository.createPlayer(newPlayer)
                .onSuccess { createdPlayer ->
                    val selectedTeam = s.teams.find { it.id == s.selectedTeamId }
                    val newPlayerWithTeam = PlayerWithTeam(createdPlayer, selectedTeam)

                    _state.update {
                        it.copy(
                            nameInput = "",
                            players = it.players + newPlayerWithTeam,
                            isLoading = false,
                            successMessage = "Spieler '$name' hinzugefügt!",
                            messageType = MessageType.INFO
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            error = e.message ?: "Fehler beim Hinzufügen",
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun removePlayer(playerId: Long) {
        viewModelScope.launch {
            val playerName = _state.value.players.find { it.player.id == playerId }?.player?.name

            repository.deletePlayer(playerId)
                .onSuccess {
                    _state.update { s ->
                        s.copy(
                            players = s.players.filterNot { it.player.id == playerId },
                            successMessage = "Spieler '$playerName' entfernt",
                            messageType = MessageType.INFO
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(error = "Fehler beim Löschen: ${e.message}")
                    }
                }
        }
    }

    private fun removePlayerByName(name: String) {
        // Find player by name and remove via ID if exists
        val player = _state.value.players.find { it.player.name == name }
        player?.player?.id?.let { id ->
            removePlayer(id)
        } ?: run {
            // Player not saved yet, just remove from local state
            _state.update { s ->
                s.copy(
                    players = s.players.filterNot { it.player.name == name },
                    successMessage = "Spieler '$name' entfernt",
                    messageType = MessageType.INFO
                )
            }
        }
    }

    private fun changeTeam(playerId: Long, teamId: Long) {
        viewModelScope.launch {
            val s = _state.value
            val teamName = s.teams.find { it.id == teamId }?.label
            val playerWithTeam = s.players.find { it.player.id == playerId } ?: return@launch

            val updatedPlayer = playerWithTeam.player.copy(team = teamId)

            repository.updatePlayer(updatedPlayer)
                .onSuccess { saved ->
                    val newTeam = s.teams.find { it.id == teamId }
                    _state.update { state ->
                        val updatedPlayers = state.players.map {
                            if (it.player.id == playerId) PlayerWithTeam(saved, newTeam)
                            else it
                        }
                        state.copy(
                            players = updatedPlayers,
                            successMessage = "${saved.name} zu '$teamName' geändert",
                            messageType = MessageType.INFO
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = "Fehler: ${e.message}") }
                }
        }
    }

    private fun changeTeamByName(playerName: String, teamId: Long) {
        val player = _state.value.players.find { it.player.name == playerName }
        player?.player?.id?.let { id ->
            changeTeam(id, teamId)
        }
    }
}

class TeamSelectViewModelFactory(
    private val gameId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamSelectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeamSelectViewModel(gameId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
