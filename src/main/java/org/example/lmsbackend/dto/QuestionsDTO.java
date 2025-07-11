package org.example.lmsbackend.dto;

public class QuestionsDTO {

    private Integer quizId;
    private String questionText;
    private String type; // "multiple_choice", "true_false", "short_answer"
    private Integer points;

    public QuestionsDTO() {
    }

    public QuestionsDTO(Integer quizId, String questionText, String type, Integer points) {
        this.quizId = quizId;
        this.questionText = questionText;
        this.type = type;
        this.points = points;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public void setQuizId(Integer quizId) {
        this.quizId = quizId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
