package org.example.lmsbackend.service;

import org.example.lmsbackend.dto.UserDTO;
import org.example.lmsbackend.model.User;
import org.example.lmsbackend.repository.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Test
    void login_WithExistingUser_ReturnsTrue() {
        UserDTO dto = new UserDTO();
        dto.setUsername("minh");
        dto.setPassword("123456");

        boolean result = userService.login(dto);
        assertTrue(result);
    }

    @Test
    void register_NewUser_Success() {
        UserDTO dto = new UserDTO();
        dto.setUsername("integration_user_" + System.currentTimeMillis());
        dto.setEmail("integration" + System.currentTimeMillis() + "@test.com");
        dto.setPassword("abc123");
        dto.setRole("student");
        dto.setFullName("Integration Test User");

        assertDoesNotThrow(() -> userService.register(dto));
    }

    @Test
    void register_InstructorWithCv_Success() {
        UserDTO dto = new UserDTO();
        dto.setUsername("instructor_" + System.currentTimeMillis());
        dto.setEmail("inst" + System.currentTimeMillis() + "@mail.com");
        dto.setPassword("abc123");
        dto.setRole("instructor");
        dto.setCvUrl("http://example.com/cv.pdf");
        dto.setFullName("Instructor Test");

        assertDoesNotThrow(() -> userService.register(dto));
    }

    @Test
    void register_InstructorWithoutCv_ThrowsException() {
        UserDTO dto = new UserDTO();
        dto.setUsername("fail_instructor_" + System.currentTimeMillis());
        dto.setEmail("fail" + System.currentTimeMillis() + "@test.com");
        dto.setPassword("abc123");
        dto.setRole("instructor"); // Không set CV
        dto.setFullName("No CV Instructor");

        assertThrows(RuntimeException.class, () -> userService.register(dto));
    }

    @Test
    void register_DuplicateUsername_ThrowsException() {
        String username = "duplicate_user_" + System.currentTimeMillis();

        UserDTO dto1 = new UserDTO();
        dto1.setUsername(username);
        dto1.setEmail("unique1" + System.currentTimeMillis() + "@test.com");
        dto1.setPassword("abc123");
        dto1.setRole("student");
        dto1.setFullName("Original");

        userService.register(dto1);

        UserDTO dto2 = new UserDTO();
        dto2.setUsername(username);
        dto2.setEmail("unique2" + System.currentTimeMillis() + "@test.com");
        dto2.setPassword("abc123");
        dto2.setRole("student");
        dto2.setFullName("Duplicate");

        assertThrows(RuntimeException.class, () -> userService.register(dto2));
    }

    @Test
    void login_AfterRegister_Success() {
        String username = "login_user_" + System.currentTimeMillis();

        UserDTO registerDto = new UserDTO();
        registerDto.setUsername(username);
        registerDto.setEmail("login" + System.currentTimeMillis() + "@test.com");
        registerDto.setPassword("abc123");
        registerDto.setRole("student");
        registerDto.setFullName("Login Test");

        userService.register(registerDto);

        UserDTO loginDto = new UserDTO();
        loginDto.setUsername(username);
        loginDto.setPassword("abc123");

        boolean result = userService.login(loginDto);
        assertTrue(result);
    }

    @Test
    void deleteUser_AfterRegister_Success() {
        String username = "delete_user_" + System.currentTimeMillis();

        UserDTO dto = new UserDTO();
        dto.setUsername(username);
        dto.setEmail("delete" + System.currentTimeMillis() + "@test.com");
        dto.setPassword("abc123");
        dto.setRole("student");
        dto.setFullName("Delete User");

        userService.register(dto);
        User user = userMapper.findByUsername(username);

        assertNotNull(user);
        boolean deleted = userService.deleteUser(user.getUserId());
        assertTrue(deleted);
    }
}


