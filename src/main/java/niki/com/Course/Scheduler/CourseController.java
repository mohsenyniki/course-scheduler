package niki.com.Course.Scheduler;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CourseController {

    private final CourseDao repo;

    public CourseController(CourseDao repo) {
        this.repo = repo;
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "query", required = false) String query, Model model) {
        List<Course> results = List.of();
        if (query != null && !query.isBlank()) {
            results = repo.findByCourseIdIgnoreCase(query.trim());
            if (results.isEmpty()) {
                results = repo.findByCourseNameContainingIgnoreCase(query.trim());
            }
        }
        model.addAttribute("results", results);
        model.addAttribute("query", query);
        return "search";
    }
}
// in this class we simply handle the get request to /search and return search.html with the results(found courses)

