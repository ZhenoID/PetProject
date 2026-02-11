//package epam.finalProject;
//
//import epam.finalProject.DAO.UserDao;
//import epam.finalProject.entity.User;
//import epam.finalProject.service.UserServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mindrot.jbcrypt.BCrypt;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Pageable;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class UserServiceImplTest {
//
//    @Mock
//    private UserDao userDao;
//
//    @InjectMocks
//    private UserServiceImpl service;
//
//    @BeforeEach
//    void setUp() {
//    }
//
//    @Test
//    void register_ShouldReturnFalse_WhenUserAlreadyExists() {
//        User u = new User();
//        u.setUsername("alice");
//        u.setPassword("pass");
//
//        when(userDao.findByUsername("alice")).thenReturn(new User());
//
//        boolean result = service.register(u);
//
//        assertFalse(result);
//        verify(userDao, never()).save(any());
//    }
//
//    @Test
//    void register_ShouldHashPasswordAndSave_WhenNewUser() {
//        User u = new User();
//        u.setUsername("bob");
//        u.setPassword("secret");
//
//        when(userDao.findByUsername("bob")).thenReturn(null);
//        when(userDao.save(u)).thenReturn(true);
//
//        boolean result = service.register(u);
//
//        assertTrue(result);
//        assertNotEquals("secret", u.getPassword());
//        assertTrue(u.getPassword().startsWith("$2a$") || u.getPassword().startsWith("$2b$"));
//        verify(userDao).save(u);
//    }
//
//    @Test
//    void getByUsername_ShouldDelegateToDao() {
//        User u = new User();
//        when(userDao.findByUsername("carol")).thenReturn(u);
//
//        User result = service.getByUsername("carol");
//        assertSame(u, result);
//        verify(userDao).findByUsername("carol");
//    }
//
//
//    @Test
//    void deleteUser_ShouldReturnDaoResult() {
//        User u = new User();
//        when(userDao.delete(u)).thenReturn(true);
//
//        assertTrue(service.deleteUser(u));
//        verify(userDao).delete(u);
//    }
//
//    @Test
//    void getById_ShouldDelegateToDao() {
//        User u = new User();
//        when(userDao.findById(5L)).thenReturn(u);
//
//        User result = service.getById(5L);
//        assertSame(u, result);
//        verify(userDao).findById(5L);
//    }
//
//    @Test
//    void authenticate_ShouldReturnTrue_WhenPasswordMatches() {
//        String raw = "mypassword";
//        String hash = BCrypt.hashpw(raw, BCrypt.gensalt());
//        User u = new User();
//        u.setUsername("dave");
//        u.setPassword(hash);
//
//        when(userDao.findByUsername("dave")).thenReturn(u);
//
//        assertTrue(service.authenticate("dave", raw));
//        verify(userDao).findByUsername("dave");
//    }
//
//    @Test
//    void authenticate_ShouldReturnFalse_WhenUserNotFound() {
//        when(userDao.findByUsername("eve")).thenReturn(null);
//        assertFalse(service.authenticate("eve", "whatever"));
//        verify(userDao).findByUsername("eve");
//    }
//
//    @Test
//    void authenticate_ShouldReturnFalse_WhenPasswordMismatch() {
//        User u = new User();
//        u.setUsername("fred");
//        u.setPassword(BCrypt.hashpw("correct", BCrypt.gensalt()));
//
//        when(userDao.findByUsername("fred")).thenReturn(u);
//        assertFalse(service.authenticate("fred", "wrongpass"));
//        verify(userDao).findByUsername("fred");
//    }
//}
