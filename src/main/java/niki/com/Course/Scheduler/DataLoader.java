package niki.com.Course.Scheduler;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataLoader implements CommandLineRunner {

    private final CourseRepository repo;

    public DataLoader(CourseRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (repo.count() == 0) {
            List<Course> samples = List.of(
                new Course("CS101", "Intro to CS", "Dr. Smith", "MWF 10:00-11:00", "Room 12", 3, List.of()),
                new Course("MATH200", "Calculus II", "Dr. Jones", "TTh 09:00-10:30", "Room 3", 4, List.of("MATH101")),
                new Course("HIST150", "World History", "Prof. Allen", "MW 14:00-15:30", "Room 7", 3, List.of())
            );
            repo.saveAll(samples);
        }
    }
}