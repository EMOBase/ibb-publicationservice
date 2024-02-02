package ibb.api.publicationservice;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/publications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublicationResource {

    @Inject
    PublicationRepository publicationRepository;

    @GET
    @Path("{id}")
    public Response findOne(@PathParam("id") Long id) {
        return publicationRepository.findById(id)
            .map(publication -> Response.ok(publication).build())
            .orElseThrow(() -> new NotFoundException("Publication does not exist"));
    }

    @GET
    @Path("/by-gene/{gene}")
    public Response findByGene(@PathParam("gene") String gene) {
        return Response.ok(publicationRepository.findByGene(gene)).build();
    }

    @POST
    @Transactional
    public Response create(
        @NotNull(message = "Publication is required")
        @Valid
        @ConvertGroup(to = Publication.ValidationGroups.Post.class)
        Publication publication
    ) {;
        return Response.ok(publicationRepository.create(publication))
            .status(Status.CREATED)
            .build();
    }

    @DELETE
    @Transactional
    @Path("{id}")
    public Response delete(Long id) {
        Publication publication = publicationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Publication does not exist"));

        publicationRepository.delete(publication);
        return Response.noContent().build();
    }
}
