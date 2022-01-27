
CREATE TABLE service_maintenance
(
    id          UUID         NOT NULL,
    service_id    UUID        NOT NULL,
    description text NULL,
    start_time  timestamp with time zone  NOT NULL,
    end_time  timestamp with time zone  NOT NULL,
    created_at  timestamp with time zone  NOT NULL DEFAULT NOW(),
    updated_at  timestamp with time zone NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (service_id) REFERENCES service (id)
);

