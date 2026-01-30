alter table fs_recommendation_result
    add dataset varchar(255) null after id;

alter table fs_recommendation_result
    add user_id varchar(255) null after dataset;