CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(64) NOT NULL UNIQUE,
    username VARCHAR(64) NOT NULL UNIQUE,
    display_name VARCHAR(64) NOT NULL,
    email VARCHAR(128) NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS roles (
    role_name VARCHAR(16) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_name VARCHAR(16) NOT NULL,
    PRIMARY KEY (user_id, role_name),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_name) REFERENCES roles(role_name) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS children (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    child_id VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    age INT NOT NULL,
    intervention_duration VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS child_guardians (
    child_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (child_id, user_id),
    CONSTRAINT fk_child_guardians_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE,
    CONSTRAINT fk_child_guardians_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS child_teachers (
    child_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (child_id, user_id),
    CONSTRAINT fk_child_teachers_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE,
    CONSTRAINT fk_child_teachers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS goal_plans (
    child_id BIGINT PRIMARY KEY,
    semester_goal TEXT NOT NULL,
    monthly_goal TEXT NOT NULL,
    CONSTRAINT fk_goal_plans_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS weekly_checkins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_id VARCHAR(64) NOT NULL UNIQUE,
    child_id BIGINT NOT NULL,
    dimension_id VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    completed BOOLEAN NOT NULL,
    reward_stars INT NOT NULL,
    CONSTRAINT fk_weekly_checkins_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS report_summaries (
    child_id BIGINT PRIMARY KEY,
    overview TEXT NOT NULL,
    overall_evaluation TEXT NOT NULL,
    next_suggestions TEXT NOT NULL,
    ai_analysis TEXT NOT NULL,
    dimension_scores_json TEXT NOT NULL,
    dimension_highlights_json TEXT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_report_summaries_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS report_history_entries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entry_id VARCHAR(64) NOT NULL UNIQUE,
    child_id BIGINT NOT NULL,
    source_item_id VARCHAR(64) NOT NULL,
    source_dimension_id VARCHAR(64) NOT NULL,
    note TEXT NOT NULL,
    generated_at BIGINT NOT NULL,
    dimension_scores_json TEXT NOT NULL,
    overview TEXT NOT NULL,
    ai_analysis TEXT NOT NULL,
    overall_evaluation TEXT NOT NULL,
    next_suggestions TEXT NOT NULL,
    CONSTRAINT fk_report_history_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS archive_checkin_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id VARCHAR(64) NOT NULL UNIQUE,
    child_id BIGINT NOT NULL,
    item_id VARCHAR(64) NOT NULL,
    dimension_id VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    note TEXT NOT NULL,
    completed BOOLEAN NOT NULL,
    reward_stars INT NOT NULL,
    timestamp BIGINT NOT NULL,
    CONSTRAINT fk_archive_records_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS resources (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(64) NOT NULL,
    is_paid BOOLEAN NOT NULL,
    summary TEXT NOT NULL,
    recommended_reason TEXT NOT NULL,
    asset_path VARCHAR(255) NULL,
    source_url TEXT NULL
);

CREATE TABLE IF NOT EXISTS resource_runtime_state (
    user_id BIGINT PRIMARY KEY,
    unlocked_resource_ids_json TEXT NOT NULL,
    search_history_json TEXT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_resource_runtime_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    auth_token VARCHAR(128) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    active_role VARCHAR(16) NULL,
    selected_child_id BIGINT NULL,
    created_at BIGINT NOT NULL,
    expires_at BIGINT NOT NULL,
    last_seen_at BIGINT NOT NULL,
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_sessions_child FOREIGN KEY (selected_child_id) REFERENCES children(id) ON DELETE SET NULL
);

INSERT INTO roles(role_name) VALUES ('PARENT') ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);
INSERT INTO roles(role_name) VALUES ('TEACHER') ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);
INSERT INTO roles(role_name) VALUES ('ADMIN') ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);
