begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|txn
operator|.
name|compactor
package|;
end_package

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|cli
operator|.
name|CliSessionState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|conf
operator|.
name|HiveConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|HiveMetaStoreClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|IMetaStoreClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|MetaStoreThread
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|ColumnStatisticsObj
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|CompactionRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|CompactionType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|LongColumnStatsData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|ShowCompactRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|ShowCompactResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|ShowCompactResponseElement
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|StringColumnStatsData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|txn
operator|.
name|CompactionInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|txn
operator|.
name|CompactionTxnHandler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|txn
operator|.
name|TxnDbUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|CommandNeedRetryException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|Driver
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|io
operator|.
name|HiveInputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|processors
operator|.
name|CommandProcessorResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|session
operator|.
name|SessionState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
operator|.
name|DelimitedInputWriter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
operator|.
name|HiveEndPoint
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
operator|.
name|StreamingConnection
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
operator|.
name|TransactionBatch
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TemporaryFolder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
specifier|public
class|class
name|TestCompactor
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestCompactor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_DATA_DIR
init|=
name|HCatUtil
operator|.
name|makePathASafeFileName
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
operator|+
name|File
operator|.
name|separator
operator|+
name|TestCompactor
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
operator|+
literal|"-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|BASIC_FILE_NAME
init|=
name|TEST_DATA_DIR
operator|+
literal|"/basic.input.data"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_WAREHOUSE_DIR
init|=
name|TEST_DATA_DIR
operator|+
literal|"/warehouse"
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TemporaryFolder
name|stagingFolder
init|=
operator|new
name|TemporaryFolder
argument_list|()
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
name|IMetaStoreClient
name|msClient
decl_stmt|;
specifier|private
name|Driver
name|driver
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|TEST_WAREHOUSE_DIR
argument_list|)
decl_stmt|;
if|if
condition|(
name|f
operator|.
name|exists
argument_list|()
condition|)
block|{
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
operator|(
operator|new
name|File
argument_list|(
name|TEST_WAREHOUSE_DIR
argument_list|)
operator|.
name|mkdirs
argument_list|()
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Could not create "
operator|+
name|TEST_WAREHOUSE_DIR
argument_list|)
throw|;
block|}
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|POSTEXECHOOKS
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
argument_list|,
name|TEST_WAREHOUSE_DIR
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEINPUTFORMAT
argument_list|,
name|HiveInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|//"org.apache.hadoop.hive.ql.io.HiveInputFormat"
name|TxnDbUtil
operator|.
name|setConfValues
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|TxnDbUtil
operator|.
name|cleanDb
argument_list|()
expr_stmt|;
name|TxnDbUtil
operator|.
name|prepDb
argument_list|()
expr_stmt|;
name|conf
operator|=
name|hiveConf
expr_stmt|;
name|msClient
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|LOOP_SIZE
init|=
literal|3
decl_stmt|;
name|String
index|[]
name|input
init|=
operator|new
name|String
index|[
name|LOOP_SIZE
operator|*
name|LOOP_SIZE
index|]
decl_stmt|;
name|int
name|k
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|LOOP_SIZE
condition|;
name|i
operator|++
control|)
block|{
name|String
name|si
init|=
name|i
operator|+
literal|""
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|1
init|;
name|j
operator|<=
name|LOOP_SIZE
condition|;
name|j
operator|++
control|)
block|{
name|String
name|sj
init|=
literal|"S"
operator|+
name|j
operator|+
literal|"S"
decl_stmt|;
name|input
index|[
name|k
index|]
operator|=
name|si
operator|+
literal|"\t"
operator|+
name|sj
expr_stmt|;
name|k
operator|++
expr_stmt|;
block|}
block|}
name|createTestDataFile
argument_list|(
name|BASIC_FILE_NAME
argument_list|,
name|input
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|conf
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|msClient
operator|!=
literal|null
condition|)
block|{
name|msClient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|driver
operator|!=
literal|null
condition|)
block|{
name|driver
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * After each major compaction, stats need to be updated on each column of the    * table/partition which previously had stats.    * 1. create a bucketed ORC backed table (Orc is currently required by ACID)    * 2. populate 2 partitions with data    * 3. compute stats    * 4. insert some data into the table using StreamingAPI    * 5. Trigger major compaction (which should update stats)    * 6. check that stats have been updated    * @throws Exception    * todo:     * 2. add non-partitioned test    * 4. add a test with sorted table?    */
annotation|@
name|Test
specifier|public
name|void
name|testStatsAfterCompactionPartTbl
parameter_list|()
throws|throws
name|Exception
block|{
comment|//as of (8/27/2014) Hive 0.14, ACID/Orc requires HiveInputFormat
name|String
name|tblName
init|=
literal|"compaction_test"
decl_stmt|;
name|String
name|tblNameStg
init|=
name|tblName
operator|+
literal|"_stg"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colNames
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|)
decl_stmt|;
name|executeStatementOnDriver
argument_list|(
literal|"drop table if exists "
operator|+
name|tblName
argument_list|,
name|driver
argument_list|)
expr_stmt|;
name|executeStatementOnDriver
argument_list|(
literal|"drop table if exists "
operator|+
name|tblNameStg
argument_list|,
name|driver
argument_list|)
expr_stmt|;
name|executeStatementOnDriver
argument_list|(
literal|"CREATE TABLE "
operator|+
name|tblName
operator|+
literal|"(a INT, b STRING) "
operator|+
literal|" PARTITIONED BY(bkt INT)"
operator|+
literal|" CLUSTERED BY(a) INTO 4 BUCKETS"
operator|+
comment|//currently ACID requires table to be bucketed
literal|" STORED AS ORC"
argument_list|,
name|driver
argument_list|)
expr_stmt|;
name|executeStatementOnDriver
argument_list|(
literal|"CREATE EXTERNAL TABLE "
operator|+
name|tblNameStg
operator|+
literal|"(a INT, b STRING)"
operator|+
literal|" ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\t' LINES TERMINATED BY '\\n'"
operator|+
literal|" STORED AS TEXTFILE"
operator|+
literal|" LOCATION '"
operator|+
name|stagingFolder
operator|.
name|newFolder
argument_list|()
operator|+
literal|"'"
argument_list|,
name|driver
argument_list|)
expr_stmt|;
name|executeStatementOnDriver
argument_list|(
literal|"load data local inpath '"
operator|+
name|BASIC_FILE_NAME
operator|+
literal|"' overwrite into table "
operator|+
name|tblNameStg
argument_list|,
name|driver
argument_list|)
expr_stmt|;
name|execSelectAndDumpData
argument_list|(
literal|"select * from "
operator|+
name|tblNameStg
argument_list|,
name|driver
argument_list|,
literal|"Dumping data for "
operator|+
name|tblNameStg
operator|+
literal|" after load:"
argument_list|)
expr_stmt|;
name|executeStatementOnDriver
argument_list|(
literal|"FROM "
operator|+
name|tblNameStg
operator|+
literal|" INSERT INTO TABLE "
operator|+
name|tblName
operator|+
literal|" PARTITION(bkt=0) "
operator|+
literal|"SELECT a, b where a< 2"
argument_list|,
name|driver
argument_list|)
expr_stmt|;
name|executeStatementOnDriver
argument_list|(
literal|"FROM "
operator|+
name|tblNameStg
operator|+
literal|" INSERT INTO TABLE "
operator|+
name|tblName
operator|+
literal|" PARTITION(bkt=1) "
operator|+
literal|"SELECT a, b where a>= 2"
argument_list|,
name|driver
argument_list|)
expr_stmt|;
name|execSelectAndDumpData
argument_list|(
literal|"select * from "
operator|+
name|tblName
argument_list|,
name|driver
argument_list|,
literal|"Dumping data for "
operator|+
name|tblName
operator|+
literal|" after load:"
argument_list|)
expr_stmt|;
name|CompactionTxnHandler
name|txnHandler
init|=
operator|new
name|CompactionTxnHandler
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|CompactionInfo
name|ci
init|=
operator|new
name|CompactionInfo
argument_list|(
literal|"default"
argument_list|,
name|tblName
argument_list|,
literal|"bkt=0"
argument_list|,
name|CompactionType
operator|.
name|MAJOR
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"List of stats columns before analyze Part1: "
operator|+
name|txnHandler
operator|.
name|findColumnsWithStats
argument_list|(
name|ci
argument_list|)
argument_list|)
expr_stmt|;
name|Worker
operator|.
name|StatsUpdater
name|su
init|=
name|Worker
operator|.
name|StatsUpdater
operator|.
name|init
argument_list|(
name|ci
argument_list|,
name|colNames
argument_list|,
name|conf
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|)
decl_stmt|;
name|su
operator|.
name|gatherStats
argument_list|()
expr_stmt|;
comment|//compute stats before compaction
name|LOG
operator|.
name|debug
argument_list|(
literal|"List of stats columns after analyze Part1: "
operator|+
name|txnHandler
operator|.
name|findColumnsWithStats
argument_list|(
name|ci
argument_list|)
argument_list|)
expr_stmt|;
name|CompactionInfo
name|ciPart2
init|=
operator|new
name|CompactionInfo
argument_list|(
literal|"default"
argument_list|,
name|tblName
argument_list|,
literal|"bkt=1"
argument_list|,
name|CompactionType
operator|.
name|MAJOR
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"List of stats columns before analyze Part2: "
operator|+
name|txnHandler
operator|.
name|findColumnsWithStats
argument_list|(
name|ci
argument_list|)
argument_list|)
expr_stmt|;
name|su
operator|=
name|Worker
operator|.
name|StatsUpdater
operator|.
name|init
argument_list|(
name|ciPart2
argument_list|,
name|colNames
argument_list|,
name|conf
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|)
expr_stmt|;
name|su
operator|.
name|gatherStats
argument_list|()
expr_stmt|;
comment|//compute stats before compaction
name|LOG
operator|.
name|debug
argument_list|(
literal|"List of stats columns after analyze Part2: "
operator|+
name|txnHandler
operator|.
name|findColumnsWithStats
argument_list|(
name|ci
argument_list|)
argument_list|)
expr_stmt|;
comment|//now make sure we get the stats we expect for partition we are going to add data to later
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ColumnStatisticsObj
argument_list|>
argument_list|>
name|stats
init|=
name|msClient
operator|.
name|getPartitionColumnStatistics
argument_list|(
name|ci
operator|.
name|dbname
argument_list|,
name|ci
operator|.
name|tableName
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|ci
operator|.
name|partName
argument_list|)
argument_list|,
name|colNames
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ColumnStatisticsObj
argument_list|>
name|colStats
init|=
name|stats
operator|.
name|get
argument_list|(
name|ci
operator|.
name|partName
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"No stats found for partition "
operator|+
name|ci
operator|.
name|partName
argument_list|,
name|colStats
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Expected column 'a' at index 0"
argument_list|,
literal|"a"
argument_list|,
name|colStats
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getColName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Expected column 'b' at index 1"
argument_list|,
literal|"b"
argument_list|,
name|colStats
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getColName
argument_list|()
argument_list|)
expr_stmt|;
name|LongColumnStatsData
name|colAStats
init|=
name|colStats
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getStatsData
argument_list|()
operator|.
name|getLongStats
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"lowValue a"
argument_list|,
literal|1
argument_list|,
name|colAStats
operator|.
name|getLowValue
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"highValue a"
argument_list|,
literal|1
argument_list|,
name|colAStats
operator|.
name|getHighValue
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"numNulls a"
argument_list|,
literal|0
argument_list|,
name|colAStats
operator|.
name|getNumNulls
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"numNdv a"
argument_list|,
literal|1
argument_list|,
name|colAStats
operator|.
name|getNumDVs
argument_list|()
argument_list|)
expr_stmt|;
name|StringColumnStatsData
name|colBStats
init|=
name|colStats
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getStatsData
argument_list|()
operator|.
name|getStringStats
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"maxColLen b"
argument_list|,
literal|3
argument_list|,
name|colBStats
operator|.
name|getMaxColLen
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"avgColLen b"
argument_list|,
literal|3.0
argument_list|,
name|colBStats
operator|.
name|getAvgColLen
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"numNulls b"
argument_list|,
literal|0
argument_list|,
name|colBStats
operator|.
name|getNumNulls
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"nunDVs"
argument_list|,
literal|2
argument_list|,
name|colBStats
operator|.
name|getNumDVs
argument_list|()
argument_list|)
expr_stmt|;
comment|//now save stats for partition we won't modify
name|stats
operator|=
name|msClient
operator|.
name|getPartitionColumnStatistics
argument_list|(
name|ciPart2
operator|.
name|dbname
argument_list|,
name|ciPart2
operator|.
name|tableName
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|ciPart2
operator|.
name|partName
argument_list|)
argument_list|,
name|colNames
argument_list|)
expr_stmt|;
name|colStats
operator|=
name|stats
operator|.
name|get
argument_list|(
name|ciPart2
operator|.
name|partName
argument_list|)
expr_stmt|;
name|LongColumnStatsData
name|colAStatsPart2
init|=
name|colStats
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getStatsData
argument_list|()
operator|.
name|getLongStats
argument_list|()
decl_stmt|;
name|StringColumnStatsData
name|colBStatsPart2
init|=
name|colStats
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getStatsData
argument_list|()
operator|.
name|getStringStats
argument_list|()
decl_stmt|;
name|HiveEndPoint
name|endPt
init|=
operator|new
name|HiveEndPoint
argument_list|(
literal|null
argument_list|,
name|ci
operator|.
name|dbname
argument_list|,
name|ci
operator|.
name|tableName
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"0"
argument_list|)
argument_list|)
decl_stmt|;
name|DelimitedInputWriter
name|writer
init|=
operator|new
name|DelimitedInputWriter
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"a"
block|,
literal|"b"
block|}
argument_list|,
literal|","
argument_list|,
name|endPt
argument_list|)
decl_stmt|;
comment|/*next call will eventually end up in HiveEndPoint.createPartitionIfNotExists() which     makes an operation on Driver     * and starts it's own CliSessionState and then closes it, which removes it from ThreadLoacal;     * thus the session     * created in this class is gone after this; I fixed it in HiveEndPoint*/
name|StreamingConnection
name|connection
init|=
name|endPt
operator|.
name|newConnection
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|TransactionBatch
name|txnBatch
init|=
name|connection
operator|.
name|fetchTransactionBatch
argument_list|(
literal|2
argument_list|,
name|writer
argument_list|)
decl_stmt|;
name|txnBatch
operator|.
name|beginNextTransaction
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TransactionBatch
operator|.
name|TxnState
operator|.
name|OPEN
argument_list|,
name|txnBatch
operator|.
name|getCurrentTransactionState
argument_list|()
argument_list|)
expr_stmt|;
name|txnBatch
operator|.
name|write
argument_list|(
literal|"50,Kiev"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|txnBatch
operator|.
name|write
argument_list|(
literal|"51,St. Petersburg"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|txnBatch
operator|.
name|write
argument_list|(
literal|"44,Boston"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|txnBatch
operator|.
name|commit
argument_list|()
expr_stmt|;
name|txnBatch
operator|.
name|beginNextTransaction
argument_list|()
expr_stmt|;
name|txnBatch
operator|.
name|write
argument_list|(
literal|"52,Tel Aviv"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|txnBatch
operator|.
name|write
argument_list|(
literal|"53,Atlantis"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|txnBatch
operator|.
name|write
argument_list|(
literal|"53,Boston"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|txnBatch
operator|.
name|commit
argument_list|()
expr_stmt|;
name|txnBatch
operator|.
name|close
argument_list|()
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
name|execSelectAndDumpData
argument_list|(
literal|"select * from "
operator|+
name|ci
operator|.
name|getFullTableName
argument_list|()
argument_list|,
name|driver
argument_list|,
name|ci
operator|.
name|getFullTableName
argument_list|()
argument_list|)
expr_stmt|;
comment|//so now we have written some new data to bkt=0 and it shows up
name|CompactionRequest
name|rqst
init|=
operator|new
name|CompactionRequest
argument_list|(
name|ci
operator|.
name|dbname
argument_list|,
name|ci
operator|.
name|tableName
argument_list|,
name|CompactionType
operator|.
name|MAJOR
argument_list|)
decl_stmt|;
name|rqst
operator|.
name|setPartitionname
argument_list|(
name|ci
operator|.
name|partName
argument_list|)
expr_stmt|;
name|txnHandler
operator|.
name|compact
argument_list|(
name|rqst
argument_list|)
expr_stmt|;
name|Worker
name|t
init|=
operator|new
name|Worker
argument_list|()
decl_stmt|;
name|t
operator|.
name|setThreadId
argument_list|(
operator|(
name|int
operator|)
name|t
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|setHiveConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|MetaStoreThread
operator|.
name|BooleanPointer
name|stop
init|=
operator|new
name|MetaStoreThread
operator|.
name|BooleanPointer
argument_list|()
decl_stmt|;
name|MetaStoreThread
operator|.
name|BooleanPointer
name|looped
init|=
operator|new
name|MetaStoreThread
operator|.
name|BooleanPointer
argument_list|()
decl_stmt|;
name|stop
operator|.
name|boolVal
operator|=
literal|true
expr_stmt|;
name|t
operator|.
name|init
argument_list|(
name|stop
argument_list|,
name|looped
argument_list|)
expr_stmt|;
name|t
operator|.
name|run
argument_list|()
expr_stmt|;
name|ShowCompactResponse
name|rsp
init|=
name|txnHandler
operator|.
name|showCompact
argument_list|(
operator|new
name|ShowCompactRequest
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ShowCompactResponseElement
argument_list|>
name|compacts
init|=
name|rsp
operator|.
name|getCompacts
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|compacts
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"ready for cleaning"
argument_list|,
name|compacts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|stats
operator|=
name|msClient
operator|.
name|getPartitionColumnStatistics
argument_list|(
name|ci
operator|.
name|dbname
argument_list|,
name|ci
operator|.
name|tableName
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|ci
operator|.
name|partName
argument_list|)
argument_list|,
name|colNames
argument_list|)
expr_stmt|;
name|colStats
operator|=
name|stats
operator|.
name|get
argument_list|(
name|ci
operator|.
name|partName
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"No stats found for partition "
operator|+
name|ci
operator|.
name|partName
argument_list|,
name|colStats
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Expected column 'a' at index 0"
argument_list|,
literal|"a"
argument_list|,
name|colStats
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getColName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Expected column 'b' at index 1"
argument_list|,
literal|"b"
argument_list|,
name|colStats
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getColName
argument_list|()
argument_list|)
expr_stmt|;
name|colAStats
operator|=
name|colStats
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getStatsData
argument_list|()
operator|.
name|getLongStats
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"lowValue a"
argument_list|,
literal|1
argument_list|,
name|colAStats
operator|.
name|getLowValue
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"highValue a"
argument_list|,
literal|53
argument_list|,
name|colAStats
operator|.
name|getHighValue
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"numNulls a"
argument_list|,
literal|0
argument_list|,
name|colAStats
operator|.
name|getNumNulls
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"numNdv a"
argument_list|,
literal|6
argument_list|,
name|colAStats
operator|.
name|getNumDVs
argument_list|()
argument_list|)
expr_stmt|;
name|colBStats
operator|=
name|colStats
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getStatsData
argument_list|()
operator|.
name|getStringStats
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"maxColLen b"
argument_list|,
literal|14
argument_list|,
name|colBStats
operator|.
name|getMaxColLen
argument_list|()
argument_list|)
expr_stmt|;
comment|//cast it to long to get rid of periodic decimal
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"avgColLen b"
argument_list|,
operator|(
name|long
operator|)
literal|6.1111111111
argument_list|,
operator|(
name|long
operator|)
name|colBStats
operator|.
name|getAvgColLen
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"numNulls b"
argument_list|,
literal|0
argument_list|,
name|colBStats
operator|.
name|getNumNulls
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"nunDVs"
argument_list|,
literal|10
argument_list|,
name|colBStats
operator|.
name|getNumDVs
argument_list|()
argument_list|)
expr_stmt|;
comment|//now check that stats for partition we didn't modify did not change
name|stats
operator|=
name|msClient
operator|.
name|getPartitionColumnStatistics
argument_list|(
name|ciPart2
operator|.
name|dbname
argument_list|,
name|ciPart2
operator|.
name|tableName
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|ciPart2
operator|.
name|partName
argument_list|)
argument_list|,
name|colNames
argument_list|)
expr_stmt|;
name|colStats
operator|=
name|stats
operator|.
name|get
argument_list|(
name|ciPart2
operator|.
name|partName
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Expected stats for "
operator|+
name|ciPart2
operator|.
name|partName
operator|+
literal|" to stay the same"
argument_list|,
name|colAStatsPart2
argument_list|,
name|colStats
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getStatsData
argument_list|()
operator|.
name|getLongStats
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Expected stats for "
operator|+
name|ciPart2
operator|.
name|partName
operator|+
literal|" to stay the same"
argument_list|,
name|colBStatsPart2
argument_list|,
name|colStats
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getStatsData
argument_list|()
operator|.
name|getStringStats
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * convenience method to execute a select stmt and dump results to log file    */
specifier|private
specifier|static
name|void
name|execSelectAndDumpData
parameter_list|(
name|String
name|selectStmt
parameter_list|,
name|Driver
name|driver
parameter_list|,
name|String
name|msg
parameter_list|)
throws|throws
name|Exception
block|{
name|executeStatementOnDriver
argument_list|(
name|selectStmt
argument_list|,
name|driver
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|valuesReadFromHiveDriver
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|driver
operator|.
name|getResults
argument_list|(
name|valuesReadFromHiveDriver
argument_list|)
expr_stmt|;
name|int
name|rowIdx
init|=
literal|0
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|msg
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|row
range|:
name|valuesReadFromHiveDriver
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|" rowIdx="
operator|+
name|rowIdx
operator|++
operator|+
literal|":"
operator|+
name|row
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Execute Hive CLI statement    * @param cmd arbitrary statement to execute    */
specifier|static
name|void
name|executeStatementOnDriver
parameter_list|(
name|String
name|cmd
parameter_list|,
name|Driver
name|driver
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Executing: "
operator|+
name|cmd
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|cpr
init|=
name|driver
operator|.
name|run
argument_list|(
name|cmd
argument_list|)
decl_stmt|;
if|if
condition|(
name|cpr
operator|.
name|getResponseCode
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to execute \""
operator|+
name|cmd
operator|+
literal|"\". Driver returned: "
operator|+
name|cpr
argument_list|)
throw|;
block|}
block|}
specifier|static
name|void
name|createTestDataFile
parameter_list|(
name|String
name|filename
parameter_list|,
name|String
index|[]
name|lines
parameter_list|)
throws|throws
name|IOException
block|{
name|FileWriter
name|writer
init|=
literal|null
decl_stmt|;
try|try
block|{
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|filename
argument_list|)
decl_stmt|;
name|file
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|writer
operator|=
operator|new
name|FileWriter
argument_list|(
name|file
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|line
range|:
name|lines
control|)
block|{
name|writer
operator|.
name|write
argument_list|(
name|line
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|writer
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

