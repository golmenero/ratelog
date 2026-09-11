CREATE TABLE IF NOT EXISTS general_config (
    key   VARCHAR(50)  PRIMARY KEY,
    value VARCHAR(500) NOT NULL,
    updated_at_epoch_ms BIGINT NOT NULL
);
