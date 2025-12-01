# IntelliJ Testing Guide for GymFlow

## Quick Start

### 1. Open Project in IntelliJ
1. Launch IntelliJ IDEA
2. **File → Open** → Navigate to `/Users/borja/Desktop/ComputerProgramming2/gymflow-app`
3. Select the `pom.xml` file
4. Click **"Open as Project"**
5. IntelliJ will automatically:
   - Import Maven dependencies
   - Configure JavaFX
   - Set up the project structure

### 2. Verify Setup
- Wait for Maven to finish downloading dependencies (check bottom status bar)
- Ensure no red error markers in the project
- Verify Java SDK is set to 17+ (File → Project Structure → Project → SDK)

### 3. Run the Application

**Option A: Run from MainApp (Recommended)**
1. Navigate to `src/main/java/com/gymflow/MainApp.java`
2. Right-click on the file
3. Select **"Run 'MainApp.main()'"**
4. The login window should appear

**Option B: Create Run Configuration**
1. **Run → Edit Configurations**
2. Click **"+"** → **"Application"**
3. Set:
   - **Name**: GymFlow
   - **Main class**: `com.gymflow.MainApp`
   - **Module**: `gymflow-app`
   - **VM options**: (leave empty, JavaFX is handled by Maven)
4. Click **"OK"** and run

**Option C: Use Maven**
1. Open Terminal in IntelliJ (View → Tool Windows → Terminal)
2. Run: `mvn javafx:run`

## Expected Behavior

### ✅ Application Starts Successfully
- Login window appears (960x600 pixels)
- Title bar shows "GymFlow"
- Clean, modern login interface with:
  - "GymFlow" heading
  - Username field
  - Password field
  - "Sign In" button

### ✅ Login Functionality
- **Empty fields**: Clicking "Sign In" shows error alert
- **Invalid credentials**: Shows "Invalid username or password" alert
- **Valid credentials**: Navigates to appropriate dashboard

### ✅ Dashboard Navigation
- **Member login** → Blue-themed Member Dashboard
- **Trainer login** → Green-themed Trainer Dashboard  
- **Admin login** → Purple-themed Admin Dashboard

### ✅ Dashboard Features
- Welcome message with user's name
- Profile information displayed
- Tabbed interface (My Workouts, Class Schedule, Profile, etc.)
- Logout button in top-right corner

### ✅ Logout Functionality
- Clicking "Logout" returns to login screen
- Session is cleared

## Testing with Database

### Default: H2 In-Memory Database
The application uses H2 in-memory database by default, which means:
- **No setup required** - works out of the box
- **Empty on first run** - you'll need to create users

### Creating Test Users

Since the database starts empty, you have two options:

**Option 1: Use Seed Data Script**
1. The database will be empty on first run
2. You can manually insert test users using SQL
3. Or wait for registration feature to be implemented

**Option 2: Use PostgreSQL/MySQL**
1. Set up your database using `db/schema.sql`
2. Insert seed data using `db/seed-data.sql`
3. Configure environment variables:
   - `GYMFLOW_DB_URL=jdbc:postgresql://localhost:5432/gymflow`
   - `GYMFLOW_DB_USER=your_username`
   - `GYMFLOW_DB_PASSWORD=your_password`
4. Set these in IntelliJ Run Configuration → Environment variables

## Troubleshooting

### ❌ "JavaFX runtime components are missing"
**Solution**: IntelliJ should auto-detect JavaFX from Maven. If not:
1. File → Project Structure → Libraries
2. Verify `javafx-controls`, `javafx-fxml` are present
3. If missing, refresh Maven: Right-click `pom.xml` → Maven → Reload Project

### ❌ "Cannot find resource /fxml/login.fxml"
**Solution**: 
1. Verify files exist in `src/main/resources/fxml/`
2. Mark `src/main/resources` as Resources Root:
   - Right-click `resources` folder → Mark Directory as → Resources Root
3. Rebuild project: Build → Rebuild Project

### ❌ Application starts but shows blank window
**Solution**:
1. Check IntelliJ console for errors
2. Verify FXML files are in correct location
3. Check that controllers are properly annotated with `@FXML`

### ❌ Database connection errors
**Solution**:
- For H2: Should work automatically (no setup needed)
- For PostgreSQL/MySQL: Verify database is running and credentials are correct
- Check console output for specific error messages

## Verification Checklist

Before considering the application "working", verify:

- [ ] Application launches without errors
- [ ] Login window displays correctly
- [ ] Can enter username and password
- [ ] Error alerts appear for invalid input
- [ ] (If you have test users) Can login successfully
- [ ] Dashboard appears after successful login
- [ ] User information displays correctly
- [ ] Logout button works
- [ ] Returns to login screen after logout

## Next Steps After Testing

Once you've verified everything works:
1. ✅ Stage and commit the changes
2. Continue with next phase of development
3. Add more features (workout plans, classes, etc.)

## Notes

- The application is designed to work with an empty database initially
- All UI components are functional but may show empty tables (data loading not yet implemented)
- Error handling is in place with user-friendly alerts
- The code follows MVC architecture for maintainability

