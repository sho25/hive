SELECT 'Upgrading MetaStore schema from 3.1.0 to 4.0.0';

USE SYS;

-- HIVE-20793
DROP TABLE IF EXISTS `WM_RESOURCEPLANS`;
CREATE EXTERNAL TABLE IF NOT EXISTS `WM_RESOURCEPLANS` (
  `NAME` string,
  `NS` string,
  `STATUS` string,
  `QUERY_PARALLELISM` int,
  `DEFAULT_POOL_PATH` string
)
STORED BY 'org.apache.hive.storage.jdbc.JdbcStorageHandler'
TBLPROPERTIES (
"hive.sql.database.type" = "METASTORE",
"hive.sql.query" =
"SELECT
  \"WM_RESOURCEPLAN\".\"NAME\",
  case when \"WM_RESOURCEPLAN\".\"NS\" is null then 'default' else \"WM_RESOURCEPLAN\".\"NS\" end AS NS,
  \"STATUS\",
  \"WM_RESOURCEPLAN\".\"QUERY_PARALLELISM\",
  \"WM_POOL\".\"PATH\"
FROM
  \"WM_RESOURCEPLAN\" LEFT OUTER JOIN \"WM_POOL\" ON \"WM_RESOURCEPLAN\".\"DEFAULT_POOL_ID\" = \"WM_POOL\".\"POOL_ID\""
);

DROP TABLE IF EXISTS `WM_TRIGGERS`;
CREATE EXTERNAL TABLE IF NOT EXISTS `WM_TRIGGERS` (
  `RP_NAME` string,
  `NS` string,
  `NAME` string,
  `TRIGGER_EXPRESSION` string,
  `ACTION_EXPRESSION` string
)
STORED BY 'org.apache.hive.storage.jdbc.JdbcStorageHandler'
TBLPROPERTIES (
"hive.sql.database.type" = "METASTORE",
"hive.sql.query" =
"SELECT
  r.\"NAME\" AS RP_NAME,
  case when r.\"NS\" is null then 'default' else r.\"NS\" end,
  t.\"NAME\" AS NAME,
  \"TRIGGER_EXPRESSION\",
  \"ACTION_EXPRESSION\"
FROM
  \"WM_TRIGGER\" t
JOIN
  \"WM_RESOURCEPLAN\" r
ON
  t.\"RP_ID\" = r.\"RP_ID\""
);

DROP TABLE IF EXISTS `WM_POOLS`;
CREATE EXTERNAL TABLE IF NOT EXISTS `WM_POOLS` (
  `RP_NAME` string,
  `NS` string,
  `PATH` string,
  `ALLOC_FRACTION` double,
  `QUERY_PARALLELISM` int,
  `SCHEDULING_POLICY` string
)
STORED BY 'org.apache.hive.storage.jdbc.JdbcStorageHandler'
TBLPROPERTIES (
"hive.sql.database.type" = "METASTORE",
"hive.sql.query" =
"SELECT
  \"WM_RESOURCEPLAN\".\"NAME\",
  case when \"WM_RESOURCEPLAN\".\"NS\" is null then 'default' else \"WM_RESOURCEPLAN\".\"NS\" end AS NS,
  \"WM_POOL\".\"PATH\",
  \"WM_POOL\".\"ALLOC_FRACTION\",
  \"WM_POOL\".\"QUERY_PARALLELISM\",
  \"WM_POOL\".\"SCHEDULING_POLICY\"
FROM
  \"WM_POOL\"
JOIN
  \"WM_RESOURCEPLAN\"
ON
  \"WM_POOL\".\"RP_ID\" = \"WM_RESOURCEPLAN\".\"RP_ID\""
);

DROP TABLE IF EXISTS `WM_POOLS_TO_TRIGGERS`;
CREATE EXTERNAL TABLE IF NOT EXISTS `WM_POOLS_TO_TRIGGERS` (
  `RP_NAME` string,
  `NS` string,
  `POOL_PATH` string,
  `TRIGGER_NAME` string
)
STORED BY 'org.apache.hive.storage.jdbc.JdbcStorageHandler'
TBLPROPERTIES (
"hive.sql.database.type" = "METASTORE",
"hive.sql.query" =
"SELECT
  \"WM_RESOURCEPLAN\".\"NAME\" AS RP_NAME,
  case when \"WM_RESOURCEPLAN\".\"NS\" is null then 'default' else \"WM_RESOURCEPLAN\".\"NS\" end AS NS,
  \"WM_POOL\".\"PATH\" AS POOL_PATH,
  \"WM_TRIGGER\".\"NAME\" AS TRIGGER_NAME
FROM \"WM_POOL_TO_TRIGGER\"
  JOIN \"WM_POOL\" ON \"WM_POOL_TO_TRIGGER\".\"POOL_ID\" = \"WM_POOL\".\"POOL_ID\"
  JOIN \"WM_TRIGGER\" ON \"WM_POOL_TO_TRIGGER\".\"TRIGGER_ID\" = \"WM_TRIGGER\".\"TRIGGER_ID\"
  JOIN \"WM_RESOURCEPLAN\" ON \"WM_POOL\".\"RP_ID\" = \"WM_RESOURCEPLAN\".\"RP_ID\"
UNION
SELECT
  \"WM_RESOURCEPLAN\".\"NAME\" AS RP_NAME,
  case when \"WM_RESOURCEPLAN\".\"NS\" is null then 'default' else \"WM_RESOURCEPLAN\".\"NS\" end AS NS,
  '<unmanaged queries>' AS POOL_PATH,
  \"WM_TRIGGER\".\"NAME\" AS TRIGGER_NAME
FROM \"WM_TRIGGER\"
  JOIN \"WM_RESOURCEPLAN\" ON \"WM_TRIGGER\".\"RP_ID\" = \"WM_RESOURCEPLAN\".\"RP_ID\"
WHERE CAST(\"WM_TRIGGER\".\"IS_IN_UNMANAGED\" AS CHAR) IN ('1', 't')
"
);

DROP TABLE IF EXISTS `WM_MAPPINGS`;
CREATE EXTERNAL TABLE IF NOT EXISTS `WM_MAPPINGS` (
  `RP_NAME` string,
  `NS` string,
  `ENTITY_TYPE` string,
  `ENTITY_NAME` string,
  `POOL_PATH` string,
  `ORDERING` int
)
STORED BY 'org.apache.hive.storage.jdbc.JdbcStorageHandler'
TBLPROPERTIES (
"hive.sql.database.type" = "METASTORE",
"hive.sql.query" =
"SELECT
  \"WM_RESOURCEPLAN\".\"NAME\",
  case when \"WM_RESOURCEPLAN\".\"NS\" is null then 'default' else \"WM_RESOURCEPLAN\".\"NS\" end AS NS,
  \"ENTITY_TYPE\",
  \"ENTITY_NAME\",
  case when \"WM_POOL\".\"PATH\" is null then '<unmanaged>' else \"WM_POOL\".\"PATH\" end,
  \"ORDERING\"
FROM \"WM_MAPPING\"
JOIN \"WM_RESOURCEPLAN\" ON \"WM_MAPPING\".\"RP_ID\" = \"WM_RESOURCEPLAN\".\"RP_ID\"
LEFT OUTER JOIN \"WM_POOL\" ON \"WM_POOL\".\"POOL_ID\" = \"WM_MAPPING\".\"POOL_ID\"
"
);

CREATE EXTERNAL TABLE IF NOT EXISTS `SCHEDULED_QUERIES` (
  `SCHEDULED_QUERY_ID` bigint,
  `SCHEDULE_NAME` string,
  `ENABLED` boolean,
  `CLUSTER_NAMESPACE` string,
  `SCHEDULE` string,
  `USER` string,
  `QUERY` string,
  `NEXT_EXECUTION` bigint,
  CONSTRAINT `SYS_PK_SCHEDULED_QUERIES` PRIMARY KEY (`SCHEDULED_QUERY_ID`) DISABLE
)
STORED BY 'org.apache.hive.storage.jdbc.JdbcStorageHandler'
TBLPROPERTIES (
"hive.sql.database.type" = "METASTORE",
"hive.sql.query" =
"SELECT
  \"SCHEDULED_QUERY_ID\",
  \"SCHEDULE_NAME\",
  \"ENABLED\",
  \"CLUSTER_NAMESPACE\",
  \"SCHEDULE\",
  \"USER\",
  \"QUERY\",
  \"NEXT_EXECUTION\"
FROM
  \"SCHEDULED_QUERIES\""
);

CREATE EXTERNAL TABLE IF NOT EXISTS `SCHEDULED_EXECUTIONS` (
  `SCHEDULED_EXECUTION_ID` bigint,
  `SCHEDULED_QUERY_ID` bigint,
  `EXECUTOR_QUERY_ID` string,
  `STATE` string,
  `START_TIME` int,
  `END_TIME` int,
  `ERROR_MESSAGE` string,
  `LAST_UPDATE_TIME` int,
  CONSTRAINT `SYS_PK_SCHEDULED_EXECUTIONS` PRIMARY KEY (`SCHEDULED_EXECUTION_ID`) DISABLE
)
STORED BY 'org.apache.hive.storage.jdbc.JdbcStorageHandler'
TBLPROPERTIES (
"hive.sql.database.type" = "METASTORE",
"hive.sql.query" =
"SELECT
  \"SCHEDULED_EXECUTION_ID\",
  \"SCHEDULED_QUERY_ID\",
  \"EXECUTOR_QUERY_ID\",
  \"STATE\",
  \"START_TIME\",
  \"END_TIME\",
  \"ERROR_MESSAGE\",
  \"LAST_UPDATE_TIME\"
FROM
  \"SCHEDULED_EXECUTIONS\""
);

CREATE EXTERNAL TABLE IF NOT EXISTS `COMPACTION_QUEUE` (
  `CQ_ID` bigint,
  `CQ_DATABASE` string,
  `CQ_TABLE` string,
  `CQ_PARTITION` string,
  `CQ_STATE` string,
  `CQ_TYPE` string,
  `CQ_TBLPROPERTIES` string,
  `CQ_WORKER_ID` string,
  `CQ_START` bigint,
  `CQ_RUN_AS` string,
  `CQ_HIGHEST_WRITE_ID` bigint,
  `CQ_HADOOP_JOB_ID` string
)
STORED BY 'org.apache.hive.storage.jdbc.JdbcStorageHandler'
TBLPROPERTIES (
"hive.sql.database.type" = "METASTORE",
"hive.sql.query" =
"SELECT
  \"COMPACTION_QUEUE\".\"CQ_ID\",
  \"COMPACTION_QUEUE\".\"CQ_DATABASE\",
  \"COMPACTION_QUEUE\".\"CQ_TABLE\",
  \"COMPACTION_QUEUE\".\"CQ_PARTITION\",
  \"COMPACTION_QUEUE\".\"CQ_STATE\",
  \"COMPACTION_QUEUE\".\"CQ_TYPE\",
  \"COMPACTION_QUEUE\".\"CQ_TBLPROPERTIES\",
  \"COMPACTION_QUEUE\".\"CQ_WORKER_ID\",
  \"COMPACTION_QUEUE\".\"CQ_START\",
  \"COMPACTION_QUEUE\".\"CQ_RUN_AS\",
  \"COMPACTION_QUEUE\".\"CQ_HIGHEST_WRITE_ID\",
  \"COMPACTION_QUEUE\".\"CQ_HADOOP_JOB_ID\"
FROM \"COMPACTION_QUEUE\"
"
);

CREATE EXTERNAL TABLE IF NOT EXISTS `COMPLETED_COMPACTIONS` (
  `CC_ID` bigint,
  `CC_DATABASE` string,
  `CC_TABLE` string,
  `CC_PARTITION` string,
  `CC_STATE` string,
  `CC_TYPE` string,
  `CC_TBLPROPERTIES` string,
  `CC_WORKER_ID` string,
  `CC_START` bigint,
  `CC_END` bigint,
  `CC_RUN_AS` string,
  `CC_HIGHEST_WRITE_ID` bigint,
  `CC_HADOOP_JOB_ID` string
)
STORED BY 'org.apache.hive.storage.jdbc.JdbcStorageHandler'
TBLPROPERTIES (
"hive.sql.database.type" = "METASTORE",
"hive.sql.query" =
"SELECT
  \"COMPLETED_COMPACTIONS\".\"CC_ID\",
  \"COMPLETED_COMPACTIONS\".\"CC_DATABASE\",
  \"COMPLETED_COMPACTIONS\".\"CC_TABLE\",
  \"COMPLETED_COMPACTIONS\".\"CC_PARTITION\",
  \"COMPLETED_COMPACTIONS\".\"CC_STATE\",
  \"COMPLETED_COMPACTIONS\".\"CC_TYPE\",
  \"COMPLETED_COMPACTIONS\".\"CC_TBLPROPERTIES\",
  \"COMPLETED_COMPACTIONS\".\"CC_WORKER_ID\",
  \"COMPLETED_COMPACTIONS\".\"CC_START\",
  \"COMPLETED_COMPACTIONS\".\"CC_END\",
  \"COMPLETED_COMPACTIONS\".\"CC_RUN_AS\",
  \"COMPLETED_COMPACTIONS\".\"CC_HIGHEST_WRITE_ID\",
  \"COMPLETED_COMPACTIONS\".\"CC_HADOOP_JOB_ID\"
FROM \"COMPLETED_COMPACTIONS\"
"
);

CREATE OR REPLACE VIEW `COMPACTIONS`
(
  `C_ID`,
  `C_CATALOG`,
  `C_DATABASE`,
  `C_TABLE`,
  `C_PARTITION`,
  `C_TYPE`,
  `C_STATE`,
  `C_HOSTNAME`,
  `C_WORKER_ID`,
  `C_START`,
  `C_DURATION`,
  `C_HADOOP_JOB_ID`,
  `C_RUN_AS`,
  `C_HIGHEST_WRITE_ID`
) AS
SELECT
  CC_ID,
  'default',
  CC_DATABASE,
  CC_TABLE,
  CC_PARTITION,
  CASE WHEN CC_TYPE = 'i' THEN 'minor' WHEN CC_TYPE = 'a' THEN 'major' ELSE 'UNKNOWN' END,
  CASE WHEN CC_STATE = 'f' THEN 'failed' WHEN CC_STATE = 's' THEN 'succeeded' WHEN CC_STATE = 'a' THEN 'attempted' ELSE 'UNKNOWN' END,
  CASE WHEN CC_WORKER_ID IS NULL THEN cast (null as string) ELSE split(CC_WORKER_ID,"-")[0] END,
  CASE WHEN CC_WORKER_ID IS NULL THEN cast (null as string) ELSE split(CC_WORKER_ID,"-")[1] END,
  CC_START,
  CASE WHEN CC_END IS NULL THEN cast (null as string) ELSE CC_END-CC_START END,
  CC_HADOOP_JOB_ID,
  CC_RUN_AS,
  CC_HIGHEST_WRITE_ID
FROM COMPLETED_COMPACTIONS
UNION ALL
SELECT
  CQ_ID,
  'default',
  CQ_DATABASE,
  CQ_TABLE,
  CQ_PARTITION,
  CASE WHEN CQ_TYPE = 'i' THEN 'minor' WHEN CQ_TYPE = 'a' THEN 'major' ELSE 'UNKNOWN' END,
  CASE WHEN CQ_STATE = 'i' THEN 'initiated' WHEN CQ_STATE = 'w' THEN 'working' WHEN CQ_STATE = 'r' THEN 'ready for cleaning' ELSE 'UNKNOWN' END,
  CASE WHEN CQ_WORKER_ID IS NULL THEN NULL ELSE split(CQ_WORKER_ID,"-")[0] END,
  CASE WHEN CQ_WORKER_ID IS NULL THEN NULL ELSE split(CQ_WORKER_ID,"-")[1] END,
  CQ_START,
  cast (null as string),
  CQ_HADOOP_JOB_ID,
  CQ_RUN_AS,
  CQ_HIGHEST_WRITE_ID
FROM COMPACTION_QUEUE;

-- HIVE-22553
CREATE EXTERNAL TABLE IF NOT EXISTS `TXNS` (
    `TXN_ID` bigint,
    `TXN_STATE` string,
    `TXN_STARTED` bigint,
    `TXN_LAST_HEARTBEAT` bigint,
    `TXN_USER` string,
    `TXN_HOST` string,
    `TXN_AGENT_INFO` string,
    `TXN_META_INFO` string,
    `TXN_HEARTBEAT_COUNT` int,
    `TXN_TYPE` int
)
STORED BY 'org.apache.hive.storage.jdbc.JdbcStorageHandler'
TBLPROPERTIES (
"hive.sql.database.type" = "METASTORE",
"hive.sql.query" =
"SELECT
    \"TXN_ID\",
    \"TXN_STATE\",
    \"TXN_STARTED\",
    \"TXN_LAST_HEARTBEAT\",
    \"TXN_USER\",
    \"TXN_HOST\",
    \"TXN_AGENT_INFO\",
    \"TXN_META_INFO\",
    \"TXN_HEARTBEAT_COUNT\",
    \"TXN_TYPE\"
FROM \"TXNS\""
);


CREATE EXTERNAL TABLE IF NOT EXISTS `TXN_COMPONENTS` (
    `TC_TXNID` bigint,
    `TC_DATABASE` string,
    `TC_TABLE` string,
    `TC_PARTITION` string,
    `TC_OPERATION_TYPE` string,
    `TC_WRITEID` bigint
)
STORED BY 'org.apache.hive.storage.jdbc.JdbcStorageHandler'
TBLPROPERTIES (
"hive.sql.database.type" = "METASTORE",
"hive.sql.query" =
"SELECT
    \"TC_TXNID\",
    \"TC_DATABASE\",
    \"TC_TABLE\",
    \"TC_PARTITION\",
    \"TC_OPERATION_TYPE\",
    \"TC_WRITEID\"
FROM \"TXN_COMPONENTS\""
);


CREATE OR REPLACE VIEW `TRANSACTIONS` (
    `TXN_ID`,
    `STATE`,
    `STARTED`,
    `LAST_HEARTBEAT`,
    `USER`,
    `HOST`,
    `AGENT_INFO`,
    `META_INFO`,
    `HEARTBEAT_COUNT`,
    `TYPE`,
    `TC_DATABASE`,
    `TC_TABLE`,
    `TC_PARTITION`,
    `TC_OPERATION_TYPE`,
    `TC_WRITEID`
) AS
SELECT DISTINCT
    T.`TXN_ID`,
    CASE WHEN T.`TXN_STATE` = 'o' THEN 'open' WHEN T.`TXN_STATE` = 'a' THEN 'aborted' WHEN T.`TXN_STATE` = 'c' THEN 'commited' ELSE 'UNKNOWN' END  AS TXN_STATE,
    FROM_UNIXTIME(T.`TXN_STARTED`) AS TXN_STARTED,
    FROM_UNIXTIME(T.`TXN_LAST_HEARTBEAT`) AS TXN_LAST_HEARTBEAT,
    T.`TXN_USER`,
    T.`TXN_HOST`,
    T.`TXN_AGENT_INFO`,
    T.`TXN_META_INFO`,
    T.`TXN_HEARTBEAT_COUNT`,
    CASE WHEN T.`TXN_TYPE` = 0 THEN 'DEFAULT' WHEN T.`TXN_TYPE` = 1 THEN 'REPL_CREATED' WHEN T.`TXN_TYPE` = 2 THEN 'READ_ONLY' WHEN T.`TXN_TYPE` = 3 THEN 'COMPACTION' END AS TXN_TYPE,
    TC.`TC_DATABASE`,
    TC.`TC_TABLE`,
    TC.`TC_PARTITION`,
    CASE WHEN TC.`TC_OPERATION_TYPE` = 's' THEN 'SELECT' WHEN TC.`TC_OPERATION_TYPE` = 'i' THEN 'INSERT' WHEN TC.`TC_OPERATION_TYPE` = 'u' THEN 'UPDATE' WHEN TC.`TC_OPERATION_TYPE` = 'c' THEN 'COMPACT' END AS OPERATION_TYPE,
    TC.`TC_WRITEID`
FROM `SYS`.`TXNS` AS T
LEFT JOIN `SYS`.`TXN_COMPONENTS` AS TC ON T.`TXN_ID` = TC.`TC_TXNID`;

CREATE EXTERNAL TABLE `HIVE_LOCKS` (
    `HL_LOCK_EXT_ID` bigint,
    `HL_LOCK_INT_ID` bigint,
    `HL_TXNID` bigint,
    `HL_DB` string,
    `HL_TABLE` string,
    `HL_PARTITION` string,
    `HL_LOCK_STATE` string,
    `HL_LOCK_TYPE` string,
    `HL_LAST_HEARTBEAT` bigint,
    `HL_ACQUIRED_AT` bigint,
    `HL_USER` string,
    `HL_HOST` string,
    `HL_HEARTBEAT_COUNT` int,
    `HL_AGENT_INFO` string,
    `HL_BLOCKEDBY_EXT_ID` bigint,
    `HL_BLOCKEDBY_INT_ID` bigint
)
STORED BY 'org.apache.hive.storage.jdbc.JdbcStorageHandler'
TBLPROPERTIES (
"hive.sql.database.type" = "METASTORE",
"hive.sql.query" =
"SELECT
    \"HL_LOCK_EXT_ID\",
    \"HL_LOCK_INT_ID\",
    \"HL_TXNID\",
    \"HL_DB\",
    \"HL_TABLE\",
    \"HL_PARTITION\",
    \"HL_LOCK_STATE\",
    \"HL_LOCK_TYPE\",
    \"HL_LAST_HEARTBEAT\",
    \"HL_ACQUIRED_AT\",
    \"HL_USER\",
    \"HL_HOST\",
    \"HL_HEARTBEAT_COUNT\",
    \"HL_AGENT_INFO\",
    \"HL_BLOCKEDBY_EXT_ID\",
    \"HL_BLOCKEDBY_INT_ID\"
FROM \"HIVE_LOCKS\""
);

CREATE OR REPLACE VIEW `LOCKS` (
    `LOCK_EXT_ID`,
    `LOCK_INT_ID`,
    `TXNID`,
    `DB`,
    `TABLE`,
    `PARTITION`,
    `LOCK_STATE`,
    `LOCK_TYPE`,
    `LAST_HEARTBEAT`,
    `ACQUIRED_AT`,
    `USER`,
    `HOST`,
    `HEARTBEAT_COUNT`,
    `AGENT_INFO`,
    `BLOCKEDBY_EXT_ID`,
    `BLOCKEDBY_INT_ID`
) AS
SELECT DISTINCT
    HL.`HL_LOCK_EXT_ID`,
    HL.`HL_LOCK_INT_ID`,
    HL.`HL_TXNID`,
    HL.`HL_DB`,
    HL.`HL_TABLE`,
    HL.`HL_PARTITION`,
    CASE WHEN HL.`HL_LOCK_STATE` = 'a' THEN 'acquired' WHEN HL.`HL_LOCK_STATE` = 'w' THEN 'waiting' END AS LOCK_STATE,
    CASE WHEN HL.`HL_LOCK_TYPE` = 'e' THEN 'exclusive' WHEN HL.`HL_LOCK_TYPE` = 'r' THEN 'shared' WHEN HL.`HL_LOCK_TYPE` = 'w' THEN 'semi-shared' END AS LOCK_TYPE,
    FROM_UNIXTIME(HL.`HL_LAST_HEARTBEAT`),
    FROM_UNIXTIME(HL.`HL_ACQUIRED_AT`),
    HL.`HL_USER`,
    HL.`HL_HOST`,
    HL.`HL_HEARTBEAT_COUNT`,
    HL.`HL_AGENT_INFO`,
    HL.`HL_BLOCKEDBY_EXT_ID`,
    HL.`HL_BLOCKEDBY_INT_ID`
FROM SYS.`HIVE_LOCKS` AS HL;

DROP TABLE IF EXISTS `VERSION`;

CREATE OR REPLACE VIEW `VERSION` AS SELECT 1 AS `VER_ID`, '4.0.0' AS `SCHEMA_VERSION`,
  'Hive release version 4.0.0' AS `VERSION_COMMENT`;

USE INFORMATION_SCHEMA;

create or replace view SCHEDULED_QUERIES  as
select
  `SCHEDULED_QUERY_ID` ,
  `SCHEDULE_NAME` ,
  `ENABLED`,
  `CLUSTER_NAMESPACE`,
  `SCHEDULE`,
  `USER`,
  `QUERY`,
  FROM_UNIXTIME(NEXT_EXECUTION) as NEXT_EXECUTION
FROM
  SYS.SCHEDULED_QUERIES
;

create or replace view SCHEDULED_EXECUTIONS as
SELECT
  SCHEDULED_EXECUTION_ID,
  SCHEDULE_NAME,
  EXECUTOR_QUERY_ID,
  `STATE`,
  FROM_UNIXTIME(START_TIME) as START_TIME,
  FROM_UNIXTIME(END_TIME) as END_TIME,
  END_TIME-START_TIME as ELAPSED,
  ERROR_MESSAGE,
  FROM_UNIXTIME(LAST_UPDATE_TIME) AS LAST_UPDATE_TIME
FROM
  SYS.SCHEDULED_EXECUTIONS SE
JOIN
  SYS.SCHEDULED_QUERIES SQ
WHERE
  SE.SCHEDULED_QUERY_ID=SQ.SCHEDULED_QUERY_ID;

CREATE OR REPLACE VIEW `COMPACTIONS`
(
  `C_ID`,
  `C_CATALOG`,
  `C_DATABASE`,
  `C_TABLE`,
  `C_PARTITION`,
  `C_TYPE`,
  `C_STATE`,
  `C_HOSTNAME`,
  `C_WORKER_ID`,
  `C_START`,
  `C_DURATION`,
  `C_HADOOP_JOB_ID`,
  `C_RUN_AS`,
  `C_HIGHEST_WRITE_ID`
) AS
SELECT DISTINCT
  C_ID,
  C_CATALOG,
  C_DATABASE,
  C_TABLE,
  C_PARTITION,
  C_TYPE,
  C_STATE,
  C_HOSTNAME,
  C_WORKER_ID,
  C_START,
  C_DURATION,
  C_HADOOP_JOB_ID,
  C_RUN_AS,
  C_HIGHEST_WRITE_ID
FROM
  `sys`.`COMPACTIONS` C JOIN `sys`.`TBLS` T ON (C.`C_TABLE` = T.`TBL_NAME`)
                        JOIN `sys`.`DBS` D ON (C.`C_DATABASE` = D.`NAME`)
                        LEFT JOIN `sys`.`TBL_PRIVS` P ON (T.`TBL_ID` = P.`TBL_ID`)
WHERE
  (NOT restrict_information_schema() OR P.`TBL_ID` IS NOT NULL
  AND (P.`PRINCIPAL_NAME`=current_user() AND P.`PRINCIPAL_TYPE`='USER'
    OR ((array_contains(current_groups(), P.`PRINCIPAL_NAME`) OR P.`PRINCIPAL_NAME` = 'public') AND P.`PRINCIPAL_TYPE`='GROUP'))
  AND P.`TBL_PRIV`='SELECT' AND P.`AUTHORIZER`=current_authorizer());

SELECT 'Finished upgrading MetaStore schema from 3.1.0 to 4.0.0';
