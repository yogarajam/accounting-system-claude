package com.accounting.service;

import com.accounting.exception.AccountingException;
import com.accounting.model.Role;
import com.accounting.model.User;
import com.accounting.repository.RoleRepository;
import com.accounting.repository.UserRepository;
import com.accounting.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createDefaultUser();
        adminRole = TestDataBuilder.createAdminRole();
        userRole = TestDataBuilder.createUserRole();
        testUser.setRole(userRole);
    }

    @Nested
    @DisplayName("UserDetailsService Implementation")
    class UserDetailsServiceTests {

        @Test
        @DisplayName("Should load user by username")
        void loadUserByUsername_WhenExists_ReturnsUserDetails() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            UserDetails result = userService.loadUserByUsername("testuser");

            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getAuthorities()).isNotEmpty();
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void loadUserByUsername_WhenNotExists_ThrowsException() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.loadUserByUsername("unknown"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        @DisplayName("Should find all users")
        void findAll_ReturnsAllUsers() {
            when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

            List<User> result = userService.findAll();

            assertThat(result).hasSize(1);
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("Should find all active users")
        void findAllActive_ReturnsActiveUsers() {
            when(userRepository.findAllActive()).thenReturn(Arrays.asList(testUser));

            List<User> result = userService.findAllActive();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should find user by ID")
        void findById_WhenExists_ReturnsUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            Optional<User> result = userService.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should find user by username")
        void findByUsername_WhenExists_ReturnsUser() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            Optional<User> result = userService.findByUsername("testuser");

            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("Create User Operations")
    class CreateUserOperations {

        @Test
        @DisplayName("Should create user successfully")
        void createUser_ValidUser_CreatesSuccessfully() {
            User newUser = TestDataBuilder.createUser(null, "newuser", "newuser@example.com");

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
            when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(2L);
                return u;
            });

            User result = userService.createUser(newUser, "password123", "USER");

            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getPassword()).isEqualTo("encodedPassword");
            assertThat(result.getRole()).isEqualTo(userRole);
            assertThat(result.getEnabled()).isTrue();
            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void createUser_DuplicateUsername_ThrowsException() {
            User newUser = TestDataBuilder.createUser(null, "testuser", "new@example.com");
            when(userRepository.existsByUsername("testuser")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(newUser, "password", "USER"))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Username already exists");
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void createUser_DuplicateEmail_ThrowsException() {
            User newUser = TestDataBuilder.createUser(null, "newuser", "testuser@example.com");
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("testuser@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(newUser, "password", "USER"))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Email already exists");
        }

        @Test
        @DisplayName("Should throw exception when role not found")
        void createUser_RoleNotFound_ThrowsException() {
            User newUser = TestDataBuilder.createUser(null, "newuser", "new@example.com");
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(roleRepository.findByName("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.createUser(newUser, "password", "INVALID"))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Role not found");
        }

        @Test
        @DisplayName("Should allow null email")
        void createUser_NullEmail_Succeeds() {
            User newUser = TestDataBuilder.createUser(null, "newuser", null);

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            User result = userService.createUser(newUser, "password", "USER");

            assertThat(result).isNotNull();
            verify(userRepository, never()).existsByEmail(any());
        }
    }

    @Nested
    @DisplayName("Update User Operations")
    class UpdateUserOperations {

        @Test
        @DisplayName("Should update user successfully")
        void updateUser_ValidUpdate_UpdatesSuccessfully() {
            User updateDetails = TestDataBuilder.createUser(null, "testuser", "updated@example.com");
            updateDetails.setFullName("Updated Name");
            updateDetails.setEnabled(true);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            User result = userService.updateUser(1L, updateDetails);

            assertThat(result.getEmail()).isEqualTo("updated@example.com");
            assertThat(result.getFullName()).isEqualTo("Updated Name");
        }

        @Test
        @DisplayName("Should throw exception when changing to existing username")
        void updateUser_DuplicateUsername_ThrowsException() {
            User updateDetails = TestDataBuilder.createUser(null, "existinguser", "test@example.com");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByUsername("existinguser")).thenReturn(true);

            assertThatThrownBy(() -> userService.updateUser(1L, updateDetails))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Username already exists");
        }

        @Test
        @DisplayName("Should allow keeping same username")
        void updateUser_SameUsername_Succeeds() {
            User updateDetails = TestDataBuilder.createUser(null, "testuser", "updated@example.com");
            updateDetails.setFullName("Updated Name");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            User result = userService.updateUser(1L, updateDetails);

            assertThat(result.getUsername()).isEqualTo("testuser");
            verify(userRepository, never()).existsByUsername(anyString());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void updateUser_UserNotFound_ThrowsException() {
            User updateDetails = TestDataBuilder.createUser(null, "newname", "new@example.com");
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(99L, updateDetails))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("Password Operations")
    class PasswordOperations {

        @Test
        @DisplayName("Should change password successfully")
        void changePassword_ValidUser_ChangesPassword() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            userService.changePassword(1L, "newPassword");

            assertThat(testUser.getPassword()).isEqualTo("newEncodedPassword");
            verify(passwordEncoder).encode("newPassword");
        }

        @Test
        @DisplayName("Should throw exception when user not found for password change")
        void changePassword_UserNotFound_ThrowsException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.changePassword(99L, "newPassword"))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("Role Operations")
    class RoleOperations {

        @Test
        @DisplayName("Should change user role successfully")
        void changeRole_ValidRole_ChangesRole() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            userService.changeRole(1L, "ADMIN");

            assertThat(testUser.getRole()).isEqualTo(adminRole);
        }

        @Test
        @DisplayName("Should throw exception when role not found")
        void changeRole_RoleNotFound_ThrowsException() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.changeRole(1L, "INVALID"))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Role not found");
        }

        @Test
        @DisplayName("Should find all roles")
        void findAllRoles_ReturnsAllRoles() {
            when(roleRepository.findAll()).thenReturn(Arrays.asList(adminRole, userRole));

            List<Role> result = userService.findAllRoles();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should create role if not exists")
        void createRoleIfNotExists_NewRole_CreatesRole() {
            when(roleRepository.findByName("MANAGER")).thenReturn(Optional.empty());
            when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
                Role r = invocation.getArgument(0);
                r.setId(3L);
                return r;
            });

            Role result = userService.createRoleIfNotExists("MANAGER");

            assertThat(result.getName()).isEqualTo("MANAGER");
            verify(roleRepository).save(any(Role.class));
        }

        @Test
        @DisplayName("Should return existing role if exists")
        void createRoleIfNotExists_ExistingRole_ReturnsExisting() {
            when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));

            Role result = userService.createRoleIfNotExists("ADMIN");

            assertThat(result).isEqualTo(adminRole);
            verify(roleRepository, never()).save(any(Role.class));
        }
    }

    @Nested
    @DisplayName("Enable/Disable User Operations")
    class EnableDisableOperations {

        @Test
        @DisplayName("Should disable user successfully")
        void disableUser_ValidUser_DisablesUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            userService.disableUser(1L);

            assertThat(testUser.getEnabled()).isFalse();
        }

        @Test
        @DisplayName("Should enable user successfully")
        void enableUser_ValidUser_EnablesUser() {
            testUser.setEnabled(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            userService.enableUser(1L);

            assertThat(testUser.getEnabled()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when disabling non-existent user")
        void disableUser_UserNotFound_ThrowsException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.disableUser(99L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("Should throw exception when enabling non-existent user")
        void enableUser_UserNotFound_ThrowsException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.enableUser(99L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("User not found");
        }
    }
}