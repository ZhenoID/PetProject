package epam.finalProject;

import epam.finalProject.DAO.GenreDao;
import epam.finalProject.entity.Genre;
import epam.finalProject.service.GenreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenreServiceImplTest {

    @Mock
    private GenreDao genreDao;

    private GenreServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GenreServiceImpl();
        ReflectionTestUtils.setField(service, "genreDao", genreDao);
    }

    @Test
    void save_ShouldReturnTrue_WhenDaoReturnsTrue() {
        Genre g = new Genre();
        g.setName("Sci-Fi");
        when(genreDao.save(g)).thenReturn(true);

        boolean ok = service.save(g);

        assertTrue(ok);
        verify(genreDao).save(g);
    }

    @Test
    void save_ShouldReturnFalse_WhenDaoReturnsFalse() {
        Genre g = new Genre();
        g.setName("Mystery");
        when(genreDao.save(g)).thenReturn(false);

        boolean ok = service.save(g);

        assertFalse(ok);
        verify(genreDao).save(g);
    }

    @Test
    void findAll_ShouldReturnList_FromDao() {
        Genre g1 = new Genre();
        g1.setId(1L);
        g1.setName("A");
        Genre g2 = new Genre();
        g2.setId(2L);
        g2.setName("B");
        List<Genre> list = Arrays.asList(g1, g2);
        when(genreDao.findAll()).thenReturn(list);

        List<Genre> result = service.findAll();

        assertSame(list, result);
        assertEquals(2, result.size());
        verify(genreDao).findAll();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenDaoReturnsEmpty() {
        when(genreDao.findAll()).thenReturn(Collections.emptyList());

        List<Genre> result = service.findAll();

        assertTrue(result.isEmpty());
        verify(genreDao).findAll();
    }

    @Test
    void findById_ShouldReturnGenre_WhenFound() {
        Genre g = new Genre();
        g.setId(5L);
        g.setName("Thriller");
        when(genreDao.findById(5L)).thenReturn(g);

        Genre result = service.findById(5L);

        assertSame(g, result);
        verify(genreDao).findById(5L);
    }

    @Test
    void findById_ShouldReturnNull_WhenNotFound() {
        when(genreDao.findById(99L)).thenReturn(null);

        Genre result = service.findById(99L);

        assertNull(result);
        verify(genreDao).findById(99L);
    }
}
