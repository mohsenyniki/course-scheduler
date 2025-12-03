-- Fix missing student record for niki03
INSERT OR REPLACE INTO students (student_id, username, first_name, last_name, email, major, year, gpa, birth_date, phone, address, city, country) 
VALUES ('100', 'niki03', 'Niki', 'User', 'niki03@example.com', 'Computer Science', 2, 3.75, '2003-01-01', '+996-555-1100', '100 Student Ave', 'Bishkek', 'Kyrgyzstan');
