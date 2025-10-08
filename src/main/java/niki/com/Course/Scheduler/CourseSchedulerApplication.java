package niki.com.Course.Scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CourseSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CourseSchedulerApplication.class, args);
	}

	public String hello()
	{
		return "home";
	}

}
