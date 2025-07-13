package org.example.lmsbackend.controller;

import org.example.lmsbackend.service.CourseService;
import org.example.lmsbackend.dto.QuestionsDTO;
import org.example.lmsbackend.service.QuestionsService;
import org.example.lmsbackend.service.QuizzesService;
import org.example.lmsbackend.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
public class QuestionsRestController {

    private final QuestionsService questionsService;
    private final CourseService courseService;
    private final QuizzesService quizzesService;

    public QuestionsRestController(QuestionsService questionsService,
                                   CourseService courseService,
                                   QuizzesService quizzesService) {
        this.questionsService = questionsService;
        this.courseService = courseService;
        this.quizzesService = quizzesService;
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'instructor')")
    public ResponseEntity<String> createQuestion(@RequestBody QuestionsDTO dto,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {


        // ✅ Nếu là instructor, chỉ cho phép tạo câu hỏi nếu là người dạy course chứa quiz đó
        if (userDetails.hasRole("instructor")) {
            boolean isOwner = courseService.isInstructorOwnerOfQuiz(userDetails.getUserId(), dto.getQuizId());
            if (!isOwner) {
                return ResponseEntity.status(403).body("🚫 Bạn không có quyền tạo câu hỏi cho quiz này.");
            }
        }

        // ✅ Tạo câu hỏi
        boolean created = questionsService.createQuestion(dto);
        if (created) {
            return ResponseEntity.ok("✅ Tạo câu hỏi thành công.");
        } else {
            return ResponseEntity.badRequest().body("❌ Tạo câu hỏi thất bại.");
        }
    }
}
