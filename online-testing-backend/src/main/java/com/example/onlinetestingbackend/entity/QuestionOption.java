package com.example.onlinetestingbackend.entity;

import com.example.onlinetestingbackend.entity.id.QuestionOptionId;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Options_table")
@IdClass(QuestionOptionId.class)
public class QuestionOption {

    @Id
    @Column(name = "question_id")
    private Integer questionId;

    @Id
    @Column(name = "option_id")
    private Integer optionId;

    @Column(name = "option_text", nullable = false, length = 256)
    private String optionText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", referencedColumnName = "question_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_options_question_id",
                    foreignKeyDefinition = "FOREIGN KEY (question_id) REFERENCES Question_table(question_id) ON DELETE CASCADE ON UPDATE CASCADE"))
    private Question question;

    // Constructors, Getters, Setters, equals, hashCode (similar to previous version)
    public QuestionOption() {}
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public Integer getOptionId() { return optionId; }
    public void setOptionId(Integer optionId) { this.optionId = optionId; }
    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; if (question != null) this.questionId = question.getQuestionId(); }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; QuestionOption that = (QuestionOption) o; return Objects.equals(questionId, that.questionId) && Objects.equals(optionId, that.optionId); }
    @Override public int hashCode() { return Objects.hash(questionId, optionId); }
}