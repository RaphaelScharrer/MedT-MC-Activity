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
                val teams = repository.getTeams().filter { it.gameId == gameId }

                // Determine category based on first team's position
                val firstTeam = teams.firstOrNull()
                val category = getCategoryForTeamPosition(firstTeam?.position ?: 0)

                // Get available words (filters out already used words)
                val availableWords = repository.getAvailableWords()
                val categoryWords = availableWords.filter { it.category == category }

                _state.update {
                    it.copy(
                        gameName = session.game?.name ?: "Spiel",
                        teams = teams,
                        currentTeam = firstTeam,
                        currentTeamIndex = 0,
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

    private fun getCategoryForTeamPosition(position: Int): WordCategory {
        return when (position % 3) {
            0 -> WordCategory.DRAW
            1 -> WordCategory.ACT
            else -> WordCategory.DESCRIBE
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
                timerSeconds = 10,  // 10 seconds for testing
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

    private fun resetForNextTurn() {
        timerJob?.cancel()

        val s = _state.value
        val nextIndex = (s.currentTeamIndex + 1) % s.teams.size
        val nextTeam = s.teams.getOrNull(nextIndex)
        val nextCategory = getCategoryForTeamPosition(nextTeam?.position ?: 0)

        // Get available words from repository (automatically excludes used words)
        val availableWords = repository.getAvailableWords()
        val categoryWords = availableWords.filter { it.category == nextCategory }

        _state.update {
            it.copy(
                currentTeamIndex = nextIndex,
                currentTeam = nextTeam,
                currentCategory = nextCategory,
                currentWord = null,
                selectedDifficulty = null,
                availableWords = categoryWords,
                timerSeconds = 10,  // 10 seconds for testing
                timerRunning = false,
                timeUp = false,
                phase = GamePhase.WORD_SELECTION
            )
        }
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
