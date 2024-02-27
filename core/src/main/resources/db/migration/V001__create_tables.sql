/*grant all on all tables in schema public to cloudsqliamuser;*/


CREATE TABLE service
(
    id          UUID         NOT NULL,
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(20)  NOT NULL,
    team        VARCHAR(100) NOT NULL,
    monitorlink VARCHAR(300) NULL,
    logglink    VARCHAR(300) NULL,
    description text NULL,
    created_at  timestamp with time zone  NOT NULL DEFAULT NOW(),
    updated_at  timestamp with time zone NULL,
    PRIMARY KEY (id)
);

CREATE TABLE service_service
(
    service1_id     UUID        NOT NULL,
    service2_id     UUID        NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NULL,
    PRIMARY KEY (service1_id, service2_id),
    FOREIGN KEY (service1_id) REFERENCES service (id),
    FOREIGN KEY (service2_id) REFERENCES service (id)
);

CREATE TABLE service_status
(
    id            UUID        NOT NULL,
    service_id    UUID        NOT NULL,
    status        VARCHAR(20) NOT NULL,
    response_time integer NULL,
    created_at    timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at    timestamp with time zone NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (service_id) REFERENCES service (id)
);

CREATE TABLE area
(
    id          UUID         NOT NULL,
    name        VARCHAR(100) NOT NULL,
    icon        VARCHAR(20) NULL,
    description text NULL,
    created_at  timestamp with time zone  NOT NULL DEFAULT NOW(),
    updated_at  timestamp with time zone NULL,
    PRIMARY KEY (id)
);

CREATE TABLE area_service
(
    area_id    UUID        NOT NULL,
    service_id UUID        NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NULL,
    PRIMARY KEY (area_id, service_id),
    FOREIGN KEY (service_id) REFERENCES service (id),
    FOREIGN KEY (area_id) REFERENCES area (id)
);



CREATE TABLE dashboard
(
    id         UUID         NOT NULL,
    name       VARCHAR(100) NOT NULL,
    created_at timestamp with time zone  NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NULL,
    PRIMARY KEY (id)
);

CREATE TABLE dashboard_area
(
    dashboard_id UUID        NOT NULL,
    area_id    UUID        NOT NULL,
    order_in_dashboard int        NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NULL,
    PRIMARY KEY (dashboard_id, area_id),
    FOREIGN KEY (dashboard_id) REFERENCES dashboard (id),
    FOREIGN KEY (area_id) REFERENCES area (id)
);