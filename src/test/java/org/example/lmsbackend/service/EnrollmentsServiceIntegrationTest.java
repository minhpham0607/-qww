package org.example.lmsbackend.service;

import org.example.lmsbackend.dto.UserDTO;
import org.example.lmsbackend.model.User;
import org.example.lmsbackend.repository.CourseMapper;
import org.example.lmsbackend.repository.EnrollmentsMapper;
import org.example.lmsbackend.repository.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EnrollmentsServiceIntegrationTest {

    @Autowired private EnrollmentsService enrollmentsService;
    @Autowired private EnrollmentsMapper enrollmentsMapper;
    @Autowired private UserService userService;
    @Autowired private UserMapper userMapper;
    @Autowired private CourseService courseService;
    @Autowired private CourseMapper courseMapper;

    private int testUserId;
    private int testCourseId;

    /**
     * Thiết lập dữ liệu test: Tạo một người dùng và một khóa học thật trong DB trước mỗi test
     */
    @BeforeEach
    void setup() {
        // Tạo người dùng mới để kiểm thử
        String username = "integration_user_" + System.currentTimeMillis();
        UserDTO dto = new UserDTO();
        dto.setUsername(username);
        dto.setPassword("abc123");
        dto.setEmail(username + "@mail.com");
        dto.setRole("student");
        dto.setFullName("Integration User");

        userService.register(dto);
        User user = userMapper.findByUsername(username);
        testUserId = user.getUserId();

        // Tạo khóa học mới để kiểm thử
        var courseDTO = new org.example.lmsbackend.dto.CourseDTO();
        courseDTO.setTitle("Integration Course");
        courseDTO.setDescription("For integration test");
        courseDTO.setCategoryId(1); // giả định categoryId 1 đã tồn tại
        courseDTO.setInstructorId(1); // giả định instructorId 1 đã tồn tại
        courseDTO.setStatus("draft");
        courseDTO.setPrice(java.math.BigDecimal.TEN);

        courseService.createCourse(courseDTO, null);
        var course = courseMapper.findCourses(null, null, null)
                .stream()
                .filter(c -> c.getTitle().equals("Integration Course"))
                .findFirst()
                .orElseThrow();
        testCourseId = course.getCourseId();
    }

    // ✅ Test 1: Ghi danh thành công
    @Test
    void enrollUser_Success() {
        boolean result = enrollmentsService.enrollUserInCourse(testUserId, testCourseId);
        assertTrue(result); // Mong đợi trả về true
    }

    // ❌ Test 2: Ghi danh trùng - không được phép ghi danh 2 lần cùng khóa học
    @Test
    void enrollUser_DuplicateEnrollment_ShouldFail() {
        enrollmentsService.enrollUserInCourse(testUserId, testCourseId); // lần 1
        boolean result = enrollmentsService.enrollUserInCourse(testUserId, testCourseId); // lần 2
        assertFalse(result); // Phải trả về false vì đã ghi danh
    }

    // ✅ Test 3: Hủy ghi danh thành công
    @Test
    void deleteEnrollment_Success() {
        enrollmentsService.enrollUserInCourse(testUserId, testCourseId);
        int deleted = enrollmentsService.deleteEnrollment(testUserId, testCourseId);
        assertEquals(1, deleted); // Mong đợi trả về 1 dòng bị xóa
    }

    // ✅ Test 4: Kiểm tra đã ghi danh
    @Test
    void isStudentEnrolled_ReturnsTrue() {
        enrollmentsService.enrollUserInCourse(testUserId, testCourseId);
        assertTrue(enrollmentsService.isStudentEnrolled(testUserId, testCourseId));
    }

    // ❌ Test 5: Hủy ghi danh khi chưa ghi danh => không có gì để xóa
    @Test
    void deleteEnrollment_WhenNotEnrolled_ShouldReturnZero() {
        int result = enrollmentsService.deleteEnrollment(testUserId, testCourseId);
        assertEquals(0, result, "Should return 0 because user is not enrolled yet");
    }

    // ❌ Test 6: Ghi danh với `courseId` không tồn tại (FK error)
    @Test
    void enrollUser_WithInvalidCourseId_ShouldFail() {
        int invalidCourseId = -99;
        assertThrows(Exception.class, () -> {
            enrollmentsService.enrollUserInCourse(testUserId, invalidCourseId);
        });
    }

    // ❌ Test 7: Ghi danh với `userId` không tồn tại (FK error)
    @Test
    void enrollUser_WithInvalidUserId_ShouldFail() {
        int invalidUserId = -100;
        assertThrows(Exception.class, () -> {
            enrollmentsService.enrollUserInCourse(invalidUserId, testCourseId);
        });
    }

    // ❌ Test 8: Kiểm tra ghi danh với dữ liệu không hợp lệ (userId & courseId đều sai)
    @Test
    void isStudentEnrolled_WithInvalidData_ShouldReturnFalse() {
        boolean result = enrollmentsService.isStudentEnrolled(-1, -1);
        assertFalse(result);
    }

    // ❌ Test 9: Lấy danh sách người dùng ghi danh vào course không tồn tại
    @Test
    void getEnrolledUsersByCourse_InvalidCourse_ShouldReturnEmptyList() {
        var users = enrollmentsService.getEnrolledUsersByCourse(-1);
        assertTrue(users.isEmpty(), "Should return empty list for invalid course");
    }

    // ✅ Test 10: Sau khi ghi danh, danh sách khóa học đã ghi danh của user phải chứa khóa đó
    @Test
    void getEnrolledCourses_AfterEnrollment_ShouldContainCourse() {
        enrollmentsService.enrollUserInCourse(testUserId, testCourseId);
        var courses = enrollmentsService.getEnrolledCourses(testUserId);
        assertTrue(
                courses.stream().anyMatch(c -> c.getCourseId() == testCourseId),
                "Enrolled course should be in the returned list"
        );
    }

    // ❌ Test 11: Lấy danh sách khóa học khi user chưa học gì => danh sách rỗng
    @Test
    void getEnrolledCourses_WhenUserHasNoCourses_ShouldReturnEmptyList() {
        int userIdWithNoCourses = testUserId + 999; // giả định userId này chưa tồn tại
        var courses = enrollmentsService.getEnrolledCourses(userIdWithNoCourses);
        assertTrue(courses.isEmpty());
    }
}
