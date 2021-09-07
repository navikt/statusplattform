/*grant all on all tables in schema public to cloudsqliamuser;*/

CREATE TABLE public.area (
    beskrivelse text NOT NULL,
    id text NOT NULL,
    ikon text NOT NULL,
    name text NOT NULL,
    rangering integer NOT NULL,
    services text[]
);
