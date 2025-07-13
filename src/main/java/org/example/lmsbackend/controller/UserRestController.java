package org.example.lmsbackend.controller;

import org.example.lmsbackend.model.User;
import org.example.lmsbackend.dto.UserDTO;
import org.example.lmsbackend.service.FileStorageService;
import org.example.lmsbackend.service.UserService;
import org.example.lmsbackend.utils.JwtTokenUtil;
import org.example.lmsbackend.repository.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserRestController {
        private final UserService userService;
        private final UserMapper userMapper;
        private final JwtTokenUtil jwtTokenUtil;
        private final FileStorageService fileStorageService;

        public UserRestController(
                UserService userService,
                UserMapper userMapper,
                JwtTokenUtil jwtTokenUtil,
                FileStorageService fileStorageService
        ) {
            this.userService = userService;
            this.userMapper = userMapper;
            this.jwtTokenUtil = jwtTokenUtil;
            this.fileStorageService = fileStorageService;
        }
    // ✅ API đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO) {
        try {
            boolean success = userService.login(userDTO);

            if (!success) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "message", "Tên đăng nhập hoặc mật khẩu không đúng"
                ));
            }

            User user = userMapper.findByUsername(userDTO.getUsername());
            String token = jwtTokenUtil.generateToken(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Đăng nhập thành công",
                    "token", token
            ));
        } catch (RuntimeException e) {
            // 🔒 Trường hợp tài khoản chưa xác minh
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    // ✅ API đăng ký
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam("fullName") String fullName,
            @RequestParam("role") String role,
            @RequestPart(value = "cv", required = false) MultipartFile cvFile
    ) {
        try {
            String cvPath = null;
            boolean isVerified;

            if ("instructor".equalsIgnoreCase(role)) {
                isVerified = false;

                if (cvFile == null || cvFile.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("message", "CV file is required for instructors"));
                }

                cvPath = fileStorageService.saveFile(cvFile, "cvs");
            } else {
                isVerified = true;
            }

            UserDTO userDTO = new UserDTO(username, password, email, fullName, role, isVerified, cvPath);
            boolean created = userService.register(userDTO);

            if (created) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "User registered successfully");
                if (cvPath != null) response.put("cvUrl", cvPath);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Registration failed"));
            }

        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if ("Username already exists".equals(message)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Tên đăng nhập đã tồn tại"));
            } else if ("Email already exists".equals(message)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email đã được sử dụng"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Internal server error"));
        }
    }



    // ✅ API lấy danh sách người dùng theo điều kiện
    @GetMapping("/list")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<User>> getUsers(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(required = false) String username) {

        List<User> users = userService.getUsers(userId, role, isVerified, username);
        return ResponseEntity.ok(users);
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UserDTO userDTO
    ) {
        try {
            boolean updated = userService.updateUser(id, userDTO);
            if (updated) {
                return ResponseEntity.ok("User updated successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Update failed: " + e.getMessage());
        }
    }
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok("Xóa người dùng thành công");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Người dùng không tồn tại");
        }
    }
}
