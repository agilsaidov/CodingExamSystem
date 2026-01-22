# Online Judge System

A comprehensive online coding exam and assessment platform built with Spring Boot. This system allows instructors to create programming exams, manage student groups, and automatically evaluate code submissions using the Judge0 API.

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Security](#security)
- [Contributing](#contributing)

## ✨ Features

### For Instructors
- **Group Management**: Create and manage student groups
- **Exam Creation**: Design exams with multiple programming problems
- **Problem Configuration**: Set up problems with custom test cases, time limits, and memory constraints
- **Real-time Monitoring**: Track student progress and exam statistics
- **Exam Control**: Activate/deactivate exams and set time windows
- **Flexible Scheduling**: Set exam start times, end times, and duration limits

### For Students
- **Exam Participation**: Take programming exams with time limits
- **Multi-language Support**: Submit code in various programming languages (via Judge0)
- **Instant Feedback**: Get immediate test case results
- **Progress Tracking**: View submission history and scores
- **Multiple Attempts**: Submit multiple solutions per problem

### System Features
- **Automated Code Evaluation**: Integration with Judge0 for secure code execution
- **Role-based Access Control**: INSTRUCTOR, STUDENT, and ADMIN roles
- **JWT Authentication**: Secure token-based authentication with Redis blacklist
- **Test Case Management**: Support for visible and hidden test cases
- **Detailed Results**: Track execution time, memory usage, and error messages
- **Asynchronous Processing**: Non-blocking submission evaluation
- **Comprehensive Statistics**: Exam-level and problem-level analytics

## 🛠 Tech Stack

- **Framework**: Spring Boot 3.x
- **Security**: Spring Security with JWT
- **Database**: PostgreSQL (JPA/Hibernate)
- **Caching**: Redis (for token blacklist)
- **Code Execution**: Judge0 API
- **Build Tool**: Maven
- **Java Version**: 17+
- **Additional Libraries**:
  - Lombok
  - Jackson (JSON processing)
  - Spring Retry
  - Spring Async

## 🏗 Architecture

### Domain Models

```
AppUser (users)
├── Role: INSTRUCTOR, STUDENT, ADMIN
├── Instructors → Create Groups and Exams
└── Students → Submit Solutions and Get Results

Group (groups)
├── Managed by Instructor
├── Contains GroupMembers (Students)
└── Has multiple Exams

Exam (exams)
├── Belongs to a Group
├── Created by an Instructor
├── Contains multiple Problems
├── Has activation status and time windows
└── Tracks ExamResults for each student

Problem (problems)
├── Belongs to an Exam
├── Has multiple TestCases
├── Configurable time and memory limits
└── Receives student Submissions

Submission (submissions)
├── Student's code solution
├── Evaluated against TestCases
└── Produces TestCaseResults

ExamResult (exam_results)
├── Tracks student's overall exam performance
├── Calculates total score and percentage
└── Monitors exam status (NOT_STARTED, IN_PROGRESS, COMPLETED)
```

### Key Components

#### Controllers
- **ExamController**: Exam CRUD and activation management
- **GroupController**: Group and membership management
- **ProblemController**: Problem and test case management
- **SubmissionController**: Code submission and evaluation
- **JudgeController**: Judge0 health check and integration

#### Services
- **ExamService**: Business logic for exam management
- **GroupService**: Group operations and access control
- **ProblemService**: Problem and test case handling
- **SubmissionService**: Asynchronous code evaluation
- **JudgeService**: Judge0 API integration with retry logic

#### Security
- **JwtService**: JWT token generation and validation
- **JwtValidationFilter**: Request authentication filter
- **SecurityConfig**: Spring Security configuration
- **Token Blacklist**: Redis-based token invalidation

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL 12+
- Redis 6+
- Judge0 API instance (self-hosted or cloud)
- Maven 3.6+

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/online-judge-system.git
cd online-judge-system
```

2. **Configure the database**
```bash
# Create PostgreSQL database
createdb judge_system
```

3. **Set up environment variables**
```bash
export DB_URL=jdbc:postgresql://localhost:5432/judge_system
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export JWT_SECRET=your-super-secret-jwt-key-min-256-bits
export JUDGE0_AUTHENTICATION_TOKEN=your_judge0_token
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

4. **Configure application.properties**
```properties
# Database
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Redis
spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT}

# Judge0
judge0.url=https://judge0-ce.p.rapidapi.com
judge0.default-cpu-time-limit=5.0
judge0.default-memory-limit=128000
judge0.default-wall-time-limit=10.0

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=10800000
```

5. **Build and run**
```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## ⚙️ Configuration

### Judge0 Configuration

The system uses Judge0 for code execution. Configure the following properties:

```java
judge0.url=https://your-judge0-instance.com
judge0.api-key=${JUDGE0_AUTHENTICATION_TOKEN}
judge0.max-retries=3
judge0.retry-delay-ms=1000
judge0.connection-timeout=10000
judge0.read-timeout=30000
```

### Default Limits

```java
# Default execution limits
judge0.default-cpu-time-limit=5.0      # seconds
judge0.default-memory-limit=128000     # KB (128 MB)
judge0.default-wall-time-limit=10.0    # seconds
```

### CORS Configuration

Update allowed origins in `SecurityConfig.java`:

```java
config.setAllowedOrigins(List.of("http://localhost:3000", "https://your-frontend.com"));
```

## 📚 API Documentation

### Authentication

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user_id": "USR1234567",
  "username": "john_doe",
  "role": "STUDENT",
  "full_name": "John Doe",
  "groups": [
    {
      "group_id": "GRP12345",
      "group_name": "CS101 Spring 2024",
      "instructor_name": "Dr. Smith",
      "member_count": 25,
      "active_exam_count": 2,
      "created_at": "2024.01.15 10:30"
    }
  ]
}
```

#### Logout
```http
POST /api/v1/auth/logout
Authorization: Bearer <token>
```

**Response:**
```
"Logout successful"
```

#### Register Student (INSTRUCTOR/ADMIN only)
```http
POST /api/v1/students/register
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "student123",
  "password": "password123",
  "role": "STUDENT",
  "fullName": "Jane Student"
}
```

#### Register Instructor (ADMIN only)
```http
POST /api/v1/admin/register/instructor
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "instructor456",
  "password": "password123",
  "role": "INSTRUCTOR",
  "fullName": "Prof. Johnson"
}
```

**Authentication Notes:**
- All endpoints (except login and judge health check) require JWT authentication
- Include token in Authorization header: `Authorization: Bearer <token>`
- Tokens expire after 3 hours
- Logout blacklists the token in Redis

### Group Management

#### Create Group
```http
POST /api/v1/groups/create
Authorization: Bearer <token>
Content-Type: application/json

{
  "groupName": "CS101 Spring 2024"
}
```

#### Add Student to Group
```http
POST /api/v1/groups/{groupId}/members/{studentId}
Authorization: Bearer <token>
```

#### Delete Group
```http
DELETE /api/v1/groups/delete/{groupId}
Authorization: Bearer <token>
```

### Exam Management

#### Create Exam
```http
POST /api/v1/exams/create
Authorization: Bearer <token>
Content-Type: application/json

{
  "groupId": "GRP12345",
  "title": "Midterm Exam",
  "description": "Programming fundamentals assessment",
  "startTime": "2024-03-01T10:00:00",
  "endTime": "2024-03-01T12:00:00",
  "durationMinutes": 120
}
```

#### Activate Exam
```http
POST /api/v1/exams/{examId}/activate
Authorization: Bearer <token>
```

#### Deactivate Exam
```http
POST /api/v1/exams/{examId}/deactivate
Authorization: Bearer <token>
```

#### Get Group Exams
```http
GET /api/v1/exams/group/{groupId}
Authorization: Bearer <token>
```

#### Get Exam Details
```http
GET /api/v1/exams/{examId}
Authorization: Bearer <token>
```

#### Update Exam
```http
PUT /api/v1/exams/{examId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Updated Title",
  "description": "Updated description",
  "startTime": "2024-03-01T10:00:00",
  "endTime": "2024-03-01T12:00:00",
  "durationMinutes": 120
}
```

#### Delete Exam
```http
DELETE /api/v1/exams/{examId}
Authorization: Bearer <token>
```

### Problem Management

#### Create Problem
```http
POST /api/v1/problems/create
Authorization: Bearer <token>
Content-Type: application/json

{
  "examId": "EXM12345",
  "title": "Two Sum",
  "description": "Given an array of integers...",
  "points": 100,
  "timeLimit": 5,
  "memoryLimit": 128000,
  "orderIndex": 1
}
```

#### Add Test Case
```http
POST /api/v1/problems/{problemId}/test-case
Authorization: Bearer <token>
Content-Type: application/json

{
  "input": "2 7 11 15\n9",
  "expectedOutput": "0 1",
  "isHidden": false,
  "orderIndex": 1,
  "points": 10
}
```

#### Delete Problem
```http
DELETE /api/v1/problems/delete/{problemId}
Authorization: Bearer <token>
```

#### Get Exam Problems
```http
GET /api/v1/problems/exam/{examId}
Authorization: Bearer <token>
```

### Submissions

#### Submit Code
```http
POST /api/v1/submissions/submit
Authorization: Bearer <token>
Content-Type: application/json

{
  "problemId": 1,
  "sourceCode": "def solution(nums, target):\n    # your code here",
  "languageId": 71
}
```

**Common Language IDs (Judge0):**
- C (GCC): 50
- C++ (G++): 54
- Java: 62
- Python 3: 71
- JavaScript (Node.js): 63

#### Get Submission Details
```http
GET /api/v1/submissions/{submissionId}
Authorization: Bearer <token>
```

#### Get Problem Submissions
```http
GET /api/v1/submissions/problem/{problemId}
Authorization: Bearer <token>
```

#### Finish Exam
```http
POST /api/v1/submissions/exam/{examId}/finish
Authorization: Bearer <token>
```

### Judge0 Integration

#### Health Check
```http
GET /api/v1/judge/health
Authorization: Bearer <token>
```

#### Submit for Evaluation
```http
POST /api/v1/judge/submit
Content-Type: application/json

{
  "sourceCode": "print('Hello World')",
  "languageId": 71,
  "stdin": "",
  "expectedOutput": "Hello World"
}
```

## 🗄 Database Schema

### Users Table
```sql
CREATE TABLE users (
    user_id VARCHAR(10) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    full_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### Groups Table
```sql
CREATE TABLE groups (
    group_id VARCHAR(10) PRIMARY KEY,
    group_name VARCHAR(50) NOT NULL,
    instructor_id VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (instructor_id) REFERENCES users(user_id)
);
```

### Group Members Table
```sql
CREATE TABLE group_members (
    group_id VARCHAR(10) NOT NULL,
    student_id VARCHAR(10) NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    PRIMARY KEY (group_id, student_id),
    FOREIGN KEY (group_id) REFERENCES groups(group_id),
    FOREIGN KEY (student_id) REFERENCES users(user_id)
);
```

### Exams Table
```sql
CREATE TABLE exams (
    exam_id VARCHAR(10) PRIMARY KEY,
    group_id VARCHAR(10) NOT NULL,
    instructor_id VARCHAR(10) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration_minutes INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups(group_id),
    FOREIGN KEY (instructor_id) REFERENCES users(user_id)
);
```

### Problems Table
```sql
CREATE TABLE problems (
    problem_id BIGSERIAL PRIMARY KEY,
    exam_id VARCHAR(10) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    points INTEGER NOT NULL,
    time_limit INTEGER DEFAULT 5,
    memory_limit INTEGER DEFAULT 128000,
    order_index INTEGER,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id)
);
```

### Test Cases Table
```sql
CREATE TABLE test_cases (
    test_case_id BIGSERIAL PRIMARY KEY,
    problem_id BIGINT NOT NULL,
    input TEXT NOT NULL,
    expected_output TEXT NOT NULL,
    is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
    order_index INTEGER,
    points INTEGER,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (problem_id) REFERENCES problems(problem_id)
);
```

### Submissions Table
```sql
CREATE TABLE submissions (
    submission_id BIGSERIAL PRIMARY KEY,
    student_id VARCHAR(10) NOT NULL,
    problem_id BIGINT NOT NULL,
    source_code TEXT NOT NULL,
    language_id INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    score INTEGER,
    passed_test_cases INTEGER,
    total_test_cases INTEGER,
    submitted_at TIMESTAMP NOT NULL,
    judged_at TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(user_id),
    FOREIGN KEY (problem_id) REFERENCES problems(problem_id)
);
```

### Test Case Results Table
```sql
CREATE TABLE test_case_results (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    test_case_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    actual_output TEXT,
    execution_time DOUBLE PRECISION,
    memory_used INTEGER,
    stderr TEXT,
    compile_output TEXT,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (submission_id) REFERENCES submissions(submission_id),
    FOREIGN KEY (test_case_id) REFERENCES test_cases(test_case_id)
);
```

### Exam Results Table
```sql
CREATE TABLE exam_results (
    result_id BIGSERIAL PRIMARY KEY,
    exam_id VARCHAR(10) NOT NULL,
    student_id VARCHAR(10) NOT NULL,
    total_score INTEGER NOT NULL DEFAULT 0,
    max_possible_score INTEGER NOT NULL,
    percentage DECIMAL(5,2),
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    time_spent_minutes INTEGER,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id),
    FOREIGN KEY (student_id) REFERENCES users(user_id)
);
```

## 🔒 Security

### Authentication Flow

1. **Login**: User submits credentials to `/api/v1/auth/login`
2. **Validation**: System validates credentials against database
3. **Token Generation**: JWT token is generated containing:
   - User ID (subject)
   - Username
   - Full name
   - Role
4. **Response**: Token and user details (including groups) are returned
5. **Subsequent Requests**: Client includes token in Authorization header
6. **Token Validation**: `JwtValidationFilter` validates every request:
   - Checks token signature
   - Verifies token hasn't expired
   - Checks token isn't blacklisted
   - Checks user isn't deleted
7. **Logout**: Token is added to Redis blacklist

### User Registration

The system uses a controlled registration process:

- **Students**: Can only be registered by INSTRUCTOR or ADMIN roles
  - Instructors register students for their courses
  - Students cannot self-register (prevents unauthorized access)
  
- **Instructors**: Can only be registered by ADMIN role
  - Maintains system integrity
  - Prevents unauthorized instructor accounts

- **Initial Setup**: First ADMIN account must be created directly in database

### Authorization

Role-based access control is implemented using `@PreAuthorize` annotations:

**STUDENT Role:**
- View exams in their groups
- Submit code solutions
- View own submissions and results
- Finish exams

**INSTRUCTOR Role:**
- All student permissions
- Create and manage groups
- Add students to groups
- Create and manage exams
- Create problems and test cases
- Activate/deactivate exams
- View all student submissions in their exams
- Register new students

**ADMIN Role:**
- Full system access
- Register new instructors
- Access all groups and exams
- System-wide monitoring

### Token Blacklist

Redis is used to maintain a blacklist for:
- **Logged out tokens** (`blisted_token:{token}`): Prevents use of old tokens after logout
- **Deleted user accounts** (`blisted_user:{userId}`): Prevents deleted users from accessing system

### Security Features

- **Password Encryption**: BCrypt hashing with salt
- **JWT Signature**: HMAC-SHA256 algorithm with 256-bit secret key
- **Token Expiration**: 3-hour validity period
- **CORS Protection**: Configurable allowed origins
- **SQL Injection Prevention**: JPA/Hibernate parameterized queries
- **XSS Protection**: Input validation and output encoding
- **CSRF Disabled**: Stateless API design with token-based auth
- **Method-level Security**: `@PreAuthorize` on controller methods
- **Request Filtering**: Custom JWT validation filter on all protected endpoints

## 📊 Exam Workflow

### For Instructors

1. Create a group and add students
2. Create an exam for the group
3. Add problems to the exam
4. Add test cases to each problem
5. Activate the exam when ready
6. Monitor student progress in real-time
7. Deactivate exam after completion
8. Review results and statistics

### For Students

1. View available exams in their groups
2. Start an exam (status changes to IN_PROGRESS)
3. View problems and submit solutions
4. See immediate feedback on test cases
5. Submit multiple attempts per problem
6. Finish exam when complete (status changes to COMPLETED)
7. View final scores and results

## 🎯 Key Features Explained

### Asynchronous Submission Processing

Submissions are processed asynchronously to avoid blocking:

```java
@Async
public CompletableFuture<Void> processSubmissionAsync(Submission submission, Problem problem)
```

This allows students to continue working while their code is being evaluated.

### Test Case Visibility

Test cases can be marked as hidden or visible:
- **Visible**: Students see input, expected output, and their actual output
- **Hidden**: Students only see if they passed or failed

### Exam Time Management

The system supports flexible time management:
- **Start Time**: When the exam becomes available
- **End Time**: When the exam closes
- **Duration**: Maximum time per student (starts when they begin)

### Score Calculation

Scores are calculated at multiple levels:
- **Test Case Level**: Points per test case
- **Problem Level**: Sum of passed test case points
- **Exam Level**: Best submission score per problem

### Judge0 Integration

The system integrates with Judge0 for secure code execution:
- Supports 60+ programming languages
- Configurable time and memory limits
- Detailed execution results
- Retry logic for reliability

## 🐛 Error Handling

The system includes comprehensive error handling:

- **BadRequestException**: Invalid input or business rule violations
- **NotFoundException**: Resource not found
- **UnauthorizedException**: Access denied
- **JudgeException**: Judge0 API errors
- **AuthException**: Authentication failures

All errors return standardized JSON responses:

```json
{
  "status": 400,
  "code": "BAD_REQUEST",
  "message": "Exam has already started",
  "timestamp": "2024-01-22T10:30:00"
}
```

## 🧪 Testing

Run tests with:

```bash
mvn test
```

## 📝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request


## 👥 Authors

- Agil Saidov - The whole project

Made with ❤️ using Spring Boot
