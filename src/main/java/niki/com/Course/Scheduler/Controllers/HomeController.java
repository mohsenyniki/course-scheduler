package niki.com.Course.Scheduler.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import niki.com.Course.Scheduler.Data.CourseDataRepo;
import niki.com.Course.Scheduler.Models.Course;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Set;

@Controller
public class HomeController {
 @Autowired
 private niki.com.Course.Scheduler.Services.EnrollmentService enrollmentService;

   @Autowired
   private CourseDataRepo courseRepo;

 @GetMapping("/")

 public String home() {

    return "home";
 }
 
   @GetMapping("/schedule")
   public String schedule(Model model, Principal principal) {
   if (principal != null) {
      String studentId = principal.getName();
      Set<String> enrolledIds = enrollmentService.getEnrolledCourses(studentId);
      List<Course> enrolledCourses = enrolledIds.stream()
         .map(id -> courseRepo.findByCourseIdIgnoreCase(id))
         .filter(list -> !list.isEmpty())
         .map(list -> list.get(0))
         .collect(Collectors.toList());
      model.addAttribute("enrolledCourses", enrolledCourses);
   }
   return "schedule"; // renders schedule.html
 }
}