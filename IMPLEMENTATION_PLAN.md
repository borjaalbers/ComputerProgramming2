# GymFlow Implementation Plan

This document tracks all tasks required to complete the GymFlow project and achieve maximum marks. Check off items as you complete them.

## Phase 1: Project Setup & Documentation ✅

### Documentation Requirements
- [ ] **Project Title and Description** (200-300 words)
  - [ ] Clear, descriptive project name
  - [ ] Description of what the system does
  - [ ] Identification of OOP as primary paradigm
  - [ ] Document saved in `docs/proposal/`

- [ ] **Significance and Innovation**
  - [ ] Explanation of project value
  - [ ] Problem identification
  - [ ] Uniqueness compared to existing solutions
  - [ ] Target market/audience analysis
  - [ ] Document saved in `docs/proposal/`

- [ ] **User Analysis**
  - [ ] Define at least 3 distinct user types (Member, Trainer, Administrator)
  - [ ] Describe characteristics, needs, and technical proficiency for each
  - [ ] Document saved in `docs/proposal/`

- [ ] **Architecture Diagrams**
  - [ ] UML Class Diagram (showing inheritance hierarchy)
  - [ ] ER Diagram (database schema)
  - [ ] System Flowchart (MVC architecture)
  - [ ] Sequence Diagrams (key workflows)
  - [ ] All diagrams saved in `docs/architecture-diagrams/`

## Phase 2: Core Java Classes (10+ Classes Required)

### Model Layer (Domain Classes)
- [x] `User` (abstract base class)
- [x] `Member` (extends User)
- [x] `Trainer` (extends User)
- [x] `Administrator` (extends User)
- [ ] `WorkoutPlan` (with relationships to Member/Trainer)
- [ ] `ClassSession` (with schedule and capacity)
- [ ] `AttendanceRecord` (links Member to ClassSession)
- [ ] `Equipment` (with status tracking)
- [ ] Additional model classes as needed (e.g., `Exercise`, `WorkoutTemplate`)

### Service Layer (Business Logic)
- [ ] `AuthService` (interface) - authentication logic
- [ ] `AuthServiceImpl` (implementation)
- [ ] `WorkoutService` - workout plan management
- [ ] `ClassScheduleService` - class scheduling
- [ ] `AttendanceService` - attendance tracking
- [ ] `EquipmentService` - equipment management
- [ ] `UserService` - user account management

### DAO Layer (Data Access)
- [ ] `UserDao` (interface)
- [ ] `UserDaoImpl` (JDBC implementation)
- [ ] `WorkoutPlanDao` (interface + implementation)
- [ ] `ClassSessionDao` (interface + implementation)
- [ ] `AttendanceDao` (interface + implementation)
- [ ] `EquipmentDao` (interface + implementation)

### Controller Layer (JavaFX)
- [x] `LoginController` (placeholder)
- [ ] `MemberDashboardController`
- [ ] `TrainerDashboardController`
- [ ] `AdminDashboardController`
- [ ] `WorkoutPlanController`
- [ ] `ClassScheduleController`
- [ ] `AttendanceController`
- [ ] `EquipmentController`

### Utility & Support Classes
- [x] `PasswordHasher` (security)
- [x] `CsvUtil` (file I/O placeholder)
- [x] `DatabaseConfig` (configuration)
- [ ] `FileImportExportService` (complete file I/O implementation)
- [ ] Custom exception classes (e.g., `AuthenticationException`, `DataAccessException`)
- [ ] Validation utilities

**Total Classes Target:** 10+ (currently have 7 placeholders, need to complete and add more)

## Phase 3: Database Implementation

### Schema Design
- [x] `schema.sql` created
- [ ] Review and finalize table structure (3-5 related tables minimum)
- [ ] Add foreign key constraints
- [ ] Add indexes for performance
- [ ] Add check constraints for data integrity

### Database Tables Required
- [x] `roles` table
- [x] `users` table
- [x] `workout_plans` table
- [x] `class_sessions` table
- [x] `attendance_records` table
- [x] `equipment` table
- [ ] Additional tables if needed (e.g., `exercises`, `workout_exercises`)

### Data Access Implementation
- [ ] Create `DatabaseConnection` class (singleton pattern)
- [ ] Implement all DAO interfaces with JDBC
- [ ] Add connection pooling (optional but recommended)
- [ ] Implement transaction management
- [ ] Add proper resource cleanup (try-with-resources)

### Seed Data
- [x] `seed-data.sql` created
- [ ] Complete seed data with realistic test users
- [ ] Add sample workout plans
- [ ] Add sample class sessions
- [ ] Add sample equipment entries

## Phase 4: JavaFX GUI Implementation

### FXML Views
- [x] `login.fxml` (placeholder)
- [ ] `member-dashboard.fxml`
- [ ] `trainer-dashboard.fxml`
- [ ] `admin-dashboard.fxml`
- [ ] `workout-plan-view.fxml`
- [ ] `class-schedule-view.fxml`
- [ ] `attendance-view.fxml`
- [ ] `equipment-management.fxml`
- [ ] `user-management.fxml` (admin only)

### Controllers
- [x] `LoginController` (basic structure)
- [ ] Complete login authentication flow
- [ ] Implement role-based navigation
- [ ] Wire up all FXML views to controllers
- [ ] Implement data binding (ObservableList, Property)
- [ ] Add input validation in controllers

### UI/UX Features
- [ ] Responsive layouts
- [ ] Error dialogs for exceptions
- [ ] Success confirmations
- [ ] Loading indicators
- [ ] Form validation feedback
- [ ] CSS styling (`app.css`)
- [ ] Consistent navigation between views

## Phase 5: Multi-User & Authentication

### Authentication System
- [ ] Complete `PasswordHasher` implementation (consider BCrypt)
- [ ] Implement login flow in `AuthService`
- [ ] Store session state (current user, role)
- [ ] Add logout functionality
- [ ] Password reset flow (optional)

### Authorization (Role-Based Access)
- [ ] Define access levels for each role:
  - [ ] Member: Read-only personal data
  - [ ] Trainer: Workout creation, class management
  - [ ] Administrator: Full system access
- [ ] Implement authorization checks in services
- [ ] Add UI visibility controls based on role
- [ ] Prevent unauthorized actions

### Concurrent User Support
- [ ] Test multiple simultaneous logins
- [ ] Handle concurrent database access
- [ ] Implement optimistic locking if needed
- [ ] Test data consistency with multiple users

## Phase 6: File I/O Operations

### Import/Export Functionality
- [ ] Complete `CsvUtil` implementation
- [ ] Export workout templates to CSV
- [ ] Import workout templates from CSV
- [ ] Export attendance reports
- [ ] Handle file format errors gracefully
- [ ] Add file validation (format, size limits)
- [ ] Provide user feedback during import/export

### Exception Handling for File I/O
- [ ] `FileNotFoundException` handling
- [ ] `IOException` handling
- [ ] Invalid format exceptions
- [ ] User-friendly error messages

## Phase 7: Exception Handling

### Exception Hierarchy
- [ ] Create custom exception classes:
  - [ ] `GymFlowException` (base exception)
  - [ ] `AuthenticationException`
  - [ ] `DataAccessException`
  - [ ] `ValidationException`
  - [ ] `FileOperationException`

### Exception Handling Strategy
- [ ] Try-catch blocks in all DAO methods
- [ ] Service layer exception handling
- [ ] Controller-level exception handling
- [ ] Global exception handler for JavaFX
- [ ] Logging exceptions (consider adding logging framework)
- [ ] User-friendly error messages (no stack traces to users)

## Phase 8: Unit Testing

### Test Coverage Requirements
- [ ] Test all service layer methods
- [ ] Test DAO layer (use H2 in-memory database)
- [ ] Test utility classes (`PasswordHasher`, `CsvUtil`)
- [ ] Test exception handling
- [ ] Test validation logic
- [ ] Achieve >80% code coverage

### Test Classes to Create
- [x] `PasswordHasherTest` (basic test exists)
- [ ] `AuthServiceTest`
- [ ] `WorkoutServiceTest`
- [ ] `ClassScheduleServiceTest`
- [ ] `AttendanceServiceTest`
- [ ] `UserDaoTest`
- [ ] `WorkoutPlanDaoTest`
- [ ] `CsvUtilTest`
- [ ] Additional tests for edge cases

### Test Organization
- [ ] All tests in `src/test/java/com/gymflow/`
- [ ] Use JUnit 5
- [ ] Use Mockito for mocking (if needed)
- [ ] Test reports saved in `docs/test-reports/`

## Phase 9: Data Persistence

### Persistence Implementation
- [ ] Ensure all user actions save to database
- [ ] Test data persistence across application restarts
- [ ] Verify foreign key relationships are maintained
- [ ] Test data integrity constraints
- [ ] Implement backup/restore functionality (optional)

### Session Management
- [ ] Save user preferences (if applicable)
- [ ] Remember last login (optional)
- [ ] Maintain session state during application run

## Phase 10: Code Quality & Best Practices

### Code Organization
- [ ] Follow MVC architecture consistently
- [ ] Proper package structure
- [ ] Meaningful class and method names
- [ ] Code comments for complex logic
- [ ] Remove all TODO comments or address them

### OOP Principles
- [ ] Demonstrate inheritance (User hierarchy) ✅
- [ ] Use abstraction (abstract classes, interfaces)
- [ ] Encapsulation (private fields, getters/setters)
- [ ] Polymorphism (method overriding, interface implementations)

### Git & Version Control
- [ ] Regular commits with descriptive messages
- [ ] Clean commit history
- [ ] No large binary files in repo
- [ ] `.gitignore` properly configured ✅
- [ ] GitHub Classroom repository set up

## Phase 11: Final Documentation & Submission

### README Updates
- [ ] Complete project description
- [ ] Installation instructions
- [ ] How to run the application
- [ ] How to run tests
- [ ] Database setup instructions
- [ ] Screenshots of the application
- [ ] Architecture overview

### Proposal Documents
- [ ] Finalize all proposal sections
- [ ] Review for grammar and clarity
- [ ] Ensure all requirements are addressed
- [ ] Add conclusion section

### Diagrams
- [ ] Finalize all architecture diagrams
- [ ] Ensure diagrams match implementation
- [ ] Export diagrams in required format (PNG/SVG)

### Test Reports
- [ ] Generate code coverage report
- [ ] Document test cases
- [ ] Save test evidence in `docs/test-reports/`

### Final Checklist Before Submission
- [ ] All 10+ classes implemented and tested
- [ ] Database with 3-5 related tables working
- [ ] JavaFX GUI fully functional
- [ ] File I/O operations working
- [ ] Exception handling throughout
- [ ] Unit tests passing
- [ ] Data persistence verified
- [ ] Multi-user support tested
- [ ] All documentation complete
- [ ] Code compiles without errors
- [ ] Application runs successfully
- [ ] GitHub repository is up to date

## Grading Rubric Mapping

Ensure each requirement is explicitly addressed:

- [ ] **10+ Java classes** with inheritance hierarchy ✅ (structure in place, need completion)
- [ ] **Database** with 3-5 related tables ✅ (schema created)
- [ ] **GUI interface** using JavaFX ✅ (structure in place)
- [ ] **File I/O operations** for data import/export (placeholder exists)
- [ ] **Exception handling** throughout the application (need implementation)
- [ ] **Unit testing** for core functionality (one test exists)
- [ ] **Data persistence** across sessions (need implementation)
- [ ] **OOP principles** demonstrated (inheritance ✅, need abstraction/interfaces)
- [ ] **Multi-user support** with authentication/authorization (structure in place)

---

**Last Updated:** [Date]
**Current Status:** Phase 1 Complete, Phase 2-11 In Progress

