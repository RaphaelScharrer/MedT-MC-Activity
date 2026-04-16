package at.ac.htlleonding.boundary;

import at.ac.htlleonding.dto.ErrorResponse;
import at.ac.htlleonding.dto.WordDTO;
import at.ac.htlleonding.model.Word;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

@Path("/api/words")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WordResource {

    @Context
    UriInfo uriInfo;

    @GET
    public Response getAllWords() {
        try {
            List<WordDTO> words = Word.streamAll()
                    .map(WordDTO::from)
                    .toList();

            return Response.ok(words).build();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.of(500, "Fehler beim Laden der Wörter", uriInfo.getPath()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getWord(@PathParam("id") Long id) {
        Word word = Word.findById(id);

        if (word == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.of(404, "Wort mit ID " + id + " nicht gefunden", uriInfo.getPath()))
                    .build();
        }

        return Response.ok(WordDTO.from(word)).build();
    }

    @GET
    @Path("/random")
    public Response getRandomWord() {
        try {
            Word word = Word.findRandomWord();

            if (word == null) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.of(404, "Keine Wörter in der Datenbank vorhanden", uriInfo.getPath()))
                        .build();
            }

            return Response.ok(WordDTO.from(word)).build();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.of(500, "Fehler beim Laden eines zufälligen Worts", uriInfo.getPath()))
                    .build();
        }
    }

    @GET
    @Path("/random/{category}")
    public Response getRandomWordByCategory(@PathParam("category") Word.WordCategory category) {
        try {
            Word word = Word.findRandomWordByCategory(category);

            if (word == null) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.of(404, "Keine Wörter in der Kategorie " + category + " gefunden", uriInfo.getPath()))
                        .build();
            }

            return Response.ok(WordDTO.from(word)).build();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.of(500, "Fehler beim Laden eines zufälligen Worts", uriInfo.getPath()))
                    .build();
        }
    }

    @GET
    @Path("/minpoints/{points}")
    public Response getWordsByMinPoints(@PathParam("points") Integer minPoints) {
        try {
            List<WordDTO> words = Word.streamByMinPoints(minPoints)
                    .map(WordDTO::from)
                    .toList();

            return Response.ok(words).build();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.of(500, "Fehler beim Laden der Wörter", uriInfo.getPath()))
                    .build();
        }
    }

    @POST
    @Transactional
    public Response createWord(@Valid WordDTO dto) {
        try {
            // Validierung: Wort und Definition dürfen nicht leer sein
            if (dto.word() == null || dto.word().trim().isEmpty()) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.of(400, "Wort darf nicht leer sein", uriInfo.getPath()))
                        .build();
            }

            if (dto.definition() == null || dto.definition().trim().isEmpty()) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.of(400, "Definition darf nicht leer sein", uriInfo.getPath()))
                        .build();
            }

            // Validierung: Punkte müssen positiv sein
            if (dto.points() == null || dto.points() < 0) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.of(400, "Punkte müssen mindestens 0 sein", uriInfo.getPath()))
                        .build();
            }

            // Prüfen ob Wort bereits existiert
            Word existing = Word.findByWord(dto.word());
            if (existing != null) {
                return Response
                        .status(Response.Status.CONFLICT)
                        .entity(ErrorResponse.of(409, "Wort '" + dto.word() + "' existiert bereits", uriInfo.getPath()))
                        .build();
            }

            Word word = dto.toEntity();
            word.persist();

            URI location = uriInfo.getAbsolutePathBuilder().path(word.id.toString()).build();
            return Response
                    .created(location)
                    .entity(WordDTO.from(word))
                    .build();

        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.of(500, "Fehler beim Erstellen des Worts: " + e.getMessage(), uriInfo.getPath()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateWord(@PathParam("id") Long id, @Valid WordDTO dto) {
        Word word = Word.findById(id);

        if (word == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.of(404, "Wort mit ID " + id + " nicht gefunden", uriInfo.getPath()))
                    .build();
        }

        try {
            // Wort aktualisieren
            if (dto.word() != null && !dto.word().trim().isEmpty()) {
                // Prüfen ob neues Wort bereits existiert (außer bei sich selbst)
                Word existing = Word.findByWord(dto.word());
                if (existing != null && !existing.id.equals(id)) {
                    return Response
                            .status(Response.Status.CONFLICT)
                            .entity(ErrorResponse.of(409, "Wort '" + dto.word() + "' existiert bereits", uriInfo.getPath()))
                            .build();
                }
                word.word = dto.word();
            }

            // Definition aktualisieren
            if (dto.definition() != null && !dto.definition().trim().isEmpty()) {
                word.definition = dto.definition();
            }

            // Punkte aktualisieren
            if (dto.points() != null && dto.points() >= 0) {
                word.points = dto.points();
            }

            // Kategorie aktualisieren
            if (dto.category() != null) {
                word.category = dto.category();
            }

            word.persist();
            return Response.ok(WordDTO.from(word)).build();

        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.of(500, "Fehler beim Aktualisieren des Worts: " + e.getMessage(), uriInfo.getPath()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteWord(@PathParam("id") Long id) {
        Word word = Word.findById(id);


        if (word == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.of(404, "Wort mit ID " + id + " nicht gefunden", uriInfo.getPath()))
                    .build();
        }

        try {
            word.delete();
            return Response.noContent().build();
        } catch (Exception e) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.of(500, "Fehler beim Löschen des Worts: " + e.getMessage(), uriInfo.getPath()))
                    .build();
        }
    }
}