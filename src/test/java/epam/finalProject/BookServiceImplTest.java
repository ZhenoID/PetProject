package epam.finalProject;

import epam.finalProject.DAO.BookDao;
import epam.finalProject.entity.Author;
import epam.finalProject.entity.Book;
import epam.finalProject.service.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class BookServiceImplTest {

    @Mock
    private BookDao bookDao;

    @InjectMocks
    private BookServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void deleteBook_ShouldReturnDaoResult(boolean daoResult) {
        Book book = new Book();
        when(bookDao.deleteBook(book)).thenReturn(daoResult);

        boolean result = service.deleteBook(book);

        assertEquals(daoResult, result);
        verify(bookDao).deleteBook(book);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void changeBook_ShouldReturnDaoResult(boolean daoResult){
        Book book = new Book();
        when(bookDao.changeBook(book)).thenReturn(daoResult);

        boolean result = service.changeBook(book);
        assertEquals(daoResult, result);
        verify(bookDao).changeBook(book);
    }

    @Test
    void findById_ShouldReturnBook_FromDao() {
        Book expected = new Book();
        expected.setId(7L);
        when(bookDao.findById(7L)).thenReturn(expected);

        Book actual = service.findById(7L);
        assertSame(expected, actual);
        verify(bookDao).findById(7L);
    }

    @Test
    void findById_ShouldReturnNull_WhenDaoReturnsNull() {
        when(bookDao.findById(42L)).thenReturn(null);

        Book actual = service.findById(42L);
        assertNull(actual);
        verify(bookDao).findById(42L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void saveBookWithAuthor_ShouldReturnDaoResult(boolean daoResult) {
        Book book = new Book();
        Author author = new Author();
        when(bookDao.saveBookWithAuthor(book, author)).thenReturn(daoResult);

        boolean result = service.saveBookWithAuthor(book, author);
        assertEquals(daoResult, result);
        verify(bookDao).saveBookWithAuthor(book, author);
    }

}