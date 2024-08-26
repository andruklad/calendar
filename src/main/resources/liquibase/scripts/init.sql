-- liquibase formatted sql

-- changeset akladkevich:1
create table public.calendar_original
(
    id          integer not null
        primary key,
    country     varchar(255),
    year        integer,
    data        jsonb,
    date_time   timestamp(6),
    is_archived boolean,
    status      varchar(255)
        constraint calendar_original_status_check
            check ((status)::text = ANY
                   (ARRAY [('NEW'::character varying)::text, ('PROCESSED'::character varying)::text]))
);

alter table public.calendar_original
    owner to postgres;

create index calendar_original_country_year_is_archived_index
    on public.calendar_original (country, year, is_archived);


create sequence public.sequence_calendar_original_id;

alter sequence public.sequence_calendar_original_id owner to postgres;


create table public.calendar_final_month
(
    id          integer not null
        primary key,
    country     varchar(255),
    month       integer,
    year        integer,
    date_time   timestamp(6),
    days        varchar(255),
    is_archived boolean
);

alter table public.calendar_final_month
    owner to postgres;

create index calendar_final_month_country_year_month_is_archived_index
    on public.calendar_final_month (country, year, month, is_archived);


create sequence public.sequence_calendar_final_month_id;

alter sequence public.sequence_calendar_final_month_id owner to postgres;


create table calendar_final_transition
(
    id          integer not null
        primary key,
    country     varchar(255),
    year        integer,
    date_time   timestamp(6),
    day_from    varchar(255),
    day_to      varchar(255),
    is_archived boolean
);

alter table calendar_final_transition
    owner to postgres;

create index calendar_final_transition_country_year_day_from_is_archived_ind
    on calendar_final_transition (country, year, day_from, is_archived);


create table calendar_final_statistic
(
    id          integer not null
        primary key,
    country     varchar(255),
    year        integer,
    date_time   timestamp(6),
    holidays    integer,
    is_archived boolean,
    workdays    integer
);

alter table calendar_final_statistic
    owner to postgres;

create index calendar_final_statistic_country_year_is_archived_index
    on calendar_final_statistic (country, year, is_archived);