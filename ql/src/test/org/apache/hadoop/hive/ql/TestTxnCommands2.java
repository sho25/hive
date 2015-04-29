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
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|FileUtils
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
name|fs
operator|.
name|Path
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
name|io
operator|.
name|AcidUtils
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
name|orc
operator|.
name|FileDump
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
name|Assert
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
name|Ignore
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
name|TestName
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
name|FileNotFoundException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FilenameFilter
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
name|Comparator
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

begin_comment
comment|/**  * TODO: this should be merged with TestTxnCommands once that is checked in  * specifically the tests; the supporting code here is just a clone of TestTxnCommands  */
end_comment

begin_class
specifier|public
class|class
name|TestTxnCommands2
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TEST_DATA_DIR
init|=
operator|new
name|File
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
name|TestTxnCommands2
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
operator|.
name|getPath
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"\\\\"
argument_list|,
literal|"/"
argument_list|)
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
comment|//bucket count for test tables; set it to 1 for easier debugging
specifier|private
specifier|static
name|int
name|BUCKET_COUNT
init|=
literal|2
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|Driver
name|d
decl_stmt|;
specifier|private
specifier|static
enum|enum
name|Table
block|{
name|ACIDTBL
argument_list|(
literal|"acidTbl"
argument_list|)
block|,
name|ACIDTBLPART
argument_list|(
literal|"acidTblPart"
argument_list|)
block|,
name|NONACIDORCTBL
argument_list|(
literal|"nonAcidOrcTbl"
argument_list|)
block|,
name|NONACIDPART
argument_list|(
literal|"nonAcidPart"
argument_list|)
block|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|name
return|;
block|}
name|Table
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|tearDown
argument_list|()
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|POSTEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
operator|.
name|varname
argument_list|,
name|TEST_WAREHOUSE_DIR
argument_list|)
expr_stmt|;
name|TxnDbUtil
operator|.
name|setConfValues
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEENFORCEBUCKETING
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TxnDbUtil
operator|.
name|prepDb
argument_list|()
expr_stmt|;
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
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|SessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|dropTables
argument_list|()
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|"(a int, b int) clustered by (a) into "
operator|+
name|BUCKET_COUNT
operator|+
literal|" buckets stored as orc TBLPROPERTIES ('transactional'='true')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table "
operator|+
name|Table
operator|.
name|ACIDTBLPART
operator|+
literal|"(a int, b int) partitioned by (p string) clustered by (a) into "
operator|+
name|BUCKET_COUNT
operator|+
literal|" buckets stored as orc TBLPROPERTIES ('transactional'='true')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
operator|+
literal|"(a int, b int) clustered by (a) into "
operator|+
name|BUCKET_COUNT
operator|+
literal|" buckets stored as orc TBLPROPERTIES ('transactional'='false')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table "
operator|+
name|Table
operator|.
name|NONACIDPART
operator|+
literal|"(a int, b int) partitioned by (p string) stored as orc TBLPROPERTIES ('transactional'='false')"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|dropTables
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|Table
name|t
range|:
name|Table
operator|.
name|values
argument_list|()
control|)
block|{
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists "
operator|+
name|t
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
if|if
condition|(
name|d
operator|!=
literal|null
condition|)
block|{
comment|//   runStatementOnDriver("set autocommit true");
name|dropTables
argument_list|()
expr_stmt|;
name|d
operator|.
name|destroy
argument_list|()
expr_stmt|;
name|d
operator|.
name|close
argument_list|()
expr_stmt|;
name|d
operator|=
literal|null
expr_stmt|;
name|TxnDbUtil
operator|.
name|cleanDb
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|FileUtils
operator|.
name|deleteDirectory
argument_list|(
operator|new
name|File
argument_list|(
name|TEST_DATA_DIR
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Ignore
argument_list|(
literal|"not needed but useful for testing"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testNonAcidInsert
parameter_list|()
throws|throws
name|Exception
block|{
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
operator|+
literal|"(a,b) values(1,2)"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rs
init|=
name|runStatementOnDriver
argument_list|(
literal|"select a,b from "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
argument_list|)
decl_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
operator|+
literal|"(a,b) values(2,3)"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rs1
init|=
name|runStatementOnDriver
argument_list|(
literal|"select a,b from "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
argument_list|)
decl_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateMixedCase
parameter_list|()
throws|throws
name|Exception
block|{
name|int
index|[]
index|[]
name|tableData
init|=
block|{
block|{
literal|1
block|,
literal|2
block|}
block|,
block|{
literal|3
block|,
literal|3
block|}
block|,
block|{
literal|5
block|,
literal|3
block|}
block|}
decl_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|"(a,b) "
operator|+
name|makeValuesClause
argument_list|(
name|tableData
argument_list|)
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"update "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" set B = 7 where A=1"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rs
init|=
name|runStatementOnDriver
argument_list|(
literal|"select a,b from "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" order by a,b"
argument_list|)
decl_stmt|;
name|int
index|[]
index|[]
name|updatedData
init|=
block|{
block|{
literal|1
block|,
literal|7
block|}
block|,
block|{
literal|3
block|,
literal|3
block|}
block|,
block|{
literal|5
block|,
literal|3
block|}
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Update failed"
argument_list|,
name|stringifyValues
argument_list|(
name|updatedData
argument_list|)
argument_list|,
name|rs
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"update "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" set B = B + 1 where A=1"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rs2
init|=
name|runStatementOnDriver
argument_list|(
literal|"select a,b from "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" order by a,b"
argument_list|)
decl_stmt|;
name|int
index|[]
index|[]
name|updatedData2
init|=
block|{
block|{
literal|1
block|,
literal|8
block|}
block|,
block|{
literal|3
block|,
literal|3
block|}
block|,
block|{
literal|5
block|,
literal|3
block|}
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Update failed"
argument_list|,
name|stringifyValues
argument_list|(
name|updatedData2
argument_list|)
argument_list|,
name|rs2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteIn
parameter_list|()
throws|throws
name|Exception
block|{
name|int
index|[]
index|[]
name|tableData
init|=
block|{
block|{
literal|1
block|,
literal|2
block|}
block|,
block|{
literal|3
block|,
literal|2
block|}
block|,
block|{
literal|5
block|,
literal|2
block|}
block|,
block|{
literal|1
block|,
literal|3
block|}
block|,
block|{
literal|3
block|,
literal|3
block|}
block|,
block|{
literal|5
block|,
literal|3
block|}
block|}
decl_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|"(a,b) "
operator|+
name|makeValuesClause
argument_list|(
name|tableData
argument_list|)
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
operator|+
literal|"(a,b) values(1,7),(3,7)"
argument_list|)
expr_stmt|;
comment|//todo: once multistatement txns are supported, add a test to run next 2 statements in a single txn
name|runStatementOnDriver
argument_list|(
literal|"delete from "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" where a in(select a from "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|"(a,b) select a,b from "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rs
init|=
name|runStatementOnDriver
argument_list|(
literal|"select a,b from "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" order by a,b"
argument_list|)
decl_stmt|;
name|int
index|[]
index|[]
name|updatedData
init|=
block|{
block|{
literal|1
block|,
literal|7
block|}
block|,
block|{
literal|3
block|,
literal|7
block|}
block|,
block|{
literal|5
block|,
literal|2
block|}
block|,
block|{
literal|5
block|,
literal|3
block|}
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Bulk update failed"
argument_list|,
name|stringifyValues
argument_list|(
name|updatedData
argument_list|)
argument_list|,
name|rs
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"update "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" set b=19 where b in(select b from "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
operator|+
literal|" where a = 3)"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rs2
init|=
name|runStatementOnDriver
argument_list|(
literal|"select a,b from "
operator|+
name|Table
operator|.
name|ACIDTBL
operator|+
literal|" order by a,b"
argument_list|)
decl_stmt|;
name|int
index|[]
index|[]
name|updatedData2
init|=
block|{
block|{
literal|1
block|,
literal|19
block|}
block|,
block|{
literal|3
block|,
literal|19
block|}
block|,
block|{
literal|5
block|,
literal|2
block|}
block|,
block|{
literal|5
block|,
literal|3
block|}
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Bulk update2 failed"
argument_list|,
name|stringifyValues
argument_list|(
name|updatedData2
argument_list|)
argument_list|,
name|rs2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInsertOverwriteWithSelfJoin
parameter_list|()
throws|throws
name|Exception
block|{
name|int
index|[]
index|[]
name|part1Data
init|=
block|{
block|{
literal|1
block|,
literal|7
block|}
block|}
decl_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
operator|+
literal|"(a,b) "
operator|+
name|makeValuesClause
argument_list|(
name|part1Data
argument_list|)
argument_list|)
expr_stmt|;
comment|//this works because logically we need S lock on NONACIDORCTBL to read and X lock to write, but
comment|//LockRequestBuilder dedups locks on the same entity to only keep the highest level lock requested
name|runStatementOnDriver
argument_list|(
literal|"insert overwrite table "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
operator|+
literal|" select 2, 9 from "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
operator|+
literal|" T inner join "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
operator|+
literal|" S on T.a=S.a"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rs
init|=
name|runStatementOnDriver
argument_list|(
literal|"select a,b from "
operator|+
name|Table
operator|.
name|NONACIDORCTBL
operator|+
literal|" order by a,b"
argument_list|)
decl_stmt|;
name|int
index|[]
index|[]
name|joinData
init|=
block|{
block|{
literal|2
block|,
literal|9
block|}
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Self join non-part insert overwrite failed"
argument_list|,
name|stringifyValues
argument_list|(
name|joinData
argument_list|)
argument_list|,
name|rs
argument_list|)
expr_stmt|;
name|int
index|[]
index|[]
name|part2Data
init|=
block|{
block|{
literal|1
block|,
literal|8
block|}
block|}
decl_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|NONACIDPART
operator|+
literal|" partition(p=1) (a,b) "
operator|+
name|makeValuesClause
argument_list|(
name|part1Data
argument_list|)
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into "
operator|+
name|Table
operator|.
name|NONACIDPART
operator|+
literal|" partition(p=2) (a,b) "
operator|+
name|makeValuesClause
argument_list|(
name|part2Data
argument_list|)
argument_list|)
expr_stmt|;
comment|//here we need X lock on p=1 partition to write and S lock on 'table' to read which should
comment|//not block each other since they are part of the same txn
name|runStatementOnDriver
argument_list|(
literal|"insert overwrite table "
operator|+
name|Table
operator|.
name|NONACIDPART
operator|+
literal|" partition(p=1) select a,b from "
operator|+
name|Table
operator|.
name|NONACIDPART
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rs2
init|=
name|runStatementOnDriver
argument_list|(
literal|"select a,b from "
operator|+
name|Table
operator|.
name|NONACIDPART
operator|+
literal|" order by a,b"
argument_list|)
decl_stmt|;
name|int
index|[]
index|[]
name|updatedData
init|=
block|{
block|{
literal|1
block|,
literal|7
block|}
block|,
block|{
literal|1
block|,
literal|8
block|}
block|,
block|{
literal|1
block|,
literal|8
block|}
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Insert overwrite partition failed"
argument_list|,
name|stringifyValues
argument_list|(
name|updatedData
argument_list|)
argument_list|,
name|rs2
argument_list|)
expr_stmt|;
comment|//insert overwrite not supported for ACID tables
block|}
comment|/**    * takes raw data and turns it into a string as if from Driver.getResults()    * sorts rows in dictionary order    */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|stringifyValues
parameter_list|(
name|int
index|[]
index|[]
name|rowsIn
parameter_list|)
block|{
assert|assert
name|rowsIn
operator|.
name|length
operator|>
literal|0
assert|;
name|int
index|[]
index|[]
name|rows
init|=
name|rowsIn
operator|.
name|clone
argument_list|()
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|rows
argument_list|,
operator|new
name|RowComp
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
index|[]
name|row
range|:
name|rows
control|)
block|{
assert|assert
name|row
operator|.
name|length
operator|>
literal|0
assert|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|value
range|:
name|row
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|value
argument_list|)
operator|.
name|append
argument_list|(
literal|"\t"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|setLength
argument_list|(
name|sb
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
name|rs
operator|.
name|add
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|rs
return|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|RowComp
implements|implements
name|Comparator
argument_list|<
name|int
index|[]
argument_list|>
block|{
specifier|public
name|int
name|compare
parameter_list|(
name|int
index|[]
name|row1
parameter_list|,
name|int
index|[]
name|row2
parameter_list|)
block|{
assert|assert
name|row1
operator|!=
literal|null
operator|&&
name|row2
operator|!=
literal|null
operator|&&
name|row1
operator|.
name|length
operator|==
name|row2
operator|.
name|length
assert|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|row1
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|int
name|comp
init|=
name|Integer
operator|.
name|compare
argument_list|(
name|row1
index|[
name|i
index|]
argument_list|,
name|row2
index|[
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|comp
operator|!=
literal|0
condition|)
block|{
return|return
name|comp
return|;
block|}
block|}
return|return
literal|0
return|;
block|}
block|}
specifier|private
name|String
name|makeValuesClause
parameter_list|(
name|int
index|[]
index|[]
name|rows
parameter_list|)
block|{
assert|assert
name|rows
operator|.
name|length
operator|>
literal|0
assert|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"values"
argument_list|)
decl_stmt|;
for|for
control|(
name|int
index|[]
name|row
range|:
name|rows
control|)
block|{
assert|assert
name|row
operator|.
name|length
operator|>
literal|0
assert|;
if|if
condition|(
name|row
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|value
range|:
name|row
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|value
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|setLength
argument_list|(
name|sb
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|//remove trailing comma
if|if
condition|(
name|row
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|setLength
argument_list|(
name|sb
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|//remove trailing comma
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|runStatementOnDriver
parameter_list|(
name|String
name|stmt
parameter_list|)
throws|throws
name|Exception
block|{
name|CommandProcessorResponse
name|cpr
init|=
name|d
operator|.
name|run
argument_list|(
name|stmt
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
name|RuntimeException
argument_list|(
name|stmt
operator|+
literal|" failed: "
operator|+
name|cpr
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|rs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|d
operator|.
name|getResults
argument_list|(
name|rs
argument_list|)
expr_stmt|;
return|return
name|rs
return|;
block|}
block|}
end_class

end_unit

