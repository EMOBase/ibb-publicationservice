package ibb.api.publicationservice;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PublicationRepository {
    
    @Inject
    Session session;

    public Publication create(Publication publication) {
        session.persist(publication);
        return publication;
    }

    public void delete(Publication publication) {
        session.remove(publication);
    }

    public void deleteAll() {
        session.createMutationQuery("delete from Publication").executeUpdate();
    }

    public Optional<Publication> findById(Long id) {
        return Optional.ofNullable(session.find(Publication.class, id));
    }

    public Optional<Publication> findByDoiOrPmid(String doi) {
        return null;
    }

    public List<Publication> findByGene(String gene) {
        return session.createSelectionQuery(
            "from Publication as p join fetch p.genes as g " +
            "join fetch p.authors as au " +
            "where g = :gene",
            Publication.class
        )
            .setParameter("gene", gene)
            .getResultList();
    }
}
