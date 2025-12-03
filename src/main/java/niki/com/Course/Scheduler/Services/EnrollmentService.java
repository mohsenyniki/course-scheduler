package niki.com.Course.Scheduler.Services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import niki.com.Course.Scheduler.Data.EnrollmentDataRepo;

import java.util.Set;

@Service
public class EnrollmentService {

    private final EnrollmentDataRepo repo;

    @Autowired
    public EnrollmentService(EnrollmentDataRepo repo) {
        this.repo = repo;
    }

    public void enroll(String studentId, String courseId) {
        repo.enroll(studentId, courseId);
    }

    public Set<String> getEnrolledCourses(String studentId) {
        return repo.findEnrolledCourseIds(studentId);
    }

    public boolean isEnrolled(String studentId, String courseId) {
        return repo.isEnrolled(studentId, courseId);
    }

    public void drop(String studentId, String courseId) {
        repo.drop(studentId, courseId);
    }

    public int countEnrolled(String courseId) {
        return repo.countEnrolledForCourse(courseId);
    }

    public void clearAllEnrollments() {
        repo.clearAllEnrollments();
    }
}
