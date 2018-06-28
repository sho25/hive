SELECT 'Upgrading MetaStore schema from 3.1.0 to 4.0.0';

-- HIVE-19416
ALTER TABLE "TBLS" ADD "TXN_ID" bigint;
ALTER TABLE "TBLS" ADD "WRITE_ID" bigint;
ALTER TABLE "TBLS" ADD "WRITEID_LIST" text;
ALTER TABLE "PARTITIONS" ADD "TXN_ID" bigint;
ALTER TABLE "PARTITIONS" ADD "WRITE_ID" bigint;
ALTER TABLE "PARTITIONS" ADD "WRITEID_LIST" text;

-- These lines need to be last.  Insert any changes above.
UPDATE "VERSION" SET "SCHEMA_VERSION"='4.0.0', "VERSION_COMMENT"='Hive release version 4.0.0' where "VER_ID"=1;
SELECT 'Finished upgrading MetaStore schema from 3.1.0 to 4.0.0';

