ALTER TABLE service_status_delta
     ADD COLUMN active boolean NOT NULL
     DEFAULT true;