CREATE TABLE service_opening_hours
(
    id UUID NOT NULL,
    service_id UUID NOT NULL,
    day_of_the_week INT NOT NULL,
    opening_time TIME NOT NULL,
    closing_time TIME NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (service_id) REFERENCES service (id),
    constraint valid_weekday_c1 check (day_of_the_week <= 6),
    constraint valid_weekday_c2 check (day_of_the_week >= 0)
);

