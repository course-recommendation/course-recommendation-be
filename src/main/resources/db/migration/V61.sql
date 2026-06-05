alter table users
    add full_name varchar(255) null after last_name;

update users
set full_name = concat_ws(' ', nullif(trim(last_name), ''), nullif(trim(first_name), ''));

alter table users
    drop column first_name,
    drop column last_name;
