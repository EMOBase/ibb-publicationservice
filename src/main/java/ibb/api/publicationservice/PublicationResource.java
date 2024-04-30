package ibb.api.publicationservice;

import java.util.List;

import org.jboss.resteasy.reactive.RestPath;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/publications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublicationResource {

    @Inject
    PublicationRepository publicationRepository;

    @GET
    @Path("{id}")
    public Publication findOne(@RestPath @NotBlank String id) {
        return publicationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Publication does not exist"));
    }

    @GET
    @Path("/by-gene/{gene}")
    public List<Publication> findByGene(@RestPath @NotBlank String gene) {
        return publicationRepository.findByGene(gene);
    }

    @POST
    public Publication create(
        @NotNull
        @Valid
        @ConvertGroup(to = Publication.ValidationGroups.Create.class)
        Publication publication
    ) {
        publicationRepository.findByGene(publication.gene).stream()
            .filter(p -> p.doi.equals(publication.doi))
            .findAny()
            .ifPresent(p -> {
                throw new BadRequestException("DOI already exists");
            }); 
        return publicationRepository.create(publication);
    }

    @DELETE
    @Authenticated
    @Path("{id}")
    public void delete(@NotBlank String id) {
        publicationRepository.delete(id);
    }
}
