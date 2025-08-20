CREATE TABLE external_services
(
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL,
    FOREIGN KEY (service_id) REFERENCES service(id)
);