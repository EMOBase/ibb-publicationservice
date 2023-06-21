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
import jakarta.inject.Inject;

@QuarkusTest
@TestHTTPEndpoint(PublicationResource.class)
public class PublicationResourceTest {

    @Inject
    ObjectMapper objectMapper;

    private String readTestData(String filename) throws IOException, URISyntaxException {
        Path path = Paths.get(this.getClass().getClassLoader().getResource(filename).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        return new String(fileBytes);
    }

    @Test
    public void testCreateValid() throws IOException, URISyntaxException {

        String postBody = readTestData("publication_post_valid_1.json");
        
        RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(postBody)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body(
                    "id", equalTo(1),
                    "updatedAt", notNullValue(),
                    "createdAt", notNullValue(),
                    "pmid", equalTo(25378303),
                    "doi", equalTo("10.1093/nar/gku1054")
                );

        RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
                .get("/1")
            .then()
                .statusCode(HttpStatus.SC_OK)
                .body(
                    "doi", equalTo("10.1093/nar/gku1054")
                );

        RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(readTestData("publication_post_valid_2.json"))
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    public void testUpdateValid() throws IOException, URISyntaxException {
        String postBody = readTestData("publication_post_valid_1.json");

        Publication publication = RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(postBody)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().as(Publication.class);

        publication.firstAuthor = "Canh Nguyen Duc";
        
        RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(objectMapper.writeValueAsString(publication))
            .when()
                .put("" + publication.id)
            .then()
                .statusCode(HttpStatus.SC_OK)
                .body(
                    "firstAuthor", equalTo("Canh Nguyen Duc")
                );
    }
}