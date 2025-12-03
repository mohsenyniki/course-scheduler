package niki.com.Course.Scheduler.Controllers;

import niki.com.Course.Scheduler.Services.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final EnrollmentService enrollmentService;

    @Autowired
    public AdminController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/admin/reset")
    public String resetPage() {
        return "admin-reset";
    }

    @PostMapping("/admin/clear-enrollments")
    public String clearAllEnrollments() {
        enrollmentService.clearAllEnrollments();
        return "redirect:/admin/reset?success=true";
    }
}
