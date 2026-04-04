# 🎓 EduTrack — Student Information System (SIS)
### Full-Stack Academic Project | Spring Boot + MySQL + Vanilla JS

---

## 📋 Table of Contents
1. [Project Overview](#overview)
2. [Technology Stack](#stack)
3. [Architecture](#architecture)
4. [Project Structure](#structure)
5. [Setup Instructions](#setup)
6. [API Reference](#api)
7. [OOSE Principles Applied](#oose)
8. [Default Credentials](#credentials)
9. [Troubleshooting](#troubleshooting)

---

## 🏛️ Project Overview <a name="overview"></a>

EduTrack SIS is a complete web-based Student Information System demonstrating:
- **Backend**: Java 17 + Spring Boot 3.2 (MVC + REST + JPA)
- **Database**: MySQL 8 with full relational schema
- **Frontend**: HTML5 + CSS3 + Vanilla JavaScript (no framework)
- **Security**: JWT-based authentication with role-based access control

### Core Modules
| Module | Features |
|--------|----------|
| 🔐 Authentication | Login/logout, JWT tokens, Role-based access (Admin/Faculty/Student) |
| 🎓 Student Management | Full CRUD, search/filter, soft delete, department association |
| 📚 Course Management | Create courses, assign faculty, enroll students, capacity limits |
| 📋 Attendance | Mark daily attendance, view by student/course, percentage calculation |
| 📝 Marks & Grades | Enter marks by exam type, auto grade calculation, GPA/CGPA report card |
| 💰 Fee Management | Create fee records, record payments, pending dues dashboard |
| 🕐 Timetable | Weekly schedule builder with server-side conflict detection |
| 📊 Dashboard | Real-time statistics: student count, enrollments, fee dues |

---

## 🛠️ Technology Stack <a name="stack"></a>

### Backend
```
Java 17
Spring Boot 3.2.0
├── spring-boot-starter-web         (REST APIs)
├── spring-boot-starter-data-jpa    (Database ORM)
├── spring-boot-starter-security    (Auth)
├── spring-boot-starter-validation  (Bean Validation)
├── jjwt 0.11.5                     (JWT tokens)
└── Lombok                          (Boilerplate reduction)
MySQL 8.x (via mysql-connector-j)
Maven (build tool)
```

### Frontend
```
HTML5 + CSS3 (custom design system, CSS Variables)
Vanilla JavaScript (ES2020+)
Fetch API (HTTP client)
Google Fonts: Playfair Display + DM Sans
```

---

## 🏗️ Architecture <a name="architecture"></a>

```
┌─────────────────────────────────────────────────────────┐
│                     FRONTEND (Browser)                   │
│  HTML Pages → JavaScript → Fetch API → REST Endpoints   │
└───────────────────────┬─────────────────────────────────┘
                        │ HTTP/JSON (JWT in header)
┌───────────────────────▼─────────────────────────────────┐
│                  SPRING BOOT BACKEND                     │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │  @Controller │→ │  @Service    │→ │  @Repository  │  │
│  │  REST Layer  │  │  Logic Layer │  │  Data Layer   │  │
│  └──────────────┘  └──────────────┘  └───────┬───────┘  │
│                                               │          │
│  ┌─────────────────────────────────────────── │ ──────┐  │
│  │  Spring Security  ←── JWT Filter           │       │  │
│  └─────────────────────────────────────────── │ ──────┘  │
└───────────────────────────────────────────────│──────────┘
                                                │ JPA/Hibernate
┌───────────────────────────────────────────────▼──────────┐
│                      MySQL 8 Database                    │
│   users │ students │ courses │ enrollments │ attendance  │
│   marks │ fees │ timetable │ departments │ faculty       │
└──────────────────────────────────────────────────────────┘
```

### Layer Responsibilities
| Layer | Package | Responsibility |
|-------|---------|---------------|
| Controller | `com.sis.controller` | Receive HTTP requests, validate input, return JSON responses |
| Service | `com.sis.service` | Business logic, calculations, orchestration |
| Repository | `com.sis.repository` | Database queries via Spring Data JPA |
| Model/Entity | `com.sis.model` | JPA entity classes mapping to DB tables |
| Config | `com.sis.config` | Security, JWT, CORS configuration |
| Exception | `com.sis.exception` | Global error handling |

---

## 📁 Project Structure <a name="structure"></a>

```
sis/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/sis/
│       │   ├── SisApplication.java
│       │   ├── config/
│       │   │   ├── JwtUtils.java
│       │   │   ├── JwtAuthFilter.java
│       │   │   └── SecurityConfig.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── StudentController.java
│       │   │   ├── Controllers.java       (Course/Attendance/Marks/Fee/Dashboard)
│       │   │   └── TimetableController.java
│       │   ├── service/
│       │   │   ├── AuthService.java
│       │   │   ├── StudentService.java
│       │   │   ├── CourseService.java
│       │   │   ├── AttendanceService.java
│       │   │   ├── MarksService.java
│       │   │   ├── FeeService.java
│       │   │   └── DashboardService.java
│       │   ├── repository/
│       │   │   ├── UserRepository.java
│       │   │   ├── StudentRepository.java
│       │   │   ├── CourseRepository.java
│       │   │   ├── EnrollmentRepository.java
│       │   │   ├── AttendanceRepository.java
│       │   │   ├── MarksRepository.java
│       │   │   ├── FeeRepository.java
│       │   │   ├── TimetableRepository.java
│       │   │   ├── FacultyRepository.java
│       │   │   └── DepartmentRepository.java
│       │   ├── model/
│       │   │   ├── User.java
│       │   │   ├── Department.java
│       │   │   ├── Student.java
│       │   │   ├── Faculty.java
│       │   │   ├── Course.java
│       │   │   ├── Enrollment.java
│       │   │   ├── Attendance.java
│       │   │   ├── Marks.java
│       │   │   ├── Fee.java
│       │   │   └── Timetable.java
│       │   └── exception/
│       │       └── GlobalExceptionHandler.java
│       └── resources/
│           ├── application.properties
│           └── schema.sql
│
└── frontend/
    ├── index.html                  (Login page)
    ├── css/
    │   └── style.css              (Complete design system)
    ├── js/
    │   └── api.js                 (API client + utilities)
    └── pages/
        ├── _sidebar.html          (Shared navigation)
        ├── dashboard.html
        ├── students.html
        ├── courses.html
        ├── attendance.html
        ├── marks.html
        ├── fees.html
        └── timetable.html
```

---

## ⚙️ Setup Instructions <a name="setup"></a>

### Prerequisites
- Java 17+ (check: `java -version`)
- Maven 3.8+ (check: `mvn -version`)
- MySQL 8.x (check: `mysql --version`)
- Any browser (Chrome/Firefox/Edge)
- VS Code / IntelliJ IDEA (optional)

---

### Step 1 — Set up MySQL Database

```bash
# Log into MySQL
mysql -u root -p

# Run the schema script
SOURCE /path/to/sis/backend/src/main/resources/schema.sql;

# Verify tables created
USE sis_db;
SHOW TABLES;
```

Expected tables: `users, students, faculty, departments, courses, enrollments, attendance, marks, fees, timetable`

---

### Step 2 — Configure Database Connection

Edit `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sis_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD_HERE
```

---

### Step 3 — Build & Run the Backend

```bash
cd sis/backend

# Build the project
mvn clean install -DskipTests

# Run Spring Boot
mvn spring-boot:run
```

✅ Server starts at: **http://localhost:8080/api**

You should see:
```
Started SisApplication in X.XXX seconds
```

---

### Step 4 — Run the Frontend

**Option A — VS Code Live Server (Recommended)**
1. Install "Live Server" extension in VS Code
2. Open `sis/frontend/` folder in VS Code
3. Right-click `index.html` → "Open with Live Server"
4. App opens at `http://127.0.0.1:5500`

**Option B — Python HTTP Server**
```bash
cd sis/frontend
python -m http.server 5500
# Open: http://localhost:5500
```

**Option C — Node.js**
```bash
cd sis/frontend
npx serve . -p 5500
# Open: http://localhost:5500
```

---

### Step 5 — Login & Test

Open the browser → `http://localhost:5500` (or Live Server URL)

Use one of the demo credentials:
| Username | Password | Role |
|----------|----------|------|
| `admin` | `password123` | Admin (full access) |
| `faculty1` | `password123` | Faculty (attendance, marks) |
| `student1` | `password123` | Student (view only) |

---

## 📡 API Reference <a name="api"></a>

All endpoints are prefixed with `http://localhost:8080/api/`

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | Login, returns JWT token |
| POST | `/auth/register` | Register new user (Admin) |

```javascript
// Login example
POST /api/auth/login
{
  "username": "admin",
  "password": "password123"
}
// Response:
{
  "token": "eyJhbGciOiJIUzI1...",
  "role": "ADMIN",
  "fullName": "System Administrator",
  "userId": 1
}
```

### Students
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/students` | All active students |
| GET | `/students/{id}` | Student by ID |
| GET | `/students/search?q=name&page=0&size=10` | Search with pagination |
| GET | `/students/department/{deptId}` | Students by department |
| POST | `/students` | Create student |
| PUT | `/students/{id}` | Update student |
| DELETE | `/students/{id}` | Soft-delete (Admin only) |

### Courses
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/courses` | All active courses |
| POST | `/courses` | Create course |
| PUT | `/courses/{id}` | Update course |
| DELETE | `/courses/{id}` | Delete course |
| POST | `/courses/{cid}/enroll/{sid}` | Enroll student |
| GET | `/courses/{cid}/enrollments` | List enrollments |

### Attendance
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/attendance/mark` | Mark attendance |
| GET | `/attendance/student/{sid}` | All attendance for student |
| GET | `/attendance/student/{sid}/course/{cid}/percentage` | % calculation |

### Marks
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/marks/enter` | Enter marks |
| GET | `/marks/student/{sid}` | All marks for student |
| GET | `/marks/student/{sid}/reportcard` | Full CGPA report |

### Fees
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/fees` | Create fee record |
| POST | `/fees/{id}/pay` | Record payment |
| GET | `/fees/student/{sid}` | Student fees |
| GET | `/fees/pending` | All pending fees |

### Dashboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/dashboard/stats` | Aggregated statistics |

---

## 🎓 OOSE Principles Applied <a name="oose"></a>

### 1. Encapsulation
- Entity fields are `private` with Lombok `@Getter`/`@Setter`
- Business logic is hidden inside Service layer classes
- `StudentService.generateStudentId()` is a private helper

### 2. Abstraction
- `JpaRepository<T, ID>` abstracts all database CRUD operations
- `UserDetailsService` interface abstracts authentication source
- Service interfaces hide implementation complexity from controllers

### 3. Inheritance
- `@MappedSuperclass` pattern for common audit fields
- Spring's `OncePerRequestFilter` extended by `JwtAuthFilter`
- RuntimeException subclasses for typed exceptions

### 4. Polymorphism
- Multiple `@RestController` implementations handled by Spring's `DispatcherServlet`
- `PasswordEncoder` interface swappable between BCrypt/Argon2
- JPA `@Enumerated` handles type conversion polymorphically

### 5. Single Responsibility Principle (SRP)
- Each Service class handles exactly one domain (Student, Course, etc.)
- Controllers only handle HTTP concerns, not business logic
- JwtUtils handles only JWT operations

### 6. Open/Closed Principle (OCP)
- New modules can extend the system without modifying existing controllers
- Fee types and exam types use `@Enumerated` — extendable via enum

### 7. Dependency Inversion
- Controllers depend on Service *interfaces*, not concrete classes
- Spring `@Autowired` injects dependencies automatically
- `UserDetailsService` interface decouples auth from user storage

### 8. MVC Architecture
```
Model     → Entity classes (Student, Course, etc.)
View      → JSON responses (frontend renders them)
Controller → REST endpoints (StudentController, etc.)
```

---

## 🔑 Default Credentials <a name="credentials"></a>

```
Admin:   admin    / password123   (Full access)
Faculty: faculty1 / password123   (Mark attendance, enter marks)
Faculty: faculty2 / password123
Student: student1 / password123   (View own records)
Student: student2 / password123
Student: student3 / password123
```

All passwords are BCrypt-hashed in the database. The raw hash for `password123` is:
`$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi`

---

## 🔧 Troubleshooting <a name="troubleshooting"></a>

### "Cannot connect to server"
- Verify Spring Boot is running: `curl http://localhost:8080/api/auth/login`
- Check port 8080 is not in use: `lsof -i:8080`

### "Access Denied" errors
- Ensure CORS origins in `application.properties` match your frontend URL
- Add your origin: `sis.cors.allowed-origins=http://localhost:5500,http://127.0.0.1:5500`

### Database connection failed
- Verify MySQL is running: `sudo systemctl status mysql`
- Confirm credentials in `application.properties`
- Ensure `sis_db` database exists and schema was imported

### "Table not found" errors
- Run `schema.sql` before starting the app
- Set `spring.jpa.hibernate.ddl-auto=update` temporarily for testing

### Login fails with valid credentials
- The BCrypt hash in `schema.sql` matches `password123`
- If you changed the hash, re-generate using: BCrypt online generator

---

## 📝 Notes for Evaluators

This project was built as an OOSE academic submission demonstrating:
- **3-tier architecture**: Presentation (HTML/JS) → Application (Spring Boot) → Data (MySQL)
- **8 functional modules** each with complete CRUD + business logic
- **JWT security** with role-based access control
- **JPA relationships**: One-to-Many and Many-to-Many properly mapped
- **Exception handling**: Global handler with typed custom exceptions
- **Input validation**: Both frontend (JS) and backend (Bean Validation)
- **Responsive UI**: Works on desktop and mobile

---

*EduTrack SIS — OOSE Academic Project | Spring Boot 3.2 + MySQL 8 + Vanilla JS*
