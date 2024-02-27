ALTER TABLE area
     ADD COLUMN contains_components boolean  DEFAULT FALSE;


ALTER TABLE area
     DROP COLUMN icon;

