package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.QuestionOption;
import com.example.onlinetestingbackend.entity.id.QuestionOptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, QuestionOptionId> {

    // 根据题目ID查询所有选项
    List<QuestionOption> findByQuestionId(Integer questionId);

    // 根据题目ID和选项ID查询特定选项 (虽然 JpaRepository 提供了 findById)
    // QuestionOption findByQuestionIdAndOptionId(Integer questionId, Integer optionId);
}
