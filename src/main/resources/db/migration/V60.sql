create table fs_course_sentiments
(
    id              bigint auto_increment primary key,
    course_id       bigint not null,
    item_sentiments json   not null
);

alter table course
    drop column extra_data;
