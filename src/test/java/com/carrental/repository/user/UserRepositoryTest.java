package com.carrental.repository.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carrental.model.Role;
import com.carrental.model.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Exists by email - should return true for existing email")
    void existsByEmail_ExistingEmail_ShouldReturnTrue() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertTrue(exists);
    }

    @Test
    @DisplayName("Exists by email - should return false for non-existing email")
    void existsByEmail_NonExistingEmail_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexisting@example.com");

        // Then
        assertFalse(exists);
    }

    @Test
    @DisplayName("Find by email - should return user when email exists")
    void findByEmail_ExistingEmail_ShouldReturnUser() {
        // Given
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByEmail("user@example.com");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("user@example.com", foundUser.get().getEmail());
        assertEquals("Jane", foundUser.get().getFirstName());
        assertEquals("Smith", foundUser.get().getLastName());
        assertEquals(Role.CUSTOMER, foundUser.get().getRole());
        assertFalse(foundUser.get().isDeleted());
    }

    @Test
    @DisplayName("Find by email - should return empty when email not exists")
    void findByEmail_NonExistingEmail_ShouldReturnEmpty() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("unknown@example.com");

        // Then
        assertTrue(foundUser.isEmpty());
    }

    @Test
    @DisplayName("Save user - should save and return user with generated ID")
    void save_ValidUser_ShouldSaveUser() {
        // Given
        User user = new User();
        user.setEmail("newuser@example.com");
        user.setPassword("encryptedPassword");
        user.setFirstName("Alice");
        user.setLastName("Johnson");
        user.setRole(Role.CUSTOMER);

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals("newuser@example.com", savedUser.getEmail());
        assertEquals("Alice", savedUser.getFirstName());
        assertEquals("Johnson", savedUser.getLastName());
        assertEquals(Role.CUSTOMER, savedUser.getRole());
        assertFalse(savedUser.isDeleted());
    }

    @Test
    @DisplayName("Find user by ID - should return user when exists")
    void findById_ExistingId_ShouldReturnUser() {
        // Given
        User user = new User();
        user.setEmail("find@example.com");
        user.setPassword("password");
        user.setFirstName("Bob");
        user.setLastName("Brown");
        user.setRole(Role.CUSTOMER);
        User savedUser = userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals("find@example.com", foundUser.get().getEmail());
    }

    @Test
    @DisplayName("Find user by ID - should return empty when not exists")
    void findById_NonExistingId_ShouldReturnEmpty() {
        // When
        Optional<User> foundUser = userRepository.findById(999L);

        // Then
        assertTrue(foundUser.isEmpty());
    }

    @Test
    @DisplayName("Find all users - should return all saved users")
    void findAll_WithSavedUsers_ShouldReturnAllUsers() {
        // Given
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPassword("password1");
        user1.setFirstName("User1");
        user1.setLastName("Last1");
        user1.setRole(Role.CUSTOMER);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");
        user2.setFirstName("User2");
        user2.setLastName("Last2");
        user2.setRole(Role.MANAGER);

        userRepository.save(user1);
        userRepository.save(user2);

        // When
        List<User> allUsers = userRepository.findAll();

        // Then
        assertEquals(2, allUsers.size());
    }

    @Test
    @DisplayName("Update user - should update user details")
    void updateUser_ExistingUser_ShouldUpdateDetails() {
        // Given
        User user = new User();
        user.setEmail("update@example.com");
        user.setPassword("password");
        user.setFirstName("Original");
        user.setLastName("Name");
        user.setRole(Role.CUSTOMER);
        User savedUser = userRepository.save(user);

        // When
        savedUser.setFirstName("Updated");
        savedUser.setLastName("Surname");
        savedUser.setRole(Role.MANAGER);
        User updatedUser = userRepository.save(savedUser);

        // Then
        assertEquals(savedUser.getId(), updatedUser.getId());
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Surname", updatedUser.getLastName());
        assertEquals(Role.MANAGER, updatedUser.getRole());
    }

    @Test
    @DisplayName("Soft delete user - should mark user as deleted")
    void softDelete_ExistingUser_ShouldMarkAsDeleted() {
        // Given
        User user = new User();
        user.setEmail("delete@example.com");
        user.setPassword("password");
        user.setFirstName("Delete");
        user.setLastName("User");
        user.setRole(Role.CUSTOMER);
        User savedUser = userRepository.save(user);

        // When
        savedUser.setDeleted(true);
        User updatedUser = userRepository.save(savedUser);

        // Then
        assertTrue(updatedUser.isDeleted());
        assertFalse(updatedUser.isEnabled());
    }

    @Test
    @DisplayName("Find users with different roles - should return users with correct roles")
    void findAll_WithDifferentRoles_ShouldReturnCorrectRoles() {
        // Given
        User customer = new User();
        customer.setEmail("customer@example.com");
        customer.setPassword("password");
        customer.setFirstName("Customer");
        customer.setLastName("User");
        customer.setRole(Role.CUSTOMER);

        User manager = new User();
        manager.setEmail("manager@example.com");
        manager.setPassword("password");
        manager.setFirstName("Manager");
        manager.setLastName("User");
        manager.setRole(Role.MANAGER);

        userRepository.save(customer);
        userRepository.save(manager);

        // When
        List<User> allUsers = userRepository.findAll();

        // Then
        assertEquals(2, allUsers.size());
        assertTrue(allUsers.stream().anyMatch(u -> u.getRole() == Role.CUSTOMER));
        assertTrue(allUsers.stream().anyMatch(u -> u.getRole() == Role.MANAGER));
    }

    @Test
    @DisplayName("UserDetails implementation - should return correct authorities")
    void getUserDetails_WithRole_ShouldReturnCorrectAuthorities() {
        // Given
        User user = new User();
        user.setEmail("auth@example.com");
        user.setPassword("password");
        user.setFirstName("Auth");
        user.setLastName("User");
        user.setRole(Role.MANAGER);
        User savedUser = userRepository.save(user);

        // When
        var authorities = savedUser.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
    }
}
