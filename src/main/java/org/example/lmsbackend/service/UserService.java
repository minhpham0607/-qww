package org.example.lmsbackend.service;

import org.example.lmsbackend.model.User;
import org.example.lmsbackend.dto.UserDTO;
import org.example.lmsbackend.email.EmailService;
import org.example.lmsbackend.repository.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;
    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // ✅ Xử lý đăng nhập
    public boolean login(UserDTO userDTO) {
        System.out.println("Trying login with username: " + userDTO.getUsername());
        System.out.println("Raw password: " + userDTO.getPassword());

        User user = userMapper.findByUsername(userDTO.getUsername());

        if (user == null) {
            System.out.println("User not found");
            return false;
        }

        if (!user.isVerified()) {
            System.out.println("User not verified");
            throw new RuntimeException("Tài khoản chưa được xác minh");
        }

        boolean match = passwordEncoder.matches(userDTO.getPassword(), user.getPassword());
        System.out.println("Password match: " + match);

        return match;
    }


    // ✅ Xử lý đăng ký
    public boolean register(UserDTO userDTO) {
        if (userMapper.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userMapper.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setFullName(userDTO.getFullName());

        // Xử lý role và kiểm tra CV nếu là instructor
        String rawRole = userDTO.getRole().toLowerCase();
        try {
            User.Role role = User.Role.valueOf(rawRole);
            user.setRole(role);

            // Nếu là instructor thì yêu cầu CV và đặt isVerified = false
            if (role == User.Role.instructor) {
                if (userDTO.getCvUrl() == null || userDTO.getCvUrl().isBlank()) {
                    throw new RuntimeException("CV is required for instructor registration.");
                }
                user.setCvUrl(userDTO.getCvUrl());
                user.setVerified(false); // ❗ Giảng viên phải chờ duyệt
            } else {
                // Các role khác dùng theo userDTO hoặc mặc định là true
                Boolean verified = userDTO.getIsVerified() != null ? userDTO.getIsVerified() : true;
                user.setVerified(verified);
            }

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + userDTO.getRole());
        }

        try {
            return userMapper.insertUser(user) > 0;
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("Username or Email already exists (DB constraint)");
        }
    }


    // ✅ Lấy danh sách người dùng có điều kiện
    public List<User> getUsers(Integer userId, String role, Boolean isVerified, String username) {
        return userMapper.findUsersByConditions(userId, role, isVerified, username);
    }

    // ✅ Cập nhật thông tin người dùng
    public boolean updateUser(Long id, UserDTO userDTO) {
        User existingUser = userMapper.findById(id);
        if (existingUser == null) {
            return false;
        }

        existingUser.setUserId(id.intValue());
        existingUser.setUsername(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setFullName(userDTO.getFullName());

        try {
            // ⚠️ CHỈ SỬA DÒNG NÀY THEO CÁCH 1
            existingUser.setRole(User.Role.valueOf(userDTO.getRole().toLowerCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + userDTO.getRole());
        }

        if (userDTO.getIsVerified() != null) {
            existingUser.setVerified(userDTO.getIsVerified());
        }

        if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        return userMapper.updateUser(existingUser) > 0;
    }

    // ✅ Xóa người dùng
    public boolean deleteUser(int id) {
        return userMapper.deleteUserById(id) > 0;
    }
    public class FileStorageService {

        public String saveFile(MultipartFile file, String subFolder) {
            try {
                String uploadDir = "uploads/" + subFolder;
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String originalFilename = file.getOriginalFilename();
                String cleanedFilename = originalFilename != null ? originalFilename.replaceAll("\\s+", "_") : "file";
                String filename = UUID.randomUUID() + "_" + cleanedFilename;

                Path filePath = uploadPath.resolve(filename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                return subFolder + "/" + filename; // Trả về đường dẫn tương đối
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu file", e);
            }
        }
    }}
