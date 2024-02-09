CREATE TABLE ops_message
(
    id UUID NOT NULL,
    intern_header VARCHAR(100) NOT NULL,
    intern_text VARCHAR(100) NOT NULL,
    extern_header VARCHAR(100) NULL,
    extern_text VARCHAR(100)  NULL,
    is_active boolean NOT NULL,
    only_show_for_nav_employees boolean NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NULL,
    PRIMARY KEY (id)
);

CREATE TABLE ops_message_service
(
    ops_message_id    UUID        NOT NULL,
    service_id      UUID        NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NULL,
    PRIMARY KEY (ops_message_id, service_id),
    FOREIGN KEY (service_id) REFERENCES service (id)
);

