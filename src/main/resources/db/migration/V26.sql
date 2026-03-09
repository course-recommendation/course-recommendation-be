alter table course
    change id2 id int auto_increment;

alter table course
    modify course_id varchar(255) charset utf8mb4 not null after id;

