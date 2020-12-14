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

INSERT INTO account (id, email, status) VALUES
(1, 'nikos@email.com', 'ACTIVE'),
(2, 'nikos2@email.com', 'ACTIVE'),
(3, 'nikos3@email.com', 'ACTIVE'),
(4, 'nikos4@email.com', 'ACTIVE'),
(5, 'nikos5@email.com', 'ACTIVE'),
(6, 'nikos6@email.com', 'ACTIVE'),
(7, 'nikos7@email.com', 'ACTIVE'),
(8, 'nikos8@email.com', 'ACTIVE'),
(9, 'nikos9@email.com', 'ACTIVE'),
(10, 'nikos10@email.com', 'ACTIVE');