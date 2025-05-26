package com.example.onlinetestingbackend.controller;

import com.example.onlinetestingbackend.dto.ExamPlainRecordDto;
import com.example.onlinetestingbackend.dto.ExamresultDto;
import com.example.onlinetestingbackend.dto.RecordDto;
import com.example.onlinetestingbackend.dto.RecordsDto;
import com.example.onlinetestingbackend.service.ExamQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam")
public class ExamResultController {
    @Autowired
    private ExamQuestionService examQuestionService;
    @PostMapping("/submit-exam")
    public ResponseEntity<String> submitExam(@RequestBody ExamPlainRecordDto examResult) {
        examQuestionService.judgeResult(examResult);
        return ResponseEntity.ok("Exam Submitted");
    }
    @GetMapping("/search-exam-for-all")
    public ResponseEntity<RecordsDto> getExamResultsForClass(@RequestParam Integer courseId, @RequestParam Integer paperId) {
        RecordsDto recordDto=examQuestionService.searchbycourseIdandpaperId(courseId,paperId);
        return ResponseEntity.ok(recordDto);
    }
    @GetMapping("search-exam-for-one")
    public ResponseEntity<RecordDto> getExamResults(@RequestParam Integer courseId, @RequestParam Integer paperId,@RequestParam Integer studentId) {
        RecordDto recordDto=examQuestionService.searchbycourseIdandpaperIdandstudentId(courseId,paperId,studentId);
        return ResponseEntity.ok(recordDto);
    }
    @GetMapping("search-exam_result-for-all")
    public  ResponseEntity<List<ExamresultDto>> getAbstractExamResult(@RequestParam Integer paperId, @RequestParam Integer courseId){
        List<ExamresultDto> body=examQuestionService.searchexamresultbybycourseIdandpaperId(courseId,paperId);
        return ResponseEntity.ok(body);
    }
    @GetMapping("search-exam_result-for-one")
    public  ResponseEntity<ExamresultDto> getAbstractExamResultForOne(@RequestParam Integer paperId, @RequestParam Integer courseId,@RequestParam Integer studentId){
        ExamresultDto body=examQuestionService.searchexamresultbybycourseIdandpaperIdandstudentId(courseId,paperId,studentId);
        return ResponseEntity.ok(body);
    }
}
