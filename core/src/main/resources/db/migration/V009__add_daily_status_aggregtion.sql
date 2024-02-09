CREATE TABLE daily_status_aggregation_service
(
    id    UUID        NOT NULL,
    service_id      UUID        NOT NULL,
    aggregation_date DATE NOT NULL,
    number_of_status_ok INTEGER NOT NULL,
    number_of_status_issue INTEGER NOT NULL,
    number_of_status_down INTEGER NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (service_id) REFERENCES service (id)
);