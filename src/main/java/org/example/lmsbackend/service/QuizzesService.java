package org.example.lmsbackend.service;

import org.example.lmsbackend.model.Quizzes;
import org.example.lmsbackend.dto.QuizzesDTO;
import org.example.lmsbackend.repository.QuizzesMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuizzesService {

    private final QuizzesMapper quizzesMapper;

    public QuizzesService(QuizzesMapper quizzesMapper) {
        this.quizzesMapper = quizzesMapper;
    }

    public void createQuiz(QuizzesDTO dto) {
        Quizzes quiz = new Quizzes();
        quiz.setContentId(dto.getContentId());
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setTimeLimit(dto.getTimeLimit());
        quiz.setTotalPoints(dto.getTotalPoints());
        quizzesMapper.insertQuiz(quiz);
    }

    public List<QuizzesDTO> getAllQuizzes() {
        List<Quizzes> entities = quizzesMapper.getAllQuizzes();
        List<QuizzesDTO> dtos = new ArrayList<>();
        for (Quizzes q : entities) {
            dtos.add(mapToDto(q));
        }
        return dtos;
    }

    public void updateQuiz(QuizzesDTO dto) {
        Quizzes quiz = new Quizzes();
        quiz.setQuizId(dto.getQuizId());
        quiz.setContentId(dto.getContentId());
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setTimeLimit(dto.getTimeLimit());
        quiz.setTotalPoints(dto.getTotalPoints());
        quizzesMapper.updateQuiz(quiz);
    }

    public void deleteQuiz(int quizId) {
        quizzesMapper.deleteQuiz(quizId);
    }

    public Integer getContentIdByQuizId(Integer quizId) {
        Quizzes quiz = quizzesMapper.findById(quizId);
        return (quiz != null) ? quiz.getContentId() : null;
    }

    private QuizzesDTO mapToDto(Quizzes quiz) {
        QuizzesDTO dto = new QuizzesDTO();
        dto.setQuizId(quiz.getQuizId());
        dto.setContentId(quiz.getContentId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setTimeLimit(quiz.getTimeLimit());
        dto.setTotalPoints(quiz.getTotalPoints());
        return dto;
    }
}
