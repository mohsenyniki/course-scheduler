package niki.com.Course.Scheduler.Controllers;

import niki.com.Course.Scheduler.Data.UserRepository;
import niki.com.Course.Scheduler.Models.Student;
import niki.com.Course.Scheduler.Models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/auth")
    public String authPage(@RequestParam(required = false) String error, 
                          @RequestParam(required = false) String logout,
                          Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("success", "You have been logged out successfully.");
        }
        return "auth";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String major,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model
    ) {
        // Validation
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Username is required");
            return "auth";
        }

        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email is required");
            return "auth";
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            model.addAttribute("error", "Invalid email format");
            return "auth";
        }

        if (firstName == null || firstName.trim().isEmpty()) {
            model.addAttribute("error", "First name is required");
            return "auth";
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            model.addAttribute("error", "Last name is required");
            return "auth";
        }

        if (major == null || major.trim().isEmpty()) {
            model.addAttribute("error", "Major is required");
            return "auth";
        }

        if (password == null || password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters");
            return "auth";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "auth";
        }

        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Username already exists");
            return "auth";
        }

        // Create new student
        int newStudentId = userRepository.getNextStudentId();
        Student newStudent = new Student();
        newStudent.setStudentId(String.valueOf(newStudentId));
        newStudent.setFirstName(firstName.trim());
        newStudent.setLastName(lastName.trim());
        newStudent.setEmail(email.trim());
        newStudent.setMajor(major.trim());
        newStudent.setEnrolledCourses(new java.util.ArrayList<>());
        newStudent.setYear(1); // Default to freshman year
        // New biographical fields will be null initially
        newStudent.setGpa(null);
        newStudent.setBirthDate(null);
        newStudent.setPhone(null);
        newStudent.setAddress(null);
        newStudent.setCity(null);
        newStudent.setCountry(null);

        // Create new user
        User newUser = new User(
                username.trim(),
                passwordEncoder.encode(password),
                newStudentId,
                "ROLE_USER",
                true
        );

        // Save both
        userRepository.saveStudent(newStudent, username.trim());
        userRepository.saveUser(newUser);

        model.addAttribute("success", "Registration successful! Please sign in.");
        return "auth";
    }
}
