UPDATE movies SET original_title = title WHERE original_title IS NULL;
UPDATE tv SET original_name = name WHERE original_name IS NULL;

ALTER TABLE movies ALTER COLUMN original_title SET NOT NULL;
ALTER TABLE tv ALTER COLUMN original_name SET NOT NULL;
