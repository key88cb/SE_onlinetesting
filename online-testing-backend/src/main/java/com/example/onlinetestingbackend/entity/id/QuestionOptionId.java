package com.example.onlinetestingbackend.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class QuestionOptionId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "question_id") // Length from Question.questionId
    private Integer questionId;

    @Column(name = "option_id") // DDL is int(8)
    private Integer optionId;

    public QuestionOptionId() {}
    public QuestionOptionId(Integer questionId, Integer optionId) { this.questionId = questionId; this.optionId = optionId; }
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public Integer getOptionId() { return optionId; }
    public void setOptionId(Integer optionId) { this.optionId = optionId; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; QuestionOptionId that = (QuestionOptionId) o; return Objects.equals(questionId, that.questionId) && Objects.equals(optionId, that.optionId); }
    @Override public int hashCode() { return Objects.hash(questionId, optionId); }
}