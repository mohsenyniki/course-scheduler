package niki.com.Course.Scheduler.Models;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Student {
   
    private String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String major;
    private List<String> enrolledCourses; // List of course IDs the student is enrolled in
    private int year; // e.g., 1 for freshman, 2 for sophomore, etc.
    
    // New biographical fields
    private Double gpa;
    private LocalDate birthDate;
    private String phone;
    private String address;
    private String city;
    private String country;
}
