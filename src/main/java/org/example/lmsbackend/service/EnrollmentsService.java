package org.example.lmsbackend.service;

import org.example.lmsbackend.dto.EnrollmentsDTO;
import org.example.lmsbackend.dto.UserDTO;
import org.example.lmsbackend.repository.EnrollmentsMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentsService {

    private final EnrollmentsMapper enrollmentMapper;

    public EnrollmentsService(EnrollmentsMapper enrollmentMapper) {
        this.enrollmentMapper = enrollmentMapper;
    }

    public List<Integer> getEnrolledCourseIds(int userId) {
        return enrollmentMapper.getEnrolledCourseIdsByUserId(userId);
    }

    public boolean enrollUserInCourse(int userId, int courseId) {
        if (isStudentEnrolled(userId, courseId)) {
            return false;
        }
        enrollmentMapper.enrollCourse(userId, courseId);
        return true;
    }

    public List<EnrollmentsDTO> getEnrolledCourses(int userId) {
        return enrollmentMapper.getEnrolledCoursesByUserId(userId);
    }

    public int deleteEnrollment(int userId, int courseId) {
        return enrollmentMapper.deleteEnrollment(userId, courseId);
    }

    public List<UserDTO> getEnrolledUsersByCourse(int courseId) {
        return enrollmentMapper.getUsersByCourseId(courseId);
    }

    public boolean isStudentEnrolled(int userId, int courseId) {
        return enrollmentMapper.countEnrollment(userId, courseId) > 0;
    }
}
