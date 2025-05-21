package com.example.onlinetestingbackend.service;

import com.example.onlinetestingbackend.dto.AutoPaperCreationRequestDto;
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
import java.util.concurrent.ThreadLocalRandom;

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
        // 1.1. 生成唯一的paperId
        Integer paperId = generateUniquePaperId();
        PaperInfo paperInfo = new PaperInfo();
        paperInfo.setPaperId(paperId);
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

        int singleChoiceNum = 0;
        int multipleChoiceNum = 0;
        int trueFalseNum = 0;
        int total_point = 0;

        PaperInfo savedPaperInfo = paperInfoRepository.save(paperInfo);

        // Step 2: 处理题目列表
        List<PaperQuestion> paperQuestions = new ArrayList<>();
        for (ManualPaperQuestionDto dto : request.getQuestions()) {
            Question question = questionRepository.findById(dto.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("Question not found"));

            // 统计题型数量
            String type = question.getQuestionType();
            if ("Single Choice".equalsIgnoreCase(type)) {
                singleChoiceNum++;
            } else if ("Multiple Choice".equalsIgnoreCase(type)) {
                multipleChoiceNum++;
            } else if ("True/False".equalsIgnoreCase(type)) {
                trueFalseNum++;
            }
            total_point += dto.getPoints();

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

        savedPaperInfo.setSingleChoiceNum(singleChoiceNum);
        savedPaperInfo.setMultipleChoiceNum(multipleChoiceNum);
        savedPaperInfo.setTrueFalseNum(trueFalseNum);
        savedPaperInfo.setTotalScores(total_point);

        // Step 3: 批量保存 PaperQuestion
        paperInfoRepository.save(savedPaperInfo);
        paperQuestionRepository.saveAll(paperQuestions);
    }

    @Transactional
    public void createAutoPaper(AutoPaperCreationRequestDto request) {
        // Step 1: 创建 PaperInfo
        Integer paperId = generateUniquePaperId();
        PaperInfo paperInfo = new PaperInfo();
        paperInfo.setPaperId(paperId);
        paperInfo.setCourseId(request.getCourseId());
        paperInfo.setCreator(request.getCreator());
        paperInfo.setOpenTime(request.getOpenTime());
        paperInfo.setCloseTime(request.getCloseTime());


        int totalScores = 0;
        int singleChoiceNum = 0;
        int multipleChoiceNum = 0;
        int trueFalseNum = 0;

        List<PaperQuestion> paperQuestions = new ArrayList<>();

        // Step 2: 根据每个题型配置获取题目
        for (AutoPaperCreationRequestDto.QuestionTypeConfig config : request.getQuestionTypeConfigs()) {
            String type = config.getType();
            Integer num = config.getNumberOfQuestions();
            Integer points = config.getPointsPerQuestion();
            List<String> tags = config.getTags();

            // 查询符合条件的题目（可根据 tag 和题型过滤）
            List<Question> questions = questionRepository.findByQuestionTypeAndTags(type, tags);

            if (questions.size() < num) {
                throw new IllegalArgumentException("Not enough questions for type: " + type);
            }

            // 随机抽取指定数量的题目
            List<Question> selectedQuestions = questions.stream()
                    .sorted((q1, q2) -> ThreadLocalRandom.current().nextInt(-1, 2))
                    .limit(num)
                    .toList();

            // 统计题型数量
            if ("Single Choice".equalsIgnoreCase(type)) {
                singleChoiceNum += selectedQuestions.size();
                paperInfo.setHighestScoresForSingleChoice(points);
            } else if ("Multiple Choice".equalsIgnoreCase(type)) {
                multipleChoiceNum += selectedQuestions.size();
                paperInfo.setHighestScoresForMultipleChoice(points);
            } else if ("True/False".equalsIgnoreCase(type)) {
                trueFalseNum += selectedQuestions.size();
                paperInfo.setHighestScoresForTrueFalse(points);
            }

            // 构建 PaperQuestion 并计算总分
            for (Question question : selectedQuestions) {
                PaperQuestion pq = new PaperQuestion();
                pq.setPaperId(paperId);
                pq.setCourseId(request.getCourseId());
                pq.setQuestionId(question.getQuestionId());
                pq.setPoints(points);
                pq.setKnowledgePoints(String.join(",", question.getTags()));
                pq.setQuestionText(question.getQuestionText());
                pq.setCorrectAnswer(question.getCorrectAnswer());

                if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                    pq.setOptionA(question.getOptions().get(0).getOptionText());
                    if (question.getOptions().size() > 1) {
                        pq.setOptionB(question.getOptions().get(1).getOptionText());
                    }
                    if (question.getOptions().size() > 2) {
                        pq.setOptionC(question.getOptions().get(2).getOptionText());
                    }
                    if (question.getOptions().size() > 3) {
                        pq.setOptionD(question.getOptions().get(3).getOptionText());
                    }
                }

                pq.setPaperInfo(paperInfo); // 关联 PaperInfo
                paperQuestions.add(pq);
                totalScores += points;
            }
        }

        // Step 3: 设置统计信息并保存 PaperInfo
        paperInfo.setSingleChoiceNum(singleChoiceNum);
        paperInfo.setMultipleChoiceNum(multipleChoiceNum);
        paperInfo.setTrueFalseNum(trueFalseNum);
        paperInfo.setTotalScores(totalScores);

        PaperInfo savedPaperInfo = paperInfoRepository.save(paperInfo);

        // Step 4: 批量保存 PaperQuestion
        paperQuestionRepository.saveAll(paperQuestions);
    }
    /**
     * 生成唯一的paperId
     */
    private Integer generateUniquePaperId() {
        Integer paperId;
        paperId = 100000 + ThreadLocalRandom.current().nextInt(900000);
        return paperId;
    }
}