ALTER TABLE post_comment
    ADD COLUMN parent_comment_id BIGINT NULL AFTER post_id,
    ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) AFTER content;
