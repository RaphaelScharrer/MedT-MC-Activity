package at.htl.activitiy_android.data.repository

import at.htl.activitiy_android.data.api.RetrofitInstance
import at.htl.activitiy_android.domain.model.Game
import at.htl.activitiy_android.domain.model.Player
import at.htl.activitiy_android.domain.model.Team
import at.htl.activitiy_android.domain.model.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * GameRepository - Single Source of Truth für alle Spieldaten.
 * Singleton-Pattern sorgt dafür, dass alle ViewModels dieselben Daten sehen.
 */
object GameRepository {

    private val api = RetrofitInstance.api
    private val mutex = Mutex()

    // ========== CURRENT GAME SESSION ==========
    private val _currentSession = MutableStateFlow(GameSession())
    val currentSession: StateFlow<GameSession> = _currentSession

    // ========== CACHED DATA ==========
    private val _allWords = MutableStateFlow<List<Word>>(emptyList())
    val allWords: StateFlow<List<Word>> = _allWords

    private var wordsLoaded = false

    // ========== SESSION MANAGEMENT ==========

    fun startNewSession(gameId: Long) {
        _currentSession.update {
            GameSession(
                gameId = gameId,
                usedWordIds = emptySet()
            )
        }
    }

    fun getCurrentGameId(): Long? = _currentSession.value.gameId

    // ========== GAME OPERATIONS ==========

    suspend fun createGame(name: String): Result<Game> = runCatching {
        mutex.withLock {
            val newGame = Game(id = null, name = name, createdOn = null, teamIds = null)
            val created = api.createGame(newGame)
            _currentSession.update { it.copy(gameId = created.id, game = created) }
            created
        }
    }

    suspend fun loadGame(gameId: Long): Result<Game> = runCatching {
        val game = api.getGame(gameId)
        _currentSession.update { it.copy(gameId = gameId, game = game) }
        game
    }

    suspend fun updateGame(gameId: Long, name: String): Result<Game> = runCatching {
        val current = _currentSession.value.game
        val updated = Game(
            id = gameId,
            name = name,
            createdOn = current?.createdOn,
            teamIds = current?.teamIds
        )
        val result = api.updateGame(gameId, updated)
        _currentSession.update { it.copy(game = result) }
        result
    }

    suspend fun getAllGames(): Result<List<Game>> = runCatching {
        api.getAllGames()
    }

    suspend fun deleteGameWithAll(gameId: Long): Result<Unit> = runCatching {
        mutex.withLock {
            // Alle Teams des Spiels laden
            val teams = api.getAllTeams().filter { it.gameId == gameId }

            // Alle Spieler jedes Teams löschen
            teams.forEach { team ->
                team.id?.let { teamId ->
                    api.getPlayersByTeam(teamId).forEach { player ->
                        player.id?.let { api.deletePlayer(it) }
                    }
                }
            }

            // Alle Teams löschen
            teams.forEach { team ->
                team.id?.let { api.deleteTeam(it) }
            }

            // Spiel löschen
            api.deleteGame(gameId)
        }
    }

    // ========== TEAM OPERATIONS ==========

    suspend fun loadTeamsForGame(gameId: Long): Result<List<Team>> = runCatching {
        val allTeams = api.getAllTeams()
        // Sortierung nach ID sichert die ursprüngliche Erstellungsreihenfolge
        // und damit eine stabile Farb-Zuweisung (unabhängig von board position)
        val teams = allTeams
            .filter { it.gameId == gameId }
            .sortedBy { it.id ?: 0L }
            .mapIndexed { index, team -> team.copy(colorIndex = index % 4) }
        _currentSession.update { it.copy(teams = teams) }
        teams
    }

    suspend fun createTeam(team: Team): Result<Team> = runCatching {
        mutex.withLock {
            val created = api.createTeam(team)
            _currentSession.update { session ->
                session.copy(teams = session.teams + created)
            }
            created
        }
    }

    suspend fun deleteTeam(teamId: Long): Result<Unit> = runCatching {
        mutex.withLock {
            api.deleteTeam(teamId)
            _currentSession.update { session ->
                session.copy(teams = session.teams.filter { it.id != teamId })
            }
        }
    }

    suspend fun deleteAllTeamsForGame(gameId: Long): Result<Unit> = runCatching {
        mutex.withLock {
            val teams = _currentSession.value.teams.filter { it.gameId == gameId }
            teams.forEach { team ->
                team.id?.let { api.deleteTeam(it) }
            }
            _currentSession.update { session ->
                session.copy(
                    teams = session.teams.filter { it.gameId != gameId },
                    players = session.players.filter { player ->
                        teams.none { it.id == player.team }
                    }
                )
            }
        }
    }

    fun getTeams(): List<Team> = _currentSession.value.teams

    // ========== PLAYER OPERATIONS ==========

    suspend fun loadPlayersForGame(gameId: Long): Result<List<Player>> = runCatching {
        val teams = _currentSession.value.teams.ifEmpty {
            loadTeamsForGame(gameId).getOrThrow()
        }
        val teamIds = teams.map { it.id }
        val allPlayers = api.getAllPlayers()
        val players = allPlayers.filter { it.team in teamIds }
        _currentSession.update { it.copy(players = players) }
        players
    }

    suspend fun createPlayer(player: Player): Result<Player> = runCatching {
        mutex.withLock {
            // Check for duplicate name in current session
            val existingNames = _currentSession.value.players.map { it.name.lowercase() }
            if (player.name.lowercase() in existingNames) {
                throw IllegalStateException("Spieler '${player.name}' existiert bereits")
            }

            val created = api.createPlayer(player)
            _currentSession.update { session ->
                session.copy(players = session.players + created)
            }
            created
        }
    }

    suspend fun updatePlayer(player: Player): Result<Player> = runCatching {
        mutex.withLock {
            val id = player.id ?: throw IllegalArgumentException("Player ID required")
            val updated = api.updatePlayer(id, player)
            _currentSession.update { session ->
                session.copy(
                    players = session.players.map {
                        if (it.id == id) updated else it
                    }
                )
            }
            updated
        }
    }

    suspend fun deletePlayer(playerId: Long): Result<Unit> = runCatching {
        mutex.withLock {
            api.deletePlayer(playerId)
            _currentSession.update { session ->
                session.copy(players = session.players.filter { it.id != playerId })
            }
        }
    }

    fun getPlayers(): List<Player> = _currentSession.value.players

    // ========== WORD OPERATIONS ==========

    suspend fun loadAllWords(): Result<List<Word>> = runCatching {
        if (!wordsLoaded) {
            val words = api.getAllWords()
            _allWords.value = words
            wordsLoaded = true
        }
        _allWords.value
    }

    fun markWordAsUsed(wordId: Long) {
        _currentSession.update { session ->
            session.copy(usedWordIds = session.usedWordIds + wordId)
        }
    }

    fun isWordUsed(wordId: Long): Boolean {
        return wordId in _currentSession.value.usedWordIds
    }

    fun getAvailableWords(): List<Word> {
        val usedIds = _currentSession.value.usedWordIds
        return _allWords.value.filter { it.id !in usedIds }
    }

    fun getUsedWordCount(): Int = _currentSession.value.usedWordIds.size

    // ========== BOARD POSITION OPERATIONS ==========

    fun initializeBoardPositions(teams: List<Team>) {
        _currentSession.update { session ->
            val positions = teams.associate { (it.id ?: 0L) to 0 }
            session.copy(teamBoardPositions = positions)
        }
    }

    fun restoreBoardPositionsFromBackend(teams: List<Team>) {
        _currentSession.update { session ->
            val positions = teams.associate { (it.id ?: 0L) to it.position }
            val finished = teams.filter { it.position >= 16 }.mapNotNull { it.id }.toSet()
            session.copy(teamBoardPositions = positions, finishedTeamIds = finished)
        }
    }

    fun getTeamBoardPosition(teamId: Long): Int {
        return _currentSession.value.teamBoardPositions[teamId] ?: 0
    }

    fun advanceTeam(teamId: Long, points: Int) {
        _currentSession.update { session ->
            val currentPos = session.teamBoardPositions[teamId] ?: 0
            val newPos = (currentPos + points).coerceAtMost(16)
            val newPositions = session.teamBoardPositions + (teamId to newPos)
            val newFinished = if (newPos >= 16) session.finishedTeamIds + teamId else session.finishedTeamIds
            session.copy(
                teamBoardPositions = newPositions,
                finishedTeamIds = newFinished
            )
        }
    }

    fun isTeamFinished(teamId: Long): Boolean {
        return teamId in _currentSession.value.finishedTeamIds
    }

    fun getActiveTeams(): List<Team> {
        val finished = _currentSession.value.finishedTeamIds
        return _currentSession.value.teams.filter { it.id !in finished }
    }

    // ========== TURN MANAGEMENT ==========

    fun getCurrentTeamIndex(): Int = _currentSession.value.currentTeamIndex

    fun setCurrentTeamIndex(index: Int) {
        _currentSession.update { it.copy(currentTeamIndex = index) }
    }

    fun advanceToNextTeam(): Team? {
        val session = _currentSession.value
        val activeTeams = session.teams.filter { it.id !in session.finishedTeamIds }
        if (activeTeams.isEmpty()) return null

        val nextIndex = (session.currentTeamIndex + 1) % activeTeams.size
        _currentSession.update { it.copy(currentTeamIndex = nextIndex) }
        return activeTeams.getOrNull(nextIndex)
    }

    // ========== SESSION CLEANUP ==========

    fun clearSession() {
        _currentSession.value = GameSession()
    }

    fun resetWordTracking() {
        _currentSession.update { it.copy(usedWordIds = emptySet()) }
    }

    // ========== UPDATE TEAM POSITION IN BACKEND ==========

    suspend fun updateTeamPositionInBackend(teamId: Long): Result<Unit> = runCatching {
        val session = _currentSession.value
        val team = session.teams.find { it.id == teamId }
            ?: throw IllegalArgumentException("Team $teamId not found")
        val boardPosition = session.teamBoardPositions[teamId]
            ?: throw IllegalArgumentException("Board position for team $teamId not found")
        api.updateTeam(teamId, team.copy(position = boardPosition))
    }

    // ========== SAVE GAME STATE ==========

    suspend fun saveGameState(): Result<Unit> = runCatching {
        val session = _currentSession.value
        session.teams.forEach { team ->
            val teamId = team.id ?: return@forEach
            val boardPosition = session.teamBoardPositions[teamId] ?: team.position
            api.updateTeam(teamId, team.copy(position = boardPosition))
        }
    }

    // ========== REFRESH ==========

    suspend fun refreshCurrentGame(): Result<Unit> = runCatching {
        val gameId = _currentSession.value.gameId ?: return@runCatching
        loadGame(gameId)
        loadTeamsForGame(gameId)
        loadPlayersForGame(gameId)
    }
}

/**
 * GameSession - Hält den aktuellen Spielzustand.
 */
data class GameSession(
    val gameId: Long? = null,
    val game: Game? = null,
    val teams: List<Team> = emptyList(),
    val players: List<Player> = emptyList(),
    val usedWordIds: Set<Long> = emptySet(),
    val currentTeamIndex: Int = 0,
    val teamBoardPositions: Map<Long, Int> = emptyMap(),
    val finishedTeamIds: Set<Long> = emptySet()
)
