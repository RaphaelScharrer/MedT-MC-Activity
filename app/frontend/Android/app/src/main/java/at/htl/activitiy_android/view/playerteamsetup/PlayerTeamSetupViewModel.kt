package at.htl.activitiy_android.view.playerteamsetup

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

class PlayerTeamSetupViewModel(
    private val gameName: String
) : ViewModel() {

    private val repository = GameRepository

    private val _state = MutableStateFlow(PlayerTeamSetupState())
    val state: StateFlow<PlayerTeamSetupState> = _state

    private var nextAutoTeam = 0

    fun onEvent(event: PlayerTeamSetupEvent) {
        when (event) {
            is PlayerTeamSetupEvent.NameChanged -> _state.update {
                it.copy(nameInput = event.value, error = null)
            }

            PlayerTeamSetupEvent.AddPlayer -> addPlayer()
            is PlayerTeamSetupEvent.RemovePlayer -> removePlayer(event.name)
            is PlayerTeamSetupEvent.CycleTeam -> cycleTeam(event.name)
            PlayerTeamSetupEvent.FinishClicked -> validateAndShowConfirm()
            PlayerTeamSetupEvent.ConfirmTeams -> persistToBackend()
            PlayerTeamSetupEvent.DismissConfirmDialog -> _state.update {
                it.copy(showConfirmDialog = false)
            }

            PlayerTeamSetupEvent.ClearMessages -> _state.update {
                it.copy(error = null, successMessage = null, persistError = null)
            }
        }
    }

    private fun addPlayer() {
        val name = _state.value.nameInput.trim()

        if (name.isEmpty()) {
            _state.update { it.copy(error = "Bitte einen Spielernamen eingeben.") }
            return
        }

        if (_state.value.players.any { it.name.equals(name, ignoreCase = true) }) {
            _state.update { it.copy(error = "Spieler '$name' existiert bereits.") }
            return
        }

        val teamPos = nextAutoTeam % 4
        nextAutoTeam++

        _state.update {
            it.copy(
                players = it.players + LocalPlayer(name = name, teamPosition = teamPos),
                nameInput = "",
                error = null,
                successMessage = "'$name' hinzugefügt"
            )
        }
    }

    private fun removePlayer(name: String) {
        _state.update {
            it.copy(
                players = it.players.filter { p -> p.name != name },
                successMessage = "'$name' entfernt"
            )
        }
    }

    private fun cycleTeam(name: String) {
        _state.update { state ->
            state.copy(
                players = state.players.map { player ->
                    if (player.name == name) {
                        player.copy(teamPosition = (player.teamPosition + 1) % 4)
                    } else {
                        player
                    }
                }
            )
        }
    }

    private fun validateAndShowConfirm() {
        val players = _state.value.players

        // Zähle Spieler pro Team
        val teamCounts = players.groupBy { it.teamPosition }.mapValues { it.value.size }
        val teamsWithPlayers = teamCounts.filter { it.value >= 1 }
        val teamsWithEnoughPlayers = teamCounts.filter { it.value >= 2 }

        if (teamsWithPlayers.size < 2) {
            _state.update {
                it.copy(error = "Es müssen mindestens 2 Teams vorhanden sein.")
            }
            return
        }

        if (teamsWithEnoughPlayers.size < 2) {
            _state.update {
                it.copy(error = "Es müssen mindestens 2 Teams mit jeweils mindestens 2 Spielern vorhanden sein.")
            }
            return
        }

        _state.update { it.copy(showConfirmDialog = true, error = null) }
    }

    private fun persistToBackend() {
        _state.update { it.copy(showConfirmDialog = false, isPersisting = true, persistError = null) }

        viewModelScope.launch {
            try {
                // 1. Spiel erstellen
                val game = repository.createGame(gameName).getOrThrow()
                val gameId = game.id ?: throw IllegalStateException("Game ID fehlt")
                repository.startNewSession(gameId)

                // 2. Teams erstellen (nur die die tatsächlich Spieler haben)
                val players = _state.value.players
                val usedPositions = players.map { it.teamPosition }.distinct().sorted()

                val teamIdByPosition = mutableMapOf<Int, Long>()

                for (pos in usedPositions) {
                    val team = Team(
                        id = null,
                        position = pos,
                        gameId = gameId,
                        playerIds = null
                    )
                    val created = repository.createTeam(team).getOrThrow()
                    teamIdByPosition[pos] = created.id
                        ?: throw IllegalStateException("Team ID fehlt")
                }

                // 3. Spieler erstellen
                for (localPlayer in players) {
                    val backendTeamId = teamIdByPosition[localPlayer.teamPosition]
                        ?: throw IllegalStateException("Team für Position ${localPlayer.teamPosition} nicht gefunden")

                    val player = Player(
                        id = null,
                        name = localPlayer.name,
                        team = backendTeamId,
                        pointsEarned = 0
                    )
                    repository.createPlayer(player).getOrThrow()
                }

                _state.update { it.copy(isPersisting = false, persistedSuccessfully = true) }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isPersisting = false,
                        persistError = "Fehler beim Speichern: ${e.message}"
                    )
                }
            }
        }
    }
}

class PlayerTeamSetupViewModelFactory(
    private val gameName: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerTeamSetupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayerTeamSetupViewModel(gameName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
