package niki.com.Course.Scheduler;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    List<Course> findByCourseName(String courseName);

    List<Course> findByInstructor(String instructor);

    List<Course> findByCredits(int credits);

    // find by course id (case-insensitive)
    List<Course> findByCourseIdIgnoreCase(String courseId);

    // partial, case-insensitive match on course name
    List<Course> findByCourseNameContainingIgnoreCase(String namePart);
}
