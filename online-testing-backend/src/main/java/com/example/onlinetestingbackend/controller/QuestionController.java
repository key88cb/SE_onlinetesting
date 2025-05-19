package com.example.onlinetestingbackend.controller;

import com.example.onlinetestingbackend.dto.QuestionDto;
import com.example.onlinetestingbackend.service.QuestionService;
import com.example.onlinetestingbackend.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 需要校验
//import jakarta.validation.Valid;
import java.util.List;
@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    private final QuestionService questionService;
    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }
    @PostMapping
    public ResponseEntity<QuestionDto> createQuestion(/*@Valid*/ @RequestBody QuestionDto questionDto) {
        QuestionDto createdQuestion = questionService.createQuestion(questionDto);
        return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    public ResponseEntity<QuestionDto> getQuestionById(@PathVariable Integer id) {
        // QuestionService 中的 getQuestionById 可能会抛出 ResourceNotFoundException,
        // 如果有全局异常处理器 (@ControllerAdvice) 会处理它并返回 404。
        // 否则，这里也可以 try-catch 或让它直接抛出。
        QuestionDto questionDto = questionService.getQuestionById(id);
        return ResponseEntity.ok(questionDto); // 返回 HTTP 200 OK 和题目数据
    }
    @GetMapping
    public ResponseEntity<List<QuestionDto>> getAllQuestions(
            @RequestParam(required = false) String subjectCategory,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) String tags // 假设按标签模糊搜索
            // 可以根据您的 QuestionRepository 和 QuestionService 添加更多查询参数
    ) {
        List<QuestionDto> questions;
        if (subjectCategory != null) {
            questions = questionService.findBySubjectCategory(subjectCategory);
        } else if (questionType != null) {
            questions = questionService.findByQuestionType(questionType);
        } else if (tags != null) {
            questions = questionService.findByTagsContaining(tags);
        }
        else {
            questions = questionService.getAllQuestions();
        }
        return ResponseEntity.ok(questions);
    }

    // 4. 更新题目 (PUT /api/questions/{id})
    @PutMapping("/{id}")
    public ResponseEntity<QuestionDto> updateQuestion(@PathVariable Integer id, /*@Valid*/ @RequestBody QuestionDto questionDetailsDto) {
        QuestionDto updatedQuestion = questionService.updateQuestion(id, questionDetailsDto);
        return ResponseEntity.ok(updatedQuestion);
    }

    // 5. 删除题目 (DELETE /api/questions/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Integer id) {
        questionService.deleteQuestion(id);
        // 返回 HTTP 204 No Content 表示成功删除且响应体为空
        return ResponseEntity.noContent().build();
    }
     @ControllerAdvice
     public class GlobalExceptionHandler {
         @ExceptionHandler(ResourceNotFoundException.class)
         public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
             return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
         }
         //TODO: 添加其他异常的处理方法
     }
}
