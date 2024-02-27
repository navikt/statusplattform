CREATE TABLE sub_area
(
    id          UUID         NOT NULL,
    name        VARCHAR(100) NOT NULL,
    created_at  timestamp with time zone  NOT NULL DEFAULT NOW(),
    updated_at  timestamp with time zone NULL,
    PRIMARY KEY (id)
);

CREATE TABLE area_sub_area
(
    area_id    UUID        NOT NULL,
    sub_area_id UUID        NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NULL,
    PRIMARY KEY (area_id, sub_area_id),
    FOREIGN KEY (sub_area_id) REFERENCES sub_area (id),
    FOREIGN KEY (area_id) REFERENCES area (id)
);

CREATE TABLE sub_area_service
(
    sub_area_id    UUID        NOT NULL,
    service_id      UUID        NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NULL,
    PRIMARY KEY (sub_area_id, service_id),
    FOREIGN KEY (service_id) REFERENCES service (id),
    FOREIGN KEY (sub_area_id) REFERENCES sub_area (id)
);