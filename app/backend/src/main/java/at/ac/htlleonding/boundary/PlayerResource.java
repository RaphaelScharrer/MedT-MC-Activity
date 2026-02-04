package at.ac.htlleonding.boundary;

import at.ac.htlleonding.dto.ErrorResponse;
import at.ac.htlleonding.dto.PlayerDTO;
import at.ac.htlleonding.model.Player;
import at.ac.htlleonding.model.Team;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

@Path("/players")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlayerResource {

    @Context
    UriInfo uriInfo;

    @GET
    public Response getAllPlayers() {
        try {
            // ✅ FIX: Use listAll() instead of streamAll()
            List<PlayerDTO> players = Player.<Player>listAll().stream()
                    .map(PlayerDTO::from)
                    .toList();

            return Response.ok(players).build();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.of(500, "Fehler beim Laden der Spieler", uriInfo.getPath()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getPlayer(@PathParam("id") Long id) {
        Player player = Player.findById(id);

        if (player == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.of(404, "Spieler mit ID " + id + " nicht gefunden", uriInfo.getPath()))
                    .build();
        }

        return Response.ok(PlayerDTO.from(player)).build();
    }

    @GET
    @Path("/team/{teamId}")
    public Response getPlayersByTeam(@PathParam("teamId") Long teamId) {
        Team team = Team.findById(teamId);

        if (team == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.of(404, "Team mit ID " + teamId + " nicht gefunden", uriInfo.getPath()))
                    .build();
        }

        try {
            // ✅ FIX: Use list() instead of streamByTeam()
            List<PlayerDTO> players = Player.<Player>list("team", team).stream()
                    .map(PlayerDTO::from)
                    .toList();

            return Response.ok(players).build();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.of(500, "Fehler beim Laden der Spieler", uriInfo.getPath()))
                    .build();
        }
    }

    @POST
    @Transactional
    public Response createPlayer(@Valid PlayerDTO dto) {
        try {
            // Validierung: Name darf nicht leer sein
            if (dto.name() == null || dto.name().trim().isEmpty()) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.of(400, "Spielername darf nicht leer sein", uriInfo.getPath()))
                        .build();
            }

            // Team laden wenn angegeben
            Team team = null;
            if (dto.team() != null) {
                team = Team.findById(dto.team());
                if (team == null) {
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(ErrorResponse.of(400, "Team mit ID " + dto.team() + " nicht gefunden", uriInfo.getPath()))
                            .build();
                }
            }

            // Prüfen ob Spieler mit diesem Namen bereits im selben Team existiert
            Player existing = Player.findByNameAndTeam(dto.name(), team);
            if (existing != null) {
                return Response
                        .status(Response.Status.CONFLICT)
                        .entity(ErrorResponse.of(409, "Spieler mit Name '" + dto.name() + "' existiert bereits in diesem Team", uriInfo.getPath()))
                        .build();
            }

            Player player = dto.toEntity(team);
            player.persist();

            URI location = uriInfo.getAbsolutePathBuilder().path(player.id.toString()).build();
            return Response
                    .created(location)
                    .entity(PlayerDTO.from(player))
                    .build();

        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.of(500, "Fehler beim Erstellen des Spielers: " + e.getMessage(), uriInfo.getPath()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updatePlayer(@PathParam("id") Long id, @Valid PlayerDTO dto) {
        Player player = Player.findById(id);

        if (player == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.of(404, "Spieler mit ID " + id + " nicht gefunden", uriInfo.getPath()))
                    .build();
        }

        try {
            // Team laden (für Duplikat-Check)
            Team newTeam = player.team;
            if (dto.team() != null) {
                newTeam = Team.findById(dto.team());
                if (newTeam == null) {
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(ErrorResponse.of(400, "Team mit ID " + dto.team() + " nicht gefunden", uriInfo.getPath()))
                            .build();
                }
            } else {
                newTeam = null;
            }

            // Name aktualisieren
            if (dto.name() != null && !dto.name().trim().isEmpty()) {
                // Prüfen ob neuer Name bereits im Ziel-Team existiert (außer bei sich selbst)
                Player existing = Player.findByNameAndTeam(dto.name(), newTeam);
                if (existing != null && !existing.id.equals(id)) {
                    return Response
                            .status(Response.Status.CONFLICT)
                            .entity(ErrorResponse.of(409, "Spieler mit Name '" + dto.name() + "' existiert bereits in diesem Team", uriInfo.getPath()))
                            .build();
                }
                player.name = dto.name();
            }

            // Team aktualisieren
            player.team = newTeam;

            // Punkte aktualisieren
            if (dto.pointsEarned() != null) {
                player.pointsEarned = dto.pointsEarned();
            }

            player.persist();
            return Response.ok(PlayerDTO.from(player)).build();

        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.of(500, "Fehler beim Aktualisieren des Spielers: " + e.getMessage(), uriInfo.getPath()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deletePlayer(@PathParam("id") Long id) {
        Player player = Player.findById(id);

        if (player == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.of(404, "Spieler mit ID " + id + " nicht gefunden", uriInfo.getPath()))
                    .build();
        }

        try {
            player.delete();
            return Response.noContent().build();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.of(500, "Fehler beim Löschen des Spielers: " + e.getMessage(), uriInfo.getPath()))
                    .build();
        }
    }
}