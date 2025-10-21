package niki.com.Course.Scheduler.Controllers;
import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/enroll")
public class EnrollmentController {

    // plan:
    // creating a temp database holding student ids and the list of courses they are enrolled in
    // than fetching the student id and the chosen course through the given URL , using @pathvariable
    // than we checking the simplest constraint: student can not enroll in the same course twice

    private Map<String, Set<String>> studentEnrollments = new HashMap<>();

    /**
     * Enroll a student in a course.
     * Example: POST /enroll/123/CS101
     */

    @GetMapping("/{studentId}/{courseId}")  // defining the endpoint
    public String enrollStudentInCourse(
            @PathVariable String studentId,     // extracting values from the URL
            @PathVariable String courseId , 
            Model model) {

        studentEnrollments.putIfAbsent(studentId, new HashSet<>());
        Set<String> enrolledCourses = studentEnrollments.get(studentId);

       // constraint: student cannot enroll twice
        if (enrolledCourses.contains(courseId)) {
            model.addAttribute("message", "You are already enrolled in this course!");
        } else {
            enrolledCourses.add(courseId);
            model.addAttribute("message", "Successfully enrolled in " + courseId + "!");
        }

        // Store current student's courses to show on schedule page
        model.addAttribute("courses", enrolledCourses);

        // Redirect to schedule page
        return "schedule"; // corresponds to schedule.html in templates folder
    }
    
    @PostMapping("/{courseId}")
    public String enrollStudentInCourse(
            @PathVariable String courseId,
            Model model) {

            // Assuming a fixed student ID for now (can be replaced with authenticated user ID)
            String studentId = "123";

            studentEnrollments.putIfAbsent(studentId, new HashSet<>());
            Set<String> enrolledCourses = studentEnrollments.get(studentId);

            // Add the course to the student's enrolled courses
            if (!enrolledCourses.contains(courseId)) {
                enrolledCourses.add(courseId);
            }

            // Pass the enrolled courses and the current course to the model
            model.addAttribute("courses", enrolledCourses);
            model.addAttribute("currentCourse", courseId);

            // Redirect to the schedule page
            return "schedule";
        }
}

// so now if the enrollement is successful - > i should be redirected to a new page that is now just a simple
// page of enrolled classes so far and later i will add schedule to it.
