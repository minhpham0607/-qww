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

    private final CourseService courseService;
    private final EnrollmentsService enrollmentsService;

    public CourseRestController(CourseService courseService, EnrollmentsService enrollmentsService) {
        this.courseService = courseService;
        this.enrollmentsService = enrollmentsService;
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
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer instructorId = null;

        if (principal instanceof CustomUserDetails customUser) {
            if (customUser.hasRole("instructor")) {
                instructorId = customUser.getId();
            }
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
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (principal instanceof CustomUserDetails customUser) {
                if (customUser.hasRole("admin") ||
                        (customUser.hasRole("instructor") && course.getInstructorId().equals(customUser.getId())) ||
                        (customUser.hasRole("student"))) {
                    return ResponseEntity.ok(course); // TODO: Add student enrollment check
                }
            }

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Bạn không có quyền truy cập khóa học này"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi server khi lấy thông tin khóa học"));
        }
    }
}
