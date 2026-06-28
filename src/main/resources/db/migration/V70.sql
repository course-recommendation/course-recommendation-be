CREATE TABLE post_comment_vote
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    comment_id BIGINT                NOT NULL,
    user_id    VARCHAR(255)          NOT NULL,
    vote_type  VARCHAR(20)           NOT NULL,
    CONSTRAINT pk_post_comment_vote PRIMARY KEY (id),
    CONSTRAINT uq_post_comment_vote UNIQUE (comment_id, user_id)
);
