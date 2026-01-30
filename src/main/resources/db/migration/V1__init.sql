create table users
(
    id         char(36)             not null
        primary key,
    email      varchar(255)         null,
    password   varchar(255)         null,
    first_name varchar(255)         null,
    last_name  varchar(255)         null,
    roles      json                 null,
    is_active  tinyint(1) default 1 null,
    constraint uq_email
        unique (email)
);
