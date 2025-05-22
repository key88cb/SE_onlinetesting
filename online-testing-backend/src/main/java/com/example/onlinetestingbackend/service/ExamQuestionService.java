package com.example.onlinetestingbackend.service;

import com.example.onlinetestingbackend.dto.ExamPlainRecordDto;
import com.example.onlinetestingbackend.dto.PlainAnswerDto;
import com.example.onlinetestingbackend.entity.DetailedResult;
import com.example.onlinetestingbackend.entity.ExamResult;
import com.example.onlinetestingbackend.entity.PaperQuestion;
import com.example.onlinetestingbackend.entity.Question;
import com.example.onlinetestingbackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        int paperId = examResult.getPaperId();
        int courseId = examResult.getCourseId();
        int studentId = examResult.getStudentId();
        int totalscore=0;
        List<PlainAnswerDto> plainAnswerDtoList=examResult.getAnswers();
        for(PlainAnswerDto plainAnswerDto:plainAnswerDtoList){
            int questionId=plainAnswerDto.getQuestionId();
            PaperQuestion question=paperQuestionRepository.findByPaperIdAndCourseIdAndQuestionId(paperId,courseId,questionId);
            String answerbystudent=plainAnswerDto.getAnswer();
            String answer=question.getCorrectAnswer();
            String type=question.getQuestionType();
            int score=question.getPoints();
            DetailedResult detailedResult=new DetailedResult();
            detailedResult.setPaperId(paperId);
            detailedResult.setCourseId(courseId);
            detailedResult.setStudentId(studentId);
            detailedResult.setStudentAnswer(answerbystudent);
            detailedResult.setQuestionId(questionId);
            detailedResult.setCorrectAnswer(answer);
            if(type.equals("Multiple Choice")){
                if(answerbystudent.equals(answer)){
                    detailedResult.setPoints(score);
                }
                else{
                    Set<Character> characterSet = toUniqueCharacterSet(answer);
                    Set<Character> set = toUniqueCharacterSet(answerbystudent);
                    if(characterSet.containsAll(set)){
                        detailedResult.setPoints((score+1)/2);
                    }
                }
            }
            else{
                if(answerbystudent.equals(answer)){
                    detailedResult.setPoints(score);
                }
                else{
                    detailedResult.setPoints(0);
                }
            }
            totalscore+=detailedResult.getPoints();
            detailedResultRepository.save(detailedResult);
        }
        ExamResult examResult1=new ExamResult();
        examResult1.setPaperId(paperId);
        examResult1.setCourseId(courseId);
        examResult1.setStudentId(studentId);
        examResult1.setTotalScore(totalscore);
        examResultRepository.save(examResult1);
    }
}
