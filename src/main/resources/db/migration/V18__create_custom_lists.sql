CREATE TABLE custom_lists (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name VARCHAR(100) NOT NULL,
  is_public BOOLEAN NOT NULL DEFAULT false,
  created_at_epoch_ms BIGINT NOT NULL
);

CREATE TABLE custom_list_items (
  id BIGSERIAL PRIMARY KEY,
  list_id BIGINT NOT NULL REFERENCES custom_lists(id) ON DELETE CASCADE,
  tmdb_id INT NOT NULL,
  media_type VARCHAR(10) NOT NULL,
  position INT NOT NULL,
  added_at_epoch_ms BIGINT NOT NULL,
  UNIQUE(list_id, tmdb_id, media_type)
);

CREATE INDEX idx_custom_lists_user ON custom_lists(user_id);
CREATE INDEX idx_custom_list_items_list ON custom_list_items(list_id);
