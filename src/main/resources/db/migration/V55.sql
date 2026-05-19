create table user_first_login
(
    id        int auto_increment
        primary key,
    user_id   varchar(255) null,
    algorithm varchar(255) null
);

alter table users
    drop column did_survey;

