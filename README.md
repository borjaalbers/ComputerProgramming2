# GymFlow - Fitness Management System

**GymFlow** is a comprehensive, multi-user desktop application built with JavaFX that streamlines gym operations by managing memberships, workout planning, class scheduling, attendance tracking, and equipment inventory. The system demonstrates enterprise-level Java development with object-oriented design, database integration, and modern UI/UX.

## 🎯 Project Overview

GymFlow addresses the operational challenges faced by small to mid-sized gyms by providing an integrated management platform. The system combines membership management, workout planning, attendance tracking, and equipment monitoring into one cohesive solution, specifically tailored for trainers and class organization.

### Key Features

- **Multi-User Authentication**: Role-based access control (Member, Trainer, Administrator)
- **Workout Plan Management**: Create, assign, and track personalized workout plans
- **Class Scheduling**: Schedule classes with capacity management and workout plan assignments
- **Attendance Tracking**: Register for classes and track attendance history
- **Equipment Management**: Monitor equipment status and service schedules
- **File I/O Operations**: Import/export workout templates and attendance reports via CSV
- **Modern UI**: Smooth, responsive JavaFX interface with gradient styling
- **Data Persistence**: H2 database with automatic initialization and seed data

## 🏗️ Architecture

GymFlow follows a **layered MVC architecture**:

- **Controller Layer**: JavaFX controllers handling user interactions
- **Service Layer**: Business logic implementation (interfaces + implementations)
- **DAO Layer**: Data access objects for database operations
- **Model Layer**: Domain entities with inheritance hierarchy (User → Member/Trainer/Admin)

### Technology Stack

- **Language**: Java 17
- **UI Framework**: JavaFX 21
- **Database**: H2 (embedded, file-based persistence)
- **Build Tool**: Maven
- **Testing**: JUnit 5
- **Code Coverage**: JaCoCo

## 📋 Requirements Met

✅ **10+ Java Classes** - 40+ classes with proper inheritance hierarchy  
✅ **Database** - 6 related tables with foreign key constraints  
✅ **JavaFX GUI** - Complete interface with role-based dashboards  
✅ **File I/O** - CSV import/export for workout templates and reports  
✅ **Exception Handling** - Custom exception hierarchy with comprehensive error handling  
✅ **Unit Testing** - 12 test classes with 90+ test methods  
✅ **Data Persistence** - H2 database with automatic initialization  
✅ **OOP Principles** - Inheritance, abstraction, interfaces, polymorphism demonstrated  
✅ **Multi-User Support** - Authentication and role-based authorization  

## 🚀 Getting Started

### Prerequisites

- **Java 17 or higher** - [Download Java](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.6+** - [Download Maven](https://maven.apache.org/download.cgi)
- **IntelliJ IDEA** (recommended) or any Java IDE

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ComputerProgramming2
   ```

2. **Import into IntelliJ**
   - Open IntelliJ IDEA
   - File → Open → Select `gymflow-app/pom.xml`
   - IntelliJ will automatically import as a Maven project
   - Wait for Maven dependencies to download

3. **Verify Setup**
   ```bash
   cd gymflow-app
   mvn clean compile
   ```

### Running the Application

**Option 1: Using Maven (Recommended)**
```bash
cd gymflow-app
mvn javafx:run
```

**Option 2: Using Run Script**
```bash
./scripts/run.sh
```

**Option 3: From IntelliJ**
- Right-click `MainApp.java` → Run 'MainApp.main()'

### Test Credentials

The application automatically initializes with demo users:

| Username | Password | Role |
|----------|----------|------|
| `member_demo` | `password123` | Member |
| `trainer_demo` | `password123` | Trainer |
| `admin_demo` | `password123` | Administrator |

### Database Setup

The database is **automatically initialized** on first run:
- Tables are created automatically
- Seed data is inserted (test users, sample workout plans, classes, equipment)
- Database file: `gymflow-app/data/gymflow.mv.db`
- No manual setup required!

## 🧪 Running Tests

### Run All Tests
```bash
cd gymflow-app
mvn test
```

### Generate Test Coverage Report
```bash
cd gymflow-app
mvn clean test jacoco:report
```

Coverage report will be generated at: `gymflow-app/target/site/jacoco/index.html`

### Test Classes

The project includes comprehensive test coverage:
- `PasswordHasherTest` - Security utility tests
- `AuthServiceTest` - Authentication logic tests
- `UserDaoTest` - User data access tests
- `WorkoutServiceTest` - Workout plan service tests
- `ClassScheduleServiceTest` - Class scheduling tests
- `AttendanceServiceTest` - Attendance tracking tests
- `WorkoutPlanDaoTest` - Workout plan DAO tests
- `ClassSessionDaoTest` - Class session DAO tests
- `AttendanceDaoTest` - Attendance DAO tests
- `CsvUtilTest` - File I/O utility tests
- `ExceptionHandlingTest` - Exception handling tests

## 📁 Project Structure

```
ComputerProgramming2/
├── gymflow-app/              # Main application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/gymflow/
│   │   │   │   ├── config/      # Database configuration
│   │   │   │   ├── controller/  # JavaFX controllers
│   │   │   │   ├── dao/         # Data access objects
│   │   │   │   ├── exception/   # Custom exceptions
│   │   │   │   ├── model/       # Domain models
│   │   │   │   ├── security/    # Authentication
│   │   │   │   ├── service/     # Business logic
│   │   │   │   └── util/        # Utilities (CSV, etc.)
│   │   │   └── resources/
│   │   │       ├── css/         # Stylesheets
│   │   │       ├── fxml/        # UI layouts
│   │   │       └── i18n/        # Internationalization
│   │   └── test/                # Test classes
│   ├── data/                    # H2 database files
│   └── pom.xml                  # Maven configuration
├── docs/                        # Documentation
│   ├── architecture-diagrams/  # UML, ER diagrams
│   ├── proposal/                # Project proposal
│   └── test-reports/           # Test evidence
├── db/                          # SQL scripts
│   ├── schema.sql              # Database schema
│   └── seed-data.sql           # Seed data template
├── scripts/                     # Helper scripts
│   ├── run.sh                  # Run application
│   ├── package.sh              # Package application
│   └── lint.sh                 # Run tests
└── README.md                    # This file
```

## 🎨 User Roles & Capabilities

### Member
- View assigned workout plans
- View class schedules
- Register/unregister for classes
- View attendance history
- Mark workouts as completed

### Trainer
- Create and manage workout plans
- Create and manage class sessions
- Assign workout plans to classes
- View members' workout plans
- Import/export workout templates (CSV)

### Administrator
- Manage all users (create, edit, delete)
- Manage equipment inventory
- View system statistics
- Export attendance reports (CSV)
- Full system access

## 🔧 Key Components

### Model Classes
- `User` (abstract) - Base class for all users
- `Member`, `Trainer`, `Administrator` - User role implementations
- `WorkoutPlan` - Workout plan entity
- `ClassSession` - Class scheduling entity
- `AttendanceRecord` - Attendance tracking
- `Equipment` - Equipment inventory
- `WorkoutCompletion` - Workout completion tracking

### Services
- `AuthService` - Authentication and authorization
- `UserService` - User management
- `WorkoutService` - Workout plan management
- `ClassScheduleService` - Class scheduling
- `AttendanceService` - Attendance tracking
- `EquipmentService` - Equipment management
- `FileImportExportService` - CSV import/export

### DAOs
- `UserDao` - User data access
- `WorkoutPlanDao` - Workout plan data access
- `ClassSessionDao` - Class session data access
- `AttendanceDao` - Attendance data access
- `EquipmentDao` - Equipment data access

## 📊 Database Schema

The database consists of 6 related tables:

1. **roles** - User role definitions
2. **users** - User accounts (references roles)
3. **workout_plans** - Workout plans (references users for member/trainer)
4. **class_sessions** - Class schedules (references trainers and workout_plans)
5. **attendance_records** - Attendance tracking (references class_sessions and users)
6. **equipment** - Equipment inventory
7. **workout_completions** - Workout completion tracking

All tables include foreign key constraints ensuring data integrity.

## 🎯 Features in Detail

### File I/O Operations
- **Export Workout Templates**: Trainers can export workout plans to CSV
- **Import Workout Templates**: Trainers can import workout plans from CSV
- **Export Attendance Reports**: Administrators can export attendance data to CSV
- **File Validation**: Size limits, format validation, error handling

### Exception Handling
- Custom exception hierarchy:
  - `GymFlowException` (base)
  - `AuthenticationException`
  - `DataAccessException`
  - `ValidationException`
  - `FileOperationException`
- Comprehensive try-catch blocks in all DAO methods
- User-friendly error messages (no stack traces)

### UI/UX Features
- Modern gradient styling
- Smooth hover animations
- Responsive layouts
- Color-coded dashboards by role
- Professional table styling
- Focus states for form fields

## 📸 Screenshots

> **Note**: Screenshots should be added here showing:
> - Login screen
> - Member dashboard
> - Trainer dashboard
> - Admin dashboard
> - Workout plan creation
> - Class registration
> - User management

## 🧪 Testing

### Test Coverage
- **12 test classes** covering all major components
- **90+ test methods** validating functionality
- Integration tests using H2 in-memory database
- Unit tests for utilities and services

### Running Tests
```bash
cd gymflow-app
mvn test
```

### View Coverage Report
```bash
cd gymflow-app
mvn jacoco:report
# Open: target/site/jacoco/index.html
```

## 📚 Documentation

- **Proposal Documents**: `docs/proposal/`
- **Architecture Diagrams**: `docs/architecture-diagrams/`
- **Test Reports**: `docs/test-reports/`
- **Implementation Plan**: `IMPLEMENTATION_PLAN.md`

## 🛠️ Development

### Building the Project
```bash
cd gymflow-app
mvn clean package
```

### Creating Executable JAR
```bash
cd gymflow-app
mvn clean package
# Executable JAR: target/gymflow-app-1.0.0.jar
```

### Code Quality
```bash
./scripts/lint.sh
```

## 🐛 Troubleshooting

### Database Issues
- If database errors occur, delete `gymflow-app/data/gymflow.mv.db` and restart
- The database will be recreated automatically

### JavaFX Runtime Issues
- Ensure Java 17+ is installed
- Verify JavaFX dependencies are downloaded (Maven will handle this)

### Port Conflicts
- H2 database uses file-based storage, no port conflicts
- If issues occur, check file permissions in `gymflow-app/data/`

## 📝 License

This project is developed for academic purposes as part of a Computer Programming course.

## 👥 Team

- **Borja** - Backend Developer & Project Coordinator
- **Lucas** - Frontend Developer (JavaFX)
- **Gregorio** - Database Developer
- **Faisal** - Testing & Quality Assurance
- **Hala** - Documentation & Integration Specialist

## 📞 Support

For issues or questions, refer to:
- `IMPLEMENTATION_PLAN.md` for project status
- `docs/proposal/` for detailed project documentation
- Test files in `gymflow-app/src/test/` for usage examples

---

**Last Updated**: December 2024  
**Version**: 1.0.0  
**Status**: ✅ Production Ready
