ALTER TABLE users
    ADD COLUMN failed_login_count INT NOT NULL DEFAULT 0,
    ADD COLUMN locked_until BIGINT NULL;

ALTER TABLE sessions
    ADD COLUMN revoked_at BIGINT NULL;

ALTER TABLE iep_documents
    ADD COLUMN safety_status VARCHAR(32) NOT NULL DEFAULT 'PASSED';

CREATE TABLE IF NOT EXISTS registration_risk (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ip_address VARCHAR(128) NOT NULL,
    device_id VARCHAR(128) NOT NULL,
    action VARCHAR(32) NOT NULL,
    created_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS captchas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    captcha_id VARCHAR(64) NOT NULL UNIQUE,
    answer_hash TEXT NOT NULL,
    question VARCHAR(64) NOT NULL,
    ip_address VARCHAR(128) NULL,
    device_id VARCHAR(128) NULL,
    expires_at BIGINT NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_user_id BIGINT NULL,
    action VARCHAR(64) NOT NULL,
    target VARCHAR(128) NULL,
    ip_address VARCHAR(128) NULL,
    success BOOLEAN NOT NULL,
    details TEXT NULL,
    created_at BIGINT NOT NULL,
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS event_outbox (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(64) NOT NULL,
    payload_json TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    processed_at BIGINT NULL
);

CREATE INDEX idx_registration_risk_ip_time ON registration_risk(ip_address, created_at);
CREATE INDEX idx_registration_risk_device_time ON registration_risk(device_id, created_at);
CREATE INDEX idx_captchas_expires ON captchas(expires_at, used);
CREATE INDEX idx_audit_logs_action_time ON audit_logs(action, created_at);
CREATE INDEX idx_event_outbox_pending ON event_outbox(processed_at, created_at);
CREATE INDEX idx_sessions_token_active ON sessions(auth_token, expires_at, revoked_at);
CREATE INDEX idx_archive_child_time ON archive_checkin_records(child_id, timestamp);
CREATE INDEX idx_reports_child_generated ON report_history_entries(child_id, generated_at);
CREATE INDEX idx_resources_category_id ON resources(category, resource_id);
