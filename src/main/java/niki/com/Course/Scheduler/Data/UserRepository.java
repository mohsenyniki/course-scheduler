package niki.com.Course.Scheduler.Data;

import niki.com.Course.Scheduler.Models.Student;
import niki.com.Course.Scheduler.Models.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initializeDefaultData();
    }

    private void initializeDefaultData() {
        // Check if default users already exist
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        if (count != null && count > 0) {
            return; // Already initialized
        }

        // Create default students
        String insertStudent = "INSERT INTO students (student_id, username, first_name, last_name, email, major, year) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertStudent, "1", "zahra", "Zahra", "Hosseini", "zahra@example.com", "Computer Science", 2);
        jdbcTemplate.update(insertStudent, "2", "parisa", "Parisa", "Rezaei", "parisa@example.com", "Mathematics", 3);
        jdbcTemplate.update(insertStudent, "3", "ali", "Ali", "Moradi", "ali@example.com", "Physics", 1);
        jdbcTemplate.update(insertStudent, "4", "armin", "Armin", "Karimi", "armin@example.com", "Engineering", 2);
        jdbcTemplate.update(insertStudent, "5", "ahmad", "Ahmad", "Ahmadi", "ahmad@example.com", "Chemistry", 4);
        jdbcTemplate.update(insertStudent, "6", "alice", "Alice", "Johnson", "alice@example.com", "Biology", 2);
        jdbcTemplate.update(insertStudent, "7", "max", "Max", "Williams", "max@example.com", "History", 1);

        // Create corresponding users with BCrypt encoded passwords (all use "password")
        String insertUser = "INSERT INTO users (username, password, student_id, role, enabled) VALUES (?, ?, ?, ?, ?)";
        String encodedPassword = passwordEncoder.encode("password");
        jdbcTemplate.update(insertUser, "zahra", encodedPassword, 1, "ROLE_USER", true);
        jdbcTemplate.update(insertUser, "parisa", encodedPassword, 2, "ROLE_USER", true);
        jdbcTemplate.update(insertUser, "ali", encodedPassword, 3, "ROLE_USER", true);
        jdbcTemplate.update(insertUser, "armin", encodedPassword, 4, "ROLE_USER", true);
        jdbcTemplate.update(insertUser, "ahmad", encodedPassword, 5, "ROLE_USER", true);
        jdbcTemplate.update(insertUser, "alice", encodedPassword, 6, "ROLE_USER", true);
        jdbcTemplate.update(insertUser, "max", encodedPassword, 7, "ROLE_USER", true);
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setStudentId(rs.getInt("student_id"));
        user.setRole(rs.getString("role"));
        user.setEnabled(rs.getBoolean("enabled"));
        return user;
    };

    private final RowMapper<Student> studentRowMapper = (rs, rowNum) -> {
        Student student = new Student();
        student.setStudentId(rs.getString("student_id"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        student.setEmail(rs.getString("email"));
        student.setMajor(rs.getString("major"));
        student.setYear(rs.getInt("year"));
        student.setEnrolledCourses(new ArrayList<>());
        return student;
    };

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?)";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, username);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<Student> findStudentByUsername(String username) {
        String sql = "SELECT * FROM students WHERE LOWER(username) = LOWER(?)";
        List<Student> students = jdbcTemplate.query(sql, studentRowMapper, username);
        return students.isEmpty() ? Optional.empty() : Optional.of(students.get(0));
    }

    public void saveUser(User user) {
        String sql = "INSERT INTO users (username, password, student_id, role, enabled) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, 
            user.getUsername().toLowerCase(), 
            user.getPassword(), 
            user.getStudentId(), 
            user.getRole(), 
            user.isEnabled()
        );
    }

    public void saveStudent(Student student) {
        String sql = "INSERT INTO students (student_id, username, first_name, last_name, email, major, year) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Extract username from email (part before @)
        String username = student.getEmail().split("@")[0].toLowerCase();
        jdbcTemplate.update(sql, 
            student.getStudentId(), 
            username, 
            student.getFirstName(), 
            student.getLastName(), 
            student.getEmail(), 
            student.getMajor(), 
            student.getYear()
        );
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    public int getNextStudentId() {
        String sql = "SELECT COALESCE(MAX(CAST(student_id AS INTEGER)), 7) + 1 FROM students WHERE student_id GLOB '[0-9]*'";
        Integer nextId = jdbcTemplate.queryForObject(sql, Integer.class);
        return nextId != null ? nextId : 8;
    }
}

