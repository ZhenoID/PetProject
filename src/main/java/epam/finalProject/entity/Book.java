package epam.finalProject.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

import java.util.List;

/**
 * Entity representing a Book with validation constraints for form inputs.
 */
public class Book {
    private Long id;

    @NotBlank(message = "{book.title.notblank}")
    private String title;

    @NotNull(message = "{book.author.notnull}")
    private Long authorId;

    @NotNull(message = "{book.year.notnull}")
    @Min(value = 0, message = "{book.year.min}")
    private Integer year;

    @NotBlank(message = "{book.description.notblank}")
    private String description;

    private Author author;

    @NotEmpty(message = "{book.genres.notempty}")
    private List<Long> genreIds;

    private List<Genre> genres;

    @NotNull(message = "{book.quantity.notnull}")
    @Min(value = 1, message = "{book.quantity.min}")
    private Integer quantity;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public List<Long> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(List<Long> genreIds) {
        this.genreIds = genreIds;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
