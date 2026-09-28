# Course Scheduler: Student Portal & Course Enrollment System

[![CI](https://github.com/mohsenyniki/course-scheduler/actions/workflows/ci.yml/badge.svg)](https://github.com/mohsenyniki/course-scheduler/actions/workflows/ci.yml)

A full-stack student portal built with **Java 17** and **Spring Boot 3**. Students can search for courses, enroll and drop, and see their classes on a weekly schedule. The app blocks enrollments that would cause a time conflict or go over a course's capacity.

## Features

- **Authentication:** registration and login with **Spring Security**, with passwords hashed using BCrypt
- **Course search:** search and filter the course catalog, and open a full detail view for any course (instructor, schedule, location, credits, capacity)
- **Enrollment:** enroll in and drop courses, with server-side checks for:
  - **schedule conflicts:** rejects a course that overlaps a class you're already enrolled in
  - **capacity:** rejects enrollment once a course is full
- **Weekly schedule:** your enrolled courses laid out on a weekly calendar view
- **Student profile:** a profile page for each logged-in student
- **Admin tools:** an admin page for resetting enrollment data

## Tech stack

| Layer | Tools |
|---|---|
| Backend | Java 17, Spring Boot 3, Spring MVC, Spring Security |
| Data | SQLite, Spring JDBC (`JdbcTemplate`), SQL schema |
| Frontend | Thymeleaf, HTML, CSS |
| Testing | JUnit 5, Mockito, AssertJ, GitHub Actions (CI) |
| Build & deploy | Maven, Docker (multi-stage build), deployed on Render |

## Architecture

```
Controllers   →  Services           →  Data repositories  →  SQLite
(Course, Enrollment, Profile,   (EnrollmentService)   (Course / Enrollment /
 Login, Registration, Admin)                           Student / User repos)
```

- **Controllers** handle routing and view rendering (Thymeleaf templates)
- **EnrollmentService** holds the enrollment business logic
- **Repositories** use `JdbcTemplate` against a SQLite database with tables for courses, prerequisites, enrollments, students and users

## Running locally

**Requirements:** Java 17+ (Maven wrapper included)

```bash
./mvnw spring-boot:run
```

Then open http://localhost:8080.

### With Docker

```bash
docker build -t course-scheduler .
docker run -p 8080:8080 course-scheduler
```

## Tests

The enrollment rules are covered by **JUnit 5** unit tests, using **Mockito** to mock the data layer so they run fast and need no database:

- **Schedule conflicts:** overlapping times are rejected. Back-to-back classes and the same time slot on different days are allowed, and Tuesday (`T`) and Thursday (`Th`) are handled correctly.
- **Capacity:** full courses are rejected, the last seat can be taken, and capacity `0` means unlimited.
- **Prerequisites:** missing prerequisites are rejected and listed by name.
- **Edge cases:** unknown courses, duplicate enrollment, unauthenticated users, dropping courses.

Run them locally:

```bash
./mvnw test
```

Tests also run automatically on every push through **GitHub Actions** (see the CI badge above).

## Author

**Nikbakht (Niki) Mohseny** · [LinkedIn](https://www.linkedin.com/in/nikbakht-mohseny-97b2a8295) · [GitHub](https://github.com/mohsenyniki)
