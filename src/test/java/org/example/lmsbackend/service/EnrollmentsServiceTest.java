package org.example.lmsbackend.service;
import org.example.lmsbackend.dto.EnrollmentsDTO;
import org.example.lmsbackend.dto.UserDTO;
import org.example.lmsbackend.repository.EnrollmentsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnrollmentsServiceTest {

    @InjectMocks
    private EnrollmentsService enrollmentsService;

    @Mock
    private EnrollmentsMapper enrollmentMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void enrollUserInCourse_WhenAlreadyEnrolled_ReturnsFalse() {
        when(enrollmentMapper.countEnrollment(1, 1)).thenReturn(1);

        boolean result = enrollmentsService.enrollUserInCourse(1, 1);

        assertFalse(result);
        verify(enrollmentMapper, never()).enrollCourse(anyInt(), anyInt());
    }

    @Test
    void enrollUserInCourse_WhenNotEnrolled_EnrollsAndReturnsTrue() {
        when(enrollmentMapper.countEnrollment(1, 1)).thenReturn(0);

        boolean result = enrollmentsService.enrollUserInCourse(1, 1);

        assertTrue(result);
        verify(enrollmentMapper).enrollCourse(1, 1);
    }

    @Test
    void getEnrolledCourses_ReturnsList() {
        List<EnrollmentsDTO> mockList = Arrays.asList(new EnrollmentsDTO(), new EnrollmentsDTO());
        when(enrollmentMapper.getEnrolledCoursesByUserId(1)).thenReturn(mockList);

        List<EnrollmentsDTO> result = enrollmentsService.getEnrolledCourses(1);

        assertEquals(2, result.size());
    }

    @Test
    void getEnrolledCourses_WhenEmpty_ReturnsEmptyList() {
        when(enrollmentMapper.getEnrolledCoursesByUserId(2)).thenReturn(Collections.emptyList());

        List<EnrollmentsDTO> result = enrollmentsService.getEnrolledCourses(2);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteEnrollment_ReturnsAffectedRows() {
        when(enrollmentMapper.deleteEnrollment(1, 1)).thenReturn(1);

        int result = enrollmentsService.deleteEnrollment(1, 1);

        assertEquals(1, result);
    }

    @Test
    void deleteEnrollment_WhenNotExists_ReturnsZero() {
        when(enrollmentMapper.deleteEnrollment(1, 999)).thenReturn(0);

        int result = enrollmentsService.deleteEnrollment(1, 999);

        assertEquals(0, result);
    }

    @Test
    void getEnrolledUsersByCourse_ReturnsListOfUsers() {
        List<UserDTO> users = Arrays.asList(new UserDTO(), new UserDTO());
        when(enrollmentMapper.getUsersByCourseId(1)).thenReturn(users);

        List<UserDTO> result = enrollmentsService.getEnrolledUsersByCourse(1);

        assertEquals(2, result.size());
    }

    @Test
    void getEnrolledUsersByCourse_WhenNoUsers_ReturnsEmptyList() {
        when(enrollmentMapper.getUsersByCourseId(2)).thenReturn(Collections.emptyList());

        List<UserDTO> result = enrollmentsService.getEnrolledUsersByCourse(2);

        assertTrue(result.isEmpty());
    }

    @Test
    void isStudentEnrolled_WhenEnrolled_ReturnsTrue() {
        when(enrollmentMapper.countEnrollment(1, 1)).thenReturn(1);

        boolean result = enrollmentsService.isStudentEnrolled(1, 1);

        assertTrue(result);
    }

    @Test
    void isStudentEnrolled_WhenNotEnrolled_ReturnsFalse() {
        when(enrollmentMapper.countEnrollment(1, 1)).thenReturn(0);

        boolean result = enrollmentsService.isStudentEnrolled(1, 1);

        assertFalse(result);
    }
}
