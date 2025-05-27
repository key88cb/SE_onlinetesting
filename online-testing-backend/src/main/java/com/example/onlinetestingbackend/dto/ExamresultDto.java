package com.example.onlinetestingbackend.dto;

import java.util.List;

public class ExamresultDto {
    private Integer studentId;
    private Integer totalScore;
    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

}
