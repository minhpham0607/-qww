package org.example.lmsbackend.controller;

import org.example.lmsbackend.service.ContentsService;
import org.example.lmsbackend.service.CourseService;
import org.example.lmsbackend.service.EnrollmentsService;
import org.example.lmsbackend.dto.QuizzesDTO;
import org.example.lmsbackend.service.QuizzesService;
import org.example.lmsbackend.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/quizzes")
public class QuizzesRestController {

    private final QuizzesService quizzesService;
    private final ContentsService contentsService;
    private final CourseService courseService;
    private final EnrollmentsService enrollmentsService;

    public QuizzesRestController(
            QuizzesService quizzesService,
            ContentsService contentsService,
            CourseService courseService,
            EnrollmentsService enrollmentsService
    ) {
        this.quizzesService = quizzesService;
        this.contentsService = contentsService;
        this.courseService = courseService;
        this.enrollmentsService = enrollmentsService;
    }
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('admin', 'instructor')")
    public ResponseEntity<String> createQuiz(@RequestBody QuizzesDTO quizzesDTO,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {

        int contentId = quizzesDTO.getContentId();

        if (userDetails.hasRole("instructor")) {
            // Lấy courseId từ content để kiểm tra instructor
            Integer courseId = contentsService.getCourseIdByContentId(contentId);
            boolean isOwner = courseService.isInstructorOfCourse(userDetails.getUserId(), courseId);
            if (!isOwner) {
                return ResponseEntity.status(403).body("Bạn không có quyền tạo quiz cho content này");
            }
        }

        quizzesService.createQuiz(quizzesDTO);
        return ResponseEntity.ok("Quiz created successfully");
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'instructor', 'student')")
    public ResponseEntity<?> getAllQuizzes(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<QuizzesDTO> allQuizzes = quizzesService.getAllQuizzes();

        // ✅ Nếu là admin hoặc instructor → trả về tất cả
        if (!userDetails.hasRole("student")) {
            return ResponseEntity.ok(allQuizzes);
        }

        int studentId = userDetails.getUserId();

        // ✅ Nếu là student → lọc những bài quiz thuộc khóa học đã đăng ký
        List<QuizzesDTO> accessible = allQuizzes.stream()
                .filter(quiz -> {
                    Integer courseId = contentsService.getCourseIdByContentId(quiz.getContentId());
                    return courseId != null && enrollmentsService.isStudentEnrolled(studentId, courseId);
                })
                .toList();

        return ResponseEntity.ok(accessible);
    }

    @PutMapping("/update/{quizId}")
    @PreAuthorize("hasAnyRole('admin', 'instructor')")
    public ResponseEntity<String> updateQuiz(@PathVariable int quizId,
                                             @RequestBody QuizzesDTO quizzesDTO,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        quizzesDTO.setQuizId(quizId);

        if (userDetails.hasRole("instructor")) {
            Integer courseId = contentsService.getCourseIdByContentId(quizzesDTO.getContentId());
            boolean isOwner = courseService.isInstructorOfCourse(userDetails.getUserId(), courseId);
            if (!isOwner) {
                return ResponseEntity.status(403).body("Bạn không có quyền sửa quiz này");
            }
        }

        quizzesService.updateQuiz(quizzesDTO);
        return ResponseEntity.ok("Quiz updated successfully");
    }

    @DeleteMapping("/delete/{quizId}")
    @PreAuthorize("hasAnyRole('admin', 'instructor')")
    public ResponseEntity<String> deleteQuiz(@PathVariable int quizId,
                                             @RequestParam int contentId,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails.hasRole("instructor")) {
            Integer courseId = contentsService.getCourseIdByContentId(contentId);
            boolean isOwner = courseService.isInstructorOfCourse(userDetails.getUserId(), courseId);
            if (!isOwner) {
                return ResponseEntity.status(403).body("Bạn không có quyền xóa quiz này");
            }
        }

        quizzesService.deleteQuiz(quizId);
        return ResponseEntity.ok("Quiz deleted successfully");
    }
}