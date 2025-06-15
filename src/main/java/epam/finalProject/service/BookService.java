package epam.finalProject.service;

import epam.finalProject.entity.Author;
import epam.finalProject.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface BookService {

    boolean deleteBook(Book book);

    boolean changeBook(Book book);

    Page<Book> findAll(Pageable pageable);

    default Page<Book> findAll() {
        return findAll(Pageable.unpaged());
    }

    Book findById(Long id);

    boolean saveBookWithAuthor(Book book, Author author);
}

