// 用AI搞了一个测试工具
package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.Question;
import com.example.onlinetestingbackend.entity.QuestionOption; // 假设您有 QuestionOption
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager; // 可选，用于更精细的数据操作

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat; // 使用 AssertJ 进行断言

@DataJpaTest // 核心注解，用于 JPA 组件的测试
public class QuestionRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    // TestEntityManager 是 @DataJpaTest 提供的一个方便的工具，
    // 用于在测试中持久化和刷新实体，而不直接使用 Repository 的 save 方法，
    // 这样可以更好地控制测试数据的准备阶段。
    @Autowired
    private TestEntityManager entityManager;

    private Question question1;
    private Question question2;

    @BeforeEach // JUnit 5: 每个测试方法执行前运行
    void setUp() {
        // 清理可能存在的旧数据（虽然 @DataJpaTest 默认回滚，但显式清理有时更好）
        // questionRepository.deleteAll(); // 如果不使用entityManager，可以直接操作repository

        // 准备测试数据
        question1 = new Question();
        question1.setSubjectCategory("数学");
        question1.setTags("代数,初等数学");
        question1.setQuestionText("1 + 1 = ?");
        question1.setQuestionType("单选题");
        question1.setCorrectAnswer("B");
        question1.setCreator("张老师");
        // 如果有选项，也一并设置
        QuestionOption q1Opt1 = new QuestionOption();
        q1Opt1.setOptionId(1); // 确保 QuestionOptionId 和 QuestionOption 实体正确设置
        // q1Opt1.setQuestionId(question1.getQuestionId()); // 如果 QuestionOptionId 中的 questionId 是手动设置的
        q1Opt1.setOptionText("A. 1");
        question1.addOption(q1Opt1); // 假设 Question 实体中有 addOption 辅助方法

        QuestionOption q1Opt2 = new QuestionOption();
        q1Opt2.setOptionId(2);
        // q1Opt2.setQuestionId(question1.getQuestionId());
        q1Opt2.setOptionText("B. 2");
        question1.addOption(q1Opt2);


        question2 = new Question();
        question2.setSubjectCategory("物理");
        question2.setTags("力学");
        question2.setQuestionText("F = ma 是哪个定律？");
        question2.setQuestionType("单选题");
        question2.setCorrectAnswer("A");
        question2.setCreator("李老师");
    }

    @Test
    void testSaveQuestion() {
        // Act
        Question savedQuestion = questionRepository.save(question1);

        // Assert
        assertThat(savedQuestion).isNotNull();
        assertThat(savedQuestion.getQuestionId()).isNotNull(); // 验证ID是否已生成
        assertThat(savedQuestion.getSubjectCategory()).isEqualTo("数学");
        assertThat(savedQuestion.getOptions()).hasSize(2); // 验证级联保存是否成功
    }

    @Test
    void testFindById() {
        // Arrange: 使用 TestEntityManager 来确保数据在事务中，并且 ID 已分配
        entityManager.persistAndFlush(question1); // 持久化并刷新到数据库，使其获得ID

        // Act
        Optional<Question> foundQuestion = questionRepository.findById(question1.getQuestionId());

        // Assert
        assertThat(foundQuestion).isPresent();
        assertThat(foundQuestion.get().getQuestionText()).isEqualTo("1 + 1 = ?");
    }

    @Test
    void testFindAllQuestions() {
        // Arrange
        entityManager.persist(question1);
        entityManager.persist(question2);
        entityManager.flush();

        // Act
        List<Question> questions = questionRepository.findAll();

        // Assert
        assertThat(questions).isNotNull();
        assertThat(questions.size()).isGreaterThanOrEqualTo(2); // 可能有其他测试或默认数据
        assertThat(questions).extracting(Question::getCreator).contains("张老师", "李老师");
    }
    @Test
    void testSaveSingleQuestionWithOptions() {
        Question newQuestion = new Question();
        newQuestion.setSubjectCategory("测试科目");
        newQuestion.setTags("测试标签");
        newQuestion.setQuestionText("测试内容？");
        newQuestion.setQuestionType("单选");
        newQuestion.setCorrectAnswer("A");
        newQuestion.setCreator("测试员");
        questionRepository.flush(); // 确保写入数据库

        QuestionOption option1 = new QuestionOption();
        option1.setOptionId(1); // 确保这个ID的唯一性逻辑（在特定Question内）
        option1.setOptionText("选项1");
        newQuestion.addOption(option1); // addOption 内部调用 option1.setQuestion(newQuestion);

        Question savedQuestion = questionRepository.save(newQuestion); // 保存父实体，级联保存子实体
        questionRepository.flush(); // 确保写入数据库

        assertThat(savedQuestion.getQuestionId()).isNotNull();
        assertThat(savedQuestion.getOptions()).hasSize(1);
        QuestionOption savedOption = savedQuestion.getOptions().get(0);
        assertThat(savedOption.getQuestionId()).isEqualTo(savedQuestion.getQuestionId()); // 关键验证
        assertThat(savedOption.getOptionId()).isEqualTo(1);

        // 可选：从数据库重新加载并验证
        Optional<Question> reloadedQuestionOpt = questionRepository.findById(savedQuestion.getQuestionId());
        assertThat(reloadedQuestionOpt).isPresent();
        assertThat(reloadedQuestionOpt.get().getOptions()).hasSize(1);
        assertThat(reloadedQuestionOpt.get().getOptions().get(0).getQuestionId()).isEqualTo(savedQuestion.getQuestionId());
    }
    @Test
    void testFindBySubjectCategory() {
        // Arrange
        entityManager.persist(question1); // 数学
        entityManager.persist(question2); // 物理
        entityManager.flush();

        // Act
        List<Question> mathQuestions = questionRepository.findBySubjectCategory("数学");
        List<Question> physicsQuestions = questionRepository.findBySubjectCategory("物理");
        List<Question> chemistryQuestions = questionRepository.findBySubjectCategory("化学");


        // Assert
        assertThat(mathQuestions).isNotNull();
        assertThat(mathQuestions.size()).isEqualTo(1);
        assertThat(mathQuestions.get(0).getQuestionText()).isEqualTo("1 + 1 = ?");

        assertThat(physicsQuestions).isNotNull();
        assertThat(physicsQuestions.size()).isEqualTo(1);
        assertThat(physicsQuestions.get(0).getCreator()).isEqualTo("李老师");

        assertThat(chemistryQuestions).isNotNull();
        assertThat(chemistryQuestions.size()).isEqualTo(0);
    }

    @Test
    void testUpdateQuestion() {
        // Arrange
        Question savedQuestion = entityManager.persistAndFlush(question1);
        Integer questionId = savedQuestion.getQuestionId();

        // Act
        // 必须重新获取实体进行修改，或者确保 detached entity 被正确 merge
        Question questionToUpdate = questionRepository.findById(questionId).orElseThrow();
        questionToUpdate.setQuestionText("What is 1 + 1?");
        questionToUpdate.setTags("算术");
        questionRepository.save(questionToUpdate); // save 也可以用于更新

        // Assert
        Question updatedQuestion = questionRepository.findById(questionId).orElseThrow();
        assertThat(updatedQuestion.getQuestionText()).isEqualTo("What is 1 + 1?");
        assertThat(updatedQuestion.getTags()).isEqualTo("算术");
    }

    @Test
    void testDeleteQuestion() {
        // Arrange
        Question savedQuestion = entityManager.persistAndFlush(question1);
        Integer questionId = savedQuestion.getQuestionId();

        // Act
        questionRepository.deleteById(questionId);
        // entityManager.flush(); // 确保删除操作已同步到数据库（在验证前）

        // Assert
        Optional<Question> deletedQuestion = questionRepository.findById(questionId);
        assertThat(deletedQuestion).isNotPresent();
    }

    @Test
    void testFindByTagsContaining() {
        // Arrange
        entityManager.persist(question1); // Tags: 代数,初等数学
        entityManager.persist(question2); // Tags: 力学
        entityManager.flush();

        // Act
        List<Question> algebraQuestions = questionRepository.findByTagsContaining("代数");
        List<Question> mathQuestions = questionRepository.findByTagsContaining("数学");

        // Assert
        assertThat(algebraQuestions).hasSize(1);
        assertThat(algebraQuestions.get(0).getQuestionId()).isEqualTo(question1.getQuestionId());

        assertThat(mathQuestions).hasSize(1); // "初等数学" 包含 "数学"
        assertThat(mathQuestions.get(0).getQuestionId()).isEqualTo(question1.getQuestionId());
    }
}