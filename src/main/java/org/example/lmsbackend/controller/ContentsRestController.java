package org.example.lmsbackend.controller;

import org.example.lmsbackend.dto.ContentsDTO;
import org.example.lmsbackend.service.ContentsService;
import org.example.lmsbackend.service.CourseService;
import org.example.lmsbackend.service.EnrollmentsService;
import org.example.lmsbackend.service.ModulesService;
import org.example.lmsbackend.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contents")
public class ContentsRestController {
    @Autowired
    private EnrollmentsService enrollmentsService;

    @Autowired
    private ContentsService contentsService;

    @Autowired
    private CourseService courseService; // 👈 để kiểm tra instructor

    @Autowired
    private ModulesService moduleService; // 👈 để tra courseId từ moduleId
    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'instructor')")
    public ResponseEntity<String> createContent(@RequestBody ContentsDTO request,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        int userId = userDetails.getUserId();

        // Nếu là instructor → kiểm tra quyền với module
        if (userDetails.hasRole("instructor")) {
            int courseId = moduleService.getCourseIdByModuleId(request.getModuleId());
            boolean isOwner = courseService.isInstructorOfCourse(userId, courseId);
            if (!isOwner) {
                return ResponseEntity.status(403).body("Bạn không có quyền tạo nội dung cho module này");
            }
        }
        contentsService.createContent(request);
        return ResponseEntity.ok("Content created successfully");
    }

    @GetMapping("/by-course/{courseId}")
    @PreAuthorize("hasAnyRole('admin', 'instructor', 'student')")
    public ResponseEntity<?> getContentsByCourse(
            @PathVariable int courseId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        int userId = userDetails.getUserId();

        // Nếu là instructor → phải dạy course đó
        if (userDetails.hasRole("instructor")) {
            if (!courseService.isInstructorOfCourse(userId, courseId)) {
                return ResponseEntity.status(403).body("Instructor không có quyền với khóa học này");
            }
        }

        // Nếu là student → phải đã đăng ký khóa học
        if (userDetails.hasRole("student")) {
            if (!enrollmentsService.isStudentEnrolled(userId, courseId)) {
                return ResponseEntity.status(403).body("Bạn chưa đăng ký khóa học này");
            }
        }

        List<ContentsDTO> contents = contentsService.getContentsByCourseId(courseId);
        return ResponseEntity.ok(contents);
    }
    @PutMapping("/{contentId}")
    @PreAuthorize("hasAnyRole('admin', 'instructor')")
    public ResponseEntity<?> updateContent(
            @PathVariable int contentId,
            @RequestBody ContentsDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // ✅ Kiểm tra quyền instructor có được sửa không (nếu cần)
        if (userDetails.hasRole("instructor")) {
            int courseId = moduleService.getCourseIdByModuleId(request.getModuleId());
            if (!courseService.isInstructorOfCourse(userDetails.getUserId(), courseId)) {
                return ResponseEntity.status(403).body("Bạn không có quyền sửa nội dung này");
            }
        }

        request.setContentId(contentId); // gán ID từ path
        contentsService.updateContent(request);
        return ResponseEntity.ok("Cập nhật nội dung thành công");
    }
    @DeleteMapping("/{contentId}")
    @PreAuthorize("hasAnyRole('admin', 'instructor')")
    public ResponseEntity<?> deleteContent(
            @PathVariable int contentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // (Optional) Kiểm tra quyền instructor có được xóa không
        if (userDetails.hasRole("instructor")) {
            // Lấy moduleId và courseId để kiểm tra quyền
            Integer moduleId = contentsService.getModuleIdByContentId(contentId);
            if (moduleId == null) {
                return ResponseEntity.badRequest().body("Nội dung không tồn tại");
            }
            int courseId = moduleService.getCourseIdByModuleId(moduleId);
            if (!courseService.isInstructorOfCourse(userDetails.getUserId(), courseId)) {
                return ResponseEntity.status(403).body("Bạn không có quyền xóa nội dung này");
            }
        }

        contentsService.deleteContent(contentId);
        return ResponseEntity.ok("Xóa nội dung thành công");
    }
}
