CREATE TABLE fsuser_preference
(
    id      BIGINT AUTO_INCREMENT NOT NULL,
    dataset SMALLINT              NULL,
    data    JSON                  NULL,
    CONSTRAINT pk_fsuserpreference PRIMARY KEY (id)
);