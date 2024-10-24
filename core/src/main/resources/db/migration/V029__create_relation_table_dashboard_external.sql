CREATE TABLE external_dashboards
(
    id UUID PRIMARY KEY,
    dashboard_id UUID NOT NULL,
    FOREIGN KEY (dashboard_id) REFERENCES dashboard(id)
);