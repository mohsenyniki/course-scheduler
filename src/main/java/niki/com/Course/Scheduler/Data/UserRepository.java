package niki.com.Course.Scheduler.Data;

import niki.com.Course.Scheduler.Models.Student;
import niki.com.Course.Scheduler.Models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
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
            // Check if niki03 exists in users but not in students (migration fix)
            Integer niki03UserCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = 'niki03'", Integer.class);
            Integer niki03StudentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students WHERE username = 'niki03'", Integer.class);
            
            if (niki03UserCount != null && niki03UserCount > 0 && (niki03StudentCount == null || niki03StudentCount == 0)) {
                // Add missing student record for niki03
                String insertStudent = "INSERT INTO students (student_id, username, first_name, last_name, email, major, year, gpa, birth_date, phone, address, city, country) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(insertStudent, "100", "niki03", "Niki", "User", "niki03@example.com", "Computer Science", 2, 3.75, "2003-01-01", "+996-555-1100", "100 Student Ave", "Bishkek", "Kyrgyzstan");
                logger.info("Created missing student record for niki03");
            }
            
            return; // Already initialized
        }

        // Create default students with biographical information
        String insertStudent = "INSERT INTO students (student_id, username, first_name, last_name, email, major, year, gpa, birth_date, phone, address, city, country) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertStudent, "1", "zahra", "Zahra", "Hosseini", "zahra@example.com", "Computer Science", 2, 3.85, "2003-05-15", "+996-555-1001", "123 University Ave", "Bishkek", "Kyrgyzstan");
        jdbcTemplate.update(insertStudent, "2", "parisa", "Parisa", "Rezaei", "parisa@example.com", "Mathematics", 3, 3.92, "2002-08-22", "+996-555-1002", "456 Student Street", "Bishkek", "Kyrgyzstan");
        jdbcTemplate.update(insertStudent, "3", "ali", "Ali", "Moradi", "ali@example.com", "Physics", 1, 3.67, "2004-03-10", "+996-555-1003", "789 Campus Road", "Bishkek", "Kyrgyzstan");
        jdbcTemplate.update(insertStudent, "4", "armin", "Armin", "Karimi", "armin@example.com", "Engineering", 2, 3.78, "2003-11-30", "+996-555-1004", "321 College Blvd", "Bishkek", "Kyrgyzstan");
        jdbcTemplate.update(insertStudent, "5", "ahmad", "Ahmad", "Ahmadi", "ahmad@example.com", "Chemistry", 4, 3.95, "2001-07-18", "+996-555-1005", "654 Academy Lane", "Bishkek", "Kyrgyzstan");
        jdbcTemplate.update(insertStudent, "6", "alice", "Alice", "Johnson", "alice@example.com", "Biology", 2, 3.88, "2003-02-25", "+996-555-1006", "987 Education Dr", "Bishkek", "Kyrgyzstan");
        jdbcTemplate.update(insertStudent, "7", "max", "Max", "Williams", "max@example.com", "History", 1, 3.72, "2004-09-05", "+996-555-1007", "147 Scholar Way", "Bishkek", "Kyrgyzstan");

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
        
        // Handle new biographical fields (may be null)
        double gpa = rs.getDouble("gpa");
        student.setGpa(rs.wasNull() ? null : gpa);
        
        java.sql.Date birthDate = rs.getDate("birth_date");
        student.setBirthDate(birthDate != null ? birthDate.toLocalDate() : null);
        
        student.setPhone(rs.getString("phone"));
        student.setAddress(rs.getString("address"));
        student.setCity(rs.getString("city"));
        student.setCountry(rs.getString("country"));
        
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

    public void saveStudent(Student student, String username) {
        String sql = "INSERT INTO students (student_id, username, first_name, last_name, email, major, year, gpa, birth_date, phone, address, city, country) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, 
            student.getStudentId(), 
            username.toLowerCase(), 
            student.getFirstName(), 
            student.getLastName(), 
            student.getEmail(), 
            student.getMajor(), 
            student.getYear(),
            student.getGpa(),
            student.getBirthDate(),
            student.getPhone(),
            student.getAddress(),
            student.getCity(),
            student.getCountry()
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

