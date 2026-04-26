CREATE TABLE IF NOT EXISTS iep_documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id VARCHAR(64) NOT NULL UNIQUE,
    child_id BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    stored_file_path TEXT NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    semester_goal TEXT NOT NULL,
    monthly_goal TEXT NOT NULL,
    weekly_goals_json TEXT NOT NULL,
    notes TEXT NULL,
    uploaded_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    CONSTRAINT fk_iep_documents_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE CASCADE,
    CONSTRAINT fk_iep_documents_user FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_iep_documents_child_uploaded_at ON iep_documents(child_id, uploaded_at);
