CREATE TABLE IF NOT EXISTS follows_users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    followed_id BIGINT NOT NULL,
    created_at_epoch_ms BIGINT NOT NULL,
    FOREIGN KEY (follower_id) REFERENCES users(id),
    FOREIGN KEY (followed_id) REFERENCES users(id),
    UNIQUE(follower_id, followed_id)
);
