

CREATE TABLE service_status_delta
(
    id            UUID        NOT NULL,
    service_id    UUID        NOT NULL,
    status        VARCHAR(20) NOT NULL,
    description   VARCHAR(1000)  NULL,
    logglink      VARCHAR(100)  NULL,
    response_time integer NULL,
    created_at    timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at    timestamp with time zone NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (service_id) REFERENCES service (id)
);