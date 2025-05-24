package com.example.onlinetestingbackend.controller;

import com.example.onlinetestingbackend.dto.ExamPlainRecordDto;
import com.example.onlinetestingbackend.entity.ExamResult;
import com.example.onlinetestingbackend.repository.ExamResultRepository;
import com.example.onlinetestingbackend.repository.PaperInfoRepository;
import com.example.onlinetestingbackend.repository.PaperQuestionRepository;
import com.example.onlinetestingbackend.repository.QuestionRepository;
import com.example.onlinetestingbackend.service.ExamQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exam")
public class ExamResultController {
    @Autowired
    private ExamQuestionService examQuestionService;
    @PostMapping("/api/exam/submit-exam")
    public ResponseEntity<String> submitExam(@RequestBody ExamPlainRecordDto examResult) {
        examQuestionService.judgeResult(examResult);
        return ResponseEntity.ok("Exam Submitted");
    }
}
