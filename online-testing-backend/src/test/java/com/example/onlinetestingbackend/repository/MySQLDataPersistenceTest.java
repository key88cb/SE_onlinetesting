package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.*;
import com.example.onlinetestingbackend.entity.id.OptionId;
// 导入其他需要的 ID 类和 Repository
import com.example.onlinetestingbackend.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit; // 导入 @Commit
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList; // 确保导入

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class MySQLDataPersistenceTest {

    @Autowired
    private QuestionRepository questionRepository;
    // @Autowired private OptionRepository optionRepository; // 如果您有 OptionRepository
    @Autowired
    private PaperInfoRepository paperInfoRepository;
    @Autowired
    private PaperQuestionRepository paperQuestionRepository;
    @Autowired
    private ExamResultRepository examResultRepository;
    @Autowired
    private DetailedResultRepository detailedResultRepository;

    @Test
    @Transactional // 将所有数据库操作包含在一个事务中
    @Commit       // 确保事务在测试成功后提交
    @DisplayName("向MySQL数据库填充数据并使其持久化")
    void populateAndPersistDataInMySQL() {
        System.out.println("开始填充数据...");

        // --- 1. 创建题目和选项 ---
        Question question1 = new Question();
        question1.setSubjectCategory("数学");
        question1.setTags("代数,初等数学");
        question1.setQuestionText("解一元一次方程: 3x - 7 = 5");
        question1.setQuestionType("单选题");
        question1.setCorrectAnswer("C");
        question1.setCreator("张老师");
        question1.setOptions(new ArrayList<>()); // 初始化列表

        Option q1OptA = new Option();
        // questionId 将在保存 Question 时通过 @MapsId 和关联自动设置
        q1OptA.setId(new OptionId(null, 1));
        q1OptA.setOptionText("A. x = 2");
        question1.addOption(q1OptA); // addOption 会设置双向关联

        Option q1OptB = new Option();
        q1OptB.setId(new OptionId(null, 2));
        q1OptB.setOptionText("B. x = 3");
        question1.addOption(q1OptB);

        Option q1OptC = new Option();
        q1OptC.setId(new OptionId(null, 3));
        q1OptC.setOptionText("C. x = 4");
        question1.addOption(q1OptC);

        Question savedQuestion1 = questionRepository.saveAndFlush(question1);
        assertThat(savedQuestion1.getQuestionId()).isNotNull();
        System.out.println("已保存题目1 ID: " + savedQuestion1.getQuestionId());


        Question question2 = new Question();
        question2.setSubjectCategory("计算机科学");
        question2.setTags("数据结构,算法");
        question2.setQuestionText("哪种排序算法的平均时间复杂度是 O(n log n)?");
        question2.setQuestionType("多选题"); // 假设是多选题
        question2.setCorrectAnswer("A,C"); // 多选题答案示例
        question2.setCreator("李博士");
        question2.setOptions(new ArrayList<>());

        Option q2OptA = new Option();
        q2OptA.setId(new OptionId(null, 1));
        q2OptA.setOptionText("A. 快速排序");
        question2.addOption(q2OptA);

        Option q2OptB = new Option();
        q2OptB.setId(new OptionId(null, 2));
        q2OptB.setOptionText("B. 冒泡排序");
        question2.addOption(q2OptB);

        Option q2OptC = new Option();
        q2OptC.setId(new OptionId(null, 3));
        q2OptC.setOptionText("C. 归并排序");
        question2.addOption(q2OptC);

        Question savedQuestion2 = questionRepository.saveAndFlush(question2);
        assertThat(savedQuestion2.getQuestionId()).isNotNull();
        System.out.println("已保存题目2 ID: " + savedQuestion2.getQuestionId());


        // --- 2. 创建试卷信息 ---
        PaperInfo paperInfo = new PaperInfo();
        paperInfo.setPaperId(2025051801); // 试卷ID
        paperInfo.setCourseId(101);       // 课程ID
        paperInfo.setCreator("教学管理员");
        paperInfo.setSingleChoiceNum(1);
        paperInfo.setMultipleChoiceNum("1"); // 您的实体中此字段是 String
        paperInfo.setTrueFalseNum("0");      // 您的实体中此字段是 String
        paperInfo.setOpenTime(LocalDateTime.now().minusDays(1)); // 假设一天前开始
        paperInfo.setCloseTime(LocalDateTime.now().plusDays(7));  // 一周后结束

        // 确保所有非空字段都被设置
        paperInfo.setHighestScoresForSingleChoice(50);
        paperInfo.setHighestScoresForMultipleChoice(50);
        paperInfo.setHighestScoresForTrueFalse(0);
        paperInfo.setTotalScores(100);
        paperInfo.setPaperQuestions(new ArrayList<>());
        paperInfo.setExamResults(new ArrayList<>());
        paperInfo.setDetailedResults(new ArrayList<>());

        // --- 3. 创建试卷题目关联 ---
        PaperQuestion pq1 = new PaperQuestion();
        pq1.setPaperId(paperInfo.getPaperId());
        pq1.setCourseId(paperInfo.getCourseId());
        pq1.setQuestionId(savedQuestion1.getQuestionId());
        pq1.setPoints(50);
        pq1.setKnowledgePoints("基础代数");
        pq1.setOptionA(savedQuestion1.getOptions().get(0).getOptionText());
        pq1.setOptionB(savedQuestion1.getOptions().get(1).getOptionText());
        pq1.setOptionC(savedQuestion1.getOptions().get(2).getOptionText());
        pq1.setQuestionText(savedQuestion1.getQuestionText());
        pq1.setCorrectAnswer(savedQuestion1.getCorrectAnswer());
        pq1.setPaperInfo(paperInfo); // 设置双向关联
        paperInfo.getPaperQuestions().add(pq1);

        PaperQuestion pq2 = new PaperQuestion();
        pq2.setPaperId(paperInfo.getPaperId());
        pq2.setCourseId(paperInfo.getCourseId());
        pq2.setQuestionId(savedQuestion2.getQuestionId());
        pq2.setPoints(50);
        pq2.setKnowledgePoints("排序算法复杂度");
        pq2.setOptionA(savedQuestion2.getOptions().get(0).getOptionText());
        pq2.setOptionB(savedQuestion2.getOptions().get(1).getOptionText());
        pq2.setOptionC(savedQuestion2.getOptions().get(2).getOptionText());
        pq2.setQuestionText(savedQuestion2.getQuestionText());
        pq2.setCorrectAnswer(savedQuestion2.getCorrectAnswer());
        pq2.setPaperInfo(paperInfo); // 设置双向关联
        paperInfo.getPaperQuestions().add(pq2);

        PaperInfo savedPaperInfo = paperInfoRepository.saveAndFlush(paperInfo);
        assertThat(savedPaperInfo.getPaperQuestions()).hasSize(2);
        System.out.println("已保存试卷信息 PaperID: " + savedPaperInfo.getPaperId() + ", CourseID: " + savedPaperInfo.getCourseId());


        // --- 4. 创建学生考试结果 ---
        Integer studentId1 = 10001;
        ExamResult examResult1 = new ExamResult();
        examResult1.setPaperId(savedPaperInfo.getPaperId());
        examResult1.setCourseId(savedPaperInfo.getCourseId());
        examResult1.setStudentId(studentId1);
        examResult1.setTotalScore(90);
        examResult1.setPaperInfo(savedPaperInfo); // 设置双向关联
        savedPaperInfo.getExamResults().add(examResult1);

        // --- 5. 创建详细答题结果 ---
        DetailedResult dr1_s1_q1 = new DetailedResult();
        dr1_s1_q1.setPaperId(savedPaperInfo.getPaperId());
        dr1_s1_q1.setCourseId(savedPaperInfo.getCourseId());
        dr1_s1_q1.setStudentId(studentId1);
        dr1_s1_q1.setQuestionId(savedQuestion1.getQuestionId());
        dr1_s1_q1.setCorrectAnswer(savedQuestion1.getCorrectAnswer());
        dr1_s1_q1.setStudentAnswer("C"); // 答对
        dr1_s1_q1.setPoints(50);
        dr1_s1_q1.setPaperInfo(savedPaperInfo); // 设置双向关联
        savedPaperInfo.getDetailedResults().add(dr1_s1_q1);

        DetailedResult dr1_s1_q2 = new DetailedResult();
        dr1_s1_q2.setPaperId(savedPaperInfo.getPaperId());
        dr1_s1_q2.setCourseId(savedPaperInfo.getCourseId());
        dr1_s1_q2.setStudentId(studentId1);
        dr1_s1_q2.setQuestionId(savedQuestion2.getQuestionId());
        dr1_s1_q2.setCorrectAnswer(savedQuestion2.getCorrectAnswer());
        dr1_s1_q2.setStudentAnswer("A"); // 只答对一个选项 (假设多选评分逻辑)
        dr1_s1_q2.setPoints(40); // 假设部分得分
        dr1_s1_q2.setPaperInfo(savedPaperInfo); // 设置双向关联
        savedPaperInfo.getDetailedResults().add(dr1_s1_q2);

        // 再次保存 PaperInfo 以级联保存 ExamResult 和 DetailedResult
        paperInfoRepository.saveAndFlush(savedPaperInfo);
        System.out.println("已保存学生 " + studentId1 + " 的考试结果和答题详情。");


        System.out.println("数据填充完毕。由于使用了 @Commit，数据将保留在 MySQL 数据库中。");
        System.out.println("数据库: " + "jdbc:mysql://localhost:3306/online_test_db");
        System.out.println("请在测试运行后检查您的数据库。");
    }
}