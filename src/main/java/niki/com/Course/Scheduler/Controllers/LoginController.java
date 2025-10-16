package niki.com.Course.Scheduler.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import niki.com.Course.Scheduler.Models.Course;
import niki.com.Course.Scheduler.Models.Student;
import niki.com.Course.Scheduler.Data.CourseDataRepo;

@Controller
public class LoginController {

    
    // This controller can be expanded in the future for login-related endpoints
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // renders login.html
    }
    

}
