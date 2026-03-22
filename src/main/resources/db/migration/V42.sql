alter table user_course_rating
    add attribute_id int null after course_id;

create table attribute
(
    id        int auto_increment
        primary key,
    algorithm varchar(255) null,
    dataset   varchar(255) null,
    value     varchar(255) null
);

