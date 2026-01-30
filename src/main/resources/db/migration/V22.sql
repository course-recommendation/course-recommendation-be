alter table fsuser_preference
    modify dataset varchar(255) null;

rename table fsuser_preference to fs_user_preference;

alter table fs_user_preference
    add user_id varchar(255) null after dataset;

