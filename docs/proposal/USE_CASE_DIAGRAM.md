# GymFlow Use-Case Diagram

## Actors (System Users)

1. **Member** - Gym members who use the system to view their workout plans and class schedules
2. **Trainer** - Fitness trainers who create workout plans and manage classes
3. **Administrator** - System administrators who manage all users, equipment, and system settings

## Use Cases by Actor

### Member Use Cases
- **UC-001**: Login to system
- **UC-002**: View personal workout plan
- **UC-003**: View class schedule
- **UC-004**: Register for class session
- **UC-005**: View attendance history
- **UC-006**: View personal profile
- **UC-007**: Update personal information

### Trainer Use Cases
- **UC-008**: Login to system
- **UC-009**: Create workout plan for member
- **UC-010**: Assign workout plan to member
- **UC-011**: Create class session
- **UC-012**: Manage class schedule
- **UC-013**: Record class attendance
- **UC-014**: View member progress
- **UC-015**: Export workout templates
- **UC-016**: Import workout templates

### Administrator Use Cases
- **UC-017**: Login to system
- **UC-018**: Create user account (Member/Trainer/Admin)
- **UC-019**: Update user account
- **UC-020**: Delete user account
- **UC-021**: Manage equipment inventory
- **UC-022**: Update equipment status
- **UC-023**: View system reports
- **UC-024**: Manage trainer assignments
- **UC-025**: View all class schedules
- **UC-026**: Export attendance reports

## Use-Case Diagram Structure

```
┌─────────────────────────────────────────────────────────────┐
│                      GymFlow System                          │
│                                                              │
│  ┌──────────────┐                                           │
│  │   Member     │                                           │
│  └──────┬───────┘                                           │
│         │                                                    │
│         ├─── Login                                          │
│         ├─── View Workout Plan                              │
│         ├─── View Class Schedule                            │
│         ├─── Register for Class                             │
│         ├─── View Attendance History                         │
│         ├─── View Profile                                   │
│         └─── Update Profile                                 │
│                                                              │
│  ┌──────────────┐                                           │
│  │   Trainer    │                                           │
│  └──────┬───────┘                                           │
│         │                                                    │
│         ├─── Login                                           │
│         ├─── Create Workout Plan                            │
│         ├─── Assign Workout Plan                            │
│         ├─── Create Class Session                           │
│         ├─── Manage Class Schedule                          │
│         ├─── Record Attendance                              │
│         ├─── View Member Progress                           │
│         ├─── Export Workout Templates                       │
│         └─── Import Workout Templates                       │
│                                                              │
│  ┌──────────────┐                                           │
│  │ Administrator│                                           │
│  └──────┬───────┘                                           │
│         │                                                    │
│         ├─── Login                                           │
│         ├─── Manage Users (Create/Update/Delete)            │
│         ├─── Manage Equipment                               │
│         ├─── View System Reports                            │
│         ├─── Manage Trainer Assignments                     │
│         ├─── View All Schedules                             │
│         └─── Export Reports                                 │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## Use-Case Relationships

### Include Relationships
- **Login** is included in all actor use cases (authentication required)
- **View Profile** includes **View Personal Information**

### Extend Relationships
- **Export Reports** extends **View System Reports** (optional feature)
- **Import Workout Templates** extends **Create Workout Plan** (optional feature)

## Notes for Creating the Diagram

1. **Use a free tool** like:
   - Draw.io (https://app.diagrams.net/) - Free, no account needed
   - PlantUML (text-based, generates diagrams)
   - Creately (free tier available)

2. **Diagram Elements**:
   - Use ovals for use cases
   - Use stick figures for actors
   - Use rectangles for system boundary
   - Use dashed arrows for <<include>> and <<extend>> relationships
   - Use solid lines for associations

3. **Layout Tips**:
   - Place actors on the left side
   - Group use cases by actor
   - Use system boundary rectangle to contain all use cases
   - Keep diagram readable and well-spaced

## How to Create This Diagram

### Option 1: Draw.io (Recommended - Free, No Account)
1. Go to https://app.diagrams.net/
2. Create new diagram → Choose "Blank Diagram"
3. Use the shape library:
   - Search "Actor" for stick figure
   - Search "Use Case" for oval shapes
   - Use rectangle for system boundary
4. Follow the structure above
5. Export as PNG or PDF

### Option 2: PlantUML (Text-Based)
Create a `.puml` file with PlantUML syntax (can be converted to image)

### Option 3: Hand-Drawn
Draw the diagram on paper, scan or photograph it, and submit as image/PDF

