// File: src/test/java/com/example/onlinetestingbackend/repository/PaperInfoRepositoryTest.java
package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.PaperInfo;
import com.example.onlinetestingbackend.entity.PaperQuestion;
import com.example.onlinetestingbackend.entity.ExamResult;
import com.example.onlinetestingbackend.entity.DetailedResult;
import com.example.onlinetestingbackend.entity.id.PaperInfoId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PaperInfoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaperInfoRepository paperInfoRepository;

    private PaperInfo paperInfo1;
    private PaperInfo paperInfo2;
    private PaperInfoId paperInfoId1;
    private PaperInfoId paperInfoId2;

    @BeforeEach
    void setUp() {
        paperInfoId1 = new PaperInfoId(1, 101); // paperId, courseId
        paperInfo1 = new PaperInfo();
        paperInfo1.setPaperId(paperInfoId1.getPaperId());
        paperInfo1.setCourseId(paperInfoId1.getCourseId());
        paperInfo1.setCreator("Prof. Alpha");
        paperInfo1.setSingleChoiceNum(10);
        paperInfo1.setMultipleChoiceNum(5);
        paperInfo1.setTrueFalseNum(5);
        paperInfo1.setOpenTime(LocalDateTime.now().plusDays(1));
        paperInfo1.setCloseTime(LocalDateTime.now().plusDays(2));
        paperInfo1.setHighestScoresForSingleChoice(50);
        paperInfo1.setHighestScoresForMultipleChoice(25);
        paperInfo1.setHighestScoresForTrueFalse(25);
        paperInfo1.setTotalScores(100);

        paperInfoId2 = new PaperInfoId(2, 102);
        paperInfo2 = new PaperInfo();
        paperInfo2.setPaperId(paperInfoId2.getPaperId());
        paperInfo2.setCourseId(paperInfoId2.getCourseId());
        paperInfo2.setCreator("Prof. Beta");
        paperInfo2.setSingleChoiceNum(8);
        paperInfo2.setMultipleChoiceNum(4);
        paperInfo2.setTrueFalseNum(4);
        paperInfo2.setOpenTime(LocalDateTime.now().plusHours(12));
        paperInfo2.setCloseTime(LocalDateTime.now().plusHours(24));
        paperInfo2.setHighestScoresForSingleChoice(40);
        paperInfo2.setHighestScoresForMultipleChoice(30);
        paperInfo2.setHighestScoresForTrueFalse(30);
        paperInfo2.setTotalScores(100);
    }

    @Test
    @DisplayName("Save PaperInfo")
    void testSavePaperInfo() {
        PaperInfo savedPaperInfo = paperInfoRepository.save(paperInfo1);
        entityManager.flush();

        assertThat(savedPaperInfo).isNotNull();
        assertThat(savedPaperInfo.getCreator()).isEqualTo("Prof. Alpha");
        assertThat(paperInfoRepository.findById(paperInfoId1)).isPresent();
    }

    @Test
    @DisplayName("Find PaperInfo by ID")
    void testFindById() {
        entityManager.persist(paperInfo1);
        entityManager.flush();

        Optional<PaperInfo> found = paperInfoRepository.findById(paperInfoId1);

        assertThat(found).isPresent();
        assertThat(found.get().getCreator()).isEqualTo("Prof. Alpha");
    }

    @Test
    @DisplayName("Find All PaperInfos")
    void testFindAll() {
        entityManager.persist(paperInfo1);
        entityManager.persist(paperInfo2);
        entityManager.flush();

        List<PaperInfo> paperInfos = paperInfoRepository.findAll();

        assertThat(paperInfos).hasSize(2);
        assertThat(paperInfos).extracting(PaperInfo::getCreator).containsExactlyInAnyOrder("Prof. Alpha", "Prof. Beta");
    }

//    @Test
//    @DisplayName("Update PaperInfo")
//    void testUpdatePaperInfo() {
//        PaperInfo persistedPaperInfo = entityManager.persistAndFlush(paperInfo1);
//        entityManager.detach(persistedPaperInfo); // Detach to simulate fetching in a new transaction context
//
//        Optional<PaperInfo> toUpdateOptional = paperInfoRepository.findById(persistedPaperInfo.getPaperId(), persistedPaperInfo.getCourseId());
//        assertThat(toUpdateOptional).isPresent();
//        PaperInfo toUpdate = toUpdateOptional.get();
//
//        toUpdate.setCreator("Dr. Gamma");
//        paperInfoRepository.saveAndFlush(toUpdate);
//
//        Optional<PaperInfo> updated = paperInfoRepository.findById(persistedPaperInfo.getPaperId(), persistedPaperInfo.getCourseId());
//        assertThat(updated).isPresent();
//        assertThat(updated.get().getCreator()).isEqualTo("Dr. Gamma");
//    }

    @Test
    @DisplayName("Delete PaperInfo")
    void testDeletePaperInfo() {
        entityManager.persistAndFlush(paperInfo1);

        paperInfoRepository.deleteById(paperInfoId1);
        entityManager.flush();

        assertThat(paperInfoRepository.findById(paperInfoId1)).isNotPresent();
    }

    @Test
    @DisplayName("查找试卷信息根据课程ID (Find PaperInfo by Course ID)")
    void testFindByCourseId() {
        // 确保 paperInfo1 在 @BeforeEach 中已经正确设置了所有非空字段
        entityManager.persist(paperInfo1); // courseId 101

        PaperInfo paperInfoCourse101Another = new PaperInfo();
        paperInfoCourse101Another.setPaperId(3); // 不同的 paperId
        paperInfoCourse101Another.setCourseId(101); // 相同的 courseId
        paperInfoCourse101Another.setCreator("Prof. Alpha");
        paperInfoCourse101Another.setSingleChoiceNum(10);
        paperInfoCourse101Another.setMultipleChoiceNum(5); // 实体中是 String 类型
        paperInfoCourse101Another.setTrueFalseNum(5);     // 实体中是 String 类型
        paperInfoCourse101Another.setTotalScores(100);

        // *** 修改点：为所有非空字段设置值 ***
        paperInfoCourse101Another.setHighestScoresForSingleChoice(50); // 示例值
        paperInfoCourse101Another.setHighestScoresForMultipleChoice(30); // 示例值
        paperInfoCourse101Another.setHighestScoresForTrueFalse(20);    // 示例值
        // 如果还有其他在 PaperInfo 中定义为 nullable=false 的字段，也需要在这里设置

        entityManager.persist(paperInfoCourse101Another);

        // 确保 paperInfo2 在 @BeforeEach 中已经正确设置了所有非空字段
        entityManager.persist(paperInfo2); // courseId 102
        entityManager.flush();

        List<PaperInfo> course101Papers = paperInfoRepository.findByCourseId(101);
        List<PaperInfo> course102Papers = paperInfoRepository.findByCourseId(102);
        List<PaperInfo> courseNonExistentPapers = paperInfoRepository.findByCourseId(999);

        assertThat(course101Papers).hasSize(2);
        assertThat(course102Papers).hasSize(1);
        assertThat(courseNonExistentPapers).isEmpty();
    }

    @Test
    @DisplayName("Find PaperInfo by Creator")
    void testFindByCreator() {
        entityManager.persist(paperInfo1); // Prof. Alpha
        entityManager.persist(paperInfo2); // Prof. Beta
        entityManager.flush();

        List<PaperInfo> alphaPapers = paperInfoRepository.findByCreator("Prof. Alpha");
        List<PaperInfo> betaPapers = paperInfoRepository.findByCreator("Prof. Beta");

        assertThat(alphaPapers).hasSize(1);
        assertThat(alphaPapers.get(0).getCourseId()).isEqualTo(101);
        assertThat(betaPapers).hasSize(1);
    }

    @Test
    @DisplayName("Find PaperInfo by Open Time Between")
    void testFindByOpenTimeBetween() {
        paperInfo1.setOpenTime(LocalDateTime.now().plusDays(1)); // Tomorrow
        paperInfo1.setCloseTime(LocalDateTime.now().plusDays(2));
        entityManager.persist(paperInfo1);

        paperInfo2.setOpenTime(LocalDateTime.now().plusDays(3)); // Day after tomorrow
        paperInfo2.setCloseTime(LocalDateTime.now().plusDays(4));
        entityManager.persist(paperInfo2);
        entityManager.flush();

        LocalDateTime searchStart = LocalDateTime.now();
        LocalDateTime searchEndTomorrow = LocalDateTime.now().plusDays(1).plusHours(1); // Includes paperInfo1
        LocalDateTime searchEndFar = LocalDateTime.now().plusDays(5);     // Includes both

        List<PaperInfo> papersOpeningSoon = paperInfoRepository.findByOpenTimeBetween(searchStart, searchEndTomorrow);
        List<PaperInfo> allPapersInFuture = paperInfoRepository.findByOpenTimeBetween(searchStart, searchEndFar);

        assertThat(papersOpeningSoon).hasSize(1);
        assertThat(papersOpeningSoon.get(0).getCreator()).isEqualTo("Prof. Alpha");
        assertThat(allPapersInFuture).hasSize(2);
    }

    @Test
    @DisplayName("Find PaperInfo by Course ID and Creator")
    void testFindByCourseIdAndCreator() {
        entityManager.persist(paperInfo1); // 101, Prof. Alpha
        entityManager.persist(paperInfo2); // 102, Prof. Beta
        entityManager.flush();

        List<PaperInfo> found = paperInfoRepository.findByCourseIdAndCreator(101, "Prof. Alpha");
        List<PaperInfo> notFoundCourse = paperInfoRepository.findByCourseIdAndCreator(102, "Prof. Alpha");
        List<PaperInfo> notFoundCreator = paperInfoRepository.findByCourseIdAndCreator(101, "Prof. Beta");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getPaperId()).isEqualTo(paperInfo1.getPaperId());
        assertThat(notFoundCourse).isEmpty();
        assertThat(notFoundCreator).isEmpty();
    }

    @Test
    @DisplayName("测试 PaperInfo 的级联删除 (Test cascade delete for PaperInfo)")
    void testCascadeDeleteForPaperInfo() {
        // 初始化 PaperInfo
        PaperInfo paper = new PaperInfo();
        paper.setPaperId(1);
        paper.setCourseId(101);
        paper.setCreator("Test Creator");
        paper.setSingleChoiceNum(1);
        paper.setMultipleChoiceNum(0);
        paper.setTrueFalseNum(0);
        paper.setTotalScores(10);

        // *** 修改点：为所有在实体中定义为 nullable=false 的字段设置值 ***
        paper.setHighestScoresForSingleChoice(5);  // 示例值
        paper.setHighestScoresForMultipleChoice(3); // 示例值
        paper.setHighestScoresForTrueFalse(2);     // 示例值
        // 如果 PaperInfo 实体中还有其他 nullable=false 的字段，也需要在这里设置

        // 初始化 PaperQuestion
        PaperQuestion pq = new PaperQuestion();
        pq.setPaperId(paper.getPaperId());
        pq.setCourseId(paper.getCourseId());
        pq.setQuestionId(1001); // 测试用的任意题目ID
        pq.setPoints(10);
        pq.setQuestionText("Sample question in paper?");
        pq.setCorrectAnswer("A");
        pq.setOptionA("A");
        pq.setOptionB("B");
        pq.setPaperInfo(paper); // 关联到 PaperInfo
        paper.getPaperQuestions().add(pq);

        entityManager.persist(paper); // 这应该会级联持久化 pq
        entityManager.flush();

        PaperInfoId pId = new PaperInfoId(paper.getPaperId(), paper.getCourseId());
        assertThat(entityManager.find(PaperInfo.class, pId)).isNotNull();
        assertThat(entityManager.find(PaperQuestion.class,
                new com.example.onlinetestingbackend.entity.id.PaperQuestionId(pId.getPaperId(), pId.getCourseId(), pq.getQuestionId()))).isNotNull();

        paperInfoRepository.deleteById(pId);
        entityManager.flush(); // 重要：以便观察级联效果

        assertThat(paperInfoRepository.findById(pId)).isNotPresent();
        // 检查 PaperQuestion 是否被级联删除了
        // (如果 PaperQuestionRepository 存在，可以用它查询；或者直接用 entityManager find)
        assertThat(entityManager.find(PaperQuestion.class,
                new com.example.onlinetestingbackend.entity.id.PaperQuestionId(pId.getPaperId(), pId.getCourseId(), pq.getQuestionId()))).isNull();
    }
}