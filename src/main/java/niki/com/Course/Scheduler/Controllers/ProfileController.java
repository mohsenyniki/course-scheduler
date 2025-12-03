package niki.com.Course.Scheduler.Controllers;

import niki.com.Course.Scheduler.Data.UserRepository;
import niki.com.Course.Scheduler.Models.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
public class ProfileController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private final UserRepository userRepository;
    
    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        logger.info("=== PROFILE ENDPOINT HIT ===");
        
        if (principal == null) {
            logger.warn("Principal is null, redirecting to /auth");
            return "redirect:/auth";
        }
        
        String username = principal.getName();
        logger.info("User '{}' accessing profile page", username);
        model.addAttribute("currentUser", username);
        
        // Get student details
        Optional<Student> studentOpt = userRepository.findStudentByUsername(username);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            logger.info("Found student record for '{}'", username);
            model.addAttribute("student", student);
            
            // Calculate year label
            String yearLabel = getYearLabel(student.getYear());
            model.addAttribute("yearLabel", yearLabel);
        } else {
            logger.error("No student record found for '{}', redirecting to home", username);
            return "redirect:/";
        }
        
        logger.info("Rendering profile.html for user '{}'", username);
        return "profile";
    }
    
    private String getYearLabel(int year) {
        return switch (year) {
            case 1 -> "Freshman";
            case 2 -> "Sophomore";
            case 3 -> "Junior";
            case 4 -> "Senior";
            default -> "Year " + year;
        };
    }
}
