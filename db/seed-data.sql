-- Seed data for development and testing.
INSERT INTO roles (name) VALUES
    ('MEMBER'),
    ('TRAINER'),
    ('ADMIN')
ON CONFLICT (name) DO NOTHING;

-- Dummy users
INSERT INTO users (role_id, username, password_hash, full_name, email)
SELECT r.id, 'member_demo', 'REPLACE_WITH_HASH', 'Demo Member', 'member@gymflow.local'
FROM roles r WHERE r.name = 'MEMBER'
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (role_id, username, password_hash, full_name, email)
SELECT r.id, 'trainer_demo', 'REPLACE_WITH_HASH', 'Demo Trainer', 'trainer@gymflow.local'
FROM roles r WHERE r.name = 'TRAINER'
ON CONFLICT (username) DO NOTHING;


INSERT INTO users (role_id, username, password_hash, full_name, email)
SELECT r.id, 'admin_demo', 'REPLACE_WITH_HASH', 'Demo Admin', 'admin@gymflow.local'
FROM roles r WHERE r.name = 'ADMIN'
ON CONFLICT (username) DO NOTHING;

-- Sample equipment
INSERT INTO equipment (name, status, last_service) VALUES
    ('Treadmill', 'AVAILABLE', '2025-10-01'),
    ('Bench Press', 'IN_USE', '2025-09-15'),
    ('Dumbbells Set', 'AVAILABLE', '2025-11-10'),
    ('Rowing Machine', 'MAINTENANCE', '2025-08-20')
ON CONFLICT (name) DO NOTHING;

-- Sample workout plans
INSERT INTO workout_plans (member_id, trainer_id, title, description, difficulty, muscle_group, workout_type, duration_minutes, equipment_needed, target_sets, target_reps, rest_seconds)
SELECT m.id, t.id, 'Full Body Starter', 'Introductory full body workout', 'Beginner', 'Full Body', 'Strength Training', 45, 'Dumbbells', 3, 12, 60
FROM users m, users t WHERE m.username = 'member_demo' AND t.username = 'trainer_demo'
ON CONFLICT DO NOTHING;

INSERT INTO workout_plans (member_id, trainer_id, title, description, difficulty, muscle_group, workout_type, duration_minutes, equipment_needed, target_sets, target_reps, rest_seconds)
SELECT m.id, t.id, 'Cardio Blast', 'High intensity cardio session', 'Intermediate', 'Cardio', 'Cardio', 30, 'Treadmill', 1, 0, 0
FROM users m, users t WHERE m.username = 'member_demo' AND t.username = 'trainer_demo'
ON CONFLICT DO NOTHING;

-- Sample class sessions
INSERT INTO class_sessions (trainer_id, title, schedule_timestamp, capacity, workout_plan_id)
SELECT t.id, 'Morning HIIT', '2025-12-03 08:00:00', 15, wp.id
FROM users t, workout_plans wp WHERE t.username = 'trainer_demo' AND wp.title = 'Cardio Blast'
ON CONFLICT DO NOTHING;

INSERT INTO class_sessions (trainer_id, title, schedule_timestamp, capacity, workout_plan_id)
SELECT t.id, 'Evening Strength', '2025-12-03 18:00:00', 10, wp.id
FROM users t, workout_plans wp WHERE t.username = 'trainer_demo' AND wp.title = 'Full Body Starter'
ON CONFLICT DO NOTHING;

-- Sample attendance records
INSERT INTO attendance_records (session_id, member_id, attended)
SELECT cs.id, m.id, TRUE
FROM class_sessions cs, users m WHERE cs.title = 'Morning HIIT' AND m.username = 'member_demo'
ON CONFLICT DO NOTHING;

INSERT INTO attendance_records (session_id, member_id, attended)
SELECT cs.id, m.id, FALSE
FROM class_sessions cs, users m WHERE cs.title = 'Evening Strength' AND m.username = 'member_demo'
ON CONFLICT DO NOTHING;
