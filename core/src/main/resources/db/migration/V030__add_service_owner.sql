CREATE TABLE service_team_owner
(
    team_katalogen_id UUID PRIMARY KEY,
    service_id UUID NOT NULL,
    FOREIGN KEY (service_id) REFERENCES service(id)
);