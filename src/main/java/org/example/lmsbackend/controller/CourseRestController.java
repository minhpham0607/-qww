package org.example.lmsbackend.controller;

import org.example.lmsbackend.model.Course;
import org.example.lmsbackend.dto.CourseDTO;
import org.example.lmsbackend.service.CourseService;
import org.example.lmsbackend.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseRestController {

    @Autowired
    private CourseService courseService;
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> createCourse(
            @RequestPart("course") CourseDTO courseDTO,
            @RequestPart("image") MultipartFile imageFile) {

        System.out.println("📥 Received courseDTO: " + courseDTO);
        System.out.println("📷 Received file: " + imageFile.getOriginalFilename());

        try {
            boolean created = courseService.createCourse(courseDTO, imageFile);
            if (!created) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Tạo khóa học thất bại"));
            }
            return ResponseEntity.ok(Map.of("message", "Tạo khóa học thành công"));
        } catch (Exception e) {
            e.printStackTrace(); // xem lỗi cụ thể ở terminal
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
            boolean isInstructor = customUser.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_instructor"));
            if (isInstructor) {
                instructorId = customUser.getId(); // ✅ lấy đúng userId
            }
            System.out.println("🔍 User ID: " + customUser.getId());
        }

        System.out.printf("getCourses with: categoryId=%s, instructorId=%s, status=%s%n",
                categoryId, instructorId, status);

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
        boolean deleted = courseService.deleteCourse(courseId);
        if (deleted) {
            return ResponseEntity.ok("Course deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }
    }

}
