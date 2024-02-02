package ibb.api.publicationservice.exceptionmapper;

import java.util.Map;

import io.quarkus.logging.Log;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CustomExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable throwable) {
        int status = 500;
        String message = "Unknown error";

        if (throwable instanceof WebApplicationException) {
            WebApplicationException exception = (WebApplicationException) throwable;
            message = exception.getMessage();
            status = exception.getResponse().getStatus();
        } else {
            Log.error(throwable.getMessage(), throwable);
        }

        return Response.status(status)
            .entity(Map.of("error", message))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
    
}
