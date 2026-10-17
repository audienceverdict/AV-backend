-- Additive only: users.role remains the primary role for existing accounts and clients.
CREATE TABLE IF NOT EXISTS user_roles (
    user_id VARCHAR(36) NOT NULL,
    role VARCHAR(32) NOT NULL,
    CONSTRAINT uk_user_roles UNIQUE (user_id, role)
);
CREATE TABLE IF NOT EXISTS user_verifier_shows (
    user_id VARCHAR(36) NOT NULL,
    show_id VARCHAR(36) NOT NULL,
    CONSTRAINT uk_user_verifier_shows UNIQUE (user_id, show_id)
);
CREATE TABLE IF NOT EXISTS role_assignment_audits (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    actor_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    previous_assignments LONGTEXT NOT NULL,
    new_assignments LONGTEXT NOT NULL,
    created_at DATETIME(6) NOT NULL
);
