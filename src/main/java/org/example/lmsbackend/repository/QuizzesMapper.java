package org.example.lmsbackend.repository;

import org.apache.ibatis.annotations.*;
import org.example.lmsbackend.model.Quizzes;

import java.util.List;

@Mapper
public interface QuizzesMapper {

    @Insert("""
        INSERT INTO quizzes (content_id, title, description, time_limit, total_points)
        VALUES (#{contentId}, #{title}, #{description}, #{timeLimit}, #{totalPoints})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "quizId")
    void insertQuiz(Quizzes quiz);

    @Select("""
        SELECT 
            quiz_id AS quizId,
            content_id AS contentId,
            title,
            description,
            time_limit AS timeLimit,
            total_points AS totalPoints
        FROM quizzes
        WHERE quiz_id = #{quizId}
    """)
    Quizzes getQuizById(int quizId);

    @Select("""
        SELECT 
            quiz_id AS quizId,
            content_id AS contentId,
            title,
            description,
            time_limit AS timeLimit,
            total_points AS totalPoints
        FROM quizzes
    """)
    List<Quizzes> getAllQuizzes();

    @Update("""
        UPDATE quizzes
        SET 
            title = #{title}, 
            description = #{description}, 
            time_limit = #{timeLimit}, 
            total_points = #{totalPoints}
        WHERE quiz_id = #{quizId}
    """)
    void updateQuiz(Quizzes quiz);

    @Delete("DELETE FROM quizzes WHERE quiz_id = #{quizId}")
    void deleteQuiz(int quizId);
    @Select("SELECT * FROM quizzes WHERE quiz_id = #{quizId}")
    Quizzes findById(Integer quizId);
    Quizzes findById(@Param("quizId") int quizId);
    @Select("SELECT content_id FROM quizzes WHERE quiz_id = #{quizId}")
    Integer getContentIdByQuizId(@Param("quizId") Integer quizId);

}
