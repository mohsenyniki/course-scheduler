package niki.com.Course.Scheduler.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller 
public class HomeController {
 @GetMapping("/") 

 public String home() {
 
    return "home"; 
 }
 
 @GetMapping("/schedule")
 public String schedule() {
    return "schedule"; // renders schedule.html
 }
}