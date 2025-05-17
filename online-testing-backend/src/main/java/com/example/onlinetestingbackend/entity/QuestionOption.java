package com.example.onlinetestingbackend.entity;

import com.example.onlinetestingbackend.entity.id.QuestionOptionId;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Options_table")
@IdClass(QuestionOptionId.class)
public class QuestionOption {
    @Id // 这个 optionId 是 QuestionOption 自身的标识部分 (在给定 Question 内唯一)
    @Column(name = "option_id")
    private Integer optionId;

    // questionId 字段将从 'question' 关联中获取其值，并作为复合主键的一部分。
    // 它本身不再需要 @Column(name="question_id")，因为 @MapsId 和 @JoinColumn 会处理。
    @Id
    private Integer questionId;

    @Column(name = "option_text", nullable = false, length = 256)
    private String optionText;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId") // 告诉 JPA：将 QuestionOptionId 中的 'questionId' 字段的值
    // 映射自这个 'question' 关联的 ID。
    @JoinColumn(name = "question_id", referencedColumnName = "question_id",
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
    public void setQuestion(Question question) { this.question = question;  }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; QuestionOption that = (QuestionOption) o; return Objects.equals(questionId, that.questionId) && Objects.equals(optionId, that.optionId); }
    @Override public int hashCode() { return Objects.hash(questionId, optionId); }
}