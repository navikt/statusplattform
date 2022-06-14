ALTER TABLE ops_message
     ADD COLUMN start_time timestamp with time zone NULL,
     ADD COLUMN end_time Date,
     ADD COLUMN severity VARCHAR(10),
     ADD COLUMN state VARCHAR(10);