package niki.com.Course.Scheduler;

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
}

