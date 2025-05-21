package com.example.onlinetestingbackend.controller;

import com.example.onlinetestingbackend.dto.AutoPaperCreationRequestDto;
import com.example.onlinetestingbackend.dto.ManualPaperCreationRequestDto;
import com.example.onlinetestingbackend.dto.UpdatePaperTimeRequestDto;
import com.example.onlinetestingbackend.service.PaperQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

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

    @PostMapping("/auto-create-paper")
    public ResponseEntity<String> createAutoPaper(@RequestBody AutoPaperCreationRequestDto request) {
        paperQuestionService.createAutoPaper(request);
        return ResponseEntity.ok("Auto paper created successfully");
    }

    @PostMapping("/update-paper-time")
    public ResponseEntity<String> updatePaperTime(@RequestBody UpdatePaperTimeRequestDto request) {
        paperQuestionService.updatePaperTime(
                request.getPaperId(),
                request.getCourseId(),
                request.getOpenTime(),
                request.getCloseTime()
        );
        return ResponseEntity.ok("Paper time updated successfully");
    }
}