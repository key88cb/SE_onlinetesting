package com.example.onlinetestingbackend.dto;

import lombok.Data;

@Data
public class SendGradeDTO {
    int studentId;
    int score;
    String name;
    float proportion;

    public SendGradeDTO() {}

    public SendGradeDTO(int studentId, int score, String name, float proportion) {
        this.studentId = studentId;
        this.score = score;
        this.name = name;
        this.proportion = proportion;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getProportion() {
        return proportion;
    }

    public void setProportion(float proportion) {
        this.proportion = proportion;
    }
}