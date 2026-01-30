alter table post
    add algorithm int null after id;

alter table post
    add dataset int null after algorithm;

