package niki.com.Course.Scheduler.Controllers;
import java.util.*;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import niki.com.Course.Scheduler.Data.CourseDataRepo;
import niki.com.Course.Scheduler.Models.Course;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/enroll")
public class EnrollmentController {

    @Autowired
    private niki.com.Course.Scheduler.Services.EnrollmentService enrollmentService;

    @Autowired
    private CourseDataRepo courseRepo;

    /**
     * Enroll a student in a course.
     * Example: POST /enroll/123/CS101
     */

    @GetMapping("/{studentId}/{courseId}")  // defining the endpoint
    public String enrollStudentInCourse(
            @PathVariable String studentId,     // extracting values from the URL
            @PathVariable String courseId , 
            Model model) {
        // legacy/testing endpoint: enroll a specific studentId
        Set<String> enrolledCourses = new HashSet<>(enrollmentService.getEnrolledCourses(studentId));

        // check prerequisites
        List<Course> found = courseRepo.findByCourseIdIgnoreCase(courseId);
        if (found.isEmpty()) {
            model.addAttribute("message", "Course not found: " + courseId);
            model.addAttribute("courses", enrolledCourses);
            return "schedule";
        }
        Course course = found.get(0);
        List<String> prereqs = course.getPrerequisites();
        if (prereqs != null && !prereqs.isEmpty()) {
            // check whether student has all prerequisites
            if (!enrolledCourses.containsAll(prereqs)) {
                // compute missing prereqs
                List<String> missing = new ArrayList<>();
                for (String p : prereqs) {
                    if (!enrolledCourses.contains(p)) missing.add(p);
                }
                model.addAttribute("message", "Missing prerequisites: " + String.join(", ", missing));
                model.addAttribute("courses", enrolledCourses);
                return "schedule";
            }
        }

       // constraint 1: student cannot enroll twice
        if (enrollmentService.isEnrolled(studentId, courseId)) {
            model.addAttribute("message", "You are already enrolled in this course!");
        } else {
            enrollmentService.enroll(studentId, courseId);
            enrolledCourses = new HashSet<>(enrollmentService.getEnrolledCourses(studentId));
            model.addAttribute("message", "Successfully enrolled in " + courseId + "!");
        }
        


        // constraint 3: if the student has time conflict with another course they are enrolled in
        // Store current student's courses to show on schedule page
        model.addAttribute("courses", enrolledCourses);

        // Redirect to schedule page
        return "schedule"; // corresponds to schedule.html in templates folder
    }
    
    @PostMapping("/{courseId}")
    public String enrollStudentInCourse(
            @PathVariable String courseId,
            Principal principal,
            Model model,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        // Use authenticated username from Principal 
        if (principal == null) {
            return "redirect:/login"; // should be redirected to login if not authenticated
        }

        String studentId = principal.getName();

        // CONSTRAINT 2: the student can enroll only if they have the prerequisites
        // check prerequisites for POST flow as well
        List<Course> foundPost = courseRepo.findByCourseIdIgnoreCase(courseId);
        if (foundPost.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Course not found: " + courseId);
            return "redirect:/schedule";
        }
        Course coursePost = foundPost.get(0);
        List<String> prereqsPost = coursePost.getPrerequisites();
        Set<String> currentEnrolled = enrollmentService.getEnrolledCourses(studentId);
        if (prereqsPost != null && !prereqsPost.isEmpty()) {
            if (!currentEnrolled.containsAll(prereqsPost)) {
                List<String> missing = new ArrayList<>();
                for (String p : prereqsPost) {
                    if (!currentEnrolled.contains(p)) missing.add(p);
                }
                redirectAttributes.addFlashAttribute("message", "Missing prerequisites: " + String.join(", ", missing));
                redirectAttributes.addFlashAttribute("courses", currentEnrolled);
                return "redirect:/schedule";
            }
        }

        if (!enrollmentService.isEnrolled(studentId, courseId)) {
            enrollmentService.enroll(studentId, courseId);
        }

        Set<String> enrolledCourses = new HashSet<>(enrollmentService.getEnrolledCourses(studentId));

        // Add flash attributes so they survive the redirect
        redirectAttributes.addFlashAttribute("courses", enrolledCourses);
        redirectAttributes.addFlashAttribute("currentCourse", courseId);

        // Redirect to the schedule page
        return "redirect:/schedule";
    }
}

// so now if the enrollement is successful - > i should be redirected to a new page that is now just a simple
// page of enrolled classes so far and later i will add schedule to it.
