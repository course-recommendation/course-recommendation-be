alter table fs_user_preference
    add algorithm varchar(255) null after dataset;

rename table fs_user_preference to user_preference;

