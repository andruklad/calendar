-- liquibase formatted sql

-- changeset akladkevich:1
create table public.calendar_original
(
    id          integer not null
        primary key,
    country     varchar(255),
    data        jsonb,
    date_time   timestamp(6),
    is_archived boolean,
    status      varchar(255)
        constraint calendar_original_status_check
            check ((status)::text = ANY ((ARRAY ['NEW'::character varying, 'PROCESSED'::character varying])::text[])),
    year        varchar(255)
);

alter table public.calendar_original
    owner to postgres;


create sequence public.sequence_calendar_original_id;

alter sequence public.sequence_calendar_original_id owner to postgres;


create table public.calendar_final_month
(
    id          integer not null
        primary key,
    country     varchar(255),
    date_time   timestamp(6),
    days        varchar(255),
    is_archived boolean,
    month       integer,
    status      varchar(255)
        constraint calendar_final_month_status_check
            check ((status)::text = ANY ((ARRAY ['NEW'::character varying, 'PROCESSED'::character varying])::text[])),
    year        integer
);

alter table public.calendar_final_month
    owner to postgres;


create sequence public.sequence_calendar_final_month_id;

alter sequence public.sequence_calendar_final_month_id owner to postgres;