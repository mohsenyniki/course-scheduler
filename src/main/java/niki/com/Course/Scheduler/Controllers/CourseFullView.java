package niki.com.Course.Scheduler.Controllers;

import niki.com.Course.Scheduler.Data.CourseDataRepo;
import niki.com.Course.Scheduler.Models.Course;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// this controller will handle requests to view detailed course descriptions when a user clicks on a course from the search results
@Controller
@RequestMapping("/courses")

public class CourseFullView {
    private final CourseDataRepo repo;
    public CourseFullView(CourseDataRepo repo) {
        this.repo = repo;
    }

    @GetMapping("/{courseId}")
   public String getCourseFullInfo(@RequestParam("courseId") String courseId, Model model) {

        List<Course> courses = repo.findByCourseIdIgnoreCase(courseId);
        if(courses.isEmpty()) {
            model.addAttribute("error", "Course not found");
            return "error"; // 
        }
        Course course = courses.get(0); // Assuming courseId is unique, get the first match
        model.addAttribute("course", course);
        return "coursefullview"; 
   }

}

// In this class, we handle GET requests to /course with a courseId parameter and return courseDescription.html with the course details
// The course details are added to the model for Thymeleaf to render
// i still get this 404 eror when i click on a course from the search results
// whic means the mapping is not working properly
// i need to check if the courseId parameter is being passed correctly in the URL when clicking