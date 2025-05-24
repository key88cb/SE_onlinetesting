package com.example.onlinetestingbackend.controller;

import com.example.onlinetestingbackend.dto.ExamPlainRecordDto;
import com.example.onlinetestingbackend.service.ExamQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exam")
public class ExamSubmissionController {

    @Autowired
    private ExamQuestionService examQuestionService;

    @PostMapping("/submit")
    public ResponseEntity<String> manualSubmit(@RequestBody ExamPlainRecordDto dto) {
        try {
            examQuestionService.submitExamManually(dto);
            return ResponseEntity.ok("试卷已成功提交并完成评分");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("提交失败: " + e.getMessage());
        }
    }

    @GetMapping("/auto-submit")
    public ResponseEntity<String> autoSubmit() {
        try {
            examQuestionService.autoSubmitExams();
            return ResponseEntity.ok("自动交卷已完成");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("自动交卷失败: " + e.getMessage());
        }
    }
}
