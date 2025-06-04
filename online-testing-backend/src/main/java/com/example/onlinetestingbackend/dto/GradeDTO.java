package com.example.onlinetestingbackend.dto;

import lombok.Data;

@Data
public class GradeDTO {
    private Integer gradeId;
    private Integer takesId;
    private Integer grade;
    private Float proportion;
    private String type; // 'attending', 'homework', 'test'
    private Integer studentId;
    private String studentName;
    private Integer courseId;
    private String courseName;

    public GradeDTO() {}

    public GradeDTO(Integer gradeId, Integer takesId, Integer grade, Float proportion, String type, Integer studentId, String studentName, Integer courseId, String courseName) {
        this.gradeId = gradeId;
        this.takesId = takesId;
        this.grade = grade;
        this.proportion = proportion;
        this.type = type;
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseId = courseId;
        this.courseName = courseName;
    }

    public Integer getGradeId() {
        return gradeId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }

    public Integer getTakesId() {
        return takesId;
    }

    public void setTakesId(Integer takesId) {
        this.takesId = takesId;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Float getProportion() {
        return proportion;
    }

    public void setProportion(Float proportion) {
        this.proportion = proportion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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
}
