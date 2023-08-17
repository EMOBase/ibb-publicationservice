package ibb.api;

import java.util.List;

import org.jboss.resteasy.reactive.RestQuery;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/publications")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublicationResource {

    @GET
    @Path("{id}")
    public Uni<Publication> fetchOne(Long id) {
        return Publication.findById(id);
    }

    @GET
    public Uni<List<Publication>> fetchAll(@NotBlank @RestQuery String gene) {
        return Publication.findByGene(gene);
    }

    @POST
    public Uni<Response> create(
        @Valid
        @ConvertGroup(to = Publication.ValidationGroups.Post.class)
        Publication publication
    ) {
        return Panache.withTransaction(publication::persist)
            .replaceWith(Response.ok(publication).status(Status.CREATED)::build);
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(
        Long id,
        
        @NotNull
        @Valid
        @ConvertGroup(to = Publication.ValidationGroups.Put.class)
        Publication publication
    ) {

        return Panache.withTransaction(() -> Publication.<Publication>findById(id)
                .onItem().ifNotNull().invoke(entity -> {
                    entity.pmid = publication.pmid;
                    entity.doi = publication.doi;
                    entity.firstAuthor = publication.firstAuthor;
                    entity.title = publication.title;
                    entity.summary = publication.summary;
                    entity.reference = publication.reference;
                    entity.year = publication.year;
                })
            )
            .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
            .onItem().ifNull().continueWith(Response.ok().status(Status.NOT_FOUND)::build);
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(Long id) {
        return Panache.withTransaction(() -> Publication.deleteById(id))
            .map(deleted -> deleted
                ? Response.ok().status(Status.NO_CONTENT).build()
                : Response.ok().status(Status.NOT_FOUND).build()
            );
    }

    @POST
    @Path("{id}/genes/{gene}")
    public Uni<Response> addGene(Long id, String gene) {
        return Panache.withTransaction(() -> Publication.<Publication>findById(id)
                .onItem().ifNotNull().invoke(entity -> entity.genes.add(gene))
            )
            .onItem().ifNotNull().transform(entity -> Response.ok().build())
            .onItem().ifNull().continueWith(Response.ok().status(Status.NOT_FOUND)::build);
    }

    @DELETE
    @Path("{id}/genes/{gene}")
    public Uni<Response> removeGene(Long id, String gene) {
        return Panache.withTransaction(() -> Publication.<Publication>findById(id)
                .onItem().ifNotNull().invoke(entity -> entity.genes.remove(gene))
            )
            .onItem().ifNotNull().transform(entity -> Response.ok().build())
            .onItem().ifNull().continueWith(Response.ok().status(Status.NOT_FOUND)::build);
    }
}
