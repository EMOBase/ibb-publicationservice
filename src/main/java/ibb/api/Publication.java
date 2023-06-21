package ibb.api;

import java.time.Instant;
import java.time.Year;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.Length;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default;

import java.util.List;
import java.util.Set;

@Entity
@NamedQueries({
    @NamedQuery(name = "Publication.findByGene", query =
        "from Publication as p join fetch p.genes as g" +
        " where g = ?1"
    )
})
public class Publication extends PanacheEntityBase {

    public static interface ValidationGroups {
        interface Post extends Default {}
        interface Put extends Default {}
    }

    @Id
    @Null(groups = ValidationGroups.Post.class)
    @NotNull(groups = ValidationGroups.Put.class)
    @GeneratedValue
    public Long id;

    @NotBlank
    @Pattern(regexp = "^10.\\d+/[-._;()/:a-zA-Z0-9]+$")
    public String doi;

    @ElementCollection(fetch = FetchType.LAZY)
    @NotNull(groups = ValidationGroups.Post.class)
    public Set<@NotBlank String> genes;

    public Long pmid;

    @NotBlank
    public String firstAuthor;

    @NotBlank
    @Column(length = Length.LONG)
    public String title;

    @NotBlank
    @Column(length = Length.LONG)
    @JsonProperty("abstract")
    public String summary;

    @NotBlank
    @Column(length = Length.LONG)
    public String reference;

    public String journal;

    @PastOrPresent
    @Schema(implementation = Integer.class)
    public Year year;

    @CreationTimestamp
    @Schema(implementation = Long.class)
    public Instant createdAt;

    @UpdateTimestamp
    @Schema(implementation = Long.class)
    public Instant updatedAt;

    public void setDoi(String doi) {
        this.doi = doi.toLowerCase();
    }

    public static Uni<List<Publication>> findByGene(String gene) {
        return list("#Publication.findByGene", gene);
    }
}