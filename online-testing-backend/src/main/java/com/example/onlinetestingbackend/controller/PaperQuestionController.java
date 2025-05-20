package com.example.onlinetestingbackend.controller;

import com.example.onlinetestingbackend.dto.ManualPaperCreationRequestDto;
import com.example.onlinetestingbackend.service.PaperQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/paper-questions")
public class PaperQuestionController {

    @Autowired
    private PaperQuestionService paperQuestionService;

    @PostMapping("/manual-create-paper")
    public ResponseEntity<String> createManualPaper(@RequestBody ManualPaperCreationRequestDto request) {
        paperQuestionService.createManualPaper(request);
        return ResponseEntity.ok("Manual paper created successfully");
    }
}