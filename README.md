# GymFlow Project Workspace

**GymFlow** is a JavaFX-based multi-user desktop application for managing gym memberships, workout planning, attendance, and equipment tracking. The project is designed to meet all course rubric requirements, including:

- Object-oriented design (MVC)
- Database integration (H2, SQL seed data)
- JavaFX GUI with FXML and CSS
- File I/O (CSV import/export for attendance and workout plans)
- Robust exception handling (custom exceptions)
- Unit and integration testing (JUnit 5)
- Persistent data storage
- Architecture and design documentation


## Repository Layout

- `gymflow-app/` – Main application code (controllers, services, DAOs, models, security, utils)
- `docs/` – Proposal, architecture diagrams (UML, ER, sequence), and test evidence
- `db/` – SQL schema and comprehensive seed data for all entities
- `scripts/` – Helper scripts for running, packaging, and linting
- `IMPLEMENTATION_PLAN.md` – Task checklist and progress tracking


## Getting Started

1. **Install Java 17+ and Maven** ([Download Maven](https://maven.apache.org/download.cgi))
2. **Import Project:** Open `gymflow-app` in IntelliJ as a Maven project (`File → Open → pom.xml`)
3. **Run the App:**
  - In terminal: `cd gymflow-app && mvn javafx:run`
  - Or: `./scripts/run.sh` from the project root
4. **Database:**
  - H2 in-memory database auto-initializes on launch
  - Schema and seed data in `db/schema.sql` and `db/seed-data.sql`
5. **Testing:**
  - Run all tests: `mvn test` (JUnit 5)
  - Test evidence and reports in `docs/test-reports/`
6. **Import/Export Data:**
  - Use the UI or service layer to import/export attendance and workout plans as CSV files


## Key Features

- **Role-based access:** Admin, Trainer, and Member dashboards
- **Workout plan and attendance management:** Create, edit, import/export via CSV
- **Robust exception handling:** Custom exceptions for authentication, validation, file I/O, and data access
- **Comprehensive seed data:** Users, roles, equipment, workout plans, class sessions, attendance
- **Unit and integration tests:** Extensive coverage for all services and DAOs
- **Architecture diagrams:** UML class, ER, and sequence diagrams in `docs/architecture-diagrams/`
- **User-friendly UI:** JavaFX with FXML and custom CSS

## Documentation & Artifacts

- **Architecture Diagrams:** See `docs/architecture-diagrams/` for UML, ER, and sequence diagrams (PlantUML source included)
- **Test Reports:** See `docs/test-reports/` for JUnit and manual test evidence
- **Seed Data:** See `db/seed-data.sql` for initial database content
- **Implementation Plan:** See `IMPLEMENTATION_PLAN.md` for a full checklist and progress tracking
