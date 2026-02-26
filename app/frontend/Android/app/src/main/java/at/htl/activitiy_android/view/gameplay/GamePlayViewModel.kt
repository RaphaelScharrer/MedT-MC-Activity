package at.htl.activitiy_android.view.gameplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import at.htl.activitiy_android.data.repository.GameRepository
import at.htl.activitiy_android.domain.model.WordCategory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GamePlayViewModel(
    private val gameId: Long
) : ViewModel() {

    private val repository = GameRepository

    private val _state = MutableStateFlow(GamePlayState())
    val state: StateFlow<GamePlayState> = _state

    private var timerJob: Job? = null

    fun onEvent(event: GamePlayEvent) {
        when (event) {
            GamePlayEvent.LoadGameData -> loadGameData()
            is GamePlayEvent.SelectDifficulty -> selectWordByDifficulty(event.points)
            GamePlayEvent.StartTimer -> startTimer()
            GamePlayEvent.TimerTick -> timerTick()
            GamePlayEvent.WordGuessed -> wordGuessed()
            GamePlayEvent.ResetForNextTurn -> resetForNextTurn()
            GamePlayEvent.ClearError -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadGameData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Load data from repository
                repository.loadGame(gameId).onFailure { throw it }
                repository.loadTeamsForGame(gameId).onFailure { throw it }
                repository.loadAllWords().onFailure { throw it }

                // Get data from repository
                val session = repository.currentSession.value
                val allTeams = repository.getTeams().filter { it.gameId == gameId }
                val activeTeams = allTeams.filter { it.id !in session.finishedTeamIds }

                // Read current team index from repository (persists across Activity transitions)
                val teamIndex = repository.getCurrentTeamIndex()
                    .coerceIn(0, (activeTeams.size - 1).coerceAtLeast(0))
                val currentTeam = activeTeams.getOrNull(teamIndex)

                // Determine category based on team's board position
                val boardPos = currentTeam?.id?.let { repository.getTeamBoardPosition(it) } ?: 0
                val category = getCategoryForBoardPosition(boardPos)

                // Get available words (filters out already used words)
                val availableWords = repository.getAvailableWords()
                val categoryWords = availableWords.filter { it.category == category }

                _state.update {
                    it.copy(
                        gameName = session.game?.name ?: "Spiel",
                        teams = activeTeams,
                        currentTeam = currentTeam,
                        currentTeamIndex = teamIndex,
                        currentCategory = category,
                        availableWords = categoryWords,
                        isLoading = false,
                        phase = GamePhase.WORD_SELECTION
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

    private fun getCategoryForBoardPosition(boardPosition: Int): WordCategory {
        if (boardPosition <= 0) {
            // On START -> Erklären
            return WordCategory.DESCRIBE
        }
        // boardPosition 1-15 maps to field index 0-14
        // Must match GameBoardActivity field colors exactly:
        // fieldIndex % 3 == 0 -> Rot -> Pantomime (ACT)
        // fieldIndex % 3 == 1 -> Blau -> Erklären (DESCRIBE)
        // fieldIndex % 3 == 2 -> Grün -> Zeichnen (DRAW)
        val fieldIndex = boardPosition - 1
        return when (fieldIndex % 3) {
            0 -> WordCategory.ACT
            1 -> WordCategory.DESCRIBE
            else -> WordCategory.DRAW
        }
    }

    private fun selectWordByDifficulty(points: Int) {
        // Only update the selected difficulty, don't pick a word yet
        _state.update {
            it.copy(selectedDifficulty = points)
        }
    }

    private fun startTimer() {
        val s = _state.value
        val points = s.selectedDifficulty ?: return

        // Find a word with the selected difficulty that hasn't been used
        val availableWord = s.availableWords
            .filter { it.points == points && !repository.isWordUsed(it.id ?: 0) }
            .randomOrNull()

        val wordToUse = if (availableWord == null) {
            // Try to find any word in the category that hasn't been used
            s.availableWords
                .filter { !repository.isWordUsed(it.id ?: 0) }
                .randomOrNull()
        } else {
            availableWord
        }

        if (wordToUse == null) {
            _state.update { it.copy(error = "Keine Wörter mehr verfügbar!") }
            return
        }

        // Mark word as used in repository (synchronized across all ViewModels)
        wordToUse.id?.let { repository.markWordAsUsed(it) }

        // Set the word and start timer
        _state.update {
            it.copy(
                currentWord = wordToUse,
                availableWords = it.availableWords.filter { w -> w.id != wordToUse.id },
                phase = GamePhase.TIMER_RUNNING,
                timerSeconds = 60,  // 10 seconds for testing
                timerRunning = true,
                timeUp = false
            )
        }

        // Start countdown
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timerSeconds > 0 && _state.value.timerRunning) {
                delay(1000)
                onEvent(GamePlayEvent.TimerTick)
            }
        }
    }

    private fun timerTick() {
        val newSeconds = _state.value.timerSeconds - 1
        if (newSeconds <= 0) {
            timerJob?.cancel()
            _state.update {
                it.copy(
                    timerSeconds = 0,
                    timerRunning = false,
                    timeUp = true,
                    phase = GamePhase.TIME_UP
                )
            }
        } else {
            _state.update { it.copy(timerSeconds = newSeconds) }
        }
    }

    private fun wordGuessed() {
        timerJob?.cancel()
        val s = _state.value
        val points = s.selectedDifficulty ?: return
        val teamId = s.currentTeam?.id ?: return

        // Award points: advance team on board (local)
        repository.advanceTeam(teamId, points)

        // Update team position in backend
        viewModelScope.launch {
            repository.updateTeamPositionInBackend(teamId).onFailure { e ->
                _state.update { it.copy(error = "Position konnte nicht gespeichert werden: ${e.message}") }
            }
        }

        // Advance to next active team for the next round
        repository.advanceToNextTeam()

        _state.update {
            it.copy(
                timerRunning = false,
                pointsAwarded = points,
                navigateToBoard = true
            )
        }
    }

    private fun resetForNextTurn() {
        timerJob?.cancel()

        // Advance to next active team (no points awarded)
        repository.advanceToNextTeam()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

class GamePlayViewModelFactory(
    private val gameId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GamePlayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GamePlayViewModel(gameId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
