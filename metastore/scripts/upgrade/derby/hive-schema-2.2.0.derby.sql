-- Timestamp: 2011-09-22 15:32:02.024
-- Source database is: /home/carl/Work/repos/hive1/metastore/scripts/upgrade/derby/mdb
-- Connection URL is: jdbc:derby:/home/carl/Work/repos/hive1/metastore/scripts/upgrade/derby/mdb
-- Specified schema is: APP
-- appendLogs: false

-- ----------------------------------------------
-- DDL Statements for functions
-- ----------------------------------------------

CREATE FUNCTION "APP"."NUCLEUS_ASCII" (C CHAR(1)) RETURNS INTEGER LANGUAGE JAVA PARAMETER STYLE JAVA READS SQL DATA CALLED ON NULL INPUT EXTERNAL NAME 'org.datanucleus.store.rdbms.adapter.DerbySQLFunction.ascii' ;

CREATE FUNCTION "APP"."NUCLEUS_MATCHES" (TEXT VARCHAR(8000),PATTERN VARCHAR(8000)) RETURNS INTEGER LANGUAGE JAVA PARAMETER STYLE JAVA READS SQL DATA CALLED ON NULL INPUT EXTERNAL NAME 'org.datanucleus.store.rdbms.adapter.DerbySQLFunction.matches' ;

-- ----------------------------------------------
-- DDL Statements for tables
-- ----------------------------------------------

CREATE TABLE "APP"."DBS" ("DB_ID" BIGINT NOT NULL, "DESC" VARCHAR(4000), "DB_LOCATION_URI" VARCHAR(4000) NOT NULL, "NAME" VARCHAR(128), "OWNER_NAME" VARCHAR(128), "OWNER_TYPE" VARCHAR(10));

CREATE TABLE "APP"."TBL_PRIVS" ("TBL_GRANT_ID" BIGINT NOT NULL, "CREATE_TIME" INTEGER NOT NULL, "GRANT_OPTION" SMALLINT NOT NULL, "GRANTOR" VARCHAR(128), "GRANTOR_TYPE" VARCHAR(128), "PRINCIPAL_NAME" VARCHAR(128), "PRINCIPAL_TYPE" VARCHAR(128), "TBL_PRIV" VARCHAR(128), "TBL_ID" BIGINT);

CREATE TABLE "APP"."DATABASE_PARAMS" ("DB_ID" BIGINT NOT NULL, "PARAM_KEY" VARCHAR(180) NOT NULL, "PARAM_VALUE" VARCHAR(4000));

CREATE TABLE "APP"."TBL_COL_PRIVS" ("TBL_COLUMN_GRANT_ID" BIGINT NOT NULL, "COLUMN_NAME" VARCHAR(1000), "CREATE_TIME" INTEGER NOT NULL, "GRANT_OPTION" SMALLINT NOT NULL, "GRANTOR" VARCHAR(128), "GRANTOR_TYPE" VARCHAR(128), "PRINCIPAL_NAME" VARCHAR(128), "PRINCIPAL_TYPE" VARCHAR(128), "TBL_COL_PRIV" VARCHAR(128), "TBL_ID" BIGINT);

CREATE TABLE "APP"."SERDE_PARAMS" ("SERDE_ID" BIGINT NOT NULL, "PARAM_KEY" VARCHAR(256) NOT NULL, "PARAM_VALUE" VARCHAR(4000));

CREATE TABLE "APP"."COLUMNS_V2" ("CD_ID" BIGINT NOT NULL, "COMMENT" VARCHAR(4000), "COLUMN_NAME" VARCHAR(1000) NOT NULL, "TYPE_NAME" VARCHAR(4000), "INTEGER_IDX" INTEGER NOT NULL);

CREATE TABLE "APP"."SORT_COLS" ("SD_ID" BIGINT NOT NULL, "COLUMN_NAME" VARCHAR(1000), "ORDER" INTEGER NOT NULL, "INTEGER_IDX" INTEGER NOT NULL);

CREATE TABLE "APP"."CDS" ("CD_ID" BIGINT NOT NULL);

CREATE TABLE "APP"."PARTITION_KEY_VALS" ("PART_ID" BIGINT NOT NULL, "PART_KEY_VAL" VARCHAR(256), "INTEGER_IDX" INTEGER NOT NULL);

CREATE TABLE "APP"."DB_PRIVS" ("DB_GRANT_ID" BIGINT NOT NULL, "CREATE_TIME" INTEGER NOT NULL, "DB_ID" BIGINT, "GRANT_OPTION" SMALLINT NOT NULL, "GRANTOR" VARCHAR(128), "GRANTOR_TYPE" VARCHAR(128), "PRINCIPAL_NAME" VARCHAR(128), "PRINCIPAL_TYPE" VARCHAR(128), "DB_PRIV" VARCHAR(128));

CREATE TABLE "APP"."IDXS" ("INDEX_ID" BIGINT NOT NULL, "CREATE_TIME" INTEGER NOT NULL, "DEFERRED_REBUILD" CHAR(1) NOT NULL, "INDEX_HANDLER_CLASS" VARCHAR(4000), "INDEX_NAME" VARCHAR(128), "INDEX_TBL_ID" BIGINT, "LAST_ACCESS_TIME" INTEGER NOT NULL, "ORIG_TBL_ID" BIGINT, "SD_ID" BIGINT);

CREATE TABLE "APP"."INDEX_PARAMS" ("INDEX_ID" BIGINT NOT NULL, "PARAM_KEY" VARCHAR(256) NOT NULL, "PARAM_VALUE" VARCHAR(4000));

CREATE TABLE "APP"."PARTITIONS" ("PART_ID" BIGINT NOT NULL, "CREATE_TIME" INTEGER NOT NULL, "LAST_ACCESS_TIME" INTEGER NOT NULL, "PART_NAME" VARCHAR(767), "SD_ID" BIGINT, "TBL_ID" BIGINT);

CREATE TABLE "APP"."SERDES" ("SERDE_ID" BIGINT NOT NULL, "NAME" VARCHAR(128), "SLIB" VARCHAR(4000));

CREATE TABLE "APP"."PART_PRIVS" ("PART_GRANT_ID" BIGINT NOT NULL, "CREATE_TIME" INTEGER NOT NULL, "GRANT_OPTION" SMALLINT NOT NULL, "GRANTOR" VARCHAR(128), "GRANTOR_TYPE" VARCHAR(128), "PART_ID" BIGINT, "PRINCIPAL_NAME" VARCHAR(128), "PRINCIPAL_TYPE" VARCHAR(128), "PART_PRIV" VARCHAR(128));

CREATE TABLE "APP"."ROLE_MAP" ("ROLE_GRANT_ID" BIGINT NOT NULL, "ADD_TIME" INTEGER NOT NULL, "GRANT_OPTION" SMALLINT NOT NULL, "GRANTOR" VARCHAR(128), "GRANTOR_TYPE" VARCHAR(128), "PRINCIPAL_NAME" VARCHAR(128), "PRINCIPAL_TYPE" VARCHAR(128), "ROLE_ID" BIGINT);

CREATE TABLE "APP"."TYPES" ("TYPES_ID" BIGINT NOT NULL, "TYPE_NAME" VARCHAR(128), "TYPE1" VARCHAR(767), "TYPE2" VARCHAR(767));

CREATE TABLE "APP"."GLOBAL_PRIVS" ("USER_GRANT_ID" BIGINT NOT NULL, "CREATE_TIME" INTEGER NOT NULL, "GRANT_OPTION" SMALLINT NOT NULL, "GRANTOR" VARCHAR(128), "GRANTOR_TYPE" VARCHAR(128), "PRINCIPAL_NAME" VARCHAR(128), "PRINCIPAL_TYPE" VARCHAR(128), "USER_PRIV" VARCHAR(128));

CREATE TABLE "APP"."PARTITION_PARAMS" ("PART_ID" BIGINT NOT NULL, "PARAM_KEY" VARCHAR(256) NOT NULL, "PARAM_VALUE" VARCHAR(4000));

CREATE TABLE "APP"."PARTITION_EVENTS" ("PART_NAME_ID" BIGINT NOT NULL, "DB_NAME" VARCHAR(128), "EVENT_TIME" BIGINT NOT NULL, "EVENT_TYPE" INTEGER NOT NULL, "PARTITION_NAME" VARCHAR(767), "TBL_NAME" VARCHAR(128));

CREATE TABLE "APP"."COLUMNS" ("SD_ID" BIGINT NOT NULL, "COMMENT" VARCHAR(256), "COLUMN_NAME" VARCHAR(128) NOT NULL, "TYPE_NAME" VARCHAR(4000) NOT NULL, "INTEGER_IDX" INTEGER NOT NULL);

CREATE TABLE "APP"."ROLES" ("ROLE_ID" BIGINT NOT NULL, "CREATE_TIME" INTEGER NOT NULL, "OWNER_NAME" VARCHAR(128), "ROLE_NAME" VARCHAR(128));

CREATE TABLE "APP"."TBLS" ("TBL_ID" BIGINT NOT NULL, "CREATE_TIME" INTEGER NOT NULL, "DB_ID" BIGINT, "LAST_ACCESS_TIME" INTEGER NOT NULL, "OWNER" VARCHAR(767), "RETENTION" INTEGER NOT NULL, "SD_ID" BIGINT, "TBL_NAME" VARCHAR(128), "TBL_TYPE" VARCHAR(128), "VIEW_EXPANDED_TEXT" LONG VARCHAR, "VIEW_ORIGINAL_TEXT" LONG VARCHAR, "MM_WATERMARK_WRITE_ID" BIGINT, "MM_NEXT_WRITE_ID" BIGINT);

CREATE TABLE "APP"."PARTITION_KEYS" ("TBL_ID" BIGINT NOT NULL, "PKEY_COMMENT" VARCHAR(4000), "PKEY_NAME" VARCHAR(128) NOT NULL, "PKEY_TYPE" VARCHAR(767) NOT NULL, "INTEGER_IDX" INTEGER NOT NULL);

CREATE TABLE "APP"."PART_COL_PRIVS" ("PART_COLUMN_GRANT_ID" BIGINT NOT NULL, "COLUMN_NAME" VARCHAR(1000), "CREATE_TIME" INTEGER NOT NULL, "GRANT_OPTION" SMALLINT NOT NULL, "GRANTOR" VARCHAR(128), "GRANTOR_TYPE" VARCHAR(128), "PART_ID" BIGINT, "PRINCIPAL_NAME" VARCHAR(128), "PRINCIPAL_TYPE" VARCHAR(128), "PART_COL_PRIV" VARCHAR(128));

CREATE TABLE "APP"."SDS" ("SD_ID" BIGINT NOT NULL, "INPUT_FORMAT" VARCHAR(4000), "IS_COMPRESSED" CHAR(1) NOT NULL, "LOCATION" VARCHAR(4000), "NUM_BUCKETS" INTEGER NOT NULL, "OUTPUT_FORMAT" VARCHAR(4000), "SERDE_ID" BIGINT, "CD_ID" BIGINT, "IS_STOREDASSUBDIRECTORIES" CHAR(1) NOT NULL);

CREATE TABLE "APP"."SEQUENCE_TABLE" ("SEQUENCE_NAME" VARCHAR(256) NOT NULL, "NEXT_VAL" BIGINT NOT NULL);

RUN '022-HIVE-11107.derby.sql';

CREATE TABLE "APP"."BUCKETING_COLS" ("SD_ID" BIGINT NOT NULL, "BUCKET_COL_NAME" VARCHAR(256), "INTEGER_IDX" INTEGER NOT NULL);

CREATE TABLE "APP"."TYPE_FIELDS" ("TYPE_NAME" BIGINT NOT NULL, "COMMENT" VARCHAR(256), "FIELD_NAME" VARCHAR(128) NOT NULL, "FIELD_TYPE" VARCHAR(767) NOT NULL, "INTEGER_IDX" INTEGER NOT NULL);

CREATE TABLE "APP"."NUCLEUS_TABLES" ("CLASS_NAME" VARCHAR(128) NOT NULL, "TABLE_NAME" VARCHAR(128) NOT NULL, "TYPE" VARCHAR(4) NOT NULL, "OWNER" VARCHAR(2) NOT NULL, "VERSION" VARCHAR(20) NOT NULL, "INTERFACE_NAME" VARCHAR(256) DEFAULT NULL);

CREATE TABLE "APP"."SD_PARAMS" ("SD_ID" BIGINT NOT NULL, "PARAM_KEY" VARCHAR(256) NOT NULL, "PARAM_VALUE" VARCHAR(4000));

CREATE TABLE "APP"."SKEWED_STRING_LIST" ("STRING_LIST_ID" BIGINT NOT NULL);

CREATE TABLE "APP"."SKEWED_STRING_LIST_VALUES" ("STRING_LIST_ID" BIGINT NOT NULL, "STRING_LIST_VALUE" VARCHAR(256), "INTEGER_IDX" INTEGER NOT NULL);

CREATE TABLE "APP"."SKEWED_COL_NAMES" ("SD_ID" BIGINT NOT NULL, "SKEWED_COL_NAME" VARCHAR(256), "INTEGER_IDX" INTEGER NOT NULL);

CREATE TABLE "APP"."SKEWED_COL_VALUE_LOC_MAP" ("SD_ID" BIGINT NOT NULL, "STRING_LIST_ID_KID" BIGINT NOT NULL, "LOCATION" VARCHAR(4000));

CREATE TABLE "APP"."SKEWED_VALUES" ("SD_ID_OID" BIGINT NOT NULL, "STRING_LIST_ID_EID" BIGINT NOT NULL, "INTEGER_IDX" INTEGER NOT NULL);

CREATE TABLE "APP"."MASTER_KEYS" ("KEY_ID" INTEGER NOT NULL generated always as identity (start with 1), "MASTER_KEY" VARCHAR(767));

CREATE TABLE "APP"."DELEGATION_TOKENS" ( "TOKEN_IDENT" VARCHAR(767) NOT NULL, "TOKEN" VARCHAR(767));

CREATE TABLE "APP"."PART_COL_STATS"("DB_NAME" VARCHAR(128) NOT NULL,"TABLE_NAME" VARCHAR(128) NOT NULL, "PARTITION_NAME" VARCHAR(767) NOT NULL, "COLUMN_NAME" VARCHAR(1000) NOT NULL, "COLUMN_TYPE" VARCHAR(128) NOT NULL, "LONG_LOW_VALUE" BIGINT, "LONG_HIGH_VALUE" BIGINT, "DOUBLE_LOW_VALUE" DOUBLE, "DOUBLE_HIGH_VALUE" DOUBLE, "BIG_DECIMAL_LOW_VALUE" VARCHAR(4000), "BIG_DECIMAL_HIGH_VALUE" VARCHAR(4000),"NUM_DISTINCTS" BIGINT, "NUM_NULLS" BIGINT NOT NULL, "AVG_COL_LEN" DOUBLE, "MAX_COL_LEN" BIGINT, "NUM_TRUES" BIGINT, "NUM_FALSES" BIGINT, "LAST_ANALYZED" BIGINT, "CS_ID" BIGINT NOT NULL, "PART_ID" BIGINT NOT NULL);

CREATE TABLE "APP"."VERSION" ("VER_ID" BIGINT NOT NULL, "SCHEMA_VERSION" VARCHAR(127) NOT NULL, "VERSION_COMMENT" VARCHAR(255));

CREATE TABLE "APP"."FUNCS" ("FUNC_ID" BIGINT NOT NULL, "CLASS_NAME" VARCHAR(4000), "CREATE_TIME" INTEGER NOT NULL, "DB_ID" BIGINT, "FUNC_NAME" VARCHAR(128), "FUNC_TYPE" INTEGER NOT NULL, "OWNER_NAME" VARCHAR(128), "OWNER_TYPE" VARCHAR(10));

CREATE TABLE "APP"."FUNC_RU" ("FUNC_ID" BIGINT NOT NULL, "RESOURCE_TYPE" INTEGER NOT NULL, "RESOURCE_URI" VARCHAR(4000), "INTEGER_IDX" INTEGER NOT NULL);

CREATE TABLE "APP"."NOTIFICATION_LOG" ("NL_ID" BIGINT NOT NULL, "DB_NAME" VARCHAR(128), "EVENT_ID" BIGINT NOT NULL, "EVENT_TIME" INTEGER NOT NULL, "EVENT_TYPE" VARCHAR(32) NOT NULL, "MESSAGE" LONG VARCHAR, "TBL_NAME" VARCHAR(128));

CREATE TABLE "APP"."NOTIFICATION_SEQUENCE" ("NNI_ID" BIGINT NOT NULL, "NEXT_EVENT_ID" BIGINT NOT NULL);

CREATE TABLE "APP"."KEY_CONSTRAINTS" ("CHILD_CD_ID" BIGINT, "CHILD_INTEGER_IDX" INTEGER NOT NULL, "CHILD_TBL_ID" BIGINT, "PARENT_CD_ID" BIGINT NOT NULL, "PARENT_INTEGER_IDX" INTEGER, "PARENT_TBL_ID" BIGINT NOT NULL,  "POSITION" BIGINT NOT NULL, "CONSTRAINT_NAME" VARCHAR(400) NOT NULL, "CONSTRAINT_TYPE" SMALLINT NOT NULL, "UPDATE_RULE" SMALLINT, "DELETE_RULE" SMALLINT, "ENABLE_VALIDATE_RELY" SMALLINT NOT NULL);

ALTER TABLE "APP"."KEY_CONSTRAINTS" ADD CONSTRAINT "CONSTRAINTS_PK" PRIMARY KEY ("CONSTRAINT_NAME", "POSITION");

CREATE INDEX "APP"."CONSTRAINTS_PARENT_TBL_ID_INDEX" ON "APP"."KEY_CONSTRAINTS"("PARENT_TBL_ID");

CREATE TABLE "APP"."TBL_WRITES" ("TW_ID" BIGINT NOT NULL, "TBL_ID" BIGINT NOT NULL, "WRITE_ID" BIGINT NOT NULL, "STATE" CHAR(1) NOT NULL, "LAST_HEARTBEAT" BIGINT);

ALTER TABLE "APP"."TBL_WRITES" ADD CONSTRAINT "TBL_WRITES_PK" PRIMARY KEY ("TW_ID");

ALTER TABLE "APP"."TBL_WRITES" ADD CONSTRAINT "TBL_WRITES_FK1" FOREIGN KEY ("TBL_ID") REFERENCES "APP"."TBLS" ("TBL_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

CREATE UNIQUE INDEX "APP"."UNIQUEWRITE" ON "APP"."TBL_WRITES" ("TBL_ID", "WRITE_ID");



-- ----------------------------------------------
-- DDL Statements for indexes
-- ----------------------------------------------

CREATE UNIQUE INDEX "APP"."UNIQUEINDEX" ON "APP"."IDXS" ("INDEX_NAME", "ORIG_TBL_ID");

CREATE INDEX "APP"."TABLECOLUMNPRIVILEGEINDEX" ON "APP"."TBL_COL_PRIVS" ("TBL_ID", "COLUMN_NAME", "PRINCIPAL_NAME", "PRINCIPAL_TYPE", "TBL_COL_PRIV", "GRANTOR", "GRANTOR_TYPE");

CREATE UNIQUE INDEX "APP"."DBPRIVILEGEINDEX" ON "APP"."DB_PRIVS" ("DB_ID", "PRINCIPAL_NAME", "PRINCIPAL_TYPE", "DB_PRIV", "GRANTOR", "GRANTOR_TYPE");

CREATE INDEX "APP"."PCS_STATS_IDX" ON "APP"."PART_COL_STATS" ("DB_NAME","TABLE_NAME","COLUMN_NAME","PARTITION_NAME");

CREATE INDEX "APP"."PARTPRIVILEGEINDEX" ON "APP"."PART_PRIVS" ("PART_ID", "PRINCIPAL_NAME", "PRINCIPAL_TYPE", "PART_PRIV", "GRANTOR", "GRANTOR_TYPE");

CREATE UNIQUE INDEX "APP"."ROLEENTITYINDEX" ON "APP"."ROLES" ("ROLE_NAME");

CREATE INDEX "APP"."TABLEPRIVILEGEINDEX" ON "APP"."TBL_PRIVS" ("TBL_ID", "PRINCIPAL_NAME", "PRINCIPAL_TYPE", "TBL_PRIV", "GRANTOR", "GRANTOR_TYPE");

CREATE UNIQUE INDEX "APP"."UNIQUETABLE" ON "APP"."TBLS" ("TBL_NAME", "DB_ID");

CREATE UNIQUE INDEX "APP"."UNIQUE_DATABASE" ON "APP"."DBS" ("NAME");

CREATE UNIQUE INDEX "APP"."USERROLEMAPINDEX" ON "APP"."ROLE_MAP" ("PRINCIPAL_NAME", "ROLE_ID", "GRANTOR", "GRANTOR_TYPE");

CREATE UNIQUE INDEX "APP"."GLOBALPRIVILEGEINDEX" ON "APP"."GLOBAL_PRIVS" ("PRINCIPAL_NAME", "PRINCIPAL_TYPE", "USER_PRIV", "GRANTOR", "GRANTOR_TYPE");

CREATE UNIQUE INDEX "APP"."UNIQUE_TYPE" ON "APP"."TYPES" ("TYPE_NAME");

CREATE INDEX "APP"."PARTITIONCOLUMNPRIVILEGEINDEX" ON "APP"."PART_COL_PRIVS" ("PART_ID", "COLUMN_NAME", "PRINCIPAL_NAME", "PRINCIPAL_TYPE", "PART_COL_PRIV", "GRANTOR", "GRANTOR_TYPE");

CREATE UNIQUE INDEX "APP"."UNIQUEPARTITION" ON "APP"."PARTITIONS" ("PART_NAME", "TBL_ID");

CREATE UNIQUE INDEX "APP"."UNIQUEFUNCTION" ON "APP"."FUNCS" ("FUNC_NAME", "DB_ID");

CREATE INDEX "APP"."FUNCS_N49" ON "APP"."FUNCS" ("DB_ID");

CREATE INDEX "APP"."FUNC_RU_N49" ON "APP"."FUNC_RU" ("FUNC_ID");

-- ----------------------------------------------
-- DDL Statements for keys
-- ----------------------------------------------

-- primary/unique
ALTER TABLE "APP"."IDXS" ADD CONSTRAINT "IDXS_PK" PRIMARY KEY ("INDEX_ID");

ALTER TABLE "APP"."TBL_COL_PRIVS" ADD CONSTRAINT "TBL_COL_PRIVS_PK" PRIMARY KEY ("TBL_COLUMN_GRANT_ID");

ALTER TABLE "APP"."CDS" ADD CONSTRAINT "SQL110922153006460" PRIMARY KEY ("CD_ID");

ALTER TABLE "APP"."DB_PRIVS" ADD CONSTRAINT "DB_PRIVS_PK" PRIMARY KEY ("DB_GRANT_ID");

ALTER TABLE "APP"."INDEX_PARAMS" ADD CONSTRAINT "INDEX_PARAMS_PK" PRIMARY KEY ("INDEX_ID", "PARAM_KEY");

ALTER TABLE "APP"."PARTITION_KEYS" ADD CONSTRAINT "PARTITION_KEY_PK" PRIMARY KEY ("TBL_ID", "PKEY_NAME");

ALTER TABLE "APP"."SEQUENCE_TABLE" ADD CONSTRAINT "SEQUENCE_TABLE_PK" PRIMARY KEY ("SEQUENCE_NAME");

ALTER TABLE "APP"."PART_PRIVS" ADD CONSTRAINT "PART_PRIVS_PK" PRIMARY KEY ("PART_GRANT_ID");

ALTER TABLE "APP"."SDS" ADD CONSTRAINT "SDS_PK" PRIMARY KEY ("SD_ID");

ALTER TABLE "APP"."SERDES" ADD CONSTRAINT "SERDES_PK" PRIMARY KEY ("SERDE_ID");

ALTER TABLE "APP"."COLUMNS" ADD CONSTRAINT "COLUMNS_PK" PRIMARY KEY ("SD_ID", "COLUMN_NAME");

ALTER TABLE "APP"."PARTITION_EVENTS" ADD CONSTRAINT "PARTITION_EVENTS_PK" PRIMARY KEY ("PART_NAME_ID");

ALTER TABLE "APP"."TYPE_FIELDS" ADD CONSTRAINT "TYPE_FIELDS_PK" PRIMARY KEY ("TYPE_NAME", "FIELD_NAME");

ALTER TABLE "APP"."ROLES" ADD CONSTRAINT "ROLES_PK" PRIMARY KEY ("ROLE_ID");

ALTER TABLE "APP"."TBL_PRIVS" ADD CONSTRAINT "TBL_PRIVS_PK" PRIMARY KEY ("TBL_GRANT_ID");

ALTER TABLE "APP"."SERDE_PARAMS" ADD CONSTRAINT "SERDE_PARAMS_PK" PRIMARY KEY ("SERDE_ID", "PARAM_KEY");

ALTER TABLE "APP"."NUCLEUS_TABLES" ADD CONSTRAINT "NUCLEUS_TABLES_PK" PRIMARY KEY ("CLASS_NAME");

ALTER TABLE "APP"."TBLS" ADD CONSTRAINT "TBLS_PK" PRIMARY KEY ("TBL_ID");

ALTER TABLE "APP"."SD_PARAMS" ADD CONSTRAINT "SD_PARAMS_PK" PRIMARY KEY ("SD_ID", "PARAM_KEY");

ALTER TABLE "APP"."DATABASE_PARAMS" ADD CONSTRAINT "DATABASE_PARAMS_PK" PRIMARY KEY ("DB_ID", "PARAM_KEY");

ALTER TABLE "APP"."DBS" ADD CONSTRAINT "DBS_PK" PRIMARY KEY ("DB_ID");

ALTER TABLE "APP"."ROLE_MAP" ADD CONSTRAINT "ROLE_MAP_PK" PRIMARY KEY ("ROLE_GRANT_ID");

ALTER TABLE "APP"."GLOBAL_PRIVS" ADD CONSTRAINT "GLOBAL_PRIVS_PK" PRIMARY KEY ("USER_GRANT_ID");

ALTER TABLE "APP"."BUCKETING_COLS" ADD CONSTRAINT "BUCKETING_COLS_PK" PRIMARY KEY ("SD_ID", "INTEGER_IDX");

ALTER TABLE "APP"."SORT_COLS" ADD CONSTRAINT "SORT_COLS_PK" PRIMARY KEY ("SD_ID", "INTEGER_IDX");

ALTER TABLE "APP"."PARTITION_KEY_VALS" ADD CONSTRAINT "PARTITION_KEY_VALS_PK" PRIMARY KEY ("PART_ID", "INTEGER_IDX");

ALTER TABLE "APP"."TYPES" ADD CONSTRAINT "TYPES_PK" PRIMARY KEY ("TYPES_ID");

ALTER TABLE "APP"."COLUMNS_V2" ADD CONSTRAINT "SQL110922153006740" PRIMARY KEY ("CD_ID", "COLUMN_NAME");

ALTER TABLE "APP"."PART_COL_PRIVS" ADD CONSTRAINT "PART_COL_PRIVS_PK" PRIMARY KEY ("PART_COLUMN_GRANT_ID");

ALTER TABLE "APP"."PARTITION_PARAMS" ADD CONSTRAINT "PARTITION_PARAMS_PK" PRIMARY KEY ("PART_ID", "PARAM_KEY");

ALTER TABLE "APP"."PARTITIONS" ADD CONSTRAINT "PARTITIONS_PK" PRIMARY KEY ("PART_ID");

ALTER TABLE "APP"."TABLE_PARAMS" ADD CONSTRAINT "TABLE_PARAMS_PK" PRIMARY KEY ("TBL_ID", "PARAM_KEY");

ALTER TABLE "APP"."SKEWED_STRING_LIST" ADD CONSTRAINT "SKEWED_STRING_LIST_PK" PRIMARY KEY ("STRING_LIST_ID");

ALTER TABLE "APP"."SKEWED_STRING_LIST_VALUES" ADD CONSTRAINT "SKEWED_STRING_LIST_VALUES_PK" PRIMARY KEY ("STRING_LIST_ID", "INTEGER_IDX");

ALTER TABLE "APP"."SKEWED_COL_NAMES" ADD CONSTRAINT "SKEWED_COL_NAMES_PK" PRIMARY KEY ("SD_ID", "INTEGER_IDX");

ALTER TABLE "APP"."SKEWED_COL_VALUE_LOC_MAP" ADD CONSTRAINT "SKEWED_COL_VALUE_LOC_MAP_PK" PRIMARY KEY ("SD_ID", "STRING_LIST_ID_KID");

ALTER TABLE "APP"."SKEWED_VALUES" ADD CONSTRAINT "SKEWED_VALUES_PK" PRIMARY KEY ("SD_ID_OID", "INTEGER_IDX");

ALTER TABLE "APP"."TAB_COL_STATS" ADD CONSTRAINT "TAB_COL_STATS_PK" PRIMARY KEY ("CS_ID");

ALTER TABLE "APP"."PART_COL_STATS" ADD CONSTRAINT "PART_COL_STATS_PK" PRIMARY KEY ("CS_ID");

ALTER TABLE "APP"."FUNCS" ADD CONSTRAINT "FUNCS_PK" PRIMARY KEY ("FUNC_ID");

ALTER TABLE "APP"."FUNC_RU" ADD CONSTRAINT "FUNC_RU_PK" PRIMARY KEY ("FUNC_ID", "INTEGER_IDX");

ALTER TABLE "APP"."NOTIFICATION_LOG" ADD CONSTRAINT "NOTIFICATION_LOG_PK" PRIMARY KEY ("NL_ID");

ALTER TABLE "APP"."NOTIFICATION_SEQUENCE" ADD CONSTRAINT "NOTIFICATION_SEQUENCE_PK" PRIMARY KEY ("NNI_ID");

-- foreign
ALTER TABLE "APP"."IDXS" ADD CONSTRAINT "IDXS_FK1" FOREIGN KEY ("ORIG_TBL_ID") REFERENCES "APP"."TBLS" ("TBL_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."IDXS" ADD CONSTRAINT "IDXS_FK2" FOREIGN KEY ("SD_ID") REFERENCES "APP"."SDS" ("SD_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."IDXS" ADD CONSTRAINT "IDXS_FK3" FOREIGN KEY ("INDEX_TBL_ID") REFERENCES "APP"."TBLS" ("TBL_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."TBL_COL_PRIVS" ADD CONSTRAINT "TBL_COL_PRIVS_FK1" FOREIGN KEY ("TBL_ID") REFERENCES "APP"."TBLS" ("TBL_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."DB_PRIVS" ADD CONSTRAINT "DB_PRIVS_FK1" FOREIGN KEY ("DB_ID") REFERENCES "APP"."DBS" ("DB_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."INDEX_PARAMS" ADD CONSTRAINT "INDEX_PARAMS_FK1" FOREIGN KEY ("INDEX_ID") REFERENCES "APP"."IDXS" ("INDEX_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."PARTITION_KEYS" ADD CONSTRAINT "PARTITION_KEYS_FK1" FOREIGN KEY ("TBL_ID") REFERENCES "APP"."TBLS" ("TBL_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."PART_PRIVS" ADD CONSTRAINT "PART_PRIVS_FK1" FOREIGN KEY ("PART_ID") REFERENCES "APP"."PARTITIONS" ("PART_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."SDS" ADD CONSTRAINT "SDS_FK1" FOREIGN KEY ("SERDE_ID") REFERENCES "APP"."SERDES" ("SERDE_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."SDS" ADD CONSTRAINT "SDS_FK2" FOREIGN KEY ("CD_ID") REFERENCES "APP"."CDS" ("CD_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."COLUMNS" ADD CONSTRAINT "COLUMNS_FK1" FOREIGN KEY ("SD_ID") REFERENCES "APP"."SDS" ("SD_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."TYPE_FIELDS" ADD CONSTRAINT "TYPE_FIELDS_FK1" FOREIGN KEY ("TYPE_NAME") REFERENCES "APP"."TYPES" ("TYPES_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."TBL_PRIVS" ADD CONSTRAINT "TBL_PRIVS_FK1" FOREIGN KEY ("TBL_ID") REFERENCES "APP"."TBLS" ("TBL_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."SERDE_PARAMS" ADD CONSTRAINT "SERDE_PARAMS_FK1" FOREIGN KEY ("SERDE_ID") REFERENCES "APP"."SERDES" ("SERDE_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."TBLS" ADD CONSTRAINT "TBLS_FK2" FOREIGN KEY ("SD_ID") REFERENCES "APP"."SDS" ("SD_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."TBLS" ADD CONSTRAINT "TBLS_FK1" FOREIGN KEY ("DB_ID") REFERENCES "APP"."DBS" ("DB_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."SD_PARAMS" ADD CONSTRAINT "SD_PARAMS_FK1" FOREIGN KEY ("SD_ID") REFERENCES "APP"."SDS" ("SD_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."DATABASE_PARAMS" ADD CONSTRAINT "DATABASE_PARAMS_FK1" FOREIGN KEY ("DB_ID") REFERENCES "APP"."DBS" ("DB_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."ROLE_MAP" ADD CONSTRAINT "ROLE_MAP_FK1" FOREIGN KEY ("ROLE_ID") REFERENCES "APP"."ROLES" ("ROLE_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."BUCKETING_COLS" ADD CONSTRAINT "BUCKETING_COLS_FK1" FOREIGN KEY ("SD_ID") REFERENCES "APP"."SDS" ("SD_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."SORT_COLS" ADD CONSTRAINT "SORT_COLS_FK1" FOREIGN KEY ("SD_ID") REFERENCES "APP"."SDS" ("SD_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."PARTITION_KEY_VALS" ADD CONSTRAINT "PARTITION_KEY_VALS_FK1" FOREIGN KEY ("PART_ID") REFERENCES "APP"."PARTITIONS" ("PART_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."COLUMNS_V2" ADD CONSTRAINT "COLUMNS_V2_FK1" FOREIGN KEY ("CD_ID") REFERENCES "APP"."CDS" ("CD_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."PART_COL_PRIVS" ADD CONSTRAINT "PART_COL_PRIVS_FK1" FOREIGN KEY ("PART_ID") REFERENCES "APP"."PARTITIONS" ("PART_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."PARTITION_PARAMS" ADD CONSTRAINT "PARTITION_PARAMS_FK1" FOREIGN KEY ("PART_ID") REFERENCES "APP"."PARTITIONS" ("PART_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."PARTITIONS" ADD CONSTRAINT "PARTITIONS_FK1" FOREIGN KEY ("TBL_ID") REFERENCES "APP"."TBLS" ("TBL_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."PARTITIONS" ADD CONSTRAINT "PARTITIONS_FK2" FOREIGN KEY ("SD_ID") REFERENCES "APP"."SDS" ("SD_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."TABLE_PARAMS" ADD CONSTRAINT "TABLE_PARAMS_FK1" FOREIGN KEY ("TBL_ID") REFERENCES "APP"."TBLS" ("TBL_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."SKEWED_STRING_LIST_VALUES" ADD CONSTRAINT "SKEWED_STRING_LIST_VALUES_FK1" FOREIGN KEY ("STRING_LIST_ID") REFERENCES "APP"."SKEWED_STRING_LIST" ("STRING_LIST_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."SKEWED_COL_NAMES" ADD CONSTRAINT "SKEWED_COL_NAMES_FK1" FOREIGN KEY ("SD_ID") REFERENCES "APP"."SDS" ("SD_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."SKEWED_COL_VALUE_LOC_MAP" ADD CONSTRAINT "SKEWED_COL_VALUE_LOC_MAP_FK1" FOREIGN KEY ("SD_ID") REFERENCES "APP"."SDS" ("SD_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."SKEWED_COL_VALUE_LOC_MAP" ADD CONSTRAINT "SKEWED_COL_VALUE_LOC_MAP_FK2" FOREIGN KEY ("STRING_LIST_ID_KID") REFERENCES "APP"."SKEWED_STRING_LIST" ("STRING_LIST_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."SKEWED_VALUES" ADD CONSTRAINT "SKEWED_VALUES_FK1" FOREIGN KEY ("SD_ID_OID") REFERENCES "APP"."SDS" ("SD_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."SKEWED_VALUES" ADD CONSTRAINT "SKEWED_VALUES_FK2" FOREIGN KEY ("STRING_LIST_ID_EID") REFERENCES "APP"."SKEWED_STRING_LIST" ("STRING_LIST_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."TAB_COL_STATS" ADD CONSTRAINT "TAB_COL_STATS_FK" FOREIGN KEY ("TBL_ID") REFERENCES TBLS("TBL_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."PART_COL_STATS" ADD CONSTRAINT "PART_COL_STATS_FK" FOREIGN KEY ("PART_ID") REFERENCES PARTITIONS("PART_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."VERSION" ADD CONSTRAINT "VERSION_PK" PRIMARY KEY ("VER_ID");

ALTER TABLE "APP"."FUNCS" ADD CONSTRAINT "FUNCS_FK1" FOREIGN KEY ("DB_ID") REFERENCES "APP"."DBS" ("DB_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."FUNC_RU" ADD CONSTRAINT "FUNC_RU_FK1" FOREIGN KEY ("FUNC_ID") REFERENCES "APP"."FUNCS" ("FUNC_ID") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------------------------
-- DDL Statements for checks
-- ----------------------------------------------

ALTER TABLE "APP"."IDXS" ADD CONSTRAINT "SQL110318025504980" CHECK (DEFERRED_REBUILD IN ('Y','N'));

ALTER TABLE "APP"."SDS" ADD CONSTRAINT "SQL110318025505550" CHECK (IS_COMPRESSED IN ('Y','N'));

-- ----------------------------
-- Transaction and Lock Tables
-- ----------------------------
RUN 'hive-txn-schema-2.2.0.derby.sql';

-- -----------------------------------------------------------------
-- Record schema version. Should be the last step in the init script
-- -----------------------------------------------------------------
INSERT INTO "APP"."VERSION" (VER_ID, SCHEMA_VERSION, VERSION_COMMENT) VALUES (1, '2.2.0', 'Hive release version 2.2.0');

