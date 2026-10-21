package com.rag.coursemanagementsystem.service;

import com.rag.coursemanagementsystem.exception.ResourceNotFoundException;
import com.rag.coursemanagementsystem.model.Course;
import com.rag.coursemanagementsystem.repository.CourseRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final VectorStore vectorStore;

    public CourseService(CourseRepository courseRepository, VectorStore vectorStore) {
        this.courseRepository = courseRepository;
        this.vectorStore = vectorStore;
    }

    /**
     * 1. Saves course entity to relational table.
     * 2. Converts course description into a 768-dim vector via Ollama and saves to pgvector.
     * 3. Marks course as indexed = true.
     */
    @Transactional
    public Course createCourse(Course course) {
        // Step A: Save entity to 'courses' table
        course.setIndexed(false);
        Course savedCourse = courseRepository.save(course);

        // Step B: Build Spring AI Document with text content and metadata
        Document document = new Document(
                savedCourse.getDescription(),
                Map.of(
                        "courseId", savedCourse.getId(),
                        "title", savedCourse.getTitle(),
                        "category", savedCourse.getCategory()
                )
        );

        // Step C: Generate embedding & save to 'vector_store' table in PostgreSQL
        vectorStore.add(List.of(document));

        // Step D: Update indexed flag
        savedCourse.setIndexed(true);
        return courseRepository.save(savedCourse);
    }

    /**
     * Fetch a course by ID or throw custom 404 ResourceNotFoundException
     */
    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    /**
     * Fetch all courses from relational DB
     */
    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }
}