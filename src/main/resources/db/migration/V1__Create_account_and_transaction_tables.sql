
-- Create accounts table
create table accounts (
    id uuid not null,
    version uuid not null,
    balance decimal(20,2) not null default 0.0,

    -- PK
    constraint accounts_pk primary key (id)
);

CREATE INDEX id_and_version_index ON accounts(id, version);