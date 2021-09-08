/*grant all on all tables in schema public to cloudsqliamuser;*/

CREATE TABLE public.area
(
    beskrivelse text COLLATE pg_catalog."default" NOT NULL,
    id text COLLATE pg_catalog."default" NOT NULL,
    ikon text COLLATE pg_catalog."default" NOT NULL,
    name text COLLATE pg_catalog."default" NOT NULL,
    rangering integer NOT NULL,
    services text[] COLLATE pg_catalog."default",
    CONSTRAINT "Area_pkey" PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE public.area
    OWNER to postgres;

CREATE TABLE public.dashboard
(
    name text COLLATE pg_catalog."default" NOT NULL,
    areas text[] COLLATE pg_catalog."default",
    CONSTRAINT dashboard_pkey PRIMARY KEY (name)
)

TABLESPACE pg_default;

ALTER TABLE public.dashboard
    OWNER to postgres;

CREATE TABLE public.record
(
    status text COLLATE pg_catalog."default",
    "timestamp" timestamp with time zone,
    responsetime integer NOT NULL,
    serviceid text COLLATE pg_catalog."default"
)

TABLESPACE pg_default;

ALTER TABLE public.record
    OWNER to postgres;


CREATE TABLE public.service
(
    name text COLLATE pg_catalog."default" NOT NULL,
    id text COLLATE pg_catalog."default" NOT NULL,
    type text COLLATE pg_catalog."default" NOT NULL,
    team text COLLATE pg_catalog."default" NOT NULL,
    dependencies text[] COLLATE pg_catalog."default" NOT NULL,
    monitorlink text COLLATE pg_catalog."default",
    description text COLLATE pg_catalog."default",
    logglink text COLLATE pg_catalog."default",
    CONSTRAINT service_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE public.service
    OWNER to postgres;
