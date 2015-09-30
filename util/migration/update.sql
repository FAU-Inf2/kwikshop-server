--liquibase formatted sql

--changeset db:1
ALTER TABLE boughtItem ADD serverInternalItem boolean NOT NULL DEFAULT(false);
UPDATE boughtItem SET serverInternalItem=true WHERE name='START_ITEM';
UPDATE boughtItem SET serverInternalItem=true WHERE name='END_ITEM';
