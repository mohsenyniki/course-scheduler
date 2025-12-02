package niki.com.Course.Scheduler.Data;

import niki.com.Course.Scheduler.Models.Student;
import niki.com.Course.Scheduler.Models.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository {
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Student> students = new HashMap<>();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private int nextStudentId = 8; // Start after default students

    public UserRepository() {
        // Initialize with default users and students
        initializeDefaultData();
    }

    private void initializeDefaultData() {
        // Create default students
        students.put("zahra", new Student("1", "Zahra", "Hosseini", "zahra@example.com", "Computer Science", new ArrayList<>(), 2));
        students.put("parisa", new Student("2", "Parisa", "Rezaei", "parisa@example.com", "Mathematics", new ArrayList<>(), 3));
        students.put("ali", new Student("3", "Ali", "Moradi", "ali@example.com", "Physics", new ArrayList<>(), 1));
        students.put("armin", new Student("4", "Armin", "Karimi", "armin@example.com", "Engineering", new ArrayList<>(), 2));
        students.put("ahmad", new Student("5", "Ahmad", "Ahmadi", "ahmad@example.com", "Chemistry", new ArrayList<>(), 4));
        students.put("alice", new Student("6", "Alice", "Johnson", "alice@example.com", "Biology", new ArrayList<>(), 2));
        students.put("max", new Student("7", "Max", "Williams", "max@example.com", "History", new ArrayList<>(), 1));

        // Create corresponding users with BCrypt encoded passwords (all use "password")
        users.put("zahra", new User("zahra", passwordEncoder.encode("password"), 1, "ROLE_USER", true));
        users.put("parisa", new User("parisa", passwordEncoder.encode("password"), 2, "ROLE_USER", true));
        users.put("ali", new User("ali", passwordEncoder.encode("password"), 3, "ROLE_USER", true));
        users.put("armin", new User("armin", passwordEncoder.encode("password"), 4, "ROLE_USER", true));
        users.put("ahmad", new User("ahmad", passwordEncoder.encode("password"), 5, "ROLE_USER", true));
        users.put("alice", new User("alice", passwordEncoder.encode("password"), 6, "ROLE_USER", true));
        users.put("max", new User("max", passwordEncoder.encode("password"), 7, "ROLE_USER", true));
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username.toLowerCase()));
    }

    public Optional<Student> findStudentByUsername(String username) {
        return Optional.ofNullable(students.get(username.toLowerCase()));
    }

    public void saveUser(User user) {
        users.put(user.getUsername().toLowerCase(), user);
    }

    public void saveStudent(Student student) {
        students.put(student.getEmail().split("@")[0].toLowerCase(), student);
    }

    public boolean existsByUsername(String username) {
        return users.containsKey(username.toLowerCase());
    }

    public int getNextStudentId() {
        return nextStudentId++;
    }
}

