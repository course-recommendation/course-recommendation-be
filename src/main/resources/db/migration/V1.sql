create table attribute
(
    id        int auto_increment
        primary key,
    algorithm varchar(255) null,
    tenant_id bigint       null,
    value     varchar(255) null
);

create table course
(
    id            int auto_increment
        primary key,
    code          varchar(255) charset utf8mb4 not null,
    algorithm     varchar(255)                 null,
    tenant_id     bigint                       null,
    name          varchar(255) charset utf8mb4 null,
    description   mediumtext                   null,
    thumbnail_url mediumtext                   null
)
    collate = utf8mb4_unicode_ci;

create table flyway_schema_history
(
    installed_rank int                                 not null
        primary key,
    version        varchar(50)                         null,
    description    varchar(200)                        not null,
    type           varchar(20)                         not null,
    script         varchar(1000)                       not null,
    checksum       int                                 null,
    installed_by   varchar(100)                        not null,
    installed_on   timestamp default CURRENT_TIMESTAMP not null,
    execution_time int                                 not null,
    success        tinyint(1)                          not null
);

create index flyway_schema_history_s_idx
    on flyway_schema_history (success);

create table fs_course_sentiment
(
    id              bigint auto_increment
        primary key,
    course_id       bigint not null,
    item_sentiments json   not null
);

create table password_reset
(
    id      varchar(36)          not null
        primary key,
    user_id varchar(36)          not null,
    token   varchar(255)         not null,
    expires datetime             not null,
    used    tinyint(1) default 0 not null,
    constraint token
        unique (token)
);

create table post
(
    id          bigint auto_increment
        primary key,
    algorithm   varchar(255)                        null,
    tenant_id   bigint                              null,
    course_code varchar(255)                        null,
    user_id     varchar(255)                        null,
    content     mediumtext                          null,
    created_at  timestamp default CURRENT_TIMESTAMP null
);

create table post_comment
(
    id                bigint auto_increment
        primary key,
    post_id           bigint                                   null,
    parent_comment_id bigint                                   null,
    course_id         varchar(255)                             null,
    user_id           varchar(255)                             null,
    content           mediumtext                               null,
    created_at        datetime(6) default CURRENT_TIMESTAMP(6) not null
);

create table post_comment_vote
(
    id         bigint auto_increment
        primary key,
    comment_id bigint       not null,
    user_id    varchar(255) not null,
    vote_type  varchar(20)  not null,
    constraint uq_post_comment_vote
        unique (comment_id, user_id)
);

create table post_vote
(
    id        bigint auto_increment
        primary key,
    post_id   bigint       not null,
    user_id   varchar(255) not null,
    vote_type varchar(20)  not null,
    constraint uq_post_vote
        unique (post_id, user_id)
);

create table recommendation_result
(
    id        int auto_increment
        primary key,
    algorithm varchar(255) null,
    tenant_id bigint       null,
    user_id   varchar(255) null,
    data      json         null
);

create table tenant
(
    id             bigint auto_increment
        primary key,
    name           varchar(255)              not null,
    algorithm      varchar(255) default 'FS' not null,
    system_enabled tinyint(1)   default 0    not null,
    constraint name
        unique (name)
);

create table user_course_rating
(
    id           int auto_increment
        primary key,
    user_id      varchar(255) null,
    course_id    int          null,
    score        int          null,
    attribute_id int          not null
);

create table user_course_status
(
    id        bigint auto_increment
        primary key,
    user_id   varchar(255) null,
    course_id varchar(255) null,
    status    varchar(255) null
);

create table user_preference
(
    id        bigint auto_increment
        primary key,
    algorithm varchar(255) null,
    tenant_id bigint       null,
    user_id   varchar(255) null,
    data      json         null
);

create table users
(
    id               varchar(255)         not null
        primary key,
    email            varchar(255)         null,
    password         varchar(255)         null,
    full_name        varchar(255)         null,
    avatar_url       mediumtext           null,
    roles            json                 null,
    is_active        tinyint(1) default 1 null,
    tenant_id        bigint               null,
    show_explanation tinyint(1) default 1 not null
);

