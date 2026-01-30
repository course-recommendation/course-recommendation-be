CREATE TABLE post
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    user_id   VARCHAR(255)          NULL,
    content   VARCHAR(255)          NULL,
    course_id VARCHAR(255)          NULL,
    CONSTRAINT pk_post PRIMARY KEY (id)
);

CREATE TABLE post_comment
(
    id      BIGINT AUTO_INCREMENT NOT NULL,
    post_id BIGINT                NULL,
    user_id VARCHAR(255)          NULL,
    content VARCHAR(255)          NULL,
    CONSTRAINT pk_postcomment PRIMARY KEY (id)
);