begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|operation
package|;
end_package

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
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|hive
operator|.
name|jdbc
operator|.
name|miniHS2
operator|.
name|MiniHS2
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
name|service
operator|.
name|cli
operator|.
name|CLIServiceClient
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
name|service
operator|.
name|cli
operator|.
name|FetchOrientation
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
name|service
operator|.
name|cli
operator|.
name|FetchType
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
name|service
operator|.
name|cli
operator|.
name|OperationHandle
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
name|service
operator|.
name|cli
operator|.
name|RowSet
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
name|service
operator|.
name|cli
operator|.
name|SessionHandle
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
name|AfterClass
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
name|BeforeClass
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

begin_comment
comment|/**  * Tests to verify operation logging layout for different modes.  */
end_comment

begin_class
specifier|public
class|class
name|TestOperationLoggingLayout
block|{
specifier|protected
specifier|static
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|protected
specifier|static
name|String
name|tableName
decl_stmt|;
specifier|private
name|File
name|dataFile
decl_stmt|;
specifier|protected
name|CLIServiceClient
name|client
decl_stmt|;
specifier|protected
specifier|static
name|MiniHS2
name|miniHS2
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
decl_stmt|;
specifier|protected
name|SessionHandle
name|sessionHandle
decl_stmt|;
specifier|protected
specifier|final
name|String
name|sql
init|=
literal|"select * from "
operator|+
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|String
name|sqlCntStar
init|=
literal|"select count(*) from "
operator|+
name|tableName
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|tableName
operator|=
literal|"TestOperationLoggingLayout_table"
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_LOGGING_OPERATION_LEVEL
operator|.
name|varname
argument_list|,
literal|"execution"
argument_list|)
expr_stmt|;
name|miniHS2
operator|=
operator|new
name|MiniHS2
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|confOverlay
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|confOverlay
operator|.
name|put
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
name|miniHS2
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
block|}
comment|/**    * Open a session, and create a table for cases usage    *    * @throws Exception    */
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|dataFile
operator|=
operator|new
name|File
argument_list|(
name|hiveConf
operator|.
name|get
argument_list|(
literal|"test.data.files"
argument_list|)
argument_list|,
literal|"kv1.txt"
argument_list|)
expr_stmt|;
name|client
operator|=
name|miniHS2
operator|.
name|getServiceClient
argument_list|()
expr_stmt|;
name|sessionHandle
operator|=
name|setupSession
argument_list|()
expr_stmt|;
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
comment|// Cleanup
name|String
name|queryString
init|=
literal|"DROP TABLE "
operator|+
name|tableName
decl_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|client
operator|.
name|closeSession
argument_list|(
name|sessionHandle
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|miniHS2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSwitchLogLayout
parameter_list|()
throws|throws
name|Exception
block|{
comment|// verify whether the sql operation log is generated and fetch correctly.
name|OperationHandle
name|operationHandle
init|=
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|sqlCntStar
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|RowSet
name|rowSetLog
init|=
name|client
operator|.
name|fetchResults
argument_list|(
name|operationHandle
argument_list|,
name|FetchOrientation
operator|.
name|FETCH_FIRST
argument_list|,
literal|1000
argument_list|,
name|FetchType
operator|.
name|LOG
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|Object
index|[]
argument_list|>
name|iter
init|=
name|rowSetLog
operator|.
name|iterator
argument_list|()
decl_stmt|;
comment|// non-verbose pattern is %-5p : %m%n. Look for " : "
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|row
init|=
name|iter
operator|.
name|next
argument_list|()
index|[
literal|0
index|]
operator|.
name|toString
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|row
operator|.
name|matches
argument_list|(
literal|"^(FATAL|ERROR|WARN|INFO|DEBUG|TRACE).*$"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|queryString
init|=
literal|"set hive.server2.logging.operation.level=verbose"
decl_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|operationHandle
operator|=
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|sqlCntStar
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// just check for first few lines, some log lines are multi-line strings which can break format
comment|// checks below
name|rowSetLog
operator|=
name|client
operator|.
name|fetchResults
argument_list|(
name|operationHandle
argument_list|,
name|FetchOrientation
operator|.
name|FETCH_FIRST
argument_list|,
literal|10
argument_list|,
name|FetchType
operator|.
name|LOG
argument_list|)
expr_stmt|;
name|iter
operator|=
name|rowSetLog
operator|.
name|iterator
argument_list|()
expr_stmt|;
comment|// verbose pattern is "%d{yy/MM/dd HH:mm:ss} %p %c{2}: %m%n"
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|row
init|=
name|iter
operator|.
name|next
argument_list|()
index|[
literal|0
index|]
operator|.
name|toString
argument_list|()
decl_stmt|;
comment|// just check if the log line starts with date
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|row
operator|.
name|matches
argument_list|(
literal|"^\\d{2}[/](0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01]).*$"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|SessionHandle
name|setupSession
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Open a session
name|SessionHandle
name|sessionHandle
init|=
name|client
operator|.
name|openSession
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// Change lock manager to embedded mode
name|String
name|queryString
init|=
literal|"SET hive.lock.manager="
operator|+
literal|"org.apache.hadoop.hive.ql.lockmgr.EmbeddedLockManager"
decl_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Drop the table if it exists
name|queryString
operator|=
literal|"DROP TABLE IF EXISTS "
operator|+
name|tableName
expr_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Create a test table
name|queryString
operator|=
literal|"create table "
operator|+
name|tableName
operator|+
literal|" (key int, value string)"
expr_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Load data
name|queryString
operator|=
literal|"load data local inpath '"
operator|+
name|dataFile
operator|+
literal|"' into table "
operator|+
name|tableName
expr_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|queryString
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Precondition check: verify whether the table is created and data is fetched correctly.
name|OperationHandle
name|operationHandle
init|=
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
name|sql
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|RowSet
name|rowSetResult
init|=
name|client
operator|.
name|fetchResults
argument_list|(
name|operationHandle
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|500
argument_list|,
name|rowSetResult
operator|.
name|numRows
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|238
argument_list|,
name|rowSetResult
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"val_238"
argument_list|,
name|rowSetResult
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
return|return
name|sessionHandle
return|;
block|}
block|}
end_class

end_unit

