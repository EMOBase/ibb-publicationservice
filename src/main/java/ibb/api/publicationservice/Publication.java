package ibb.api.publicationservice;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.groups.Default;

// The Elasticsearch client maps documents to this class (and Author) by
// reflection, which a native executable only allows for registered classes.
@RegisterForReflection
public class Publication {

    public static interface ValidationGroups {
        interface Create extends Default {}
        interface Update extends Default {}
    }

    public static class Author {
        @NotBlank
        public String firstName;

        @NotBlank
        public String lastName;
    }

    @Null(groups = ValidationGroups.Create.class)
    @NotNull(groups = ValidationGroups.Update.class)
    public String id;

    @NotBlank
    public String doi;

    @NotBlank
    public String gene;

    @NotEmpty
    public List<Author> authors = new ArrayList<>();

    public String pmid;

    @NotBlank
    public String title;

    @NotBlank
    @JsonProperty("abstract")
    public String summary;

    @NotBlank
    public String reference;

    public String journal;

    @PastOrPresent
    @NotNull
    @Schema(implementation = Integer.class)
    public Year year;
}
