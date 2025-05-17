package com.example.onlinetestingbackend.entity;

import com.example.onlinetestingbackend.entity.id.PaperInfoId;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Paper_info")
@IdClass(PaperInfoId.class)
public class PaperInfo {

    @Id
    @Column(name = "paper_id")
    // IMPORTANT LIMITATION: Hibernate's ddl-auto=create will NOT generate
    // AUTO_INCREMENT for 'paper_id' here because it's part of a composite key.
    // @GeneratedValue is for single-column IDs.
    // The DDL will create 'paper_id INT NOT NULL'.
    // To get the exact AUTO_INCREMENT behavior from your DDL,
    // you'd need to apply that part of your SQL script manually after table creation by Hibernate,
    // or manage this table's DDL entirely with your script.
    private Integer paperId;

    @Id
    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "creator", nullable = false, length = 20)
    private String creator;

    @Column(name = "single_choice_num", nullable = false) // int(3) display width not specified
    private Integer singleChoiceNum;

    @Column(name = "multiple_choice_num", nullable = false, length = 20) // As per DDL: varchar(20)
    private String multipleChoiceNum;

    @Column(name = "true_false_num", nullable = false, length = 20) // As per DDL: varchar(20)
    private String trueFalseNum;

    @Column(name = "open_time", columnDefinition = "datetime(0)")
    private LocalDateTime openTime;

    @Column(name = "close_time", columnDefinition = "datetime(0)")
    private LocalDateTime closeTime;

    @Column(name = "highest_scores_for_single_choice", nullable = false)
    private Integer highestScoresForSingleChoice;

    @Column(name = "highest_scores_for_multiple_choice", nullable = false)
    private Integer highestScoresForMultipleChoice;

    @Column(name = "highest_scores_for_true_false", nullable = false)
    private Integer highestScoresForTrueFalse;

    @Column(name = "total_scores", nullable = false)
    private Integer totalScores;

    @OneToMany(mappedBy = "paperInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Paper_questions> paperQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "paperInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ExamResult> examResults = new ArrayList<>();

    @OneToMany(mappedBy = "paperInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetailedResult> detailedResults = new ArrayList<>();

    // Constructors, Getters, Setters, equals, hashCode (similar to previous version)
    public PaperInfo() {}
    public Integer getPaperId() { return paperId; }
    public void setPaperId(Integer paperId) { this.paperId = paperId; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public Integer getSingleChoiceNum() { return singleChoiceNum; }
    public void setSingleChoiceNum(Integer singleChoiceNum) { this.singleChoiceNum = singleChoiceNum; }
    public String getMultipleChoiceNum() { return multipleChoiceNum; }
    public void setMultipleChoiceNum(String multipleChoiceNum) { this.multipleChoiceNum = multipleChoiceNum; }
    public String getTrueFalseNum() { return trueFalseNum; }
    public void setTrueFalseNum(String trueFalseNum) { this.trueFalseNum = trueFalseNum; }
    public LocalDateTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalDateTime openTime) { this.openTime = openTime; }
    public LocalDateTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalDateTime closeTime) { this.closeTime = closeTime; }
    public Integer getHighestScoresForSingleChoice() { return highestScoresForSingleChoice; }
    public void setHighestScoresForSingleChoice(Integer h) { this.highestScoresForSingleChoice = h; }
    public Integer getHighestScoresForMultipleChoice() { return highestScoresForMultipleChoice; }
    public void setHighestScoresForMultipleChoice(Integer h) { this.highestScoresForMultipleChoice = h; }
    public Integer getHighestScoresForTrueFalse() { return highestScoresForTrueFalse; }
    public void setHighestScoresForTrueFalse(Integer h) { this.highestScoresForTrueFalse = h; }
    public Integer getTotalScores() { return totalScores; }
    public void setTotalScores(Integer totalScores) { this.totalScores = totalScores; }
    public List<Paper_questions> getPaperQuestions() { return paperQuestions; }
    public void setPaperQuestions(List<Paper_questions> paperQuestions) { this.paperQuestions = paperQuestions; }
    public List<ExamResult> getExamResults() { return examResults; }
    public void setExamResults(List<ExamResult> examResults) { this.examResults = examResults; }
    public List<DetailedResult> getDetailedResults() { return detailedResults; }
    public void setDetailedResults(List<DetailedResult> detailedResults) { this.detailedResults = detailedResults; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; PaperInfo pi = (PaperInfo) o; return Objects.equals(paperId, pi.paperId) && Objects.equals(courseId, pi.courseId); }
    @Override public int hashCode() { return Objects.hash(paperId, courseId); }
}