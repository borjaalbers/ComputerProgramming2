# GymFlow Project Plan - Kanban Model

## Team Organization

### Team Members
- **Borja** - Backend Developer & Project Coordinator
- **Lucas** - Frontend Developer (JavaFX)
- **Gregorio** - Database Developer
- **Faisal** - Testing & Quality Assurance
- **Hala** - Documentation & Integration Specialist

### Team Structure
Our team is organized using a **Kanban workflow** with three main columns:
- **To Do**: Tasks not yet started
- **In Progress**: Tasks currently being worked on
- **Done**: Completed tasks

We use **GitHub Issues/Projects** for task tracking and **daily standups** (virtual) to coordinate progress.

---

## Individual Roles & Assigned Tasks

### Borja - Backend Developer & Project Coordinator
**Role**: Lead backend development, coordinate team efforts, manage model/service layers

**Assigned Tasks**:
- ✅ Project skeleton setup (Maven, structure)
- 🔄 Phase 2: Model Layer - User hierarchy enhancement
- ⏳ Phase 2: Service Layer - AuthService, WorkoutService, ClassScheduleService
- ⏳ Phase 2: Service Layer - AttendanceService, EquipmentService, UserService
- ⏳ Phase 5: Authentication System implementation
- ⏳ Phase 5: Authorization (Role-Based Access) implementation
- ⏳ Phase 7: Exception Hierarchy (custom exceptions)
- ⏳ Phase 7: Exception Handling Strategy
- ⏳ Phase 10: Code Quality & OOP Principles review
- ⏳ Team coordination and code reviews

**Progress Status**: 
- **Completed**: Project structure, initial model placeholders
- **In Progress**: User model hierarchy enhancement
- **Upcoming**: Service layer implementation, authentication system

---

### Lucas - Frontend Developer (JavaFX)
**Role**: Design and implement all JavaFX GUI components, controllers, and user interface

**Assigned Tasks**:
- ✅ Login FXML placeholder
- ⏳ Phase 4: FXML Views - All dashboard views (Member, Trainer, Admin)
- ⏳ Phase 4: FXML Views - Workout plan, class schedule, attendance views
- ⏳ Phase 4: FXML Views - Equipment management, user management views
- ⏳ Phase 4: Controllers - Complete login authentication flow
- ⏳ Phase 4: Controllers - All dashboard controllers
- ⏳ Phase 4: Controllers - Workout, schedule, attendance controllers
- ⏳ Phase 4: UI/UX Features - Responsive layouts, error dialogs
- ⏳ Phase 4: UI/UX Features - CSS styling, navigation
- ⏳ Phase 5: UI visibility controls based on role
- ⏳ Phase 6: File I/O UI - Import/export dialogs

**Progress Status**:
- **Completed**: Basic login view structure
- **In Progress**: Dashboard layouts design
- **Upcoming**: Controller implementation, UI styling

---

### Gregorio - Database Developer
**Role**: Database schema design, DAO implementation, data persistence, connection management

**Assigned Tasks**:
- ✅ Database schema.sql creation
- ✅ Seed data.sql creation
- ⏳ Phase 3: Schema Design - Finalize table structure, add constraints
- ⏳ Phase 3: Schema Design - Add indexes, check constraints
- ⏳ Phase 3: Data Access - DatabaseConnection class (singleton)
- ⏳ Phase 2: DAO Layer - UserDao implementation (JDBC)
- ⏳ Phase 2: DAO Layer - WorkoutPlanDao, ClassSessionDao
- ⏳ Phase 2: DAO Layer - AttendanceDao, EquipmentDao
- ⏳ Phase 3: Seed Data - Complete with realistic test data
- ⏳ Phase 3: Transaction management, connection pooling
- ⏳ Phase 9: Data Persistence - Verify all CRUD operations
- ⏳ Phase 9: Data Persistence - Test foreign key relationships

**Progress Status**:
- **Completed**: Initial schema and seed data structure
- **In Progress**: Schema refinement and constraint addition
- **Upcoming**: DAO implementations, connection management

---

### Faisal - Testing & Quality Assurance
**Role**: Unit testing, integration testing, exception handling validation, code quality

**Assigned Tasks**:
- ✅ PasswordHasherTest (basic test)
- ⏳ Phase 8: Unit Testing - Service layer tests (AuthService, WorkoutService, etc.)
- ⏳ Phase 8: Unit Testing - DAO layer tests (UserDao, WorkoutPlanDao, etc.)
- ⏳ Phase 8: Unit Testing - Utility class tests (CsvUtil, validation)
- ⏳ Phase 8: Unit Testing - Exception handling tests
- ⏳ Phase 5: Concurrent User Support - Multi-user testing
- ⏳ Phase 7: Exception Handling - Test exception scenarios
- ⏳ Phase 9: Data Persistence - Test data integrity
- ⏳ Phase 8: Code coverage reports (>80% target)
- ⏳ Phase 10: Code Quality - Review OOP principles implementation
- ⏳ Phase 11: Test Reports - Generate and document test evidence

**Progress Status**:
- **Completed**: Initial test structure
- **In Progress**: Test framework setup
- **Upcoming**: Comprehensive test suite development

---

### Hala - Documentation & Integration Specialist
**Role**: Documentation, diagrams, file I/O operations, final integration, proposal writing

**Assigned Tasks**:
- ⏳ Phase 1: Project Title and Description (200-300 words)
- ⏳ Phase 1: Significance and Innovation document
- ⏳ Phase 1: User Analysis document
- ⏳ Phase 1: Architecture Diagrams - UML Class Diagram
- ⏳ Phase 1: Architecture Diagrams - ER Diagram
- ⏳ Phase 1: Architecture Diagrams - System Flowchart
- ⏳ Phase 1: Architecture Diagrams - Sequence Diagrams
- ⏳ Phase 6: File I/O Operations - Complete CsvUtil implementation
- ⏳ Phase 6: File I/O Operations - Import/export functionality
- ⏳ Phase 6: File I/O Exception Handling
- ⏳ Phase 11: README Updates - Complete documentation
- ⏳ Phase 11: Proposal Documents - Finalize all sections
- ⏳ Phase 11: Diagrams - Finalize and export all diagrams
- ⏳ Phase 11: Final integration testing

**Progress Status**:
- **Completed**: Project plan structure
- **In Progress**: Use-case diagram, proposal documents
- **Upcoming**: Architecture diagrams, file I/O implementation

---

## Kanban Board Status

### To Do (Not Started)
- Model classes: WorkoutPlan, ClassSession, AttendanceRecord, Equipment
- All service implementations
- All DAO implementations
- All FXML views and controllers
- Complete authentication/authorization
- File I/O operations
- Exception hierarchy
- Unit tests (except PasswordHasherTest)
- Final documentation

### In Progress (Current Sprint)
- User model hierarchy enhancement (Borja)
- Database schema refinement (Gregorio)
- Use-case diagram creation (Hala)
- Test framework setup (Faisal)
- Dashboard layout design (Lucas)

### Done (Completed)
- Project skeleton and structure
- Maven build configuration
- Basic database schema
- Login FXML placeholder
- PasswordHasher utility
- Basic User model classes (placeholders)
- Implementation plan document

---

## Progress Tracking

### Overall Project Progress: ~15%

**By Phase**:
- Phase 1 (Documentation): 20% - Structure in place, content in progress
- Phase 2 (Core Classes): 25% - Basic structure, need enhancement
- Phase 3 (Database): 30% - Schema created, needs refinement
- Phase 4 (JavaFX GUI): 10% - Login view only
- Phase 5 (Authentication): 0% - Not started
- Phase 6 (File I/O): 5% - Placeholder exists
- Phase 7 (Exception Handling): 0% - Not started
- Phase 8 (Unit Testing): 5% - One test exists
- Phase 9 (Data Persistence): 0% - Not started
- Phase 10 (Code Quality): 10% - Structure in place
- Phase 11 (Final Documentation): 15% - Plan created

---

## Communication & Collaboration

### Communication Channels
- **GitHub**: Code repository, issues, pull requests
- **Daily Standups**: Brief updates on progress and blockers
- **Weekly Meetings**: Detailed progress review and planning

### Collaboration Practices
- All code changes via pull requests with code review
- Regular commits with descriptive messages
- Branch strategy: `main` (stable), `develop` (integration), feature branches
- Documentation updates synchronized with code changes

---

## Diagram Links

**Note**: Diagrams will be created and stored in `docs/architecture-diagrams/`. Once created using Draw.io or similar tools, links will be added here.

### Planned Diagrams:
1. **Use-Case Diagram** - [To be created] - `docs/architecture-diagrams/use-case-diagram.png`
2. **UML Class Diagram** - [To be created] - `docs/architecture-diagrams/class-diagram.png`
3. **ER Diagram** - [To be created] - `docs/architecture-diagrams/er-diagram.png`
4. **System Flowchart** - [To be created] - `docs/architecture-diagrams/system-flowchart.png`
5. **Sequence Diagrams** - [To be created] - `docs/architecture-diagrams/sequence-diagrams/`

**Diagram Creation Tool**: Draw.io (https://app.diagrams.net/) - Free, no account required

---

## Risk Management

### Identified Risks
1. **Time Management**: Large scope, need to prioritize core features
2. **Integration Challenges**: Multiple layers need to work together
3. **Database Complexity**: Ensuring proper relationships and constraints
4. **JavaFX Learning Curve**: Team members new to JavaFX

### Mitigation Strategies
1. Focus on MVP first (login, basic CRUD, one role dashboard)
2. Regular integration testing as features are completed
3. Database schema reviewed early and iteratively
4. Pair programming for JavaFX components

---

## Next Steps (This Week)

1. **Borja**: Complete User model hierarchy enhancement
2. **Lucas**: Design and create Member dashboard FXML
3. **Gregorio**: Finalize database schema with all constraints
4. **Faisal**: Set up test framework and write first DAO test
5. **Hala**: Complete use-case diagram and start proposal documents

---

**Last Updated**: [Current Date]
**Next Review**: [Weekly Review Date]

