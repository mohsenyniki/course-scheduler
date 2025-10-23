package niki.com.Course.Scheduler.Controllers;
import java.util.*;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import niki.com.Course.Scheduler.Data.CourseDataRepo;
import niki.com.Course.Scheduler.Models.Course;
import java.time.LocalTime;
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

        // check time conflicts
        String targetSchedule = course.getSchedule();
        String targetDays = extractDays(targetSchedule);
        LocalTime targetStart = parseStart(targetSchedule);
        LocalTime targetEnd = parseEnd(targetSchedule);
        for (String enrolledId : enrolledCourses) {
            List<Course> enrolledFound = courseRepo.findByCourseIdIgnoreCase(enrolledId);
            if (enrolledFound.isEmpty()) continue;
            Course e = enrolledFound.get(0);
            String eSchedule = e.getSchedule();
            String eDays = extractDays(eSchedule);
            LocalTime eStart = parseStart(eSchedule);
            LocalTime eEnd = parseEnd(eSchedule);
            if (daysOverlap(targetDays, eDays) && timesOverlap(targetStart, targetEnd, eStart, eEnd)) {
                model.addAttribute("message", "Time conflict with " + e.getCourseId() + " (" + eSchedule + ")");
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

        // constraint 3: if the student has time conflict with another course they are enrolled in
        // check time conflicts for POST flow
        String targetSchedulePost = coursePost.getSchedule();
        String targetDaysPost = extractDays(targetSchedulePost);
        LocalTime targetStartPost = parseStart(targetSchedulePost);
        LocalTime targetEndPost = parseEnd(targetSchedulePost);
        for (String enrolledId : currentEnrolled) {
            List<Course> enrolledFound = courseRepo.findByCourseIdIgnoreCase(enrolledId);
            if (enrolledFound.isEmpty()) continue;
            Course e = enrolledFound.get(0);
            String eSchedule = e.getSchedule();
            String eDays = extractDays(eSchedule);
            LocalTime eStart = parseStart(eSchedule);
            LocalTime eEnd = parseEnd(eSchedule);
            if (daysOverlap(targetDaysPost, eDays) && timesOverlap(targetStartPost, targetEndPost, eStart, eEnd)) {
                redirectAttributes.addFlashAttribute("message", "Time conflict with " + e.getCourseId() + " (" + eSchedule + ")");
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
        redirectAttributes.addFlashAttribute("message", "Successfully enrolled in " + courseId + "!");

        // Redirect to the schedule page
        return "redirect:/schedule";
    }

    // Helpers for schedule parsing and overlap detection
    private String extractDays(String schedule) {
        if (schedule == null) return "";
        String norm = normalizeSchedule(schedule);
        String[] parts = norm.trim().split(" ");
        if (parts.length < 2) return "";
        // assume last part is times, earlier parts (first) are days token(s)
        // e.g. "MWF 10:00-11:00" -> parts[0] = "MWF"
        return parts[0];
    }

    private LocalTime parseStart(String schedule) {
    if (schedule == null) return null;
    String norm = normalizeSchedule(schedule);
    // find patterns like 10, 10:00, 09:30 (dash has been normalized)
    java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{1,2}(?::\\d{2})?)-(\\d{1,2}(?::\\d{2})?)").matcher(norm);
        if (!m.find()) return null;
        String s = m.group(1);
        if (!s.contains(":")) s = s + ":00";
        try {
            return LocalTime.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime parseEnd(String schedule) {
    if (schedule == null) return null;
    String norm = normalizeSchedule(schedule);
    java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{1,2}(?::\\d{2})?)-(\\d{1,2}(?::\\d{2})?)").matcher(norm);
        if (!m.find()) return null;
        String s = m.group(2);
        if (!s.contains(":")) s = s + ":00";
        try {
            return LocalTime.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean daysOverlap(String a, String b) {
        if (a == null || b == null) return false;
    Set<String> sa = tokenizeDays(normalizeDays(a));
    Set<String> sb = tokenizeDays(normalizeDays(b));
        for (String t : sa) {
            if (sb.contains(t)) return true;
        }
        return false;
    }

    // Tokenize day string into tokens like M, T, W, Th, F, S, Su
    private Set<String> tokenizeDays(String s) {
        Set<String> out = new HashSet<>();
        if (s == null) return out;
        String t = s.trim();
        int i = 0;
        while (i < t.length()) {
            char c = t.charAt(i);
            if (c == 'T') {
                // check for 'Th'
                if (i + 1 < t.length() && t.charAt(i + 1) == 'h') {
                    out.add("Th");
                    i += 2;
                    continue;
                } else {
                    out.add("T");
                    i++;
                    continue;
                }
            }
            // handle two-letter Sunday 'Su' (if used)
            if (c == 'S') {
                if (i + 1 < t.length() && t.charAt(i + 1) == 'u') {
                    out.add("Su");
                    i += 2;
                    continue;
                } else {
                    out.add("S");
                    i++;
                    continue;
                }
            }
            // single-letter tokens M, W, F, etc.
            out.add(String.valueOf(c));
            i++;
        }
        return out;
    }

    // Normalize schedule by collapsing spaces and removing spaces around dash: "10:00 - 11:00" -> "10:00-11:00"
    private String normalizeSchedule(String s) {
        if (s == null) return null;
        String t = s.trim();
        // collapse multiple spaces
        t = t.replaceAll("\\s+", " ");
        // remove spaces around dash
        t = t.replaceAll("\\s*-\\s*", "-");
        return t;
    }

    // Normalize days token string by collapsing spaces (used before tokenization)
    private String normalizeDays(String s) {
        if (s == null) return null;
        return s.replaceAll("\\s+", "");
    }

    private boolean timesOverlap(LocalTime s1, LocalTime e1, LocalTime s2, LocalTime e2) {
        if (s1 == null || e1 == null || s2 == null || e2 == null) return false;
        // overlap exists if start1 < end2 AND start2 < end1
        return s1.isBefore(e2) && s2.isBefore(e1);
    }
}

// so now if the enrollement is successful - > i should be redirected to a new page that is now just a simple
// page of enrolled classes so far and later i will add schedule to it.
