package org.example.lmsbackend.service;

import org.example.lmsbackend.model.Questions;
import org.example.lmsbackend.dto.QuestionsDTO;
import org.example.lmsbackend.repository.QuestionsMapper;
import org.example.lmsbackend.repository.QuizzesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionsService {

    @Autowired
    private QuestionsMapper questionsMapper;

    @Autowired
    private QuizzesMapper quizzesMapper;

    // Tạo mới câu hỏi
    public boolean createQuestion(QuestionsDTO dto) {


        Questions question = new Questions();
        question.setQuiz(new org.example.lmsbackend.model.Quizzes(dto.getQuizId())); // chỉ cần ID
        question.setQuestionText(dto.getQuestionText());
        question.setType(Questions.Type.valueOf(dto.getType()));
        question.setPoints(dto.getPoints());

        questionsMapper.insertQuestion(question);
        return true;
    }

    // (Tùy chọn) Lấy danh sách câu hỏi theo quiz
    public List<Questions> getQuestionsByQuizId(int quizId) {
        return questionsMapper.findByQuizId(quizId);
    }
}
