package com.example.onlinetestingbackend.service;

import ch.qos.logback.classic.Logger;
import com.example.onlinetestingbackend.dto.ExamPlainRecordDto;
import com.example.onlinetestingbackend.dto.PlainAnswerDto;
import com.example.onlinetestingbackend.dto.ScoreEditDto;
import com.example.onlinetestingbackend.entity.DetailedResult;
import com.example.onlinetestingbackend.entity.ExamResult;
import com.example.onlinetestingbackend.entity.PaperQuestion;
import com.example.onlinetestingbackend.entity.Question;
import com.example.onlinetestingbackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ExamQuestionService {

    @Autowired
    private ExamResultRepository examResultRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private PaperInfoRepository paperInfoRepository;
    @Autowired
    private PaperQuestionRepository paperQuestionRepository;
    @Autowired
    private DetailedResultRepository detailedResultRepository;
    private static Set<Character> toUniqueCharacterSet(String str) {
        Set<Character> set = new HashSet<>();
        for (char c : str.toCharArray()) {
            set.add(c);
        }
        return set;
    }
    @Transactional
    public void judgeResult(ExamPlainRecordDto examResult) {
        try {
            int paperId = examResult.getPaperId();
            int courseId = examResult.getCourseId();
            int studentId = examResult.getStudentId();
            int totalscore = 0;
            List<PlainAnswerDto> plainAnswerDtoList = examResult.getAnswers();
            List<DetailedResult> DetailResults = new ArrayList<>();
            for (PlainAnswerDto plainAnswerDto : plainAnswerDtoList) {
                int questionId = plainAnswerDto.getQuestionId();
                PaperQuestion question = paperQuestionRepository.findByPaperIdAndCourseIdAndQuestionId(paperId, courseId, questionId);
                String answerbystudent = plainAnswerDto.getAnswer();
                String answer = question.getCorrectAnswer();
                String type = question.getQuestionType();
                int score = question.getPoints();
                DetailedResult detailedResult = new DetailedResult();
                detailedResult.setPaperId(paperId);
                detailedResult.setCourseId(courseId);
                detailedResult.setStudentId(studentId);
                detailedResult.setStudentAnswer(answerbystudent);
                detailedResult.setQuestionId(questionId);
                detailedResult.setCorrectAnswer(answer);
                if (type.equals("Multiple Choice")) {
                    if (answerbystudent.equals(answer)) {
                        detailedResult.setPoints(score);
                    } else {
                        Set<Character> characterSet = toUniqueCharacterSet(answer);
                        Set<Character> set = toUniqueCharacterSet(answerbystudent);
                        if (characterSet.containsAll(set)) {
                            detailedResult.setPoints((score + 1) / 2);
                        } else {
                            detailedResult.setPoints(0);
                        }
                    }
                } else {
                    if (answerbystudent.equals(answer)) {
                        detailedResult.setPoints(score);
                    } else {
                        detailedResult.setPoints(0);
                    }
                }
                totalscore += detailedResult.getPoints();
                DetailResults.add(detailedResult);
//                detailedResultRepository.save(detailedResult);
//                DetailedResult temp = detailedResultRepository.findByPaperIdAndCourseIdAndStudentIdAndQuestionId(paperId, courseId, studentId, questionId);
            }
            detailedResultRepository.saveAll(DetailResults);
            ExamResult examResult1 = new ExamResult();
            examResult1.setPaperId(paperId);
            examResult1.setCourseId(courseId);
            examResult1.setStudentId(studentId);
            examResult1.setTotalScore(totalscore);
            examResultRepository.save(examResult1);
            Optional<ExamResult> temp = examResultRepository.findByPaperIdAndCourseIdAndStudentId(paperId,courseId,studentId)
                    .stream().findFirst();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw new RuntimeException("评分失败", e); // 重新抛出异常，确保事务回滚

        }
    }
    @Transactional
    public void editResultforonestudent(ScoreEditDto scoreEditDto) {
        int paperId = scoreEditDto.getPaperId();
        int courseId = scoreEditDto.getCourseId();
        int studentId = scoreEditDto.getStudentId();
        int questionId = scoreEditDto.getQuestionId();
        int score = scoreEditDto.getScore();
        int totalscore = 0;
        DetailedResult detailedResult =detailedResultRepository.findByPaperIdAndCourseIdAndStudentIdAndQuestionId(paperId,courseId,studentId,questionId);
        Optional<ExamResult> examResult=examResultRepository.findByPaperIdAndCourseIdAndStudentId(paperId,courseId,studentId);
        if (!examResult.isPresent()) {
            System.err.println("未查到对应学生");
            return;
        }
        int totalscoretemp=examResult.get().getTotalScore();
        int score2=scoreEditDto.getScore();
        int scoretemp=score-score2;
        totalscore=totalscoretemp+scoretemp;
        ExamResult examResult1=new ExamResult();
        examResult1.setPaperId(paperId);
        examResult1.setCourseId(courseId);
        examResult1.setStudentId(studentId);
        examResult1.setTotalScore(totalscore);
        examResultRepository.save(examResult1);
    }
    @Transactional
    public void editResultformanystudent(List<ScoreEditDto> scoreEditDtos) {
        for (ScoreEditDto scoreEditDto : scoreEditDtos) {
        int paperId = scoreEditDto.getPaperId();
        int courseId = scoreEditDto.getCourseId();
        int studentId = scoreEditDto.getStudentId();
        int questionId = scoreEditDto.getQuestionId();
        int score = scoreEditDto.getScore();
        int totalscore = 0;
        DetailedResult detailedResult = detailedResultRepository.findByPaperIdAndCourseIdAndStudentIdAndQuestionId(paperId, courseId, studentId, questionId);
        Optional<ExamResult> examResult = examResultRepository.findByPaperIdAndCourseIdAndStudentId(paperId, courseId, studentId);
        if (!examResult.isPresent()) {
            System.err.println("未查到对应学生");
            return;
        }
        int totalscoretemp = examResult.get().getTotalScore();
        int score2 = detailedResult.getPoints();
        int scoretemp = score - score2;
        totalscore = totalscoretemp + scoretemp;
        ExamResult examResult1 = new ExamResult();
        examResult1.setPaperId(paperId);
        examResult1.setCourseId(courseId);
        examResult1.setStudentId(studentId);
        examResult1.setTotalScore(totalscore);
        ExamResult temp=examResultRepository.save(examResult1);
        System.out.println("<UNK>");
        }
    }

}
