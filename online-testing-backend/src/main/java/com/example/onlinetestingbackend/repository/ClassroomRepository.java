package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Integer> {
}
