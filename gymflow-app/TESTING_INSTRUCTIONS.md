# Testing Instructions for GymFlow Application

## Prerequisites

1. **IntelliJ IDEA** (recommended version 2022.3 or later)
2. **Java 17+** installed and configured
3. **Maven** installed (or use IntelliJ's bundled Maven)

## Setup in IntelliJ

### 1. Import the Project
1. Open IntelliJ IDEA
2. File → Open → Navigate to `/Users/borja/Desktop/ComputerProgramming2/gymflow-app`
3. Select `pom.xml` and click "Open as Project"
4. IntelliJ will automatically import it as a Maven project

### 2. Configure JavaFX SDK (if needed)
- IntelliJ should automatically detect JavaFX from Maven dependencies
- If you see JavaFX-related errors, ensure JavaFX is properly configured in Project Structure

### 3. Set Up Database Connection

The application uses H2 in-memory database by default. For testing with actual data:

**Option A: Use H2 In-Memory (Default - No Setup Required)**
- The app will start with an empty database
- You'll need to create users manually or use seed data

**Option B: Use PostgreSQL/MySQL**
1. Set environment variables before running:
   ```bash
   export GYMFLOW_DB_URL="jdbc:postgresql://localhost:5432/gymflow"
   export GYMFLOW_DB_USER="your_username"
   export GYMFLOW_DB_PASSWORD="your_password"
   ```
2. Or configure in IntelliJ Run Configuration → Environment variables

### 4. Run the Application

**Method 1: Using IntelliJ Run Configuration**
1. Right-click on `MainApp.java` in the project tree
2. Select "Run 'MainApp.main()'"
3. IntelliJ will automatically configure JavaFX runtime

**Method 2: Using Maven**
1. Open IntelliJ's Terminal (View → Tool Windows → Terminal)
2. Navigate to `gymflow-app` directory
3. Run: `mvn javafx:run`

**Method 3: Using Run Script**
1. From project root: `./scripts/run.sh`

## Testing the Login Flow

### Expected Behavior

1. **Application Starts**
   - Login window should appear (960x600)
   - Title: "GymFlow"
   - Username and Password fields visible
   - "Sign In" button present

2. **Login Attempts**
   - **Empty fields**: Should show error alert "Please enter both username and password"
   - **Invalid credentials**: Should show error alert "Invalid username or password"
   - **Valid credentials**: Should navigate to role-specific dashboard

3. **Dashboard Navigation**
   - **Member**: Navigates to Member Dashboard (blue theme)
   - **Trainer**: Navigates to Trainer Dashboard (green theme)
   - **Admin**: Navigates to Admin Dashboard (purple theme)

4. **Dashboard Features**
   - Welcome message with user's name
   - Profile information displayed
   - Logout button functional (returns to login)

### Test Users

Since we're using H2 in-memory by default, you'll need to create test users. The database will be empty on first run.

**To create test users, you can:**
1. Use the seed data SQL script: `db/seed-data.sql`
2. Or manually insert via database console
3. Or implement a registration feature (future enhancement)

### Known Limitations

- **Empty Database**: On first run with H2 in-memory, no users exist
- **No Registration UI**: Users must be created via database
- **Dashboard Tables**: Currently show empty tables (data loading not yet implemented)

## Troubleshooting

### Application Won't Start
- Check Java version: `java -version` (should be 17+)
- Verify Maven dependencies: `mvn dependency:resolve`
- Check for compilation errors: `mvn clean compile`

### FXML Loading Errors
- Verify FXML files are in `src/main/resources/fxml/`
- Check that resource paths in code match actual file locations
- Ensure FXML files are included in build (check `target/classes/fxml/`)

### Database Connection Errors
- Verify database is running (if using PostgreSQL/MySQL)
- Check connection string format
- Verify credentials are correct
- For H2, ensure no connection errors in console

### UI Not Displaying Correctly
- Check JavaFX version compatibility
- Verify all JavaFX dependencies are resolved
- Check for missing CSS files (if any)

## Next Steps After Testing

Once you've verified the application runs:
1. Test login with actual database users
2. Verify role-based navigation works
3. Test logout functionality
4. Check that all dashboards load correctly
5. Report any issues found

## Notes for Development

- The application uses a singleton pattern for `DatabaseConnection` and `SessionManager`
- All controllers follow MVC pattern
- Error handling uses JavaFX Alert dialogs
- FXML files use inline styling (can be moved to CSS later)

