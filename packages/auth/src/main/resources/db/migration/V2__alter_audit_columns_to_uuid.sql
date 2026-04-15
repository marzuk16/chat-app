ALTER TABLE credentials ALTER COLUMN created_by TYPE UUID USING created_by::UUID;
ALTER TABLE credentials ALTER COLUMN updated_by TYPE UUID USING updated_by::UUID;
