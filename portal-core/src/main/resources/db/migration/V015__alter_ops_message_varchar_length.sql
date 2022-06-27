ALTER TABLE ops_message ALTER COLUMN intern_text type VARCHAR(500);
ALTER TABLE ops_message ALTER COLUMN extern_text type VARCHAR(500);
ALTER TABLE ops_message DROP COLUMN state;