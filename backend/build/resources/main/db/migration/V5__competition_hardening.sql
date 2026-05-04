ALTER TABLE iep_documents
    ALTER COLUMN safety_status SET DEFAULT 'PASSED_BY_RULES';

UPDATE iep_documents
SET safety_status = 'PASSED_BY_RULES'
WHERE safety_status = 'PASSED';

CREATE INDEX idx_registration_risk_action_time ON registration_risk(action, created_at);
