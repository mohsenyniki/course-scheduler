package niki.com.Course.Scheduler.Services;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EnrollmentService {

    // In-memory store mapping username -> set of courseIds
    private final Map<String, Set<String>> studentEnrollments = new HashMap<>();

    public void enroll(String studentId, String courseId) {
        studentEnrollments.putIfAbsent(studentId, new HashSet<>());
        studentEnrollments.get(studentId).add(courseId);
    }

    public Set<String> getEnrolledCourses(String studentId) {
        return studentEnrollments.getOrDefault(studentId, Collections.emptySet());
    }

    public boolean isEnrolled(String studentId, String courseId) {
        return studentEnrollments.getOrDefault(studentId, Collections.emptySet()).contains(courseId);
    }
}
