package com.sis.config;

import com.sis.model.*;
import com.sis.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DatabaseSeeder {

    @Bean
    public CommandLineRunner initDatabase(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            StudentRepository studentRepository,
            FacultyRepository facultyRepository,
            CourseRepository courseRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // ── 1. Departments ──────────────────────────────────────────────
            if (departmentRepository.count() == 0) {
                List<Department> departments = List.of(
                    Department.builder().name("Computer Science & Engineering").code("CSE").build(),
                    Department.builder().name("Electronics & Communication").code("ECE").build(),
                    Department.builder().name("Mechanical Engineering").code("MECH").build(),
                    Department.builder().name("Civil Engineering").code("CIVIL").build(),
                    Department.builder().name("Information Technology").code("IT").build()
                );
                departmentRepository.saveAll(departments);
                System.out.println("✅ Departments seeded.");
            }

            // ── 2. Admin user ───────────────────────────────────────────────
            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("password123"))
                    .email("admin@sis.com")
                    .fullName("System Administrator")
                    .role(User.Role.ADMIN)
                    .active(true)
                    .build());
                System.out.println("✅ Admin user seeded  →  username: admin  |  password: password123");
            }

            // ── 3. Faculty user ─────────────────────────────────────────────
            if (!userRepository.existsByUsername("faculty1")) {
                userRepository.save(User.builder()
                    .username("faculty1")
                    .password(passwordEncoder.encode("password123"))
                    .email("faculty1@sis.com")
                    .fullName("Dr. Ramesh Kumar")
                    .role(User.Role.FACULTY)
                    .active(true)
                    .build());
                System.out.println("✅ Faculty user seeded  →  username: faculty1  |  password: password123");
            }

            // ── 4. Student user (login account) ─────────────────────────────
            if (!userRepository.existsByUsername("student1")) {
                userRepository.save(User.builder()
                    .username("student1")
                    .password(passwordEncoder.encode("password123"))
                    .email("student1@sis.com")
                    .fullName("Arjun Sharma")
                    .role(User.Role.STUDENT)
                    .active(true)
                    .build());
                System.out.println("✅ Student user seeded  →  username: student1  |  password: password123");
            }

            // ── 5. Faculty profiles ──────────────────────────────────────────
            if (facultyRepository.count() == 0) {
                User facultyUser = userRepository.findByUsername("faculty1").orElseThrow();
                Department cse = departmentRepository.findByCode("CSE").orElseThrow();
                
                facultyRepository.save(Faculty.builder()
                    .facultyId("FAC2024001")
                    .user(facultyUser)
                    .firstName("Ramesh")
                    .lastName("Kumar")
                    .email("faculty1@sis.com")
                    .department(cse)
                    .designation("Associate Professor")
                    .active(true)
                    .build());
                System.out.println("✅ Faculty profile seeded.");
            }

            // ── 6. Sample Student records (for the students table) ───────────
            if (studentRepository.count() == 0) {
                Department cse  = departmentRepository.findByCode("CSE").orElseThrow();
                Department ece  = departmentRepository.findByCode("ECE").orElseThrow();
                Department it   = departmentRepository.findByCode("IT").orElseThrow();
                User studentUser = userRepository.findByUsername("student1").orElseThrow();

                List<Student> students = List.of(
                    Student.builder()
                        .studentId("STU2023001")
                        .user(studentUser)
                        .firstName("Arjun").lastName("Sharma")
                        .email("arjun.sharma@student.edu").phone("9000001111")
                        .dob(LocalDate.of(2003, 6, 15)).gender(Student.Gender.MALE)
                        .yearOfStudy(2).department(cse).active(true)
                        .address("12, MG Road, Bangalore").admissionDate(LocalDate.of(2023, 7, 1))
                        .build(),
                    Student.builder()
                        .studentId("STU2024001")
                        .firstName("Priya").lastName("Nair")
                        .email("priya.nair@student.edu").phone("9000002222")
                        .dob(LocalDate.of(2004, 3, 22)).gender(Student.Gender.FEMALE)
                        .yearOfStudy(1).department(ece).active(true)
                        .address("45, Anna Salai, Chennai").admissionDate(LocalDate.of(2024, 7, 1))
                        .build(),
                    Student.builder()
                        .studentId("STU2022001")
                        .firstName("Rohit").lastName("Verma")
                        .email("rohit.verma@student.edu").phone("9000003333")
                        .dob(LocalDate.of(2002, 11, 8)).gender(Student.Gender.MALE)
                        .yearOfStudy(3).department(it).active(true)
                        .address("78, Banjara Hills, Hyderabad").admissionDate(LocalDate.of(2022, 7, 1))
                        .build(),
                    Student.builder()
                        .studentId("STU2023002")
                        .firstName("Sneha").lastName("Patel")
                        .email("sneha.patel@student.edu").phone("9000004444")
                        .dob(LocalDate.of(2003, 9, 1)).gender(Student.Gender.FEMALE)
                        .yearOfStudy(2).department(cse).active(true)
                        .address("33, CG Road, Ahmedabad").admissionDate(LocalDate.of(2023, 7, 1))
                        .build()
                );
                studentRepository.saveAll(students);
                System.out.println("✅ Sample students seeded (4 records).");
            }

            // ── 7. Sample Courses ────────────────────────────────────────────
            if (courseRepository.count() == 0) {
                Department cse = departmentRepository.findByCode("CSE").orElseThrow();
                Department ece = departmentRepository.findByCode("ECE").orElseThrow();
                Faculty faculty = facultyRepository.findAll().get(0);

                List<Course> courses = List.of(
                    Course.builder()
                        .courseCode("CS101").courseName("Data Structures & Algorithms")
                        .credits(4).department(cse).faculty(faculty).semester(1)
                        .description("Fundamental algorithms and data structures.").active(true).build(),
                    Course.builder()
                        .courseCode("CS201").courseName("Operating Systems")
                        .credits(4).department(cse).faculty(faculty).semester(3)
                        .description("Internal workings of OS kernels.").active(true).build(),
                    Course.builder()
                        .courseCode("EC101").courseName("Digital Electronics")
                        .credits(3).department(ece).faculty(faculty).semester(1)
                        .description("Introduction to digital logic design.").active(true).build()
                );
                courseRepository.saveAll(courses);
                System.out.println("✅ Sample courses seeded.");
            }
        };
    }
}
