package at.ac.htlleonding.boundary;

import at.ac.htlleonding.dto.ErrorResponse;
import at.ac.htlleonding.dto.TeamDTO;
import at.ac.htlleonding.model.Game;
import at.ac.htlleonding.model.Team;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

@Path("/teams")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TeamResource {

    @Context
    UriInfo uriInfo;

    @GET
    public Response getAllTeams() {
        try {
            List<TeamDTO> teams = Team.<Team>listAll().stream()
                    .map(TeamDTO::new)
                    .toList();

            return Response.ok(teams).build();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.of(500, "Fehler beim Laden der Teams", uriInfo.getPath()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getTeamById(@PathParam("id") Long id) {
        Team team = Team.findById(id);
        if (team == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(new TeamDTO(team)).build();
    }

    @GET
    @Path("/position/{position}")
    public Response getTeamByPosition(@PathParam("position") Integer position) {
        Team team = Team.findByPosition(position);
        if (team == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(new TeamDTO(team)).build();
    }

    @POST
    @Transactional
    public Response createTeam(TeamDTO teamDTO) {
        Team team = teamDTO.toEntity();

        if (teamDTO.gameId() != null) {
            Game game = Game.findById(teamDTO.gameId());
            if (game == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Game not found")
                        .build();
            }
            team.game = game;
        }

        team.persist();
        return Response.status(Response.Status.CREATED)
                .entity(new TeamDTO(team))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateTeam(@PathParam("id") Long id, TeamDTO teamDTO) {
        Team team = Team.findById(id);
        if (team == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        team.position = teamDTO.position();

        if (teamDTO.gameId() != null) {
            Game game = Game.findById(teamDTO.gameId());
            if (game == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Game not found")
                        .build();
            }
            team.game = game;
        }

        return Response.ok(new TeamDTO(team)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteTeam(@PathParam("id") Long id) {
        Team team = Team.findById(id);
        if (team == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        team.delete();
        return Response.noContent().build();
    }

    @DELETE
    @Transactional
    public Response deleteAllTeams() {
        Team.deleteAllTeams();
        return Response.noContent().build();
    }
}