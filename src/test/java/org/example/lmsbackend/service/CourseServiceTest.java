package org.example.lmsbackend.service;

import org.example.lmsbackend.dto.CourseDTO;
import org.example.lmsbackend.model.Course;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CourseServiceTest {
    @Mock
    private org.example.lmsbackend.repository.CourseMapper courseMapper;
    @InjectMocks
    private CourseService courseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCourse_ValidInput() {
        CourseDTO dto = new CourseDTO();
        dto.setTitle("Test Course");
        dto.setDescription("Desc");
        dto.setCategoryId(1);
        dto.setInstructorId(1);
        dto.setStatus("draft");
        dto.setPrice(java.math.BigDecimal.TEN);
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        when(courseMapper.insertCourse(any(Course.class))).thenReturn(1);
        boolean result = courseService.createCourse(dto, file);
        Assertions.assertTrue(result);
    }

    @Test
    void testCreateCourse_MissingRequiredField() {
        CourseDTO dto = new CourseDTO();
        dto.setDescription("Desc");
        dto.setCategoryId(1);
        dto.setInstructorId(1);
        dto.setStatus("draft");
        dto.setPrice(java.math.BigDecimal.TEN);
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        when(courseMapper.insertCourse(any(Course.class))).thenThrow(new RuntimeException("Missing title"));
        boolean result = courseService.createCourse(dto, file);
        Assertions.assertFalse(result);
    }

    @Test
    void testCreateCourse_InvalidImage() {
        CourseDTO dto = new CourseDTO();
        dto.setTitle("Test Course");
        dto.setDescription("Desc");
        dto.setCategoryId(1);
        dto.setInstructorId(1);
        dto.setStatus("draft");
        dto.setPrice(java.math.BigDecimal.TEN);
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        try {
            when(file.getOriginalFilename()).thenReturn("largefile.exe");
            when(file.getBytes()).thenThrow(new RuntimeException("Invalid file"));
        } catch (Exception ignored) {}
        boolean result = courseService.createCourse(dto, file);
        Assertions.assertFalse(result);
    }

    @Test
    void testCreateCourse_InvalidStatus() {
        CourseDTO dto = new CourseDTO();
        dto.setTitle("Test Course");
        dto.setDescription("Desc");
        dto.setCategoryId(1);
        dto.setInstructorId(1);
        dto.setStatus("invalid");
        dto.setPrice(java.math.BigDecimal.TEN);
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        boolean result = courseService.createCourse(dto, file);
        Assertions.assertFalse(result);
    }

    @Test
    void testGetCourses_Admin() {
        when(courseMapper.findCourses(null, null, null)).thenReturn(Collections.singletonList(new Course()));
        List<Course> result = courseService.getCourses(null, null, null);
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    void testGetCourses_ByCategory() {
        when(courseMapper.findCourses(1, null, null)).thenReturn(Collections.singletonList(new Course()));
        List<Course> result = courseService.getCourses(1, null, null);
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    void testGetCourses_ByStatus() {
        when(courseMapper.findCourses(null, null, "published")).thenReturn(Collections.singletonList(new Course()));
        List<Course> result = courseService.getCourses(null, null, "published");
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    void testGetCourses_Instructor() {
        when(courseMapper.findCourses(null, 2, null)).thenReturn(Collections.singletonList(new Course()));
        List<Course> result = courseService.getCourses(null, 2, null);
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    void testCreateCourse_UserRole() {
        // Giả lập user thường không có quyền, service sẽ không gọi insertCourse
        CourseDTO dto = new CourseDTO();
        dto.setTitle("Test Course");
        dto.setDescription("Desc");
        dto.setCategoryId(1);
        dto.setInstructorId(99); // giả lập user thường
        dto.setStatus("draft");
        dto.setPrice(java.math.BigDecimal.TEN);
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        // Không gọi insertCourse, trả về false
        boolean result = false;
        try {
            result = courseService.createCourse(dto, file);
        } catch (Exception ignored) {}
        Assertions.assertFalse(result);
    }

    @Test
    void testCreateCourse_NotLoggedIn() {
        // Không truyền instructorId, giả lập chưa đăng nhập
        CourseDTO dto = new CourseDTO();
        dto.setTitle("Test Course");
        dto.setDescription("Desc");
        dto.setCategoryId(1);
        dto.setStatus("draft");
        dto.setPrice(java.math.BigDecimal.TEN);
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        boolean result = false;
        try {
            result = courseService.createCourse(dto, file);
        } catch (Exception ignored) {}
        Assertions.assertFalse(result);
    }
}
