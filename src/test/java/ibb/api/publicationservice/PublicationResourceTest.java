package ibb.api.publicationservice;

import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@QuarkusTest
@TestHTTPEndpoint(PublicationResource.class)
public class PublicationResourceTest {

    @Inject
    PublicationRepository publicationRepository;

    @Inject
    ObjectMapper objectMapper;

    @AfterEach
    @Transactional
    void cleanUp() {
        publicationRepository.deleteAll();
    }

    @Test
    public void testCreate_ShouldSucceed_WhenAllFieldsAreValid() {
        givenJSONFromFile("publication_valid_full.json")
            .when().post()
            .then()
                .body("error", nullValue())
                .statusCode(201)
                .body(
                    "id", notNullValue(),
                    "updatedAt", notNullValue(),
                    "createdAt", notNullValue(),
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
                .statusCode(201)
                .body(
                    "id", notNullValue(),
                    "updatedAt", notNullValue(),
                    "createdAt", notNullValue(),
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
        publication.setDoi(null);

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400)
                .body("error", equalTo("Doi is required"));
    }

    @Test
    public void testCreate_ShouldFail_WhenDoiIsEmptyString() {
        Publication publication = getValidPublication();
        publication.setDoi("");

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400)
                .body("error", equalTo("Doi is invalid"));
    }

    @Test
    public void testCreate_ShouldFail_WhenAuthorsAreMissing() {
        Publication publication = getValidPublication();
        publication.setAuthors(null);

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400)
                .body("error", equalTo("Authors are required"));
    }

    @Test
    public void testCreate_ShouldFail_WhenAuthorsAreEmptyList() {
        Publication publication = getValidPublication();
        publication.setAuthors(Collections.emptyList());

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400)
                .body("error", equalTo("Authors are required"));
    }

    @Test
    public void testCreate_ShouldFail_WhenGenesAreMissing() {
        Publication publication = getValidPublication();
        publication.setGenes(null);

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400)
                .body("error", equalTo("Genes are required"));
    }
    
    @Test
    public void testCreate_ShouldFail_WhenGenesAreEmptySet() {
        Publication publication = getValidPublication();
        publication.setGenes(Collections.emptySet());

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400)
                .body("error", equalTo("Genes are required"));
    }

    @Test
    public void testCreate_ShouldFail_WhenTitleIsMissing() {
        Publication publication = getValidPublication();
        publication.setTitle(null);

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400)
                .body("error", equalTo("Title is required"));
    }
    
    @Test
    public void testCreate_ShouldFail_WhenAbstractIsMissing() {
        Publication publication = getValidPublication();
        publication.setSummary(null);

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400)
                .body("error", equalTo("Abstract is required"));
    }

    @Test
    public void testCreate_ShouldFail_WhenReferenceIsMissing() {
        Publication publication = getValidPublication();
        publication.setReference(null);

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400)
                .body("error", equalTo("Reference is required"));
    }
        
    @Test
    public void testCreate_ShouldFail_WhenYearIsMissing() {
        Publication publication = getValidPublication();
        publication.setYear(null);

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400)
                .body("error", equalTo("Year is required"));
    }

    @Test
    public void testCreate_ShouldFail_WhenYearIsInTheFuture() {
        Publication publication = getValidPublication();
        publication.setYear(Year.of(2023000));

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400)
                .body("error", equalTo("Year is invalid"));
    }

    @Test
    public void testCreate_ShouldFail_WhenIdIsPresent() {
        Publication publication = getValidPublication();
        publication.setId(1L);

        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(400)
                .body("error", equalTo("Id must not be provided"));
    }

    @Test
    public void testCreate_ShouldFail_WhenDoiAlreadyExists() {
        Publication dbPublication = createExamplePublication();
        Publication submitPublication = getValidPublication();
        submitPublication.setDoi(dbPublication.getDoi());

        givenJSON()
            .body(submitPublication)
            .when().post()
            .then()
                .statusCode(400)
                .body("error", equalTo("Doi already exists"));
    }


    /* Helper methods */


    private Publication getValidPublication() {
        Path path = Path.of("src", "test", "resources", "publication_valid_full.json");
        try {
            return objectMapper.readValue(readJSONString(path), Publication.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    Publication createExamplePublication() {
        return publicationRepository.create(getValidPublication());
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
