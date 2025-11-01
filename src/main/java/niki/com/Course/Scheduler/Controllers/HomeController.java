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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;

@Controller
public class HomeController {
   // logger used to emit schedule debug information when requested
   private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HomeController.class);
 @Autowired
 private niki.com.Course.Scheduler.Services.EnrollmentService enrollmentService;

   @Autowired
   private CourseDataRepo courseRepo;

 @GetMapping("/")
 public String home(Model model, Principal principal) {
    if (principal != null) {
       String username = principal.getName();
       model.addAttribute("currentUser", username);
       // For now we don't have a student profile lookup implemented, use username as display name
       model.addAttribute("displayName", username);
    }
    return "home";
 }
    
   @GetMapping("/schedule")
   public String schedule(Model model, Principal principal) {
      if (principal == null) {
         // show empty schedule or prompt to login
         model.addAttribute("enrolledCourses", Collections.emptyList());
         model.addAttribute("timeSlots", Collections.emptyList());
        model.addAttribute("scheduleTable", Collections.emptyMap());
        model.addAttribute("days", List.of("Monday","Tuesday","Wednesday","Thursday","Friday"));
         return "schedule";
      }

      String studentId = principal.getName();

      // Convert enrolled IDs -> Course objects
      Set<String> enrolledIds = enrollmentService.getEnrolledCourses(studentId);
      List<Course> enrolledCourses = enrolledIds.stream()
            .map(id -> courseRepo.findByCourseIdIgnoreCase(id))
            .filter(list -> !list.isEmpty())
            .map(list -> list.get(0))
            .collect(Collectors.toList());

      // Build hourly time slots from 08:00 to 21:00 (last slot 21:00-22:00)
      List<String> timeSlots = new ArrayList<>();
      for (int hour = 8; hour < 22; hour++) {
         String start = String.format("%02d:00", hour);
         String end = String.format("%02d:00", hour + 1);
         timeSlots.add(start + " - " + end);
      }

      // Initialize schedule table: slot -> (dayName -> courseId)
      Map<String, Map<String, String>> scheduleTable = new LinkedHashMap<>();
      for (String slot : timeSlots) {
         Map<String, String> dayMap = new LinkedHashMap<>();
         dayMap.put("Monday", "");
         dayMap.put("Tuesday", "");
         dayMap.put("Wednesday", "");
         dayMap.put("Thursday", "");
         dayMap.put("Friday", "");
         scheduleTable.put(slot, dayMap);
      }

      // normalization helper for time tokens
      java.util.function.UnaryOperator<String> normSlot = s -> {
         if (s == null) return null;
         String t = s.trim();
         t = t.replaceAll("\\s*-\\s*", " - ");
         t = t.replaceAll("\\s+", " ");
         return t;
      };

      // day tokenizer that understands Th and Su
      java.util.function.Function<String, List<String>> tokenizeDays = s -> {
         List<String> out = new ArrayList<>();
         if (s == null) return out;
         String t = s.replaceAll("\\s+", "");
         int i = 0;
         while (i < t.length()) {
            char c = t.charAt(i);
            if (c == 'T') {
               if (i + 1 < t.length() && t.charAt(i + 1) == 'h') { out.add("Th"); i += 2; continue; }
               out.add("T"); i++; continue;
            }
            if (c == 'S') {
               if (i + 1 < t.length() && t.charAt(i + 1) == 'u') { out.add("Su"); i += 2; continue; }
               out.add("S"); i++; continue;
            }
            out.add(String.valueOf(c)); i++;
         }
         return out;
      };

      Map<String, String> dayNameMap = Map.of(
            "M", "Monday",
            "T", "Tuesday",
            "W", "Wednesday",
            "Th", "Thursday",
            "F", "Friday",
            "S", "Saturday",
            "Su", "Sunday"
      );

   // Prepare a debug map so the template can display per-course matching info
   Map<String, String> scheduleDebug = new LinkedHashMap<>();

   // Fill schedule table by matching course start hour to the slot
      for (Course c : enrolledCourses) {
         String schedule = c.getSchedule();
         if (schedule == null) continue;
         String[] parts = schedule.trim().split("\\s+", 2);
         if (parts.length < 2) continue;
         String daysToken = parts[0];
         String timesToken = normSlot.apply(parts[1]);

         String[] timeParts = timesToken.split("-");
         if (timeParts.length < 2) continue;
         String startTimeStr = timeParts[0].replaceAll("\\s+", "");
         String[] hm = startTimeStr.split(":");
         if (hm.length < 1) continue;
         int startHour;
         try {
            startHour = Integer.parseInt(hm[0]);
         } catch (NumberFormatException ex) {
            continue;
         }
         String paddedStart = String.format("%02d:%02d", startHour, hm.length > 1 ? Integer.parseInt(hm[1]) : 0);
         String startSlotStr = paddedStart + " - " + String.format("%02d:00", (startHour + 1));

         String matchedSlot = null;
         if (scheduleTable.containsKey(startSlotStr)) {
            matchedSlot = startSlotStr;
         } else {
            // fallback: match by hour (handles missing leading zeros)
            String hourPrefix = String.format("%02d:", startHour);
            for (String slot : timeSlots) {
               if (slot.startsWith(hourPrefix)) { matchedSlot = slot; break; }
            }
         }
         if (matchedSlot == null) continue;

         List<String> tokens = tokenizeDays.apply(daysToken);
         // record debug info and log mapping
         String tokenStr = String.join(",", tokens);
         logger.info("Schedule mapping for {} -> schedule='{}' matchedSlot='{}' tokens='{}'", c.getCourseId(), schedule, matchedSlot, tokenStr);
         scheduleDebug.put(c.getCourseId(), "schedule='" + schedule + "' matchedSlot='" + matchedSlot + "' tokens='[" + tokenStr + "]'");
         for (String tok : tokens) {
            String dayName = dayNameMap.get(tok);
            if (dayName != null) {
               // store a friendly label (id + name) in the table cell so the UI shows course name
               String label = c.getCourseId();
               if (c.getCourseName() != null && !c.getCourseName().isEmpty()) label += " - " + c.getCourseName();
               scheduleTable.get(matchedSlot).put(dayName, label);
            }
         }
      }

      model.addAttribute("enrolledCourses", enrolledCourses);
      model.addAttribute("timeSlots", timeSlots);
      model.addAttribute("days", List.of("Monday","Tuesday","Wednesday","Thursday","Friday"));
      model.addAttribute("scheduleTable", scheduleTable);
      // provide per-course debug info to the template (optional display)
      model.addAttribute("scheduleDebug", scheduleDebug);
      return "schedule"; // renders schedule.html
   }
}