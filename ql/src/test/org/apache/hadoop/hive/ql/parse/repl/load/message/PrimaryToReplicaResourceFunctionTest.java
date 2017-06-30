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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|FileSystem
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
name|api
operator|.
name|Function
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
name|ResourceType
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
name|ResourceUri
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
name|exec
operator|.
name|ReplCopyTask
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
name|exec
operator|.
name|Task
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
name|parse
operator|.
name|ReplicationSpec
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
name|parse
operator|.
name|SemanticException
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|MetaData
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
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|powermock
operator|.
name|core
operator|.
name|classloader
operator|.
name|annotations
operator|.
name|PrepareForTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|powermock
operator|.
name|modules
operator|.
name|junit4
operator|.
name|PowerMockRunner
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
import|;
end_import

begin_import
import|import static
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|CreateFunctionHandler
operator|.
name|PrimaryToReplicaResourceFunction
import|;
end_import

begin_import
import|import static
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|MessageHandler
operator|.
name|Context
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|is
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertThat
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|powermock
operator|.
name|api
operator|.
name|mockito
operator|.
name|PowerMockito
operator|.
name|mockStatic
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|powermock
operator|.
name|api
operator|.
name|mockito
operator|.
name|PowerMockito
operator|.
name|when
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|any
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|PowerMockRunner
operator|.
name|class
argument_list|)
annotation|@
name|PrepareForTest
argument_list|(
block|{
name|PrimaryToReplicaResourceFunction
operator|.
name|class
block|,
name|FileSystem
operator|.
name|class
block|,
name|ReplCopyTask
operator|.
name|class
block|,
name|System
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|PrimaryToReplicaResourceFunctionTest
block|{
specifier|private
name|PrimaryToReplicaResourceFunction
name|function
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|Function
name|functionObj
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|FileSystem
name|mockFs
decl_stmt|;
specifier|private
specifier|static
name|Logger
name|logger
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|PrimaryToReplicaResourceFunctionTest
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|MetaData
name|metadata
init|=
operator|new
name|MetaData
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|functionObj
argument_list|)
decl_stmt|;
name|Context
name|context
init|=
operator|new
name|Context
argument_list|(
literal|"primaryDb"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|hiveConf
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|logger
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPL_FUNCTIONS_ROOT_DIR
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"/someBasePath/withADir/"
argument_list|)
expr_stmt|;
name|function
operator|=
operator|new
name|PrimaryToReplicaResourceFunction
argument_list|(
name|context
argument_list|,
name|metadata
argument_list|,
literal|"replicaDbName"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|createDestinationPath
parameter_list|()
throws|throws
name|IOException
throws|,
name|SemanticException
throws|,
name|URISyntaxException
block|{
name|mockStatic
argument_list|(
name|FileSystem
operator|.
name|class
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|FileSystem
operator|.
name|get
argument_list|(
name|any
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockFs
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockFs
operator|.
name|getScheme
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"hdfs"
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockFs
operator|.
name|getUri
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|URI
argument_list|(
literal|"hdfs"
argument_list|,
literal|"somehost:9000"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|mockStatic
argument_list|(
name|System
operator|.
name|class
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|functionObj
operator|.
name|getFunctionName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"someFunctionName"
argument_list|)
expr_stmt|;
name|mockStatic
argument_list|(
name|ReplCopyTask
operator|.
name|class
argument_list|)
expr_stmt|;
name|Task
name|mock
init|=
name|mock
argument_list|(
name|Task
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|ReplCopyTask
operator|.
name|getLoadCopyTask
argument_list|(
name|any
argument_list|(
name|ReplicationSpec
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|Path
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|Path
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|HiveConf
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mock
argument_list|)
expr_stmt|;
name|ResourceUri
name|resourceUri
init|=
name|function
operator|.
name|destinationResourceUri
argument_list|(
operator|new
name|ResourceUri
argument_list|(
name|ResourceType
operator|.
name|JAR
argument_list|,
literal|"hdfs://localhost:9000/user/someplace/ab.jar#e094828883"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|resourceUri
operator|.
name|getUri
argument_list|()
argument_list|,
name|is
argument_list|(
name|equalTo
argument_list|(
literal|"hdfs://somehost:9000/someBasePath/withADir/replicaDbName/somefunctionname/"
operator|+
name|String
operator|.
name|valueOf
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
operator|+
literal|"/ab.jar"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
