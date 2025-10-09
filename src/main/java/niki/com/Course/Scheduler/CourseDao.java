package niki.com.Course.Scheduler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CourseDao {

    private final JdbcTemplate jdbc;

    public CourseDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long count() {
        Long c = jdbc.queryForObject("select count(*) from course", Long.class);
        return c == null ? 0 : c;
    }

    public void saveAll(List<Course> courses) {
        final String insertCourse = "insert into course (course_id, course_name, credits, instructor, location, schedule) values (?,?,?,?,?,?)";
        final String insertPrereq = "insert into course_prerequisites (course_course_id, prerequisites) values (?,?)";
        for (Course course : courses) {
            jdbc.update(insertCourse, course.getCourseId(), course.getCourseName(), course.getCredits(), course.getInstructor(), course.getLocation(), course.getSchedule());
            if (course.getPrerequisites() != null) {
                for (String p : course.getPrerequisites()) {
                    jdbc.update(insertPrereq, course.getCourseId(), p);
                }
            }
        }
    }

    public List<Course> findByCourseIdIgnoreCase(String id) {
        String sql = "select course_id, course_name, credits, instructor, location, schedule from course where upper(course_id)=upper(?)";
        List<Course> list = jdbc.query(sql, new Object[]{id}, new CourseRowMapper());
        return list;
    }

    public List<Course> findByCourseNameContainingIgnoreCase(String namePart) {
        String sql = "select course_id, course_name, credits, instructor, location, schedule from course where lower(course_name) like '%' || lower(?) || '%'";
        List<Course> list = jdbc.query(sql, new Object[]{namePart}, new CourseRowMapper());
        return list;
    }

    private class CourseRowMapper implements RowMapper<Course> {
        @Override
        public Course mapRow(ResultSet rs, int rowNum) throws SQLException {
            String courseId = rs.getString("course_id");
            String courseName = rs.getString("course_name");
            int credits = rs.getInt("credits");
            String instructor = rs.getString("instructor");
            String location = rs.getString("location");
            String schedule = rs.getString("schedule");
            List<String> prereqs = jdbc.query("select prerequisites from course_prerequisites where course_course_id = ?", new Object[]{courseId}, (r, i) -> r.getString("prerequisites"));
            return new Course(courseId, courseName, instructor, schedule, location, credits, prereqs);
        }
    }
}
