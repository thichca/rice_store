package swp.se1889.g1.rice_store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import swp.se1889.g1.rice_store.entity.User;
import swp.se1889.g1.rice_store.repository.UserRepository;
import swp.se1889.g1.rice_store.service.Iservice.UserService;

import java.util.Collection;
import java.util.Collections;

@Service
public class UserServiceIpml implements UserService {

    private UserRepository userRepository;

    @Autowired
    public UserServiceIpml(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                rolesToAuthorities(user)
        );
    }

    private Collection<? extends GrantedAuthority> rolesToAuthorities(User role) {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getRole()));
    }

    public User getCurrentUser() {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username);
    }

    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public Long getCurrentStoreId() {
        User user = getCurrentUser();
        return user != null ? user.getStoreId() : null;
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "guest";
    }
}
