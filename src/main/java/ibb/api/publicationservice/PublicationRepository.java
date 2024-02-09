package ibb.api.publicationservice;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class PublicationRepository {
    
    @Inject
    ElasticsearchClient esClient;

    @ConfigProperty(name = "publicationservice.elasticsearch.index")
    String index;

    @ConfigProperty(name = "publicationservice.elasticsearch.delete-index-on-start", defaultValue = "false")
    boolean deleteIndexOnStart;

    private static final int MAX_RESULTS = 10000;

    void setup(@Observes StartupEvent ev) {
        try {
            if (deleteIndexOnStart) {
                esClient.indices().delete(i -> i.index(index).ignoreUnavailable(true));
            }
            if (!esClient.indices().exists(i -> i.index(index)).value()) {
                esClient.indices().create(i -> i.index(index));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Publication create(Publication publication) {
        try {
			publication.id = esClient.index(i -> i.index(index).document(publication)).id();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        return publication;
    }

    public void delete(String id) {
        try {
            esClient.delete(i -> i.index(index).id(id));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Publication> findById(String id) {
        try {
            GetResponse<Publication> response = esClient.get(
                g -> g.index(index).id(id),
                Publication.class
            );
            return Optional.ofNullable(response.source()).map(p -> {
                p.id = response.id();
                return p;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Publication> findByGene(String gene) {
        try {
            var response = esClient.search(s -> s
                .index(index)
                .size(MAX_RESULTS)
                .query(q -> q
                    .term(t -> t
                        .field("gene.keyword")
                        .value(gene)
                    )
                ),
                Publication.class);
            return response.hits().hits().stream().map(h -> {
                Publication p = h.source();
                p.id = h.id();
                return p;
            }).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void refresh() {
        try {
            esClient.indices().refresh(i -> i.index(index));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void recreateIndex() {
        try {
            esClient.indices().delete(i -> i.index(index).ignoreUnavailable(true));
            esClient.indices().create(i -> i.index(index));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
