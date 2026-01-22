package com.accounting.service;

import com.accounting.exception.AccountingException;
import com.accounting.model.Role;
import com.accounting.model.User;
import com.accounting.repository.RoleRepository;
import com.accounting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findAllActive() {
        return userRepository.findAllActive();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User createUser(User user, String rawPassword, String roleName) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new AccountingException("Username already exists: " + user.getUsername());
        }

        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new AccountingException("Email already exists: " + user.getEmail());
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new AccountingException("Role not found: " + roleName));

        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AccountingException("User not found: " + id));

        if (!user.getUsername().equals(userDetails.getUsername())
            && userRepository.existsByUsername(userDetails.getUsername())) {
            throw new AccountingException("Username already exists: " + userDetails.getUsername());
        }

        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setFullName(userDetails.getFullName());
        user.setEnabled(userDetails.getEnabled());

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AccountingException("User not found: " + id));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void changeRole(Long id, String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AccountingException("User not found: " + id));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new AccountingException("Role not found: " + roleName));

        user.setRole(role);
        userRepository.save(user);
    }

    @Transactional
    public void disableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AccountingException("User not found: " + id));

        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public void enableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AccountingException("User not found: " + id));

        user.setEnabled(true);
        userRepository.save(user);
    }

    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public Role createRoleIfNotExists(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }
}