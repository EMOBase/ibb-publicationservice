package ibb.api;

import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

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
    ObjectMapper objectMapper;

    @Test
    public void testPostShouldSucceedWhenAllFieldsAreValid() {
        Publication publication = getExamplePublication();
        
        givenJSON()
            .body(publication)
            .when().post()
            .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body(
                    "id", notNullValue(),
                    "updatedAt", notNullValue(),
                    "createdAt", notNullValue(),
                    "pmid", notNullValue(),
                    "doi", notNullValue(),
                    "firstAuthor", notNullValue(),
                    "title", notNullValue(),
                    "abstract", notNullValue(),
                    "reference", notNullValue(),
                    "journal", notNullValue(),
                    "year", notNullValue()
                );
    }

    @Test
    public void testPostShouldSucceedWhenPmidIsMissing() {
        Publication publication = getExamplePublication();
        publication.pmid = null;
        
        givenJSON()
            .body(publication)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body(
                    "id", notNullValue(),
                    "updatedAt", notNullValue(),
                    "createdAt", notNullValue(),
                    "pmid", nullValue(),
                    "doi", notNullValue(),
                    "firstAuthor", notNullValue(),
                    "title", notNullValue(),
                    "abstract", notNullValue(),
                    "reference", notNullValue(),
                    "journal", notNullValue(),
                    "year", notNullValue()
                );
    }

    @Test
    public void testPostShouldFailWhenDoiIsMissing() {
        Publication publication = getExamplePublication();
        publication.doi = null;
        
        givenJSON()
            .body(publication)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(
                    "violations.message", hasItem("Doi must not be blank")
                );
    }    
    
    @Test
    public void testPostShouldFailWhenDoiIsEmptyString() {
        Publication publication = getExamplePublication();
        publication.doi = "";
        
        givenJSON()
            .body(publication)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(
                    "violations.message", hasItem("Doi must not be blank")
                );
    }

    private String readTestData(String filename) throws URISyntaxException, IOException {
        Path path = Paths.get(this.getClass().getClassLoader().getResource(filename).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        return new String(fileBytes);
    }

    private Publication getExamplePublication() {
        try {
            String json = readTestData("publication_post_valid_1.json");
            return objectMapper.readValue(json, Publication.class);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("Can not create example publication from resource files");
        }
    }

    private RequestSpecification givenJSON() {
        return RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON);
    }
}
