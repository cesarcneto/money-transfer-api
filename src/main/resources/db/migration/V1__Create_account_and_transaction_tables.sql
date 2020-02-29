
-- Create accounts table
create table accounts (
  id uuid not null,
  balance decimal(20,2) not null default 0.0
);