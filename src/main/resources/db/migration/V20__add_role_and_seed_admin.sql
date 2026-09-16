ALTER TABLE users
    ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';

INSERT INTO users (username, email, password_hash, created_at_epoch_ms, lang, metadata_lang, role)
VALUES ('admin', 'admin@admin.com', '$2a$10$Dk8VqAOVVV4ExQESwZJExuwGlHHewix5fvKNUsSktd.0wS4SKjnWq',
        0, 'en', 'en', 'SUPERADMIN')
ON CONFLICT (username) DO UPDATE
    SET role = 'SUPERADMIN';
