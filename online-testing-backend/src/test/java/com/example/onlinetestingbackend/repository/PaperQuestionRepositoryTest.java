// File: src/test/java/com/example/onlinetestingbackend/repository/PaperQuestionRepositoryTest.java
package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.PaperInfo;
import com.example.onlinetestingbackend.entity.PaperQuestion;
import com.example.onlinetestingbackend.entity.id.PaperInfoId;
import com.example.onlinetestingbackend.entity.id.PaperQuestionId;
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
public class PaperQuestionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaperQuestionRepository paperQuestionRepository;

    private PaperInfo paperInfo1;
    private PaperQuestion paperQuestion1;
    private PaperQuestion paperQuestion2;
    private PaperQuestionId paperQuestionId1;
    private PaperQuestionId paperQuestionId2;

    @BeforeEach // 确保这是 @BeforeEach
    void setUp() {
        // Create and persist a PaperInfo first, as PaperQuestion depends on it
        paperInfo1 = new PaperInfo();
        paperInfo1.setPaperId(1);
        paperInfo1.setCourseId(101);
        paperInfo1.setCreator("Test Creator");
        paperInfo1.setSingleChoiceNum(2);
        paperInfo1.setMultipleChoiceNum("0");
        paperInfo1.setTrueFalseNum("0");
        paperInfo1.setTotalScores(20);

        // *** 再次确认添加这些缺失的非空字段赋值 ***
        paperInfo1.setHighestScoresForSingleChoice(10); // 示例值
        paperInfo1.setHighestScoresForMultipleChoice(5);  // 示例值
        paperInfo1.setHighestScoresForTrueFalse(5);     // 示例值
        // 如果 PaperInfo 实体中还有其他 nullable=false 的字段，也需要在这里设置
        // 例如，如果 openTime 和 closeTime 也被改成了非空，也需要设置它们
        // paperInfo1.setOpenTime(LocalDateTime.now().plusDays(1));
        // paperInfo1.setCloseTime(LocalDateTime.now().plusDays(2));


        entityManager.persistAndFlush(paperInfo1); // 这一步必须成功


        paperQuestionId1 = new PaperQuestionId(paperInfo1.getPaperId(), paperInfo1.getCourseId(), 1001);
        paperQuestion1 = new PaperQuestion();
        paperQuestion1.setPaperId(paperQuestionId1.getPaperId());
        paperQuestion1.setCourseId(paperQuestionId1.getCourseId());
        paperQuestion1.setQuestionId(paperQuestionId1.getQuestionId());
        paperQuestion1.setPoints(10);
        paperQuestion1.setKnowledgePoints("Basics");
        paperQuestion1.setOptionA("Option A1");
        paperQuestion1.setOptionB("Option B1");
        // 确保所有在 PaperQuestion 中定义为 nullable=false 的字段也被设置
        paperQuestion1.setQuestionText("What is 1+1?");
        paperQuestion1.setCorrectAnswer("B");
        paperQuestion1.setPaperInfo(paperInfo1);


        paperQuestionId2 = new PaperQuestionId(paperInfo1.getPaperId(), paperInfo1.getCourseId(), 1002);
        paperQuestion2 = new PaperQuestion();
        paperQuestion2.setPaperId(paperQuestionId2.getPaperId());
        paperQuestion2.setCourseId(paperQuestionId2.getCourseId());
        paperQuestion2.setQuestionId(paperQuestionId2.getQuestionId());
        paperQuestion2.setPoints(10);
        paperQuestion2.setKnowledgePoints("Advanced");
        paperQuestion2.setOptionA("Option A2");
        paperQuestion2.setOptionB("Option B2");
        paperQuestion2.setQuestionText("What is 2+2?");
        paperQuestion2.setCorrectAnswer("A");
        paperQuestion2.setPaperInfo(paperInfo1);
    }    @Test
    @DisplayName("Save PaperQuestion")
    void testSavePaperQuestion() {
        PaperQuestion saved = paperQuestionRepository.save(paperQuestion1);
        entityManager.flush();

        assertThat(saved).isNotNull();
        assertThat(saved.getPoints()).isEqualTo(10);
        Optional<PaperQuestion> found = paperQuestionRepository.findById(paperQuestionId1);
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("Find PaperQuestion by ID")
    void testFindById() {
        entityManager.persist(paperQuestion1);
        entityManager.flush();

        Optional<PaperQuestion> found = paperQuestionRepository.findById(paperQuestionId1);

        assertThat(found).isPresent();
        assertThat(found.get().getQuestionText()).isEqualTo("What is 1+1?");
    }

    @Test
    @DisplayName("Find All PaperQuestions")
    void testFindAll() {
        entityManager.persist(paperQuestion1);
        entityManager.persist(paperQuestion2);
        entityManager.flush();

        List<PaperQuestion> all = paperQuestionRepository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(PaperQuestion::getKnowledgePoints).containsExactlyInAnyOrder("Basics", "Advanced");
    }

    @Test
    @DisplayName("Update PaperQuestion")
    void testUpdatePaperQuestion() {
        PaperQuestion persisted = entityManager.persistAndFlush(paperQuestion1);
        entityManager.detach(persisted);

        Optional<PaperQuestion> toUpdateOpt = paperQuestionRepository.findById(paperQuestionId1);
        assertThat(toUpdateOpt).isPresent();
        PaperQuestion toUpdate = toUpdateOpt.get();

        toUpdate.setPoints(15);
        paperQuestionRepository.saveAndFlush(toUpdate);

        Optional<PaperQuestion> updated = paperQuestionRepository.findById(paperQuestionId1);
        assertThat(updated).isPresent();
        assertThat(updated.get().getPoints()).isEqualTo(15);
    }

    @Test
    @DisplayName("Delete PaperQuestion")
    void testDeletePaperQuestion() {
        entityManager.persistAndFlush(paperQuestion1);

        paperQuestionRepository.deleteById(paperQuestionId1);
        entityManager.flush();

        assertThat(paperQuestionRepository.findById(paperQuestionId1)).isNotPresent();
    }

    @Test
    @DisplayName("Find by PaperId and CourseId")
    void testFindByPaperIdAndCourseId() {
        entityManager.persist(paperQuestion1);
        entityManager.persist(paperQuestion2);
        entityManager.flush();

        List<PaperQuestion> questions = paperQuestionRepository.findByPaperIdAndCourseId(paperInfo1.getPaperId(), paperInfo1.getCourseId());

        assertThat(questions).hasSize(2);
        assertThat(questions).extracting(PaperQuestion::getQuestionId).containsExactlyInAnyOrder(1001, 1002);
    }

    @Test
    @DisplayName("Find by KnowledgePoints Containing and PaperId and CourseId")
    void testFindByKnowledgePointsContainingAndPaperIdAndCourseId() {
        entityManager.persist(paperQuestion1); // KnowledgePoints: Basics
        entityManager.persist(paperQuestion2); // KnowledgePoints: Advanced
        entityManager.flush();

        List<PaperQuestion> basicQuestions = paperQuestionRepository.findByKnowledgePointsContainingAndPaperIdAndCourseId("Basic", paperInfo1.getPaperId(), paperInfo1.getCourseId());
        List<PaperQuestion> advancedQuestions = paperQuestionRepository.findByKnowledgePointsContainingAndPaperIdAndCourseId("Adv", paperInfo1.getPaperId(), paperInfo1.getCourseId());
        List<PaperQuestion> nonExistent = paperQuestionRepository.findByKnowledgePointsContainingAndPaperIdAndCourseId("NonExistent", paperInfo1.getPaperId(), paperInfo1.getCourseId());

        assertThat(basicQuestions).hasSize(1);
        assertThat(basicQuestions.get(0).getQuestionId()).isEqualTo(1001);
        assertThat(advancedQuestions).hasSize(1);
        assertThat(advancedQuestions.get(0).getQuestionId()).isEqualTo(1002);
        assertThat(nonExistent).isEmpty();
    }
}