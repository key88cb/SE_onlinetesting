package com.example.onlinetestingbackend.controller;

import com.example.onlinetestingbackend.dto.*;
import com.example.onlinetestingbackend.entity.PaperInfo;
import com.example.onlinetestingbackend.exception.ResourceNotFoundException;
import com.example.onlinetestingbackend.service.PaperQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/paper-questions")
public class PaperQuestionController {

    @Autowired
    private PaperQuestionService paperQuestionService;

    @PostMapping("/manual-create-paper")
    public ResponseEntity<String> createManualPaper(@RequestBody ManualPaperCreationRequestDto request) {
        System.out.println("Received request: " + request);
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
                request.getCloseTime(),
                request.getPaperName()
        );
        return ResponseEntity.ok("Paper time updated successfully");
    }

    @GetMapping("/query-papers")
    public ResponseEntity<List<PaperInfoDto>> queryPapers(@RequestParam Integer courseId, @RequestParam String creator) {
        System.out.println(creator);
        System.out.println(courseId);
        List<PaperInfo> papers = paperQuestionService.findByCourseIdAndCreator(courseId, creator);
        List<PaperInfoDto> dtos = new ArrayList<>();

        for (PaperInfo paperInfo : papers) {
            PaperInfoDto dto = new PaperInfoDto();
            dto.setPaperId(paperInfo.getPaperId());
            dto.setCourseId(paperInfo.getCourseId());
            dto.setCreator(paperInfo.getCreator());
            dto.setOpenTime(paperInfo.getOpenTime());
            dto.setCloseTime(paperInfo.getCloseTime());
            dto.setTotalScores(paperInfo.getTotalScores());
            dto.setPaperName(paperInfo.getPaperName());
            dtos.add(dto);
        }

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/query-paper-and-questions")
    public ResponseEntity<PaperInfoDto> queryPaperAndQuestions(@RequestParam Integer courseId,@RequestParam Integer paperId) {
        PaperInfoDto paperAndQuestions = paperQuestionService.findByCourseIdAndPaperId(courseId,paperId);
        return ResponseEntity.ok(paperAndQuestions);
    }

    @DeleteMapping("/delete-paper")
    public ResponseEntity<String> deletePaper(@RequestBody DeletePaperRequestDto request) {
        paperQuestionService.deletePaperById(request.getPaperId(), request.getCourseId());
        return ResponseEntity.ok("Paper and its questions deleted successfully");
    }
    @ControllerAdvice
     public class GlobalExceptionHandler {
         @ExceptionHandler(ResourceNotFoundException.class)
         public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
             return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
         }
     }
}