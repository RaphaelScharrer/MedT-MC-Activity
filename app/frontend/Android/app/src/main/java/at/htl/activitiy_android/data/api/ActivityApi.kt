package at.htl.activitiy_android.data.api

import at.htl.activitiy_android.domain.model.Game
import at.htl.activitiy_android.domain.model.Player
import at.htl.activitiy_android.domain.model.Team
import at.htl.activitiy_android.domain.model.Word
import retrofit2.http.*

interface ActivityApi {

    // ========== GAME ENDPOINTS ==========

    @GET("games")
    suspend fun getAllGames(): List<Game>

    @GET("games/{id}")
    suspend fun getGame(@Path("id") id: Long): Game

    @POST("games")
    suspend fun createGame(@Body game: Game): Game

    @PUT("games/{id}")
    suspend fun updateGame(@Path("id") id: Long, @Body game: Game): Game

    @DELETE("games/{id}")
    suspend fun deleteGame(@Path("id") id: Long)

    @DELETE("games")
    suspend fun deleteAllGames()

    // ========== TEAM ENDPOINTS ==========

    @GET("teams")
    suspend fun getAllTeams(): List<Team>

    @GET("teams/{id}")
    suspend fun getTeam(@Path("id") id: Long): Team

    @GET("teams/position/{position}")
    suspend fun getTeamByPosition(@Path("position") position: Int): Team

    @POST("teams")
    suspend fun createTeam(@Body team: Team): Team

    @PUT("teams/{id}")
    suspend fun updateTeam(@Path("id") id: Long, @Body team: Team): Team

    @DELETE("teams/{id}")
    suspend fun deleteTeam(@Path("id") id: Long)

    @DELETE("teams")
    suspend fun deleteAllTeams()

    // ========== PLAYER ENDPOINTS ==========

    @GET("players")
    suspend fun getAllPlayers(): List<Player>

    @GET("players/{id}")
    suspend fun getPlayer(@Path("id") id: Long): Player

    @GET("players/team/{teamId}")
    suspend fun getPlayersByTeam(@Path("teamId") teamId: Long): List<Player>

    @POST("players")
    suspend fun createPlayer(@Body player: Player): Player

    @PUT("players/{id}")
    suspend fun updatePlayer(@Path("id") id: Long, @Body player: Player): Player

    @DELETE("players/{id}")
    suspend fun deletePlayer(@Path("id") id: Long)

    // ========== WORD ENDPOINTS ==========

    @GET("api/words")
    suspend fun getAllWords(): List<Word>

    @GET("api/words/{id}")
    suspend fun getWord(@Path("id") id: Long): Word

    @GET("api/words/random")
    suspend fun getRandomWord(): Word

    @GET("api/words/random/{category}")
    suspend fun getRandomWordByCategory(@Path("category") category: String): Word

    @GET("api/words/minpoints/{points}")
    suspend fun getWordsByMinPoints(@Path("points") minPoints: Int): List<Word>

    @POST("api/words")
    suspend fun createWord(@Body word: Word): Word

    @PUT("api/words/{id}")
    suspend fun updateWord(@Path("id") id: Long, @Body word: Word): Word

    @DELETE("api/words/{id}")
    suspend fun deleteWord(@Path("id") id: Long)
}