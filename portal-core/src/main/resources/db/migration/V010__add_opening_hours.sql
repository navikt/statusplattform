CREATE TABLE service_opening_hours
(
    id UUID NOT NULL,
    service_id UUID NOT NULL,
    day_of_the_week DOW NOT NULL,
    opening_time TIME NOT NULL,
    closing_time TIME NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (service_id) REFERENCES service (id)
);

