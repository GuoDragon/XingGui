ALTER TABLE users
    ADD COLUMN avatar_key VARCHAR(64) NULL;

ALTER TABLE children
    ADD COLUMN birth_date DATE NULL,
    ADD COLUMN intervention_start_date DATE NULL,
    ADD COLUMN avatar_key VARCHAR(64) NULL;
