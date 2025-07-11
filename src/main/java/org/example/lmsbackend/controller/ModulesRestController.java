package org.example.lmsbackend.controller;

import org.example.lmsbackend.service.CourseService;
import org.example.lmsbackend.model.Enrollment;
import org.example.lmsbackend.service.EnrollmentsService;
import org.example.lmsbackend.dto.ModulesDTO;
import org.example.lmsbackend.service.ModulesService;
import org.example.lmsbackend.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/modules")
public class ModulesRestController {
    @Autowired
    private CourseService courseService;
    @Autowired
    private EnrollmentsService enrollmentsService;

    @Autowired
    private ModulesService moduleService;

    @PostMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('admin', 'instructor')")
    public ResponseEntity<String> createModule(@PathVariable int courseId,
                                               @RequestBody ModulesDTO request,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        // Gán courseId từ URL vào request
        request.setCourseId(courseId);

        // Kiểm tra instructor có dạy khóa này không
        if (userDetails.hasRole("instructor")) {
            boolean isOwner = courseService.isInstructorOfCourse(userDetails.getUserId(), courseId);
            if (!isOwner) {
                return ResponseEntity.status(403).body("Bạn không có quyền tạo module cho khóa học này");
            }
        }

        moduleService.createModule(request);
        return ResponseEntity.ok("Module created successfully");
    }
    @GetMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('admin', 'instructor', 'student')")
    public ResponseEntity<?> getModulesByCourse(@PathVariable int courseId,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {

        int userId = userDetails.getUserId();

        if (userDetails.hasRole("instructor")) {
            boolean isOwner = courseService.isInstructorOfCourse(userId, courseId);
            if (!isOwner) {
                return ResponseEntity.status(403).body("Bạn không dạy khóa học này");
            }
        }

        if (userDetails.hasRole("student")) {
            boolean isEnrolled = enrollmentsService.isStudentEnrolled(userId, courseId);
            if (!isEnrolled) {
                return ResponseEntity.status(403).body("Bạn chưa đăng ký khóa học này");
            }
        }

        return ResponseEntity.ok(moduleService.getModulesByCourseId(courseId));
    }
    @PutMapping("/{moduleId}")
    @PreAuthorize("hasAnyRole('admin', 'instructor')")
    public ResponseEntity<String> updateModule(@PathVariable int moduleId,
                                               @RequestBody ModulesDTO request,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        request.setModuleId(moduleId); // Gán moduleId vào DTO
        int courseId = request.getCourseId(); // Đảm bảo frontend gửi courseId

        // Instructor chỉ được sửa module của khóa học mình dạy
        if (userDetails.hasRole("instructor")) {
            boolean isOwner = courseService.isInstructorOfCourse(userDetails.getUserId(), courseId);
            if (!isOwner) {
                return ResponseEntity.status(403).body("Bạn không có quyền sửa module này");
            }
        }

        moduleService.updateModule(request);
        return ResponseEntity.ok("Module updated successfully");
    }
    @DeleteMapping("/{moduleId}")
    @PreAuthorize("hasAnyRole('admin', 'instructor')")
    public ResponseEntity<String> deleteModule(@PathVariable int moduleId,
                                               @RequestParam int courseId, // gửi kèm courseId để check quyền
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails.hasRole("instructor")) {
            boolean isOwner = courseService.isInstructorOfCourse(userDetails.getUserId(), courseId);
            if (!isOwner) {
                return ResponseEntity.status(403).body("Bạn không có quyền xóa module này");
            }
        }

        moduleService.deleteModule(moduleId);
        return ResponseEntity.ok("Module deleted successfully");
    }


}
