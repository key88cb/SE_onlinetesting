package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Integer> {
    @Query("SELECT t FROM TimeSlot t WHERE t.timeSlotId IN :ids ORDER BY t.timeSlotId")
    List<TimeSlot> findAllByIds(@Param("ids") List<Integer> ids);
}
