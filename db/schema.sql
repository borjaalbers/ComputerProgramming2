-- Database schema for GymFlow
-- TODO: replace placeholders with finalized DDL.

CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    role_id INTEGER NOT NULL REFERENCES roles(id),
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workout_plans (
    id SERIAL PRIMARY KEY,
    member_id INTEGER REFERENCES users(id),
    trainer_id INTEGER REFERENCES users(id),
    title VARCHAR(150) NOT NULL,
    description TEXT,
    difficulty VARCHAR(50),
    muscle_group VARCHAR(100),
    workout_type VARCHAR(50),
    duration_minutes INTEGER,
    equipment_needed TEXT,
    target_sets INTEGER,
    target_reps INTEGER,
    rest_seconds INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS class_sessions (
    id SERIAL PRIMARY KEY,
    trainer_id INTEGER REFERENCES users(id),
    title VARCHAR(150) NOT NULL,
    schedule_timestamp TIMESTAMP NOT NULL,
    capacity INTEGER DEFAULT 10,
    workout_plan_id INTEGER REFERENCES workout_plans(id)
);

CREATE TABLE IF NOT EXISTS attendance_records (
    id SERIAL PRIMARY KEY,
    session_id INTEGER REFERENCES class_sessions(id),
    member_id INTEGER REFERENCES users(id),
    attended BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS equipment (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    status VARCHAR(50) DEFAULT 'AVAILABLE',
    last_service DATE
);
