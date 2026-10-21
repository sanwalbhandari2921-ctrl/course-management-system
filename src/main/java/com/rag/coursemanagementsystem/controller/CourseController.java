package com.rag.coursemanagementsystem.controller;

import com.rag.coursemanagementsystem.model.Course;
import com.rag.coursemanagementsystem.service.CourseService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final VectorStore vectorStore;

    public CourseController(CourseService courseService, VectorStore vectorStore) {
        this.courseService = courseService;
        this.vectorStore = vectorStore;
    }

    // ==========================================
    // 1. CREATE COURSE (Saves Entity + Ingests Vector)
    // ==========================================
    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        Course createdCourse = courseService.createCourse(course);
        return new ResponseEntity<>(createdCourse, HttpStatus.CREATED);
    }

    // ==========================================
    // 2. READ OPERATIONS (Relational DB)
    // ==========================================
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    // ==========================================
    // 3. VECTOR SIMILARITY SEARCH (RAG Search)
    // ==========================================
    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchCourses(
            @RequestParam String query,
            @RequestParam(defaultValue = "3") int topK) {

        // Converts prompt query into a 768-dim vector via nomic-embed-text
        // and searches vector_store table using Cosine Distance
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .build()
        );

        return ResponseEntity.ok(results);
    }
}