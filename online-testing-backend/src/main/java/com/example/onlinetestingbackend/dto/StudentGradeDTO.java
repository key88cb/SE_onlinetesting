package com.example.onlinetestingbackend.dto;

import lombok.Data;

@Data
public class StudentGradeDTO {
    private Integer courseId;
    private String courseName;
    private Integer score;
    private Integer credits;
    private Double gpa;
    private String semester;

    public StudentGradeDTO() {
    }

    public StudentGradeDTO(Integer courseId, String courseName, Integer score, Integer credits, Double gpa, String semester) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.score = score;
        this.credits = credits;
        this.gpa = gpa;
        this.semester = semester;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Double getGpa() {
        return gpa;
    }

    public void setGpa(Double gpa) {
        this.gpa = gpa;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }
}
