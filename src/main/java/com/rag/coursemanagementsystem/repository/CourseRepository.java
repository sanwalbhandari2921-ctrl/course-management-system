package com.rag.coursemanagementsystem.repository;

import com.rag.coursemanagementsystem.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Custom query method to find unindexed courses for background processing
    List<Course> findByIndexedFalse();

    List<Course> findByCategory(String category);
}