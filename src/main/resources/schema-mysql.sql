ALTER TABLE RIDE_REQUEST
    ADD COLUMN IF NOT EXISTS accepted_driver_display_id VARCHAR(255) NULL;

ALTER TABLE driver
    ADD COLUMN IF NOT EXISTS driver_identifier VARCHAR(255) NULL;

ALTER TABLE driver
    ADD COLUMN IF NOT EXISTS driver_display_id VARCHAR(255) NULL;

UPDATE driver
SET driver_identifier = identifier
WHERE driver_identifier IS NULL OR driver_identifier = '';

UPDATE driver
SET driver_display_id = CONCAT('DRV-', UPPER(REGEXP_REPLACE(driver_identifier, '[^A-Z0-9]+', '-')))
WHERE (driver_display_id IS NULL OR driver_display_id = '')
  AND driver_identifier IS NOT NULL
  AND driver_identifier <> '';
