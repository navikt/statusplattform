CREATE TABLE citizen_user
(
    id UUID NOT NULL,
    fornavn VARCHAR(50)  NOT NULL,
    etternavn VARCHAR(50) NOT NULL,
    tlf VARCHAR(8),
    epost VARCHAR(50),
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NULL,
    PRIMARY KEY (id),
);

