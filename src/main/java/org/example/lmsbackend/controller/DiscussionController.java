package org.example.lmsbackend.controller;

import org.example.lmsbackend.dto.DiscussionDTO;
import org.example.lmsbackend.service.DiscussionService;
import org.example.lmsbackend.service.CourseService;
import org.example.lmsbackend.service.EnrollmentsService;
import org.example.lmsbackend.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionController {
    private final DiscussionService discussionService;
    private final CourseService courseService;
    private final EnrollmentsService enrollmentsService;

    public DiscussionController(DiscussionService discussionService,
                                CourseService courseService,
                                EnrollmentsService enrollmentsService) {
        this.discussionService = discussionService;
        this.courseService = courseService;
        this.enrollmentsService = enrollmentsService;
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'instructor', 'student')")
    public List<DiscussionDTO> getAllDiscussions(@RequestParam Integer courseId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Chỉ trả về thảo luận của khóa học mà user có quyền
        if (userDetails.hasRole("instructor")) {
            if (!courseService.isInstructorOfCourse(userDetails.getUserId(), courseId)) {
                return List.of();
            }
        }
        if (userDetails.hasRole("student")) {
            if (!enrollmentsService.isStudentEnrolled(userDetails.getUserId(), courseId)) {
                return List.of();
            }
        }
        return discussionService.getDiscussionsByCourse(courseId);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('admin', 'instructor', 'student')")
    public List<DiscussionDTO> getDiscussionsByCourse(@PathVariable Integer courseId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.hasRole("instructor")) {
            if (!courseService.isInstructorOfCourse(userDetails.getUserId(), courseId)) {
                return List.of();
            }
        }
        if (userDetails.hasRole("student")) {
            if (!enrollmentsService.isStudentEnrolled(userDetails.getUserId(), courseId)) {
                return List.of();
            }
        }
        return discussionService.getDiscussionsByCourse(courseId);
    }

    @PostMapping
    @PreAuthorize("hasRole('instructor')")
    public ResponseEntity<DiscussionDTO> createDiscussion(@RequestBody DiscussionDTO dto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Kiểm tra instructor có dạy khóa này không
        if (!courseService.isInstructorOfCourse(userDetails.getUserId(), dto.getCourseId())) {
            return ResponseEntity.status(403).build();
        }
        dto.setUserId(userDetails.getUserId()); // Gán userId đúng
        DiscussionDTO created = discussionService.createDiscussion(dto);
        if (created == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('instructor')")
    public ResponseEntity<DiscussionDTO> updateDiscussion(@PathVariable Integer id, @RequestBody DiscussionDTO dto) {
        DiscussionDTO updated = discussionService.updateDiscussion(id, dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('instructor')")
    public ResponseEntity<Void> deleteDiscussion(@PathVariable Integer id) {
        boolean deleted = discussionService.deleteDiscussion(id);
        if (!deleted) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
