CREATE TABLE post_vote
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    post_id   BIGINT                NOT NULL,
    user_id   VARCHAR(255)          NOT NULL,
    vote_type VARCHAR(20)           NOT NULL,
    CONSTRAINT pk_post_vote PRIMARY KEY (id),
    CONSTRAINT uq_post_vote UNIQUE (post_id, user_id)
);
