package com.example.onlinetestingbackend.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.example.onlinetestingbackend.entity.PaperInfo;
import com.example.onlinetestingbackend.entity.id.PaperInfoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface PaperInfoRepository extends JpaRepository<PaperInfo, PaperInfoId> {

    // 根据课程ID查询试卷信息
    List<PaperInfo> findByCourseId(Integer courseId);

    // 根据创建者查询试卷信息
    List<PaperInfo> findByCreator(String creator);

    // 根据开放时间范围查询试卷
    List<PaperInfo> findByOpenTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 根据课程ID和创建者查询
    List<PaperInfo> findByCourseIdAndCreator(Integer courseId, String creator);

    PaperInfo findByCourseIdAndPaperId(Integer courseId, Integer paperId);

    void deleteById(PaperInfoId id);
}