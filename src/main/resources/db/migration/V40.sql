rename table user_course to user_course_status;

create table user_course_rating
(
    id        int auto_increment
        primary key,
    user_id   varchar(255) null,
    course_id int          null,
    rating    int          null
);

