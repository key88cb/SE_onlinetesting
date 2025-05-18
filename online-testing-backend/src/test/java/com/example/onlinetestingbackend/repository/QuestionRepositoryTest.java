package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.Option; // Changed from QuestionOption
import com.example.onlinetestingbackend.entity.id.OptionId; // Import OptionId
import com.example.onlinetestingbackend.entity.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class QuestionRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Question question1;
    private Question question2;

    @BeforeEach
    void setUp() {
        // Prepare test data
        question1 = new Question();
        question1.setSubjectCategory("数学");
        question1.setTags("代数,初等数学");
        question1.setQuestionText("1 + 1 = ?");
        question1.setQuestionType("单选题");
        question1.setCorrectAnswer("B");
        question1.setCreator("张老师");

        // Setting up options for question1
        Option q1Opt1 = new Option();
        OptionId q1Opt1Id = new OptionId();
        q1Opt1Id.setOptionIdValue(1); // Set the 'option_id' part of the composite key
        // q1Opt1Id.setQuestionId() will be handled by @MapsId via the association
        q1Opt1.setId(q1Opt1Id);
        q1Opt1.setOptionText("A. 1");
        question1.addOption(q1Opt1); // addOption sets up the bidirectional relationship

        Option q1Opt2 = new Option();
        OptionId q1Opt2Id = new OptionId();
        q1Opt2Id.setOptionIdValue(2);
        q1Opt2.setId(q1Opt2Id);
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
        entityManager.flush(); // Ensure options are also persisted and IDs are set

        // Assert
        assertThat(savedQuestion).isNotNull();
        assertThat(savedQuestion.getQuestionId()).isNotNull();
        assertThat(savedQuestion.getSubjectCategory()).isEqualTo("数学");
        assertThat(savedQuestion.getOptions()).hasSize(2);

        // Verify that the Option's questionId part of OptionId is correctly set
        Option savedOption = savedQuestion.getOptions().get(0);
        assertThat(savedOption.getId()).isNotNull();
        assertThat(savedOption.getId().getQuestionId()).isEqualTo(savedQuestion.getQuestionId());
        assertThat(savedOption.getId().getOptionIdValue()).isEqualTo(1); // Or 2, depending on order
    }

    @Test
    void testFindById() {
        // Arrange
        // Persist question1 with its options
        Question persistedQuestion = entityManager.persist(question1);
        entityManager.flush();


        // Act
        Optional<Question> foundQuestionOptional = questionRepository.findById(persistedQuestion.getQuestionId());

        // Assert
        assertThat(foundQuestionOptional).isPresent();
        Question foundQuestion = foundQuestionOptional.get();
        assertThat(foundQuestion.getQuestionText()).isEqualTo("1 + 1 = ?");
        assertThat(foundQuestion.getOptions()).hasSize(2);
        assertThat(foundQuestion.getOptions().get(0).getOptionText()).isEqualTo("A. 1");
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
        assertThat(questions.size()).isGreaterThanOrEqualTo(2);
        assertThat(questions).extracting(Question::getCreator).contains("张老师", "李老师");
    }

    @Test
    @DisplayName("测试：保存单个 Question 及其 Options，并打印调试信息")
    void testSaveSingleQuestionWithOptions() {
        System.out.println("--- testSaveSingleQuestionWithOptions 开始 ---");

        // 1. 创建 Question 对象
        Question newQuestion = new Question();
        newQuestion.setSubjectCategory("测试科目");
        newQuestion.setTags("测试标签");
        newQuestion.setQuestionText("测试内容？");
        newQuestion.setQuestionType("单选");
        newQuestion.setCorrectAnswer("A");
        newQuestion.setCreator("测试员");
        System.out.println("1. 创建 newQuestion 对象 (ID 此时应为 null): " + newQuestion.getQuestionId());

        // 2. 创建 Option 对象
        Option option1 = new Option();
        OptionId option1Id = new OptionId();
        option1Id.setOptionIdValue(1); // Set the specific part of the Option's PK
        option1.setId(option1Id);
        option1.setOptionText("选项1");
        System.out.println("2. 创建 option1 对象 (ID: " + option1.getId() + ", questionId in ID: " + (option1.getId() != null ? option1.getId().getQuestionId() : "null") + ", optionIdValue in ID: " + (option1.getId() != null ? option1.getId().getOptionIdValue() : "null") + ")");

        // 3. 建立对象间的引用 using addOption (which internally calls option.setQuestion(this))
        System.out.println("3. 调用 newQuestion.addOption(option1) 来建立双向关联...");
        newQuestion.addOption(option1); // This sets option1.setQuestion(newQuestion)

        // 4. 保存父实体 Question. Due to CascadeType.ALL and @MapsId, Option should be handled.
        System.out.println("4. 调用 questionRepository.saveAndFlush(newQuestion) 来保存 Question 和级联保存 Option...");
        Question finalSavedQuestion = null;
        try {
            finalSavedQuestion = questionRepository.saveAndFlush(newQuestion);
            System.out.println("5. saveAndFlush 执行完毕。");
        } catch (Exception e) {
            System.err.println("!!! saveAndFlush 抛出异常 !!!");
            e.printStackTrace();
            throw e;
        }

        // 6. 断言
        System.out.println("6. 开始执行断言...");
        assertThat(finalSavedQuestion).isNotNull();
        assertThat(finalSavedQuestion.getQuestionId()).isNotNull();
        System.out.println("   finalSavedQuestion ID: " + finalSavedQuestion.getQuestionId());
        assertThat(finalSavedQuestion.getOptions()).hasSize(1);

        Option savedOption = finalSavedQuestion.getOptions().get(0);
        assertThat(savedOption.getId()).isNotNull();
        System.out.println("   断言前，savedOption.getId().getQuestionId(): " + savedOption.getId().getQuestionId());
        System.out.println("   断言前，savedOption.getId().getOptionIdValue(): " + savedOption.getId().getOptionIdValue());

        // The questionId in OptionId should now be populated by @MapsId
        assertThat(savedOption.getId().getQuestionId()).isEqualTo(finalSavedQuestion.getQuestionId());
        assertThat(savedOption.getId().getOptionIdValue()).isEqualTo(1); // Or use the helper: savedOption.getOptionIdValue()
        assertThat(savedOption.getOptionText()).isEqualTo("选项1");

        // 7. 可选：从数据库重新加载并验证
        System.out.println("7. 从数据库重新加载并验证...");
        entityManager.clear(); // Clear persistence context to ensure fresh load
        Optional<Question> reloadedQuestionOpt = questionRepository.findById(finalSavedQuestion.getQuestionId());
        assertThat(reloadedQuestionOpt).isPresent();
        Question reloadedQuestion = reloadedQuestionOpt.get();
        assertThat(reloadedQuestion.getOptions()).hasSize(1);
        Option reloadedOption = reloadedQuestion.getOptions().get(0);
        System.out.println("   重新加载后，reloadedOption.getId().getQuestionId(): " + reloadedOption.getId().getQuestionId());
        assertThat(reloadedOption.getId().getQuestionId()).isEqualTo(finalSavedQuestion.getQuestionId());
        assertThat(reloadedOption.getId().getOptionIdValue()).isEqualTo(1);
        System.out.println("--- testSaveSingleQuestionWithOptions 结束 ---");
    }

    @Test
    void testFindBySubjectCategory() {
        // Arrange
        entityManager.persist(question1); // 数学
        entityManager.persist(question2); // 物理
        entityManager.flush();

        // Act
        // Assuming QuestionRepository has: List<Question> findBySubjectCategory(String subjectCategory);
        List<Question> mathQuestions = questionRepository.findBySubjectCategory("数学");
        List<Question> physicsQuestions = questionRepository.findBySubjectCategory("物理");
        List<Question> chemistryQuestions = questionRepository.findBySubjectCategory("化学");


        // Assert
        assertThat(mathQuestions).isNotNull().hasSize(1);
        assertThat(mathQuestions.get(0).getQuestionText()).isEqualTo("1 + 1 = ?");

        assertThat(physicsQuestions).isNotNull().hasSize(1);
        assertThat(physicsQuestions.get(0).getCreator()).isEqualTo("李老师");

        assertThat(chemistryQuestions).isNotNull().isEmpty();
    }

    @Test
    void testUpdateQuestion() {
        // Arrange
        Question savedQuestion = entityManager.persistAndFlush(question1);
        Integer questionId = savedQuestion.getQuestionId();
        entityManager.detach(savedQuestion); // Detach to simulate fetching in a new transaction

        // Act
        Question questionToUpdate = questionRepository.findById(questionId).orElseThrow(
                () -> new AssertionError("Question not found with ID: " + questionId)
        );
        questionToUpdate.setQuestionText("What is 1 + 1?");
        questionToUpdate.setTags("算术");
        // Example of updating an option as well:
        if (!questionToUpdate.getOptions().isEmpty()) {
            questionToUpdate.getOptions().get(0).setOptionText("A. One");
        }
        questionRepository.saveAndFlush(questionToUpdate);

        // Assert
        Question updatedQuestion = questionRepository.findById(questionId).orElseThrow();
        assertThat(updatedQuestion.getQuestionText()).isEqualTo("What is 1 + 1?");
        assertThat(updatedQuestion.getTags()).isEqualTo("算术");
        if (!updatedQuestion.getOptions().isEmpty()) {
            assertThat(updatedQuestion.getOptions().get(0).getOptionText()).isEqualTo("A. One");
        }
    }

    @Test
    void testDeleteQuestion() {
        // Arrange
        Question savedQuestion = entityManager.persistAndFlush(question1);
        Integer questionId = savedQuestion.getQuestionId();

        // Act
        questionRepository.deleteById(questionId);
        entityManager.flush(); // Ensure delete is processed

        // Assert
        Optional<Question> deletedQuestion = questionRepository.findById(questionId);
        assertThat(deletedQuestion).isNotPresent();

        // Also verify that options are deleted due to orphanRemoval=true and CascadeType.ALL
        // This requires querying for options directly if you have an OptionRepository,
        // or checking if any options with the deleted question's ID exist.
        // For simplicity here, we rely on the cascade.
    }

    @Test
    void testFindByTagsContaining() {
        // Arrange
        entityManager.persist(question1); // Tags: 代数,初等数学
        entityManager.persist(question2); // Tags: 力学
        entityManager.flush();

        // Act
        // Assuming QuestionRepository has: List<Question> findByTagsContaining(String tag);
        List<Question> algebraQuestions = questionRepository.findByTagsContaining("代数");
        List<Question> mathTagQuestions = questionRepository.findByTagsContaining("数学"); // "初等数学" contains "数学"

        // Assert
        assertThat(algebraQuestions).hasSize(1);
        assertThat(algebraQuestions.get(0).getQuestionId()).isEqualTo(question1.getQuestionId());

        assertThat(mathTagQuestions).hasSize(1);
        assertThat(mathTagQuestions.get(0).getQuestionId()).isEqualTo(question1.getQuestionId());
    }
}