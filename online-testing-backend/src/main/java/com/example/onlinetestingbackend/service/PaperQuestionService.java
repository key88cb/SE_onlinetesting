package com.example.onlinetestingbackend.service;

import com.example.onlinetestingbackend.dto.ManualPaperCreationRequestDto;
import com.example.onlinetestingbackend.dto.ManualPaperQuestionDto;
import com.example.onlinetestingbackend.entity.PaperInfo;
import com.example.onlinetestingbackend.entity.PaperQuestion;
import com.example.onlinetestingbackend.entity.Question;
import com.example.onlinetestingbackend.repository.PaperInfoRepository;
import com.example.onlinetestingbackend.repository.PaperQuestionRepository;
import com.example.onlinetestingbackend.repository.QuestionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaperQuestionService {

    @Autowired
    private PaperInfoRepository paperInfoRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private PaperQuestionRepository paperQuestionRepository;

    @Transactional
    public void createManualPaper(ManualPaperCreationRequestDto request) {
        // Step 1: 创建 PaperInfo
        PaperInfo paperInfo = new PaperInfo();
        paperInfo.setCourseId(request.getCourseId());
        paperInfo.setCreator(request.getCreator());
        paperInfo.setSingleChoiceNum(request.getSingleChoiceNum());
        paperInfo.setMultipleChoiceNum(request.getMultipleChoiceNum());
        paperInfo.setTrueFalseNum(request.getTrueFalseNum());
        paperInfo.setOpenTime(request.getOpenTime());
        paperInfo.setCloseTime(request.getCloseTime());
        paperInfo.setTotalScores(request.getTotalScores());
        paperInfo.setHighestScoresForSingleChoice(request.getHighestScoresForSingleChoice());
        paperInfo.setHighestScoresForMultipleChoice(request.getHighestScoresForMultipleChoice());
        paperInfo.setHighestScoresForTrueFalse(request.getHighestScoresForTrueFalse());

        PaperInfo savedPaperInfo = paperInfoRepository.save(paperInfo);

        // Step 2: 处理题目列表
        List<PaperQuestion> paperQuestions = new ArrayList<>();
        for (ManualPaperQuestionDto dto : request.getQuestions()) {
            Question question = questionRepository.findById(dto.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("Question not found"));

            PaperQuestion paperQuestion = new PaperQuestion();
            paperQuestion.setPaperId(savedPaperInfo.getPaperId());//?
            paperQuestion.setCourseId(savedPaperInfo.getCourseId());
            paperQuestion.setQuestionId(question.getQuestionId());
            paperQuestion.setPoints(dto.getPoints());
            paperQuestion.setKnowledgePoints(question.getTags());
            paperQuestion.setQuestionText(question.getQuestionText());
            paperQuestion.setCorrectAnswer(question.getCorrectAnswer());

            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                paperQuestion.setOptionA(question.getOptions().get(0).getOptionText());
                if (question.getOptions().size() > 1) {
                    paperQuestion.setOptionB(question.getOptions().get(1).getOptionText());
                }
                if (question.getOptions().size() > 2) {
                    paperQuestion.setOptionC(question.getOptions().get(2).getOptionText());
                }
                if (question.getOptions().size() > 3) {
                    paperQuestion.setOptionD(question.getOptions().get(3).getOptionText());
                }
            }

            paperQuestion.setPaperInfo(savedPaperInfo); // 关联 PaperInfo
            paperQuestions.add(paperQuestion);
        }

        // Step 3: 批量保存 PaperQuestion
        paperQuestionRepository.saveAll(paperQuestions);
    }
}
