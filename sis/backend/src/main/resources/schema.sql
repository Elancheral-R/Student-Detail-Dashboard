-- ============================================================
--  Student Information System (SIS) — MySQL Schema
--  Run this script BEFORE starting the Spring Boot app.
-- ============================================================

CREATE DATABASE IF NOT EXISTS sis_db;
USE sis_db;

-- ─────────────────────────────────────────
-- 1. USERS  (authentication + roles)
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,          -- BCrypt hash
    email       VARCHAR(100) NOT NULL UNIQUE,
    full_name   VARCHAR(100) NOT NULL,
    role        ENUM('ADMIN','FACULTY','STUDENT') NOT NULL DEFAULT 'STUDENT',
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────
-- 2. DEPARTMENTS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS departments (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(100) NOT NULL UNIQUE,
    code  VARCHAR(10)  NOT NULL UNIQUE
);

-- ─────────────────────────────────────────
-- 3. STUDENTS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS students (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id      VARCHAR(20)  NOT NULL UNIQUE,   -- e.g. "STU2024001"
    user_id         BIGINT       REFERENCES users(id),
    first_name      VARCHAR(50)  NOT NULL,
    last_name       VARCHAR(50)  NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    phone           VARCHAR(20),
    dob             DATE,
    gender          ENUM('MALE','FEMALE','OTHER'),
    address         TEXT,
    department_id   BIGINT REFERENCES departments(id),
    year_of_study   INT NOT NULL DEFAULT 1,          -- 1‑4
    admission_date  DATE,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────
-- 4. FACULTY
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS faculty (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    faculty_id    VARCHAR(20)  NOT NULL UNIQUE,
    user_id       BIGINT REFERENCES users(id),
    first_name    VARCHAR(50)  NOT NULL,
    last_name     VARCHAR(50)  NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    department_id BIGINT REFERENCES departments(id),
    designation   VARCHAR(100),
    active        BOOLEAN NOT NULL DEFAULT TRUE
);

-- ─────────────────────────────────────────
-- 5. COURSES
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS courses (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code   VARCHAR(20)  NOT NULL UNIQUE,
    course_name   VARCHAR(150) NOT NULL,
    credits       INT NOT NULL DEFAULT 3,
    department_id BIGINT REFERENCES departments(id),
    faculty_id    BIGINT REFERENCES faculty(id),
    semester      INT NOT NULL DEFAULT 1,
    description   TEXT,
    max_students  INT DEFAULT 60,
    active        BOOLEAN NOT NULL DEFAULT TRUE
);

-- ─────────────────────────────────────────
-- 6. ENROLLMENTS  (Students ↔ Courses  M:N)
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS enrollments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id  BIGINT NOT NULL REFERENCES students(id),
    course_id   BIGINT NOT NULL REFERENCES courses(id),
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status      ENUM('ACTIVE','DROPPED','COMPLETED') DEFAULT 'ACTIVE',
    UNIQUE KEY uq_enrollment (student_id, course_id)
);

-- ─────────────────────────────────────────
-- 7. ATTENDANCE
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS attendance (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT  NOT NULL REFERENCES students(id),
    course_id    BIGINT  NOT NULL REFERENCES courses(id),
    class_date   DATE    NOT NULL,
    status       ENUM('PRESENT','ABSENT','LATE') NOT NULL DEFAULT 'ABSENT',
    remarks      VARCHAR(255),
    marked_by    BIGINT REFERENCES faculty(id),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_attendance (student_id, course_id, class_date)
);

-- ─────────────────────────────────────────
-- 8. MARKS / EXAMINATIONS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS marks (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT          NOT NULL REFERENCES students(id),
    course_id    BIGINT          NOT NULL REFERENCES courses(id),
    exam_type    ENUM('INTERNAL1','INTERNAL2','MIDTERM','FINAL') NOT NULL,
    max_marks    DECIMAL(5,2)    NOT NULL DEFAULT 100,
    obtained     DECIMAL(5,2)    NOT NULL DEFAULT 0,
    grade        VARCHAR(5),
    remarks      VARCHAR(255),
    exam_date    DATE,
    entered_by   BIGINT REFERENCES faculty(id),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_marks (student_id, course_id, exam_type)
);

-- ─────────────────────────────────────────
-- 9. FEES
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS fees (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT          NOT NULL REFERENCES students(id),
    fee_type        ENUM('TUITION','HOSTEL','TRANSPORT','EXAM','MISC') NOT NULL,
    amount          DECIMAL(10,2)   NOT NULL,
    amount_paid     DECIMAL(10,2)   NOT NULL DEFAULT 0,
    due_date        DATE,
    paid_date       DATE,
    academic_year   VARCHAR(10)     NOT NULL,   -- e.g. "2024-25"
    payment_method  ENUM('CASH','CARD','UPI','BANK_TRANSFER') DEFAULT 'CASH',
    receipt_no      VARCHAR(50),
    status          ENUM('PENDING','PARTIAL','PAID') DEFAULT 'PENDING',
    remarks         VARCHAR(255),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────
-- 10. TIMETABLE
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS timetable (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id    BIGINT NOT NULL REFERENCES courses(id),
    day_of_week  ENUM('MON','TUE','WED','THU','FRI','SAT') NOT NULL,
    start_time   TIME NOT NULL,
    end_time     TIME NOT NULL,
    room_no      VARCHAR(20),
    semester     INT NOT NULL DEFAULT 1,
    academic_year VARCHAR(10),
    UNIQUE KEY uq_timetable (course_id, day_of_week, start_time)
);

-- ============================================================
--  SAMPLE DATA
-- ============================================================

-- Departments
INSERT INTO departments (name, code) VALUES
('Computer Science & Engineering', 'CSE'),
('Electronics & Communication',    'ECE'),
('Mechanical Engineering',         'MECH'),
('Civil Engineering',              'CIVIL'),
('Information Technology',         'IT');

-- Users  (password = "password123" → BCrypt hash)
INSERT INTO users (username, password, email, full_name, role) VALUES
('admin',     '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@sis.edu',       'System Administrator', 'ADMIN'),
('faculty1',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'drsmith@sis.edu',     'Dr. John Smith',       'FACULTY'),
('faculty2',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'profpriya@sis.edu',   'Prof. Priya Nair',     'FACULTY'),
('student1',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'arjun@student.edu',   'Arjun Kumar',          'STUDENT'),
('student2',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'sneha@student.edu',   'Sneha Patel',          'STUDENT'),
('student3',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'rahul@student.edu',   'Rahul Sharma',         'STUDENT'),
('student4',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'divya@student.edu',   'Divya Menon',          'STUDENT'),
('student5',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'kiran@student.edu',   'Kiran Reddy',          'STUDENT');

-- Faculty
INSERT INTO faculty (faculty_id, user_id, first_name, last_name, email, phone, department_id, designation) VALUES
('FAC001', 2, 'John',  'Smith', 'drsmith@sis.edu',   '9876543210', 1, 'Associate Professor'),
('FAC002', 3, 'Priya', 'Nair',  'profpriya@sis.edu', '9876543211', 2, 'Assistant Professor');

-- Students
INSERT INTO students (student_id, user_id, first_name, last_name, email, phone, dob, gender, department_id, year_of_study, admission_date) VALUES
('STU2024001', 4, 'Arjun',  'Kumar',  'arjun@student.edu', '9000001111', '2005-03-15', 'MALE',   1, 2, '2024-07-01'),
('STU2024002', 5, 'Sneha',  'Patel',  'sneha@student.edu', '9000002222', '2005-06-22', 'FEMALE', 1, 2, '2024-07-01'),
('STU2024003', 6, 'Rahul',  'Sharma', 'rahul@student.edu', '9000003333', '2005-01-10', 'MALE',   1, 2, '2024-07-01'),
('STU2024004', 7, 'Divya',  'Menon',  'divya@student.edu', '9000004444', '2005-09-05', 'FEMALE', 2, 3, '2023-07-01'),
('STU2024005', 8, 'Kiran',  'Reddy',  'kiran@student.edu', '9000005555', '2004-12-20', 'MALE',   2, 3, '2023-07-01');

-- Courses
INSERT INTO courses (course_code, course_name, credits, department_id, faculty_id, semester) VALUES
('CS101', 'Introduction to Programming',   3, 1, 1, 1),
('CS201', 'Data Structures & Algorithms',  4, 1, 1, 3),
('CS301', 'Database Management Systems',   3, 1, 1, 5),
('EC101', 'Basic Electronics',             3, 2, 2, 1),
('EC201', 'Digital Signal Processing',     4, 2, 2, 3);

-- Enrollments
INSERT INTO enrollments (student_id, course_id, status) VALUES
(1,1,'ACTIVE'),(1,2,'ACTIVE'),(1,3,'ACTIVE'),
(2,1,'ACTIVE'),(2,2,'ACTIVE'),(2,3,'ACTIVE'),
(3,1,'ACTIVE'),(3,2,'ACTIVE'),
(4,4,'ACTIVE'),(4,5,'ACTIVE'),
(5,4,'ACTIVE'),(5,5,'ACTIVE');

-- Attendance (last 5 classes per course)
INSERT INTO attendance (student_id, course_id, class_date, status, marked_by) VALUES
(1,1,'2025-03-01','PRESENT',1),(1,1,'2025-03-03','PRESENT',1),(1,1,'2025-03-05','ABSENT',1),(1,1,'2025-03-08','PRESENT',1),(1,1,'2025-03-10','PRESENT',1),
(2,1,'2025-03-01','PRESENT',1),(2,1,'2025-03-03','ABSENT',1),(2,1,'2025-03-05','PRESENT',1),(2,1,'2025-03-08','PRESENT',1),(2,1,'2025-03-10','PRESENT',1),
(3,1,'2025-03-01','ABSENT',1),(3,1,'2025-03-03','PRESENT',1),(3,1,'2025-03-05','PRESENT',1),(3,1,'2025-03-08','ABSENT',1),(3,1,'2025-03-10','PRESENT',1),
(1,2,'2025-03-02','PRESENT',1),(1,2,'2025-03-04','PRESENT',1),(1,2,'2025-03-06','PRESENT',1),(1,2,'2025-03-09','ABSENT',1),(1,2,'2025-03-11','PRESENT',1);

-- Marks
INSERT INTO marks (student_id, course_id, exam_type, max_marks, obtained, grade, exam_date, entered_by) VALUES
(1,1,'INTERNAL1', 25, 22, 'O',  '2025-02-10', 1),
(1,1,'MIDTERM',   50, 42, 'A+', '2025-03-15', 1),
(2,1,'INTERNAL1', 25, 18, 'B',  '2025-02-10', 1),
(2,1,'MIDTERM',   50, 38, 'A',  '2025-03-15', 1),
(3,1,'INTERNAL1', 25, 20, 'A',  '2025-02-10', 1),
(3,1,'MIDTERM',   50, 35, 'B+', '2025-03-15', 1),
(1,2,'INTERNAL1', 25, 24, 'O',  '2025-02-12', 1),
(4,4,'INTERNAL1', 25, 21, 'A+', '2025-02-10', 2),
(5,4,'INTERNAL1', 25, 19, 'A',  '2025-02-10', 2);

-- Fees
INSERT INTO fees (student_id, fee_type, amount, amount_paid, due_date, academic_year, status, receipt_no) VALUES
(1,'TUITION', 50000, 50000, '2024-08-01', '2024-25', 'PAID',    'REC20240001'),
(1,'EXAM',     2000,  2000, '2024-11-01', '2024-25', 'PAID',    'REC20240002'),
(2,'TUITION', 50000, 25000, '2024-08-01', '2024-25', 'PARTIAL', 'REC20240003'),
(3,'TUITION', 50000,     0, '2024-08-01', '2024-25', 'PENDING', NULL),
(4,'TUITION', 50000, 50000, '2023-08-01', '2023-24', 'PAID',    'REC20230001'),
(5,'TUITION', 50000, 50000, '2023-08-01', '2023-24', 'PAID',    'REC20230002');

-- Timetable
INSERT INTO timetable (course_id, day_of_week, start_time, end_time, room_no, semester, academic_year) VALUES
(1,'MON','09:00:00','10:00:00','LH-101',1,'2024-25'),
(1,'WED','09:00:00','10:00:00','LH-101',1,'2024-25'),
(1,'FRI','09:00:00','10:00:00','LH-101',1,'2024-25'),
(2,'TUE','10:00:00','11:30:00','LH-102',3,'2024-25'),
(2,'THU','10:00:00','11:30:00','LH-102',3,'2024-25'),
(3,'MON','11:00:00','12:00:00','LH-103',5,'2024-25'),
(3,'WED','11:00:00','12:00:00','LH-103',5,'2024-25'),
(4,'TUE','09:00:00','10:00:00','LH-201',1,'2024-25'),
(4,'FRI','09:00:00','10:00:00','LH-201',1,'2024-25'),
(5,'MON','14:00:00','15:30:00','LH-202',3,'2024-25');
