SELECT 'Upgrading MetaStore schema from 3.0.0 to 3.1.0';

-- HIVE-19440
ALTER TABLE "GLOBAL_PRIVS" ADD COLUMN "AUTHORIZER" character varying(128) DEFAULT NULL::character varying;
ALTER TABLE "GLOBAL_PRIVS" DROP CONSTRAINT "GLOBALPRIVILEGEINDEX";
ALTER TABLE ONLY "GLOBAL_PRIVS"
    ADD CONSTRAINT "GLOBALPRIVILEGEINDEX" UNIQUE ("AUTHORIZER", "PRINCIPAL_NAME", "PRINCIPAL_TYPE", "USER_PRIV", "GRANTOR", "GRANTOR_TYPE");

ALTER TABLE "DB_PRIVS" ADD COLUMN "AUTHORIZER" character varying(128) DEFAULT NULL::character varying;
ALTER TABLE "DB_PRIVS" DROP CONSTRAINT "DBPRIVILEGEINDEX";
ALTER TABLE ONLY "DB_PRIVS"
    ADD CONSTRAINT "DBPRIVILEGEINDEX" UNIQUE ("AUTHORIZER", "DB_ID", "PRINCIPAL_NAME", "PRINCIPAL_TYPE", "DB_PRIV", "GRANTOR", "GRANTOR_TYPE");

ALTER TABLE "TBL_PRIVS" ADD COLUMN "AUTHORIZER" character varying(128) DEFAULT NULL::character varying;
ALTER TABLE "TBL_PRIVS" DROP CONSTRAINT "DBPRIVILEGEINDEX";
ALTER TABLE ONLY "DB_PRIVS"
    ADD CONSTRAINT "DBPRIVILEGEINDEX" UNIQUE ("AUTHORIZER", "DB_ID", "PRINCIPAL_NAME", "PRINCIPAL_TYPE", "DB_PRIV", "GRANTOR", "GRANTOR_TYPE");

ALTER TABLE "PART_PRIVS" ADD COLUMN "AUTHORIZER" character varying(128) DEFAULT NULL::character varying;
ALTER TABLE "PART_PRIVS" DROP CONSTRAINT "TABLEPRIVILEGEINDEX";
CREATE INDEX "TABLEPRIVILEGEINDEX" ON "TBL_PRIVS" USING btree ("AUTHORIZER", "TBL_ID", "PRINCIPAL_NAME", "PRINCIPAL_TYPE", "TBL_PRIV", "GRANTOR", "GRANTOR_TYPE");

ALTER TABLE "TBL_COL_PRIVS" ADD COLUMN "AUTHORIZER" character varying(128) DEFAULT NULL::character varying;
ALTER TABLE "TBL_COL_PRIVS" DROP CONSTRAINT "TABLECOLUMNPRIVILEGEINDEX";
CREATE INDEX "TABLECOLUMNPRIVILEGEINDEX" ON "TBL_COL_PRIVS" USING btree ("AUTHORIZER", "TBL_ID", "COLUMN_NAME", "PRINCIPAL_NAME", "PRINCIPAL_TYPE", "TBL_COL_PRIV", "GRANTOR", "GRANTOR_TYPE");

ALTER TABLE "PART_COL_PRIVS" ADD COLUMN "AUTHORIZER" character varying(128) DEFAULT NULL::character varying;
ALTER TABLE "PART_COL_PRIVS" DROP CONSTRAINT "PARTITIONCOLUMNPRIVILEGEINDEX";
CREATE INDEX "PARTITIONCOLUMNPRIVILEGEINDEX" ON "PART_COL_PRIVS" USING btree ("AUTHORIZER", "PART_ID", "COLUMN_NAME", "PRINCIPAL_NAME", "PRINCIPAL_TYPE", "PART_COL_PRIV", "GRANTOR", "GRANTOR_TYPE");

-- These lines need to be last.  Insert any changes above.
UPDATE "VERSION" SET "SCHEMA_VERSION"='3.1.0', "VERSION_COMMENT"='Hive release version 3.1.0' where "VER_ID"=1;
SELECT 'Finished upgrading MetaStore schema from 3.0.0 to 3.1.0';
