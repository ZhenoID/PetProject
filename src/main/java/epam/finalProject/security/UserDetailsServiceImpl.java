package epam.finalProject.security;

import epam.finalProject.DAO.UserDao;
import epam.finalProject.DAO.UserDaoImpl;
import epam.finalProject.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserDao userDao = new UserDaoImpl();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDao.findByUsername(username);
        if (user == null) throw new UsernameNotFoundException("User not found");

        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), List.of(authority));
    }
}
