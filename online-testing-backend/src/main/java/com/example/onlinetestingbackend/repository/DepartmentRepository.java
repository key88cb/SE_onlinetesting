package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
}
