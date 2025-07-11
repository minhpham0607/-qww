package org.example.lmsbackend.repository;

import org.apache.ibatis.annotations.*;
import org.example.lmsbackend.model.Questions;

import java.util.List;

@Mapper
public interface QuestionsMapper {

    // Thêm câu hỏi
    @Insert("""
        INSERT INTO questions (quiz_id, question_text, type, points)
        VALUES (#{quiz.quizId}, #{questionText}, #{type}, #{points})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "questionId")
    void insertQuestion(Questions question);

    // Lấy danh sách câu hỏi theo quiz_id
    @Select("""
        SELECT question_id, quiz_id, question_text, type, points
        FROM questions
        WHERE quiz_id = #{quizId}
    """)
    List<Questions> findByQuizId(@Param("quizId") int quizId);

    // Cập nhật câu hỏi
    @Update("""
        UPDATE questions
        SET question_text = #{questionText},
            type = #{type},
            points = #{points}
        WHERE question_id = #{questionId}
    """)
    void updateQuestion(Questions question);

    // Xóa câu hỏi theo ID
    @Delete("DELETE FROM questions WHERE question_id = #{questionId}")
    void deleteQuestion(@Param("questionId") int questionId);
}
