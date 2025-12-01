# GymFlow Implementation Plan

This document tracks all tasks required to complete the GymFlow project and achieve maximum marks. Check off items as you complete them.

## Phase 1: Project Setup & Documentation ✅

### Documentation Requirements
- [x] **Project Title and Description** (200-300 words)
  - [x] Clear, descriptive project name
  - [x] Description of what the system does
  - [x] Identification of OOP as primary paradigm
  - [x] Document saved in `docs/proposal/`

- [x] **Significance and Innovation**
  - [x] Explanation of project value
  - [x] Problem identification
  - [x] Uniqueness compared to existing solutions
  - [x] Target market/audience analysis
  - [x] Document saved in `docs/proposal/`

- [x] **User Analysis**
  - [x] Define at least 3 distinct user types (Member, Trainer, Administrator)
  - [x] Describe characteristics, needs, and technical proficiency for each
  - [x] Document saved in `docs/proposal/`

- [ ] **Architecture Diagrams**
  - [ ] UML Class Diagram (showing inheritance hierarchy)
  - [ ] ER Diagram (database schema)
  - [x] System Flowchart (MVC architecture) - mentioned in proposal
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
- [x] `AuthService` (interface) - authentication logic
- [x] `AuthServiceImpl` (implementation)
- [ ] `WorkoutService` - workout plan management
- [ ] `ClassScheduleService` - class scheduling
- [ ] `AttendanceService` - attendance tracking
- [ ] `EquipmentService` - equipment management
- [ ] `UserService` - user account management

### DAO Layer (Data Access)
- [x] `UserDao` (interface)
- [x] `UserDaoImpl` (JDBC implementation)
- [ ] `WorkoutPlanDao` (interface + implementation)
- [ ] `ClassSessionDao` (interface + implementation)
- [ ] `AttendanceDao` (interface + implementation)
- [ ] `EquipmentDao` (interface + implementation)

### Controller Layer (JavaFX)
- [x] `LoginController` (basic structure - **SIGN IN NOT FULLY WORKING**)
- [x] `MemberDashboardController`
- [x] `TrainerDashboardController`
- [x] `AdminDashboardController`
- [ ] `WorkoutPlanController`
- [ ] `ClassScheduleController`
- [ ] `AttendanceController`
- [ ] `EquipmentController`

### Utility & Support Classes
- [x] `PasswordHasher` (security - SHA-256 implementation)
- [x] `CsvUtil` (file I/O placeholder)
- [x] `DatabaseConfig` (configuration)
- [x] `DatabaseConnection` (singleton pattern)
- [x] `DatabaseInitializer` (database schema and seed data initialization)
- [x] `SessionManager` (session management)
- [x] `UserFactory` (factory pattern for User creation)
- [x] `Role` (enum)
- [ ] `FileImportExportService` (complete file I/O implementation)
- [ ] Custom exception classes (e.g., `AuthenticationException`, `DataAccessException`)
- [ ] Validation utilities

**Total Classes Target:** 10+ (currently have ~15 classes, but need to add more domain models and complete functionality)

## Phase 3: Database Implementation

### Schema Design
- [x] `schema.sql` created
- [x] Review and finalize table structure (3-5 related tables minimum) - 6 tables created
- [x] Add foreign key constraints - foreign keys defined in schema
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
- [x] Create `DatabaseConnection` class (singleton pattern)
- [x] Implement UserDao with JDBC (try-with-resources used)
- [ ] Implement all other DAO interfaces with JDBC
- [ ] Add connection pooling (optional but recommended)
- [ ] Implement transaction management
- [x] Add proper resource cleanup (try-with-resources) - implemented in UserDaoImpl

### Database Initialization
- [x] **CRITICAL: Database schema not automatically initialized on startup** ✅ FIXED
- [x] Create database initialization service/utility - `DatabaseInitializer` created
- [x] Auto-create tables if they don't exist - implemented in `DatabaseInitializer`
- [x] Auto-insert seed data on first run - implemented with test users (password: "password123")

### Seed Data
- [x] `seed-data.sql` created
- [ ] Complete seed data with realistic test users (hashes need to be generated)
- [ ] Add sample workout plans
- [ ] Add sample class sessions
- [ ] Add sample equipment entries

## Phase 4: JavaFX GUI Implementation

### FXML Views
- [x] `login.fxml` (created)
- [x] `member-dashboard.fxml` (created)
- [x] `trainer-dashboard.fxml` (created)
- [x] `admin-dashboard.fxml` (created)
- [ ] `workout-plan-view.fxml`
- [ ] `class-schedule-view.fxml`
- [ ] `attendance-view.fxml`
- [ ] `equipment-management.fxml`
- [ ] `user-management.fxml` (admin only)

### Controllers
- [x] `LoginController` (basic structure - **SIGN IN NOT FULLY WORKING**)
- [x] Complete login authentication flow (implemented but needs debugging)
- [x] Implement role-based navigation (implemented)
- [x] Wire up dashboard FXML views to controllers (done)
- [ ] Implement data binding (ObservableList, Property) - needed for future features
- [x] Add input validation in controllers (basic validation in LoginController)

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
- [x] Complete `PasswordHasher` implementation (SHA-256 - consider BCrypt for production)
- [x] Implement login flow in `AuthService` (implemented but **NOT FULLY WORKING**)
- [x] Store session state (current user, role) - SessionManager implemented
- [x] Add logout functionality - implemented in all dashboard controllers
- [ ] Password reset flow (optional)

### Authorization (Role-Based Access)
- [x] Define access levels for each role:
  - [x] Member: Read-only personal data (dashboard shows user info)
  - [x] Trainer: Workout creation, class management (dashboard created)
  - [x] Administrator: Full system access (dashboard created)
- [ ] Implement authorization checks in services (not yet implemented)
- [x] Add UI visibility controls based on role (different dashboards per role)
- [ ] Prevent unauthorized actions (needs service-level checks)

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
- [x] `AuthServiceTest` (integration test with H2)
- [x] `UserDaoTest` (integration test with H2)
- [ ] `WorkoutServiceTest`
- [ ] `ClassScheduleServiceTest`
- [ ] `AttendanceServiceTest`
- [ ] `WorkoutPlanDaoTest`
- [ ] `CsvUtilTest`
- [ ] Additional tests for edge cases

### Test Organization
- [x] All tests in `src/test/java/com/gymflow/`
- [x] Use JUnit 5
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
- [x] Demonstrate inheritance (User hierarchy) ✅ - User abstract class, Member/Trainer/Admin extend it
- [x] Use abstraction (abstract classes, interfaces) - User is abstract, AuthService/UserDao are interfaces
- [x] Encapsulation (private fields, getters/setters) - all model classes use encapsulation
- [x] Polymorphism (method overriding, interface implementations) - UserFactory uses polymorphism, interfaces implemented

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

- [x] **10+ Java classes** with inheritance hierarchy ✅ (~15 classes implemented: User hierarchy, services, DAOs, controllers, utilities)
- [x] **Database** with 3-5 related tables ✅ (6 tables in schema: roles, users, workout_plans, class_sessions, attendance_records, equipment)
- [x] **GUI interface** using JavaFX ✅ (login + 3 dashboards created, basic navigation working)
- [ ] **File I/O operations** for data import/export (CsvUtil placeholder exists, needs implementation)
- [ ] **Exception handling** throughout the application (basic try-catch in place, need custom exceptions)
- [x] **Unit testing** for core functionality (3 test classes: PasswordHasherTest, AuthServiceTest, UserDaoTest)
- [ ] **Data persistence** across sessions (database connection works, but schema not auto-initialized)
- [x] **OOP principles** demonstrated (inheritance ✅, abstraction ✅, interfaces ✅, polymorphism ✅)
- [x] **Multi-user support** with authentication/authorization (structure in place, but sign-in not working)

---

## Current Priority Issues

### 🟡 HIGH PRIORITY: Sign-In Needs Testing
- Login flow is implemented
- Database initialization is now fixed
- Test users created: member_demo, trainer_demo, admin_demo (password: "password123")
- **Action Required:** Test sign-in functionality to verify it works

### ✅ FIXED: Database Not Auto-Initialized
- DatabaseInitializer created and integrated into MainApp
- Tables are now auto-created on startup
- Seed data with proper password hashes is inserted

### 🟡 HIGH PRIORITY: Missing Domain Models
- Need to create: `WorkoutPlan`, `ClassSession`, `AttendanceRecord`, `Equipment` model classes
- These are required for core functionality

### 🟡 HIGH PRIORITY: Missing Services & DAOs
- Need to implement services and DAOs for workout plans, classes, attendance, equipment
- Required for dashboard functionality

---

**Last Updated:** 2024-12-19
**Current Status:** 
- Phase 1: Mostly Complete (diagrams needed)
- Phase 2: Partially Complete (~15 classes, need domain models)
- Phase 3: Schema created but not auto-initialized
- Phase 4: Basic dashboards created, login needs fixing
- Phase 5: Authentication structure in place but not working
- Phase 6-11: Not Started

