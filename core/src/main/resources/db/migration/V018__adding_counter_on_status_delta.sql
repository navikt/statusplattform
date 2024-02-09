ALTER TABLE service_status_delta
     ADD COLUMN counter integer NOT NULL
     DEFAULT 0;