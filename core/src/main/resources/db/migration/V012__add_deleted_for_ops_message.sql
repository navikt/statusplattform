ALTER TABLE ops_message
     ADD COLUMN deleted boolean
     DEFAULT FALSE;