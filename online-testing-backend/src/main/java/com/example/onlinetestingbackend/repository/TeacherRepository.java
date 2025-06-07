package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
}
