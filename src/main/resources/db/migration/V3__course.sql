CREATE TABLE user_course
(
    id        BIGINT       NOT NULL,
    user_id   VARCHAR(255) NULL,
    course_id VARCHAR(255) NULL,
    status    SMALLINT     NULL,
    CONSTRAINT pk_usercourse PRIMARY KEY (id)
);