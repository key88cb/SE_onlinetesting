package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.Option;
import com.example.onlinetestingbackend.entity.id.OptionId; // 确保路径正确
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionRepository extends JpaRepository<Option, OptionId> {

    // 根据题目ID查询所有选项
    // Spring Data JPA 将会查找 Option.id.questionId (Option实体中的id字段，其类型为OptionId，OptionId中有一个questionId字段)
    List<Option> findById_QuestionId(Integer questionId);

    // 如果你需要根据题目ID和选项自身的ID值查找，可以这样做：
    // Optional<Option> findById_QuestionIdAndId_OptionIdValue(Integer questionId, Integer optionIdValue);
    // 不过，通常直接使用 JpaRepository 提供的 findById(OptionId optionIdInstance) 会更直接，
    // 你需要先创建一个 OptionId 的实例。
}