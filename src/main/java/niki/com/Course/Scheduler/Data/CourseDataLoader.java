package niki.com.Course.Scheduler.Data;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import niki.com.Course.Scheduler.Models.Course;

@Component
public class CourseDataLoader implements CommandLineRunner {

    private final CourseDataRepo courseRepo;

    public CourseDataLoader(CourseDataRepo courseRepo) {
        this.courseRepo = courseRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        if (courseRepo.count() == 0) {
            List<Course> samples = List.of(
                // CS201: no prereqs
                new Course("CS101", "Intro to CS", "Dr. Smith", "MWF 10:00-11:00", "Room 12", 3, "Fall", "Bloomington", List.of()),

                new Course("CS301", "Intro to CS", "Dr. Smith", "MWF 10:00-11:00", "Room 12", 3, "Fall", "Bloomington", List.of("CS201")),
                // MATH200: no prereqs
                new Course("MATH200", "Calculus II", "Dr. Jones", "TTh 09:00-10:30", "Room 3", 4, "Fall", "Bloomington", List.of("MATH100")),
                new Course("HIST250", "World History", "Prof. Allen", "MW 14:00-15:30", "Room 7", 3, "Spring", "Indianapolis",List.of("HIST100")),
                // CSCI311 requires CS201
                new Course("CSCI311", "Programming Languages", "Prof. Allen", "MW 14:00-15:30", "Room 7", 3, "Spring", "Indianapolis",List.of("CS101")),
                // INFO301 requires CS201
                new Course("INFO301", "Informatics fundamentals", "Prof. Bob", "MW 14:00-15:30", "Room 7", 3, "Summer", "Kokomo",List.of("INFO201")),
                new Course("BUS201", "Intro to Business", "Prof. Alex", "MW 14:00-15:30", "Room 7", 3, "Summer", "Kokomo",List.of()),
                new Course("ENG131", "Intensive Writing", "Prof. Ali", "MW 14:00-15:30", "Room 7", 3, "Fall", "Indianapolis",List.of()),
                // BIO300 requires ENG131
                new Course("BIO300", "Theory of Evolution", "Prof. Ahmad", "MW 14:00-15:30", "Room 7", 3, "Fall", "Bloomington",List.of("ENG131"))

            );
            courseRepo.saveAll(samples); 
        }
    }
}