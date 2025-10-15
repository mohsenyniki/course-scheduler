package niki.com.Course.Scheduler;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CourseController {

    private final CourseDataRepo repo;

    public CourseController(CourseDataRepo repo) {
        this.repo = repo;
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "campus", required = false) String campus,
            @RequestParam(name = "semester", required = false) String semester,
            Model model) {

        // Check if all inputs are empty
        if ((query == null || query.isBlank()) &&
            (campus == null || campus.isBlank()) &&
            (semester == null || semester.isBlank())) {
            model.addAttribute("error", "Please enter a search term or select a filter.");
            return "home"; // go back to homepage with error message
        }

        // Perform database search with dynamic filtering
        List<Course> results = repo.findCourses(query, campus, semester);

        // Add data to the model for Thymeleaf to render
        model.addAttribute("results", results);
        model.addAttribute("query", query);
        model.addAttribute("campus", campus);
        model.addAttribute("semester", semester);

        return "home"; // renders search.html with the results
    }
}
// in this class we simply handle the get request to /search and return search.html with the results(found courses)

