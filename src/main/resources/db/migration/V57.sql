alter table users
    drop key uq_email;

alter table users
    add constraint uq_email
        unique (tenant_id, email);

