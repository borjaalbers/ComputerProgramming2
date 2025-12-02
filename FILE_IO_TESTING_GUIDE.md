# File I/O Testing Guide

This guide explains how to manually test all File I/O features in GymFlow.

## Prerequisites

1. **Run the application**: `cd gymflow-app && mvn javafx:run`
2. **Login as a Trainer**: Use `trainer_demo` / `password123` (or create a new trainer account)
3. **Login as an Admin**: Use `admin_demo` / `password123` (or create a new admin account)
4. **Have at least one workout plan created** (as a trainer)

---

## Test 1: Export Workout Plans (Trainer Dashboard)

### Steps:
1. Login as a **Trainer**
2. Navigate to **"Workout Plans"** tab
3. Ensure you have at least one workout plan created
4. Click **"Export"** button
5. A file chooser dialog will appear
6. Choose a location and filename (e.g., `my_workout_plans.csv`)
7. Click **"Save"**

### Expected Results:
- ✅ **Success dialog** appears: "Exported X workout plan(s) to my_workout_plans.csv"
- ✅ CSV file is created at the chosen location
- ✅ Open the CSV file in Excel/Text Editor and verify it contains:
  - Header row: `Title,Description,Difficulty,Member ID,Trainer ID,Muscle Group,Workout Type,Duration Minutes,Equipment Needed,Target Sets,Target Reps,Rest Seconds,Created At`
  - Data rows with all workout plan information

### Test File Overwrite Protection:
1. Export to the same file again
2. **Expected**: Confirmation dialog appears: "File already exists. Do you want to overwrite it?"
3. Click **"Cancel"** → File should NOT be overwritten
4. Click **"OK"** → File should be overwritten with new data

---

## Test 2: Import Workout Plans (Trainer Dashboard)

### Steps:
1. Login as a **Trainer**
2. Navigate to **"Workout Plans"** tab
3. Click **"Import"** button
4. A file chooser dialog will appear
5. Select a CSV file (use the one exported in Test 1, or create a new one)
6. Click **"Open"**

### Expected Results:
- ✅ **Success dialog** appears: "Imported X workout plan(s) successfully"
- ✅ New workout plans appear in the table
- ✅ All fields are correctly imported (muscle group, workout type, duration, etc.)

### Test with Valid CSV File:
Create a CSV file with this content (save as `test_import.csv`):
```csv
Title,Description,Difficulty,Member ID,Trainer ID,Muscle Group,Workout Type,Duration Minutes,Equipment Needed,Target Sets,Target Reps,Rest Seconds,Created At
Morning Cardio,30-minute morning run,Intermediate,1,2,Full Body,Cardio,30,Treadmill,0,0,0,2024-12-02 10:00:00
Strength Training,Upper body workout,Advanced,1,2,Chest,Strength Training,45,Dumbbells,3,12,60,2024-12-02 11:00:00
```

**Note**: Replace `Member ID` and `Trainer ID` with actual IDs from your database.

### Test with Invalid CSV File:
1. Create a CSV file with wrong header (e.g., `Name,Age`)
2. Try to import it
3. **Expected**: Error dialog: "Invalid CSV header" or "Invalid file format"

### Test Duplicate Detection:
1. Export your workout plans
2. Import the same file again
3. **Expected**: Success dialog shows: "Imported 0 workout plan(s) successfully, X duplicate(s) skipped"

### Test with Old Format CSV (Backward Compatibility):
Create a CSV file with old format (without new fields):
```csv
Title,Description,Difficulty,Member ID,Trainer ID,Created At
Leg Day,Lower body workout,Beginner,1,2,2024-12-02 10:00:00
```
1. Import this file
2. **Expected**: Should import successfully (backward compatible)
3. New fields will be null/empty, but plan is created

---

## Test 3: Export Attendance Report (Admin Dashboard)

### Steps:
1. Login as an **Admin**
2. Navigate to **"System Reports"** tab
3. Ensure there are attendance records in the system (members must have registered for classes)
4. Click **"Export Attendance Report"** button
5. A file chooser dialog will appear
6. Choose a location and filename (e.g., `attendance_report.csv`)
7. Click **"Save"**

### Expected Results:
- ✅ **Success dialog** appears: "Exported X attendance record(s) to attendance_report.csv"
- ✅ CSV file is created at the chosen location
- ✅ Open the CSV file and verify it contains:
  - Header row: `Record ID,Session ID,Class Name,Member ID,Member Name,Attended`
  - Data rows with:
    - Record ID
    - Session ID
    - **Class Name** (not just ID!)
    - Member ID
    - **Member Name** (not just ID!)
    - Attended status (Yes/No)

### Test File Overwrite Protection:
1. Export to the same file again
2. **Expected**: Confirmation dialog appears: "File already exists. Do you want to overwrite it?"
3. Click **"Cancel"** → File should NOT be overwritten
4. Click **"OK"** → File should be overwritten

### Test with No Data:
1. If no attendance records exist, click **"Export Attendance Report"**
2. **Expected**: Error dialog: "No attendance records found to export"

---

## Test 4: Error Handling

### Test Invalid File Format:
1. Try to import a `.txt` file (not CSV)
2. **Expected**: Error dialog: "File must have .csv extension"

### Test Empty File:
1. Create an empty CSV file
2. Try to import it
3. **Expected**: Error dialog: "CSV file is empty" or "File is empty"

### Test File Too Large:
1. Create a CSV file larger than 10MB (if possible)
2. Try to import it
3. **Expected**: Error dialog: "File size exceeds maximum allowed size"

### Test File Not Found:
1. Delete a file after selecting it in the file chooser
2. Try to import/export
3. **Expected**: Error dialog: "File not found" or "Failed to export/import"

### Test Malformed CSV:
Create a CSV file with invalid data:
```csv
Title,Description,Difficulty,Member ID,Trainer ID,Muscle Group,Workout Type,Duration Minutes,Equipment Needed,Target Sets,Target Reps,Rest Seconds,Created At
Invalid Plan,,,0,0,,,abc,invalid,xyz,invalid,invalid,invalid-date
```
1. Import this file
2. **Expected**: Error messages for invalid rows, but valid rows are still imported

---

## Test 5: Field Validation

### Test Required Fields:
Create a CSV file missing required fields:
```csv
Title,Description,Difficulty,Member ID,Trainer ID,Muscle Group,Workout Type,Duration Minutes,Equipment Needed,Target Sets,Target Reps,Rest Seconds,Created At
,Empty title test,Intermediate,1,2,Chest,Strength Training,45,Dumbbells,3,12,60,2024-12-02 10:00:00
```
1. Import this file
2. **Expected**: Error: "Title is required" for that row

### Test Invalid IDs:
Create a CSV file with invalid member/trainer IDs:
```csv
Title,Description,Difficulty,Member ID,Trainer ID,Muscle Group,Workout Type,Duration Minutes,Equipment Needed,Target Sets,Target Reps,Rest Seconds,Created At
Test Plan,Test,Intermediate,-1,0,Chest,Strength Training,45,Dumbbells,3,12,60,2024-12-02 10:00:00
```
1. Import this file
2. **Expected**: Error: "Member ID must be greater than 0" or "Trainer ID must be greater than 0"

---

## Test 6: CSV Field Escaping

### Test Commas in Fields:
Create a CSV file with commas in description:
```csv
Title,Description,Difficulty,Member ID,Trainer ID,Muscle Group,Workout Type,Duration Minutes,Equipment Needed,Target Sets,Target Reps,Rest Seconds,Created At
"Plan, with comma","Description, with comma, and more",Intermediate,1,2,Chest,Strength Training,45,"Dumbbells, Barbell",3,12,60,2024-12-02 10:00:00
```
1. Export workout plans
2. Import the exported file
3. **Expected**: Commas are properly escaped and fields are correctly parsed

### Test Quotes in Fields:
Create a workout plan with quotes in the description (via UI), then export and import.
1. **Expected**: Quotes are properly escaped (doubled: `""`) in CSV

---

## Test 7: Integration Testing

### Complete Workflow:
1. **As Trainer**: Create 2-3 workout plans with all fields filled
2. **As Trainer**: Export workout plans to `backup.csv`
3. **As Trainer**: Delete one workout plan from the UI
4. **As Trainer**: Import `backup.csv`
5. **Expected**: Deleted workout plan is restored

### Cross-User Testing:
1. **As Trainer A**: Export workout plans
2. **Logout**
3. **As Trainer B**: Import the same file
4. **Expected**: Workout plans are imported with Trainer B's ID (not Trainer A's)

---

## Test 8: Performance Testing

### Test Large File:
1. Create a CSV file with 100+ workout plans
2. Import it
3. **Expected**: All plans are imported successfully (may take a few seconds)
4. Check success dialog shows correct count

---

## Common Issues and Solutions

### Issue: "No suitable driver found"
- **Solution**: Ensure H2 database dependency is in `pom.xml` without `<scope>test</scope>`

### Issue: "File already exists" dialog doesn't appear
- **Solution**: Ensure you're testing with an actual existing file (not a new filename)

### Issue: Imported plans don't show new fields
- **Solution**: Check that the CSV file uses the new format with all 13 columns

### Issue: Member/Class names show as "Unknown"
- **Solution**: Ensure the member/class IDs in the CSV exist in the database

---

## Success Criteria Checklist

- [ ] Export workout plans creates valid CSV with all fields
- [ ] Import workout plans correctly parses all fields
- [ ] File overwrite protection works (confirmation dialog)
- [ ] Duplicate detection works (skips duplicates)
- [ ] Attendance export includes member and class names
- [ ] Error handling shows user-friendly messages
- [ ] Backward compatibility works (old CSV format imports)
- [ ] CSV field escaping works (commas, quotes)
- [ ] Validation works (required fields, invalid IDs)
- [ ] Success/error dialogs appear correctly

---

## Notes

- All file operations use the `.csv` extension
- Maximum file size is 10MB
- Duplicate detection is based on title + member ID combination
- Imported workout plans use the current trainer's ID (for trainer imports)
- Attendance export enriches data with names from the database

