package at.ac.htlleonding.boundary;

import at.ac.htlleonding.dto.GameDTO;
import at.ac.htlleonding.model.Game;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;

@Path("/games")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameResource {

    @GET
    public List<GameDTO> getAllGames() {
        return Game.<Game>listAll().stream()
                .map(GameDTO::new)
                .toList();
    }

    @GET
    @Path("/{id}")
    public Response getGameById(@PathParam("id") Long id) {
        Game game = Game.findById(id);
        if (game == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(new GameDTO(game)).build();
    }

    @POST
    @Transactional
    public Response createGame(GameDTO gameDTO) {
        Game game = gameDTO.toEntity();
        game.createdOn = LocalDateTime.now();
        game.persist();
        return Response.status(Response.Status.CREATED)
                .entity(new GameDTO(game))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateGame(@PathParam("id") Long id, GameDTO gameDTO) {
        Game game = Game.findById(id);
        if (game == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        game.name = gameDTO.name();
        return Response.ok(new GameDTO(game)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteGame(@PathParam("id") Long id) {
        Game game = Game.findById(id);
        if (game == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        game.delete();
        return Response.noContent().build();
    }

    @DELETE
    @Transactional
    public Response deleteAllGames() {
        Game.deleteAll();
        return Response.noContent().build();
    }
}