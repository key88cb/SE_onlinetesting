package com.example.onlinetestingbackend.dto;

import java.util.ArrayList;
import java.util.List;

public class ExamPlainRecordtempDto {
    private Integer paperId;
    private Integer courseId;
    private Integer studentId;
    private List<PlainAnswerDto> answers=new ArrayList<PlainAnswerDto>();

    public ExamPlainRecordtempDto() {
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getPaperId() {
        return paperId;
    }

    public void setPaperId(Integer paperId) {
        this.paperId = paperId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public List<PlainAnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<PlainAnswerDto> answers) {
        this.answers = answers;
    }


}

