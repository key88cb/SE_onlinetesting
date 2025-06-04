package com.example.onlinetestingbackend.service;


import com.example.onlinetestingbackend.dto.GradeDTO;
import com.example.onlinetestingbackend.dto.GradeStatusDTO;
import com.example.onlinetestingbackend.dto.OutGradeDTO;
import com.example.onlinetestingbackend.dto.StudentGradeDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GradeService {
    List<GradeDTO> getAllGrades(Integer studentId);

    List<StudentGradeDTO> getAllStudentGrades(Integer studentId);

    List<GradeStatusDTO> getAllStudentGradesBySection(Integer teacherId);

    void addGrade(List<OutGradeDTO> outGrade);
}