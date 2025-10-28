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
  prerequisites VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS enrollment (
  student_username VARCHAR(255) NOT NULL,
  course_course_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (student_username, course_course_id)
);
