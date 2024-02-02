package ibb.api.publicationservice.exceptionmapper;

import java.util.Map;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * This can not be handled in CustomExceptionMapper
 * because Quarkus has a default ExceptionMapper for ConstraintViolationException
 * which has higher priority than CustomExceptionMapper
 * https://github.com/quarkusio/quarkus/issues/12006
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations()
            .stream()
            .map(ConstraintViolation::getMessage)
            .findFirst()
            .orElse("Unknown constraint violation error");
        return Response.status(Status.BAD_REQUEST).entity(Map.of("error", message)).build();
    }
    
}
