package org.example.lmsbackend.service;

import org.example.lmsbackend.dto.UserDTO;
import org.example.lmsbackend.email.EmailService;
import org.example.lmsbackend.model.User;
import org.example.lmsbackend.repository.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailService = mock(EmailService.class);
        userService = new UserService(userMapper, passwordEncoder, emailService);
    }

    @Test
    void login_Success() {
        UserDTO dto = new UserDTO();
        dto.setUsername("test");
        dto.setPassword("123456");

        User user = new User();
        user.setPassword("encoded");
        user.setVerified(true);

        when(userMapper.findByUsername("test")).thenReturn(user);
        when(passwordEncoder.matches("123456", "encoded")).thenReturn(true);

        assertTrue(userService.login(dto));
    }

    @Test
    void login_UserNotFound() {
        when(userMapper.findByUsername("notfound")).thenReturn(null);

        UserDTO dto = new UserDTO();
        dto.setUsername("notfound");
        dto.setPassword("123");

        assertFalse(userService.login(dto));
    }

    @Test
    void login_NotVerified_ThrowsException() {
        User user = new User();
        user.setVerified(false);

        when(userMapper.findByUsername("abc")).thenReturn(user);

        UserDTO dto = new UserDTO();
        dto.setUsername("abc");
        dto.setPassword("123");

        assertThrows(RuntimeException.class, () -> userService.login(dto));
    }

    @Test
    void register_ThrowsIfUsernameExists() {
        when(userMapper.existsByUsername("abc")).thenReturn(true);

        UserDTO dto = new UserDTO();
        dto.setUsername("abc");

        assertThrows(RuntimeException.class, () -> userService.register(dto));
    }

    @Test
    void register_ThrowsIfEmailExists() {
        when(userMapper.existsByUsername("abc")).thenReturn(false);
        when(userMapper.existsByEmail("a@a.com")).thenReturn(true);

        UserDTO dto = new UserDTO();
        dto.setUsername("abc");
        dto.setEmail("a@a.com");

        assertThrows(RuntimeException.class, () -> userService.register(dto));
    }

    @Test
    void register_InstructorWithoutCv_ThrowsException() {
        when(userMapper.existsByUsername(any())).thenReturn(false);
        when(userMapper.existsByEmail(any())).thenReturn(false);

        UserDTO dto = new UserDTO();
        dto.setUsername("inst");
        dto.setRole("instructor");
        dto.setCvUrl(null);

        assertThrows(RuntimeException.class, () -> userService.register(dto));
    }

    @Test
    void deleteUser_ReturnsTrue() {
        when(userMapper.deleteUserById(1)).thenReturn(1);
        assertTrue(userService.deleteUser(1));
    }

    @Test
    void deleteUser_ReturnsFalse() {
        when(userMapper.deleteUserById(1)).thenReturn(0);
        assertFalse(userService.deleteUser(1));
    }

    @Test
    void updateUser_ReturnsFalse_IfUserNotFound() {
        when(userMapper.findById(1L)).thenReturn(null);
        assertFalse(userService.updateUser(1L, new UserDTO()));
    }

    @Test
    void getUsers_ReturnsList() {
        when(userMapper.findUsersByConditions(null, null, null, null)).thenReturn(Collections.emptyList());
        assertEquals(0, userService.getUsers(null, null, null, null).size());
    }
}
