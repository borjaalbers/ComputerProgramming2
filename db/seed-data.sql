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
