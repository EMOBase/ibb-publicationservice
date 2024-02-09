package ibb.api.publicationservice;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;

@QuarkusTest
@TestHTTPEndpoint(PublicationResource.class)
public class PublicationResourceTest {

    @Inject
    PublicationRepository publicationRepository;

    @Inject
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        publicationRepository.recreateIndex();
        publicationRepository.refresh();
    }

    @Test
    public void testCreate_ShouldSucceed_WhenAllFieldsAreValid() {
        givenJSONFromFile("publication_valid_full.json")
            .when().post()
            .then()
                .statusCode(200)
                .body(
                    "id", notNullValue(),
                    "pmid", notNullValue(),
                    "doi", notNullValue(),
                    "authors", notNullValue(),
                    "authors.size()", equalTo(8),
                    "title", notNullValue(),
                    "abstract", notNullValue(),
                    "reference", notNullValue(),
                    "journal", notNullValue(),
                    "year", notNullValue()
                );
    }

    @Test
    public void testCreate_ShouldSucceed_WhenPmidIsMissing() {
        givenJSONFromFile("publication_valid_without_pmid.json")
            .when().post()
            .then()
                .body("error", nullValue())
                .statusCode(200)
                .body(
                    "id", notNullValue(),
                    "pmid", nullValue(),
                    "doi", notNullValue(),
                    "authors", notNullValue(),
                    "authors.size()", equalTo(8),
                    "title", notNullValue(),
                    "abstract", notNullValue(),
                    "reference", notNullValue(),
                    "journal", notNullValue(),
                    "year", notNullValue()
                );
    }

    @Test
    public void testCreate_ShouldFail_WhenDoiIsMissing() {
        Publication publication = getValidPublication();
        publication.doi = null;

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400);
    }

    @Test
    public void testCreate_ShouldFail_WhenDoiIsEmptyString() {
        Publication publication = getValidPublication();
        publication.doi = "";

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400);
    }

    @Test
    public void testCreate_ShouldFail_WhenAuthorsAreMissing() {
        Publication publication = getValidPublication();
        publication.authors = null;

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400);
    }

    @Test
    public void testCreate_ShouldFail_WhenAuthorsAreEmptyList() {
        Publication publication = getValidPublication();
        publication.authors = Collections.emptyList();

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400);
    }

    @Test
    public void testCreate_ShouldFail_WhenGeneIsMissing() {
        Publication publication = getValidPublication();
        publication.gene = null;

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400);
    }
    
    @Test
    public void testCreate_ShouldFail_WhenGeneIsEmptyString() {
        Publication publication = getValidPublication();
        publication.gene = "";

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400);
    }

    @Test
    public void testCreate_ShouldFail_WhenTitleIsMissing() {
        Publication publication = getValidPublication();
        publication.title = null;

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400);
    }
    
    @Test
    public void testCreate_ShouldFail_WhenAbstractIsMissing() {
        Publication publication = getValidPublication();
        publication.summary = null;

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400);
    }

    @Test
    public void testCreate_ShouldFail_WhenReferenceIsMissing() {
        Publication publication = getValidPublication();
        publication.reference = null;

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400);
    }
        
    @Test
    public void testCreate_ShouldFail_WhenYearIsMissing() {
        Publication publication = getValidPublication();
        publication.year = null;

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400);
    }

    @Test
    public void testCreate_ShouldFail_WhenYearIsInTheFuture() {
        Publication publication = getValidPublication();
        publication.year = Year.of(2023000);

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400);
    }

    @Test
    public void testCreate_ShouldFail_WhenIdIsPresent() {
        Publication publication = getValidPublication();
        publication.id = "123";

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400);
    }

    @Test
    public void testCreate_ShouldFail_WhenDoiAlreadyExists() {
        Publication dbPublication = createExamplePublication();
        Publication submitPublication = getValidPublication();
        submitPublication.doi = dbPublication.doi;

        givenJSON()
            .body(submitPublication)
            .when().post()
            .then()
                .statusCode(400);
    }

    private Publication getValidPublication() {
        Path path = Path.of("src", "test", "resources", "publication_valid_full.json");
        try {
            return objectMapper.readValue(readJSONString(path), Publication.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Publication createExamplePublication() {
        Publication publication = publicationRepository.create(getValidPublication());
        publicationRepository.refresh();
        return publication;
    }

    private String readJSONString(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + path.toString());
        }
    }

    private RequestSpecification givenJSON() {
        return RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON);
    }

    private RequestSpecification givenJSONFromFile(String filename) {
        Path path = Path.of("src", "test", "resources", filename);
        String jsonString = readJSONString(path);
        return givenJSON().body(jsonString);
    }
}
