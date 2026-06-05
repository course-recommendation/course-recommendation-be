alter table user_course_rating
    add attribute_id int null;

update user_course_rating ucr
    inner join course c on ucr.course_id = c.id
    inner join attribute a on a.value = ucr.attribute_value
        and a.algorithm = convert(c.algorithm using utf8mb4) collate utf8mb4_0900_ai_ci
        and a.tenant_id = c.tenant_id
set ucr.attribute_id = a.id;

delete from user_course_rating where attribute_id is null;

alter table user_course_rating
    drop column attribute_value;

alter table user_course_rating
    modify attribute_id int not null;
