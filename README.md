# GymFlow Project Workspace

This repository hosts **GymFlow**, a JavaFX-based multi-user desktop
application that manages gym memberships, workout planning, attendance, and
equipment tracking. The project is structured to meet the course rubric:
object-oriented design, database integration, GUI, file I/O, exception handling,
unit testing, and persistent data storage.

## Repository Layout

- `gymflow-app/` – Maven-based IntelliJ module containing the JavaFX
  application code (`controller`, `service`, `dao`, `model`, etc.).
- `docs/` – Proposal, diagrams, and testing evidence. Use this folder to store
  all deliverables required for grading.
- `db/` – SQL schema and seed data for local and test databases.
- `scripts/` – Helper scripts for running, packaging, and linting.
- `.github/workflows/` – Continuous integration workflows (to be added later).
- `IMPLEMENTATION_PLAN.md` – Comprehensive checklist of all tasks to complete.

## Getting Started

1. Install Java 17+ and Maven (https://maven.apache.org/download.cgi).
2. Import `gymflow-app` into IntelliJ as a Maven project (File → Open → select `pom.xml`).
3. Use `mvn javafx:run` from the `gymflow-app` directory, or use `./scripts/run.sh` from the project root.

## Next Steps

- Flesh out the JavaFX UI and business logic inside `gymflow-app/src/main`.
- Document design artifacts inside `docs/`.
- Keep proposals and reports synchronized with implementation progress.

Refer to the course rubric to ensure every requirement is mapped to an artifact
inside this workspace.