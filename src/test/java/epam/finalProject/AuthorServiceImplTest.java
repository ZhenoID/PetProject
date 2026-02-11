//package epam.finalProject;
//
//import epam.finalProject.DAO.AuthorDaoImpl;
//import epam.finalProject.entity.Author;
//import epam.finalProject.service.AuthorServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class AuthorServiceImplTest {
//
//    @Mock
//    private AuthorDaoImpl authorDao;
//
//    @InjectMocks
//    private AuthorServiceImpl authorService;
//
//    @BeforeEach
//    void init() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void save_shouldDelegateToDao() {
//        Author a = new Author();
//        a.setName("John Doe");
//        when(authorDao.save(a)).thenReturn(true);
//
//        boolean result = authorService.save(a);
//
//        assertTrue(result);
//        verify(authorDao).save(a);
//    }
//
//    @Test
//    void update_shouldDelegateToDao() {
//        Author a = new Author();
//        a.setId(10L);
//        a.setName("Jane");
//        when(authorDao.update(a)).thenReturn(true);
//
//        boolean result = authorService.update(a);
//
//        assertTrue(result);
//        verify(authorDao).update(a);
//    }
//
//    @Test
//    void delete_shouldDelegateToDao() {
//        when(authorDao.delete(5L)).thenReturn(true);
//
//        boolean result = authorService.delete(5L);
//
//        assertTrue(result);
//        verify(authorDao).delete(5L);
//    }
//
//    @Test
//    void findById_shouldReturnAuthor() {
//        Author a = new Author();
//        a.setId(2L);
//        a.setName("Foo");
//        when(authorDao.findById(2L)).thenReturn(a);
//
//        Author result = authorService.findById(2L);
//
//        assertEquals("Foo", result.getName());
//        verify(authorDao).findById(2L);
//    }
//
//    @Test
//    void findAll_shouldReturnList() {
//        Author a1 = new Author();
//        a1.setName("A");
//        Author a2 = new Author();
//        a2.setName("B");
//        List<Author> list = Arrays.asList(a1, a2);
//        when(authorDao.findAll()).thenReturn(list);
//
//        List<Author> result = authorService.findAll();
//
//        assertEquals(2, result.size());
//        verify(authorDao).findAll();
//    }
//
//    @Test
//    void existsById_shouldReturnTrueIfDaoSaysSo() {
//        when(authorDao.existsById(7L)).thenReturn(true);
//
//        assertTrue(authorService.existsById(7L));
//        verify(authorDao).existsById(7L);
//    }
//}