alter table tenant
    add algorithm varchar(255) not null default 'FS';

-- NOTE: per-tenant algorithm is introduced. Removing per-entity algorithm columns is a larger refactor and not done in this migration.
