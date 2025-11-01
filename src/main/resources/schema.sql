-- Manual schema for Course Scheduler (SQLite)
CREATE TABLE IF NOT EXISTS course (
  course_id VARCHAR(255) PRIMARY KEY,
  course_name VARCHAR(255),
  credits INTEGER NOT NULL,
  instructor VARCHAR(255),
  location VARCHAR(255),
  schedule VARCHAR(255),
  semester VARCHAR(255),
  campus_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS course_prerequisites (
  course_course_id VARCHAR(255) NOT NULL,
  prerequisites VARCHAR(255),
  FOREIGN KEY(course_course_id) REFERENCES course(course_id),
  FOREIGN KEY(prerequisites) REFERENCES course(course_id)
);

CREATE TABLE IF NOT EXISTS enrollment (
  student_username VARCHAR(255) NOT NULL,
  course_course_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (student_username, course_course_id),
  FOREIGN KEY(student_username) REFERENCES student(username),
  FOREIGN KEY(course_course_id) REFERENCES course(course_id)
);

-- Optional student directory table. The app currently stores enrollments by username
-- so we keep username as the primary key to match enrollment.student_username.
CREATE TABLE IF NOT EXISTS student (
  username VARCHAR(255) PRIMARY KEY,
  full_name VARCHAR(255),
  email VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
