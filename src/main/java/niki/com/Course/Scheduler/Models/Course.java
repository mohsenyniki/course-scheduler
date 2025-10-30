package niki.com.Course.Scheduler.Models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Course {

    private String courseId;
    private String courseName;
    private String instructor;
    private String schedule;
    private String location;
    private int credits;
    private String semester;
    private String campusId;

    private List<String> prerequisites;
    // course description
    // 

    /**
     * Returns the start time portion extracted from the schedule string.
     * Example: schedule = "MWF 10:00-11:00" -> returns "10:00"
     */
    public String getStartTime() {
        if (this.schedule == null) return "";
        String[] parts = this.schedule.trim().split(" ");
        if (parts.length == 0) return "";
        String times = parts[parts.length - 1];
        if (!times.contains("-")) return "";
        String[] t = times.split("-");
        return t.length > 0 ? t[0] : "";
    }

    /**
     * Returns the end time portion extracted from the schedule string.
     * Example: schedule = "MWF 10:00-11:00" -> returns "11:00"
     */
    public String getEndTime() {
        if (this.schedule == null) return "";
        String[] parts = this.schedule.trim().split(" ");
        if (parts.length == 0) return "";
        String times = parts[parts.length - 1];
        if (!times.contains("-")) return "";
        String[] t = times.split("-");
        return t.length > 1 ? t[1] : "";
    }
}

