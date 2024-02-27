ALTER TABLE service
     ADD COLUMN polling_on_prem boolean
     DEFAULT FALSE;