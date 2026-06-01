create table tenant
(
    id   bigint auto_increment primary key,
    name varchar(255) not null,
    unique (name)
);

alter table course
    add tenant_id bigint null after algorithm;

alter table post
    add tenant_id bigint null after algorithm;

alter table recommendation_result
    add tenant_id bigint null after algorithm;

alter table user_preference
    add tenant_id bigint null after algorithm;

alter table attribute
    add tenant_id bigint null after algorithm;

alter table user_first_login
    add tenant_id bigint null after algorithm;
