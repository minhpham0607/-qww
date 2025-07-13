package org.example.lmsbackend.service;

import org.example.lmsbackend.dto.UserDTO;
import org.example.lmsbackend.email.EmailService;
import org.example.lmsbackend.model.User;
import org.example.lmsbackend.repository.UserMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public boolean login(UserDTO userDTO) {
        User user = userMapper.findByUsername(userDTO.getUsername());
        if (user == null) return false;

        if (!user.isVerified()) {
            throw new RuntimeException("Tài khoản chưa được xác minh");
        }

        return passwordEncoder.matches(userDTO.getPassword(), user.getPassword());
    }

    public boolean register(UserDTO userDTO) {
        if (userMapper.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userMapper.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setFullName(userDTO.getFullName());

        try {
            User.Role role = User.Role.valueOf(userDTO.getRole().toLowerCase());
            user.setRole(role);

            if (role == User.Role.instructor) {
                if (userDTO.getCvUrl() == null || userDTO.getCvUrl().isBlank()) {
                    throw new RuntimeException("CV is required for instructor registration.");
                }
                user.setCvUrl(userDTO.getCvUrl());
                user.setVerified(false);
            } else {
                user.setVerified(userDTO.getIsVerified() != null ? userDTO.getIsVerified() : true);
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

    public List<User> getUsers(Integer userId, String role, Boolean isVerified, String username) {
        return userMapper.findUsersByConditions(userId, role, isVerified, username);
    }

    public boolean updateUser(Long id, UserDTO userDTO) {
        User existingUser = userMapper.findById(id);
        if (existingUser == null) return false;

        existingUser.setUserId(id.intValue());
        existingUser.setUsername(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setFullName(userDTO.getFullName());

        try {
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

    public boolean deleteUser(int id) {
        return userMapper.deleteUserById(id) > 0;
    }
}
