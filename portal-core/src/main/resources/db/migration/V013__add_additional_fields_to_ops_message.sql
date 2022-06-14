ALTER TABLE ops_message ADD COLUMN start_time timestamp with time zone NULL;
ALTER TABLE ops_message ADD COLUMN end_time Date;
ALTER TABLE ops_message ADD COLUMN severity VARCHAR(10);
ALTER TABLE ops_message ADD COLUMN state VARCHAR(10);
