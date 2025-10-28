package niki.com.Course.Scheduler.Data;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class EnrollmentDataRepo {

    private final JdbcTemplate jdbc;

    public EnrollmentDataRepo(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void enroll(String username, String courseId) {
        // use insert or ignore so duplicate enrollments won't throw
        jdbc.update("INSERT OR IGNORE INTO enrollment (student_username, course_course_id) VALUES (?,?)", username, courseId);
    }

    public Set<String> findEnrolledCourseIds(String username) {
    List<String> list = jdbc.queryForList("SELECT course_course_id FROM enrollment WHERE student_username = ?", String.class, username);
        return new HashSet<>(list);
    }

    public boolean isEnrolled(String username, String courseId) {
    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM enrollment WHERE student_username = ? AND course_course_id = ?", Integer.class, username, courseId);
        return count != null && count > 0;
    }
}
