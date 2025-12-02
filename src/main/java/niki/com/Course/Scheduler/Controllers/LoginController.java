package niki.com.Course.Scheduler.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    // LoginController removed - Spring Security handles /login POST automatically
    // If user navigates to /login directly, they'll be redirected by Spring Security to /auth
}
