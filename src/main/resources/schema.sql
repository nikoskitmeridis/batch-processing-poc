drop table if exists account;
create table account
(
    id bigint not null
        constraint account_pkey
            primary key,
    email varchar(255),
    status varchar(255)
);
alter table account owner to postgres;

alter table if exists task drop constraint if exists fklwemxvu8tvwvxrxfabpjnttfb;
drop table if exists operation;
create table operation
(
    id bigint not null
        constraint operation_pkey
            primary key,
    payload json,
    status varchar(255),
    type varchar(255)
);
alter table operation owner to postgres;

drop table if exists task;
create table if not exists task
(
    id bigint not null
    constraint task_pkey
    primary key,
    payload json,
    result varchar(255),
    status varchar(255),
    type varchar(255),
    operation_id bigint
    constraint fklwemxvu8tvwvxrxfabpjnttfb
    references operation
    );
alter table task owner to postgres;

