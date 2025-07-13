package org.example.lmsbackend.service;

import org.example.lmsbackend.dto.CourseDTO;
import org.example.lmsbackend.model.Course;
import org.example.lmsbackend.repository.CourseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CourseServiceIntegrationTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseMapper courseMapper;

    @Test
    void createCourse_WithValidData_ShouldSucceed() {
        CourseDTO dto = new CourseDTO();
        dto.setTitle("Integration Course " + System.currentTimeMillis());
        dto.setDescription("This is a course for integration testing");
        dto.setCategoryId(1); // Ensure category with ID 1 exists
        dto.setInstructorId(1); // Ensure instructor with ID 1 exists
        dto.setStatus("draft");
        dto.setPrice(BigDecimal.valueOf(99.99));

        boolean created = courseService.createCourse(dto, null); // No image
        assertTrue(created);

        List<Course> result = courseMapper.findCourses(null, 1, null);
        assertTrue(result.stream().anyMatch(c -> c.getTitle().contains("Integration Course")));
    }

    @Test
    void getCourses_ByCategory_ShouldReturnResults() {
        List<Course> courses = courseService.getCourses(1, null, null);
        assertNotNull(courses);
    }

    @Test
    void getCourses_ByInstructor_ShouldReturnResults() {
        List<Course> courses = courseService.getCourses(null, 1, null);
        assertNotNull(courses);
    }

    @Test
    void getCourses_ByStatus_ShouldReturnResults() {
        List<Course> courses = courseService.getCourses(null, null, "draft");
        assertNotNull(courses);
    }

    @Test
    void createCourse_InvalidStatus_ShouldFail() {
        CourseDTO dto = new CourseDTO();
        dto.setTitle("Invalid Status Test");
        dto.setDescription("Test");
        dto.setCategoryId(1);
        dto.setInstructorId(1);
        dto.setStatus("not-a-valid-status");
        dto.setPrice(BigDecimal.TEN);

        boolean result = courseService.createCourse(dto, null);
        assertFalse(result);
    }
    @Test
    void createCourse_WithoutInstructor_ShouldFail() {
        CourseDTO dto = new CourseDTO();
        dto.setTitle("No Instructor Test");
        dto.setDescription("Missing instructor");
        dto.setCategoryId(1);
        dto.setStatus("draft");
        dto.setPrice(BigDecimal.valueOf(49.99));

        boolean result = courseService.createCourse(dto, null);
        assertFalse(result);
    }

    @Test
    void createCourse_WithDuplicateTitle_ShouldSucceed() {
        String title = "Duplicate Title " + System.currentTimeMillis();

        CourseDTO dto1 = new CourseDTO();
        dto1.setTitle(title);
        dto1.setDescription("First");
        dto1.setCategoryId(1);
        dto1.setInstructorId(1);
        dto1.setStatus("draft");
        dto1.setPrice(BigDecimal.TEN);

        CourseDTO dto2 = new CourseDTO();
        dto2.setTitle(title);
        dto2.setDescription("Second");
        dto2.setCategoryId(1);
        dto2.setInstructorId(1);
        dto2.setStatus("draft");
        dto2.setPrice(BigDecimal.TEN);

        assertTrue(courseService.createCourse(dto1, null));
        assertTrue(courseService.createCourse(dto2, null)); // ✅ Cho phép trùng title nếu không unique
    }

    @Test
    void getCourses_WithNoMatch_ShouldReturnEmptyList() {
        List<Course> result = courseService.getCourses(-999, -999, "nonexistent");
        assertTrue(result.isEmpty());
    }
    @Test
    void deleteCourse_AfterCreation_ShouldSucceed() {
        // Bước 1: Tạo khóa học mới
        String uniqueTitle = "Course To Delete " + System.currentTimeMillis();
        CourseDTO dto = new CourseDTO();
        dto.setTitle(uniqueTitle);
        dto.setDescription("This course will be deleted");
        dto.setCategoryId(1); // giả định tồn tại
        dto.setInstructorId(1); // giả định tồn tại
        dto.setStatus("draft");
        dto.setPrice(BigDecimal.valueOf(49.99));

        boolean created = courseService.createCourse(dto, null);
        assertTrue(created, "Tạo khóa học thất bại");

        // Bước 2: Tìm lại courseId vừa tạo
        List<Course> createdCourses = courseMapper.findCourses(null, 1, null);
        Course createdCourse = createdCourses.stream()
                .filter(c -> c.getTitle().equals(uniqueTitle))
                .findFirst()
                .orElse(null);

        assertNotNull(createdCourse, "Không tìm thấy khóa học vừa tạo");
        int courseId = createdCourse.getCourseId();

        // Bước 3: Thực hiện xóa
        boolean deleted = courseService.deleteCourse(courseId);
        assertTrue(deleted, "Xóa khóa học không thành công");

        // Bước 4: Đảm bảo khóa học không còn tồn tại
        List<Course> afterDelete = courseMapper.findCourses(null, 1, null);
        boolean exists = afterDelete.stream().anyMatch(c -> c.getCourseId() == courseId);
        assertFalse(exists, "Khóa học vẫn còn sau khi xóa");
    }

}
