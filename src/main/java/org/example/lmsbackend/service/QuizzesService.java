package org.example.lmsbackend.service;

import org.example.lmsbackend.model.Quizzes;
import org.example.lmsbackend.dto.QuizzesDTO;
import org.example.lmsbackend.repository.QuizzesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuizzesService {
    @Autowired
    private QuizzesMapper quizzesMapper;

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
            QuizzesDTO dto = new QuizzesDTO();
            dto.setQuizId(q.getQuizId());
            dto.setContentId(q.getContentId());
            dto.setTitle(q.getTitle());
            dto.setDescription(q.getDescription());
            dto.setTimeLimit(q.getTimeLimit());
            dto.setTotalPoints(q.getTotalPoints());
            dtos.add(dto);
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


}
