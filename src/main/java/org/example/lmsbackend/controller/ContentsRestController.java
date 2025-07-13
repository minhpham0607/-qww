package org.example.lmsbackend.controller;

import org.example.lmsbackend.dto.ContentsDTO;
import org.example.lmsbackend.service.ContentsService;
import org.example.lmsbackend.service.CourseService;
import org.example.lmsbackend.service.EnrollmentsService;
import org.example.lmsbackend.service.ModulesService;
import org.example.lmsbackend.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contents")
public class ContentsRestController {
    private static final String ROLE_INSTRUCTOR = "instructor";
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_STUDENT = "student";

    private final EnrollmentsService enrollmentsService;
    private final ContentsService contentsService;
    private final CourseService courseService;
    private final ModulesService moduleService;

    public ContentsRestController(
            EnrollmentsService enrollmentsService,
            ContentsService contentsService,
            CourseService courseService,
            ModulesService moduleService
    ) {
        this.enrollmentsService = enrollmentsService;
        this.contentsService = contentsService;
        this.courseService = courseService;
        this.moduleService = moduleService;
    }

    private ResponseEntity<String> checkInstructorOwnership(int userId, int moduleId, String action) {
        int courseId = moduleService.getCourseIdByModuleId(moduleId);
        if (!courseService.isInstructorOfCourse(userId, courseId)) {
            return ResponseEntity.status(403).body("Bạn không có quyền " + action + " nội dung này");
        }
        return null;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'instructor')")
    public ResponseEntity<String> createContent(@RequestBody ContentsDTO request,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        int userId = userDetails.getUserId();

        if (userDetails.hasRole(ROLE_INSTRUCTOR)) {
            ResponseEntity<String> err = checkInstructorOwnership(userId, request.getModuleId(), "tạo");
            if (err != null) return err;
        }

        contentsService.createContent(request);
        return ResponseEntity.ok("Content created successfully");
    }

    @GetMapping("/by-course/{courseId}")
    @PreAuthorize("hasAnyRole('admin', 'instructor', 'student')")
    public ResponseEntity<Object> getContentsByCourse(
            @PathVariable int courseId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        int userId = userDetails.getUserId();

        if (userDetails.hasRole(ROLE_INSTRUCTOR)) {
            if (!courseService.isInstructorOfCourse(userId, courseId)) {
                return ResponseEntity.status(403).body("Instructor không có quyền với khóa học này");
            }
        }

        if (userDetails.hasRole(ROLE_STUDENT)) {
            if (!enrollmentsService.isStudentEnrolled(userId, courseId)) {
                return ResponseEntity.status(403).body("Bạn chưa đăng ký khóa học này");
            }
        }

        List<ContentsDTO> contents = contentsService.getContentsByCourseId(courseId);
        return ResponseEntity.ok(contents);
    }

    @PutMapping("/{contentId}")
    @PreAuthorize("hasAnyRole('admin', 'instructor')")
    public ResponseEntity<String> updateContent(
            @PathVariable int contentId,
            @RequestBody ContentsDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails.hasRole(ROLE_INSTRUCTOR)) {
            ResponseEntity<String> err = checkInstructorOwnership(userDetails.getUserId(), request.getModuleId(), "sửa");
            if (err != null) return err;
        }

        request.setContentId(contentId);
        contentsService.updateContent(request);
        return ResponseEntity.ok("Cập nhật nội dung thành công");
    }

    @DeleteMapping("/{contentId}")
    @PreAuthorize("hasAnyRole('admin', 'instructor')")
    public ResponseEntity<String> deleteContent(
            @PathVariable int contentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails.hasRole(ROLE_INSTRUCTOR)) {
            Integer moduleId = contentsService.getModuleIdByContentId(contentId);
            if (moduleId == null) {
                return ResponseEntity.badRequest().body("Nội dung không tồn tại");
            }
            ResponseEntity<String> err = checkInstructorOwnership(userDetails.getUserId(), moduleId, "xóa");
            if (err != null) return err;
        }

        contentsService.deleteContent(contentId);
        return ResponseEntity.ok("Xóa nội dung thành công");
    }
}
