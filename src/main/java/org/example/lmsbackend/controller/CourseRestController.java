package org.example.lmsbackend.controller;

import org.example.lmsbackend.dto.CourseDTO;
import org.example.lmsbackend.dto.EnrollmentsDTO;
import org.example.lmsbackend.model.Course;
import org.example.lmsbackend.security.CustomUserDetails;
import org.example.lmsbackend.service.CourseService;
import org.example.lmsbackend.service.EnrollmentsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
@RestController
@RequestMapping("/api/courses")
public class CourseRestController {

    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_INSTRUCTOR = "instructor";
    private static final String ROLE_STUDENT = "student";

    private final CourseService courseService;
    private final EnrollmentsService enrollmentsService;

    public CourseRestController(CourseService courseService, EnrollmentsService enrollmentsService) {
        this.courseService = courseService;
        this.enrollmentsService = enrollmentsService;
    }

    private CustomUserDetails getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }
        return null;
    }

    private boolean canAccessCourse(CustomUserDetails user, Course course) {
        if (user.hasRole(ROLE_ADMIN)) return true;
        if (user.hasRole(ROLE_INSTRUCTOR) && course.getInstructorId().equals(user.getId())) return true;
        if (user.hasRole(ROLE_STUDENT)) {
            // TODO: kiểm tra user đã đăng ký khóa học hay chưa
            return true;
        }
        return false;
    }

    @GetMapping("/all-with-status")
    @PreAuthorize("hasRole('student') or hasRole('admin') or hasRole('instructor')")
    public ResponseEntity<List<Map<String, Object>>> getAllCoursesWithStatus(@RequestParam int userId) {
        List<Course> allCourses = courseService.getCourses(null, null, null);
        List<EnrollmentsDTO> enrolled = enrollmentsService.getEnrolledCourses(userId);

        Set<Integer> enrolledCourseIds = new HashSet<>();
        for (EnrollmentsDTO dto : enrolled) {
            enrolledCourseIds.add(dto.getCourseId());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Course course : allCourses) {
            Map<String, Object> item = new HashMap<>();
            item.put("courseId", course.getCourseId());
            item.put("title", course.getTitle());
            item.put("description", course.getDescription());
            item.put("price", course.getPrice());
            item.put("thumbnailUrl", course.getThumbnailUrl());
            item.put("enrolled", enrolledCourseIds.contains(course.getCourseId()));
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> createCourse(
            @RequestPart("course") CourseDTO courseDTO,
            @RequestPart("image") MultipartFile imageFile) {

        try {
            boolean created = courseService.createCourse(courseDTO, imageFile);
            if (!created) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Tạo khóa học thất bại"));
            }
            return ResponseEntity.ok(Map.of("message", "Tạo khóa học thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('admin') or hasRole('instructor')")
    public ResponseEntity<List<Course>> listCourses(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status
    ) {
        CustomUserDetails currentUser = getCurrentUser();
        Integer instructorId = null;

        if (currentUser != null && currentUser.hasRole(ROLE_INSTRUCTOR)) {
            instructorId = currentUser.getId();
        }

        List<Course> courses = courseService.getCourses(categoryId, instructorId, status);
        return ResponseEntity.ok(courses);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> updateCourse(
            @PathVariable("id") Integer courseId,
            @RequestPart("course") Course course,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        course.setCourseId(courseId);

        boolean updated = courseService.updateCourse(course, imageFile);
        if (updated) {
            return ResponseEntity.ok("Course updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> deleteCourse(@PathVariable("id") Integer courseId) {
        try {
            boolean deleted = courseService.deleteCourse(courseId);
            if (deleted) {
                return ResponseEntity.ok("Course deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Cannot delete course. Please remove all related videos and enrollments first.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while deleting the course.");
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('student') or hasRole('instructor') or hasRole('admin')")
    public ResponseEntity<?> getCourseById(@PathVariable("id") Integer courseId) {
        try {
            Optional<Course> courseOpt = courseService.getCourseById(courseId);
            if (courseOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Khóa học không tồn tại"));
            }

            Course course = courseOpt.get();
            CustomUserDetails user = getCurrentUser();

            if (user == null || !canAccessCourse(user, course)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Bạn không có quyền truy cập khóa học này"));
            }

            return ResponseEntity.ok(course);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi server khi lấy thông tin khóa học"));
        }
    }
}
