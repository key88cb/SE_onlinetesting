package com.example.onlinetestingbackend.controller;

import com.example.onlinetestingbackend.dto.*;
import com.example.onlinetestingbackend.entity.PaperInfo;
import com.example.onlinetestingbackend.service.PaperQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/query-papers")
    public ResponseEntity<List<PaperInfoDto>> queryPapers(@RequestBody QueryPaperRequestDto request) {
        List<PaperInfo> papers = paperQuestionService.findByCourseIdAndCreator(request.getCourseId(), request.getCreator());
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

    @PostMapping("/query-paper-and-questions")
    public ResponseEntity<PaperInfoDto> queryPaperAndQuestions(@RequestBody QueryPaperQuestionRequestDto request) {
        PaperInfoDto paperAndQuestions = paperQuestionService.findByCourseIdAndPaperId(request.getCourseId(), request.getPaperId());
        return ResponseEntity.ok(paperAndQuestions);
    }

    @DeleteMapping("/delete-paper")
    public ResponseEntity<String> deletePaper(@RequestBody DeletePaperRequestDto request) {
        paperQuestionService.deletePaperById(request.getPaperId(), request.getCourseId());
        return ResponseEntity.ok("Paper and its questions deleted successfully");
    }
}