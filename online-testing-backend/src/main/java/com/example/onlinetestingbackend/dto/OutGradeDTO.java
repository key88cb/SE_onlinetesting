package com.example.onlinetestingbackend.dto;
import lombok.Data;

@Data
public class OutGradeDTO {
    private Integer grade;
    private Float proportion;
    private String type; // 'attending', 'homework', 'test'
    private Integer studentId;
    private Integer secId;
    private String Name;
    private int id;

    public OutGradeDTO() {
    }

    public OutGradeDTO(Integer grade, Float proportion, String type, Integer studentId, Integer secId, String name, int id) {
        this.grade = grade;
        this.proportion = proportion;
        this.type = type;
        this.studentId = studentId;
        this.secId = secId;
        Name = name;
        this.id = id;
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

    public Integer getSecId() {
        return secId;
    }

    public void setSecId(Integer secId) {
        this.secId = secId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}