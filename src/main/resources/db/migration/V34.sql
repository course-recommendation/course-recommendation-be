create table course_rating
(
    id        int auto_increment
        primary key,
    course_id varchar(255) null,
    user_id   varchar(255) null
);

alter table post
    change course_id course_code varchar(255) null;



