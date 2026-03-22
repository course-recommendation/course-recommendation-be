alter table post
    add dataset varchar(255) null after id;

alter table post
    add algorithm varchar(255) null after dataset;

