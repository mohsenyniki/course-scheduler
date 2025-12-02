package niki.com.Course.Scheduler.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String username;
    private String password; // BCrypt encoded
    private Integer studentId; // Foreign key to Student
    private String role; // e.g., "ROLE_USER"
    private boolean enabled;
}
