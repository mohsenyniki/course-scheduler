package niki.com.Course.Scheduler.Controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import niki.com.Course.Scheduler.Data.CourseDataRepo;
import niki.com.Course.Scheduler.Models.Course;
import niki.com.Course.Scheduler.Services.EnrollmentService;

/**
 * Unit tests for the enrollment rules in {@link EnrollmentController}:
 * prerequisites, schedule-conflict detection, capacity limits, and dropping courses.
 *
 * The data layer is mocked with Mockito, so these tests run without a database.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EnrollmentControllerTest {

    private static final String STUDENT = "niki";
    private static final String REDIRECT_SCHEDULE = "redirect:/schedule";

    @Mock
    private EnrollmentService enrollmentService;

    @Mock
    private CourseDataRepo courseRepo;

    @InjectMocks
    private EnrollmentController controller;

    private final Principal principal = () -> STUDENT;
    private RedirectAttributesModelMap redirect;

    @BeforeEach
    void setUp() {
        redirect = new RedirectAttributesModelMap();
    }

    // ---------- helpers ----------

    private static Course course(String id, String schedule, int capacity, List<String> prereqs) {
        return new Course(id, id + " name", "Instructor", schedule, "Room 1",
                3, "Fall", "Bloomington", capacity, prereqs);
    }

    private void givenCourse(Course c) {
        when(courseRepo.findByCourseIdIgnoreCase(c.getCourseId())).thenReturn(List.of(c));
    }

    private void givenEnrolledIn(String... courseIds) {
        when(enrollmentService.getEnrolledCourses(STUDENT)).thenReturn(Set.of(courseIds));
    }

    private String enroll(String courseId) {
        return controller.enrollStudentInCourse(courseId, principal, new ExtendedModelMap(), redirect);
    }

    private String flashMessage() {
        return (String) redirect.getFlashAttributes().get("message");
    }

    // ---------- enrollment: happy path ----------

    @Test
    @DisplayName("Enrolls the student when there are no conflicts and seats are available")
    void enrollsWhenEverythingChecksOut() {
        givenCourse(course("CS101", "MWF 10:00 - 11:00", 3, List.of()));
        givenEnrolledIn();
        when(enrollmentService.countEnrolled("CS101")).thenReturn(0);

        String view = enroll("CS101");

        assertThat(view).isEqualTo(REDIRECT_SCHEDULE);
        verify(enrollmentService).enroll(STUDENT, "CS101");
        assertThat(flashMessage()).isEqualTo("Successfully enrolled in CS101!");
    }

    @Test
    @DisplayName("Redirects to login when the user is not authenticated")
    void redirectsToLoginWhenNotAuthenticated() {
        String view = controller.enrollStudentInCourse("CS101", null, new ExtendedModelMap(), redirect);

        assertThat(view).isEqualTo("redirect:/login");
        verifyNoInteractions(enrollmentService, courseRepo);
    }

    @Test
    @DisplayName("Rejects a course that does not exist")
    void rejectsUnknownCourse() {
        when(courseRepo.findByCourseIdIgnoreCase("NOPE999")).thenReturn(List.of());

        enroll("NOPE999");

        assertThat(flashMessage()).isEqualTo("Course not found: NOPE999");
        verify(enrollmentService, never()).enroll(anyString(), anyString());
    }

    // ---------- prerequisites ----------

    @Test
    @DisplayName("Rejects enrollment when prerequisites are missing and lists them")
    void rejectsMissingPrerequisites() {
        givenCourse(course("CS301", "MWF 08:00 - 09:00", 3, List.of("CS101", "MATH100")));
        givenEnrolledIn("CS101");
        givenCourse(course("CS101", "TTh 13:00-14:00", 3, List.of()));

        enroll("CS301");

        assertThat(flashMessage()).isEqualTo("Missing prerequisites: MATH100");
        verify(enrollmentService, never()).enroll(anyString(), anyString());
    }

    // ---------- schedule conflicts ----------

    @Nested
    @DisplayName("Schedule conflict detection")
    class ScheduleConflicts {

        @Test
        @DisplayName("Rejects a course whose time overlaps an enrolled course on the same days")
        void rejectsOverlappingTimes() {
            givenCourse(course("CS101", "MWF 10:00 - 11:00", 3, List.of()));
            givenCourse(course("ENG131", "MWF 10:30-11:30", 3, List.of()));
            givenEnrolledIn("ENG131");

            enroll("CS101");

            assertThat(flashMessage()).isEqualTo("Time conflict with ENG131 (MWF 10:30-11:30)");
            verify(enrollmentService, never()).enroll(anyString(), anyString());
        }

        @Test
        @DisplayName("Allows back-to-back classes (one ends exactly when the other starts)")
        void allowsBackToBackClasses() {
            givenCourse(course("CS101", "MWF 10:00 - 11:00", 3, List.of()));
            givenCourse(course("ENG131", "MWF 09:00 - 10:00", 3, List.of()));
            givenEnrolledIn("ENG131");

            enroll("CS101");

            verify(enrollmentService).enroll(STUDENT, "CS101");
        }

        @Test
        @DisplayName("Allows the same time slot on different days")
        void allowsSameTimeOnDifferentDays() {
            givenCourse(course("CS101", "MWF 10:00 - 11:00", 3, List.of()));
            givenCourse(course("MATH200", "TTh 10:00-11:00", 3, List.of()));
            givenEnrolledIn("MATH200");

            enroll("CS101");

            verify(enrollmentService).enroll(STUDENT, "CS101");
        }

        @Test
        @DisplayName("Treats Tuesday (T) and Thursday (Th) as different days")
        void distinguishesTuesdayFromThursday() {
            givenCourse(course("BUS201", "T 10:00-11:00", 3, List.of()));
            givenCourse(course("HIST250", "Th 10:00-11:00", 3, List.of()));
            givenEnrolledIn("HIST250");

            enroll("BUS201");

            verify(enrollmentService).enroll(STUDENT, "BUS201");
        }

        @Test
        @DisplayName("Detects a Thursday conflict inside a TTh schedule")
        void detectsThursdayConflictInTTh() {
            givenCourse(course("MATH200", "TTh 09:00-10:30", 3, List.of()));
            givenCourse(course("HIST250", "Th 10:00-11:00", 3, List.of()));
            givenEnrolledIn("HIST250");

            enroll("MATH200");

            assertThat(flashMessage()).startsWith("Time conflict with HIST250");
            verify(enrollmentService, never()).enroll(anyString(), anyString());
        }
    }

    // ---------- capacity ----------

    @Nested
    @DisplayName("Capacity limits")
    class Capacity {

        @Test
        @DisplayName("Rejects enrollment when the course is full")
        void rejectsWhenCourseIsFull() {
            givenCourse(course("CS101", "MWF 10:00 - 11:00", 3, List.of()));
            givenEnrolledIn();
            when(enrollmentService.isEnrolled(STUDENT, "CS101")).thenReturn(false);
            when(enrollmentService.countEnrolled("CS101")).thenReturn(3);

            enroll("CS101");

            assertThat(flashMessage()).isEqualTo("Course CS101 is full (capacity 3)");
            verify(enrollmentService, never()).enroll(anyString(), anyString());
        }

        @Test
        @DisplayName("Allows enrollment when one seat is left")
        void allowsLastSeat() {
            givenCourse(course("CS101", "MWF 10:00 - 11:00", 3, List.of()));
            givenEnrolledIn();
            when(enrollmentService.countEnrolled("CS101")).thenReturn(2);

            enroll("CS101");

            verify(enrollmentService).enroll(STUDENT, "CS101");
        }

        @Test
        @DisplayName("Treats capacity 0 as unlimited")
        void capacityZeroMeansUnlimited() {
            givenCourse(course("CS101", "MWF 10:00 - 11:00", 0, List.of()));
            givenEnrolledIn();
            when(enrollmentService.countEnrolled("CS101")).thenReturn(500);

            enroll("CS101");

            verify(enrollmentService).enroll(STUDENT, "CS101");
        }
    }

    // ---------- dropping ----------

    @Nested
    @DisplayName("Dropping courses")
    class Dropping {

        @Test
        @DisplayName("Drops a course the student is enrolled in")
        void dropsEnrolledCourse() {
            when(enrollmentService.isEnrolled(STUDENT, "CS101")).thenReturn(true);
            givenEnrolledIn();

            String view = controller.dropCourseForCurrentUser("CS101", principal, redirect);

            assertThat(view).isEqualTo(REDIRECT_SCHEDULE);
            verify(enrollmentService).drop(STUDENT, "CS101");
            assertThat(flashMessage()).isEqualTo("Dropped course CS101");
        }

        @Test
        @DisplayName("Does nothing when the student is not enrolled in the course")
        void doesNotDropWhenNotEnrolled() {
            when(enrollmentService.isEnrolled(STUDENT, "CS101")).thenReturn(false);
            givenEnrolledIn();

            controller.dropCourseForCurrentUser("CS101", principal, redirect);

            verify(enrollmentService, never()).drop(anyString(), anyString());
            assertThat(flashMessage()).isEqualTo("You are not enrolled in CS101");
        }
    }
}
