package niki.com.Course.Scheduler.Security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import niki.com.Course.Scheduler.Services.EnrollmentService;

import java.io.IOException;

@Component
public class EnrollmentAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Autowired
    private EnrollmentService enrollmentService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        SavedRequest saved = requestCache.getRequest(request, response);
        if (saved != null) {
            String url = saved.getRedirectUrl();
            if (url != null && url.contains("/enroll/")) {
                int idx = url.lastIndexOf("/enroll/");
                if (idx >= 0) {
                    String after = url.substring(idx + "/enroll/".length());
                    int q = after.indexOf('?');
                    if (q >= 0) after = after.substring(0, q);
                    String courseId = after;
                    String username = authentication.getName();
                    // perform enrollment (no extra checks here; service will persist)
                    enrollmentService.enroll(username, courseId);
                }
            }
            // After processing enrollment, redirect to schedule
            response.sendRedirect(request.getContextPath() + "/schedule");
        } else {
            // Default: redirect to home page (mapped to root "/")
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}
