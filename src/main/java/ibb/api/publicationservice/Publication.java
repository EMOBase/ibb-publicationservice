package ibb.api.publicationservice;

import java.time.Instant;
import java.time.Year;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.Length;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(indexes = {
    @Index(name = "idx_Publication_doi", columnList = "doi", unique = true),
    @Index(name = "idx_Publication_pmid", columnList = "pmid", unique = true)
})
public class Publication {

    public static interface ValidationGroups {
        interface Post extends Default {}
        interface Put extends Default {}
    }

    @Id
    @Null(groups = ValidationGroups.Post.class, message = "Id must not be provided")
    @NotNull(groups = ValidationGroups.Put.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Doi is required")
    @Pattern(regexp = "^10.\\d+/[-._;()/:a-zA-Z0-9]+$", message = "Doi is invalid")
    private String doi;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(indexes = {@Index(name = "idx_Publication_genes", columnList = "genes")})
    @NotEmpty(groups = ValidationGroups.Post.class, message = "Genes are required")
    private Set<String> genes = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @OrderColumn
    @NotEmpty(message = "Authors are required")
    private List<String> authors = new ArrayList<>();

    private Long pmid;

    @NotBlank(message = "Title is required")
    @Column(length = Length.LONG)
    private String title;

    @NotBlank(message = "Abstract is required")
    @Column(length = Length.LONG)
    @JsonProperty("abstract")
    private String summary;

    @NotBlank(message = "Reference is required")
    @Column(length = Length.LONG)
    private String reference;

    private String journal;

    @PastOrPresent(message = "Year is invalid")
    @NotNull(message = "Year is required")
    @Schema(implementation = Integer.class)
    private Year year;

    @CreationTimestamp
    @Schema(implementation = Long.class)
    private Instant createdAt;

    @UpdateTimestamp
    @Schema(implementation = Long.class)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public Set<String> getGenes() {
        return genes;
    }

    public void setGenes(Set<String> genes) {
        this.genes = genes;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public Long getPmid() {
        return pmid;
    }

    public void setPmid(Long pmid) {
        this.pmid = pmid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public Year getYear() {
        return year;
    }

    public void setYear(Year year) {
        this.year = year;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
