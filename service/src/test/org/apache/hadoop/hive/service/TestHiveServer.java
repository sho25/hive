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
name|service
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
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
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|FieldSchema
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
name|Schema
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
name|service
operator|.
name|HiveInterface
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
name|service
operator|.
name|HiveClient
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
name|service
operator|.
name|HiveServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TSocket
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransport
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
name|serde2
operator|.
name|dynamic_type
operator|.
name|DynamicSerDe
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
name|serde
operator|.
name|Constants
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
name|io
operator|.
name|BytesWritable
import|;
end_import

begin_class
specifier|public
class|class
name|TestHiveServer
extends|extends
name|TestCase
block|{
specifier|private
name|HiveInterface
name|client
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|host
init|=
literal|"localhost"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|port
init|=
literal|10000
decl_stmt|;
specifier|private
name|Path
name|dataFilePath
decl_stmt|;
specifier|private
specifier|static
name|String
name|tableName
init|=
literal|"testhivedrivertable"
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|boolean
name|standAloneServer
init|=
literal|false
decl_stmt|;
specifier|private
name|TTransport
name|transport
decl_stmt|;
specifier|public
name|TestHiveServer
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|TestHiveServer
operator|.
name|class
argument_list|)
expr_stmt|;
name|String
name|dataFileDir
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"test.data.files"
argument_list|)
operator|.
name|replace
argument_list|(
literal|'\\'
argument_list|,
literal|'/'
argument_list|)
operator|.
name|replace
argument_list|(
literal|"c:"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|dataFilePath
operator|=
operator|new
name|Path
argument_list|(
name|dataFileDir
argument_list|,
literal|"kv1.txt"
argument_list|)
expr_stmt|;
comment|// See data/conf/hive-site.xml
name|String
name|paramStr
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.service.standalone.server"
argument_list|)
decl_stmt|;
if|if
condition|(
name|paramStr
operator|!=
literal|null
operator|&&
name|paramStr
operator|.
name|equals
argument_list|(
literal|"true"
argument_list|)
condition|)
name|standAloneServer
operator|=
literal|true
expr_stmt|;
block|}
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
if|if
condition|(
name|standAloneServer
condition|)
block|{
try|try
block|{
name|transport
operator|=
operator|new
name|TSocket
argument_list|(
name|host
argument_list|,
name|port
argument_list|)
expr_stmt|;
name|TProtocol
name|protocol
init|=
operator|new
name|TBinaryProtocol
argument_list|(
name|transport
argument_list|)
decl_stmt|;
name|client
operator|=
operator|new
name|HiveClient
argument_list|(
name|protocol
argument_list|)
expr_stmt|;
name|transport
operator|.
name|open
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|client
operator|=
operator|new
name|HiveServer
operator|.
name|HiveServerHandler
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
if|if
condition|(
name|standAloneServer
condition|)
block|{
name|transport
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testExecute
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|client
operator|.
name|execute
argument_list|(
literal|"drop table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{     }
try|try
block|{
name|client
operator|.
name|execute
argument_list|(
literal|"create table "
operator|+
name|tableName
operator|+
literal|" (num int)"
argument_list|)
expr_stmt|;
name|client
operator|.
name|execute
argument_list|(
literal|"load data local inpath '"
operator|+
name|dataFilePath
operator|.
name|toString
argument_list|()
operator|+
literal|"' into table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|client
operator|.
name|execute
argument_list|(
literal|"select count(1) as cnt from "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|String
name|row
init|=
name|client
operator|.
name|fetchOne
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|row
argument_list|,
literal|"500"
argument_list|)
expr_stmt|;
name|Schema
name|schema
init|=
name|client
operator|.
name|getSchema
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|listFields
init|=
name|schema
operator|.
name|getFieldSchemas
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|listFields
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|listFields
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|,
literal|"cnt"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|listFields
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
literal|"i64"
argument_list|)
expr_stmt|;
name|client
operator|.
name|execute
argument_list|(
literal|"drop table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|t
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|notestExecute
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|client
operator|.
name|execute
argument_list|(
literal|"drop table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{     }
name|client
operator|.
name|execute
argument_list|(
literal|"create table "
operator|+
name|tableName
operator|+
literal|" (num int)"
argument_list|)
expr_stmt|;
name|client
operator|.
name|execute
argument_list|(
literal|"load data local inpath '"
operator|+
name|dataFilePath
operator|.
name|toString
argument_list|()
operator|+
literal|"' into table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|client
operator|.
name|execute
argument_list|(
literal|"select count(1) from "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|String
name|row
init|=
name|client
operator|.
name|fetchOne
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|row
argument_list|,
literal|"500"
argument_list|)
expr_stmt|;
name|client
operator|.
name|execute
argument_list|(
literal|"drop table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|transport
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test metastore call    */
specifier|public
name|void
name|testMetastore
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|client
operator|.
name|execute
argument_list|(
literal|"drop table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{     }
name|client
operator|.
name|execute
argument_list|(
literal|"create table "
operator|+
name|tableName
operator|+
literal|" (num int)"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|tabs
init|=
name|client
operator|.
name|get_tables
argument_list|(
literal|"default"
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|tabs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|client
operator|.
name|execute
argument_list|(
literal|"drop table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|/**     *    */
specifier|public
name|void
name|testFetch
parameter_list|()
throws|throws
name|Exception
block|{
comment|// create and populate a table with 500 rows.
try|try
block|{
name|client
operator|.
name|execute
argument_list|(
literal|"drop table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{     }
name|client
operator|.
name|execute
argument_list|(
literal|"create table "
operator|+
name|tableName
operator|+
literal|" (key int, value string)"
argument_list|)
expr_stmt|;
name|client
operator|.
name|execute
argument_list|(
literal|"load data local inpath '"
operator|+
name|dataFilePath
operator|.
name|toString
argument_list|()
operator|+
literal|"' into table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
try|try
block|{
comment|// fetchAll test
name|client
operator|.
name|execute
argument_list|(
literal|"select key, value from "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|client
operator|.
name|fetchAll
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|500
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|client
operator|.
name|fetchAll
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// fetchOne test
name|client
operator|.
name|execute
argument_list|(
literal|"select key, value from "
operator|+
name|tableName
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|500
condition|;
name|i
operator|++
control|)
block|{
name|String
name|str
init|=
name|client
operator|.
name|fetchOne
argument_list|()
decl_stmt|;
if|if
condition|(
name|str
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|assertTrue
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
name|client
operator|.
name|fetchOne
argument_list|()
argument_list|,
literal|""
argument_list|)
expr_stmt|;
comment|// fetchN test
name|client
operator|.
name|execute
argument_list|(
literal|"select key, value from "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|client
operator|.
name|fetchN
argument_list|(
literal|499
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
literal|499
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|client
operator|.
name|fetchN
argument_list|(
literal|499
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|client
operator|.
name|fetchN
argument_list|(
literal|499
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testDynamicSerde
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|client
operator|.
name|execute
argument_list|(
literal|"drop table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{     }
name|client
operator|.
name|execute
argument_list|(
literal|"create table "
operator|+
name|tableName
operator|+
literal|" (key int, value string)"
argument_list|)
expr_stmt|;
name|client
operator|.
name|execute
argument_list|(
literal|"load data local inpath '"
operator|+
name|dataFilePath
operator|.
name|toString
argument_list|()
operator|+
literal|"' into table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
comment|//client.execute("select key, count(1) from " + tableName + " where key> 10 group by key");
name|String
name|sql
init|=
literal|"select key, value from "
operator|+
name|tableName
operator|+
literal|" where key> 10"
decl_stmt|;
name|client
operator|.
name|execute
argument_list|(
name|sql
argument_list|)
expr_stmt|;
comment|// Instantiate DynamicSerDe
name|DynamicSerDe
name|ds
init|=
operator|new
name|DynamicSerDe
argument_list|()
decl_stmt|;
name|Properties
name|dsp
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|dsp
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|thrift
operator|.
name|TCTLSeparatedProtocol
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|dsp
operator|.
name|setProperty
argument_list|(
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
name|Constants
operator|.
name|META_TABLE_NAME
argument_list|,
literal|"result"
argument_list|)
expr_stmt|;
name|String
name|serDDL
init|=
operator|new
name|String
argument_list|(
literal|"struct result { "
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|schema
init|=
name|client
operator|.
name|getSchema
argument_list|()
operator|.
name|getFieldSchemas
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|schema
operator|.
name|size
argument_list|()
condition|;
name|pos
operator|++
control|)
block|{
if|if
condition|(
name|pos
operator|!=
literal|0
condition|)
name|serDDL
operator|=
name|serDDL
operator|.
name|concat
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|serDDL
operator|=
name|serDDL
operator|.
name|concat
argument_list|(
name|schema
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|serDDL
operator|=
name|serDDL
operator|.
name|concat
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
name|serDDL
operator|=
name|serDDL
operator|.
name|concat
argument_list|(
name|schema
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|serDDL
operator|=
name|serDDL
operator|.
name|concat
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
name|dsp
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_DDL
argument_list|,
name|serDDL
argument_list|)
expr_stmt|;
name|dsp
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_LIB
argument_list|,
name|ds
operator|.
name|getClass
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|dsp
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|FIELD_DELIM
argument_list|,
literal|"9"
argument_list|)
expr_stmt|;
name|ds
operator|.
name|initialize
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
name|dsp
argument_list|)
expr_stmt|;
name|String
name|row
init|=
name|client
operator|.
name|fetchOne
argument_list|()
decl_stmt|;
name|Object
name|o
init|=
name|ds
operator|.
name|deserialize
argument_list|(
operator|new
name|BytesWritable
argument_list|(
name|row
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|o
operator|.
name|getClass
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|"class java.util.ArrayList"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|?
argument_list|>
name|lst
init|=
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|o
decl_stmt|;
name|assertEquals
argument_list|(
name|lst
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|238
argument_list|)
expr_stmt|;
comment|// TODO: serde doesn't like underscore  -- struct result { string _c0}
name|sql
operator|=
literal|"select count(1) as c from "
operator|+
name|tableName
expr_stmt|;
name|client
operator|.
name|execute
argument_list|(
name|sql
argument_list|)
expr_stmt|;
name|row
operator|=
name|client
operator|.
name|fetchOne
argument_list|()
expr_stmt|;
name|serDDL
operator|=
operator|new
name|String
argument_list|(
literal|"struct result { "
argument_list|)
expr_stmt|;
name|schema
operator|=
name|client
operator|.
name|getSchema
argument_list|()
operator|.
name|getFieldSchemas
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|schema
operator|.
name|size
argument_list|()
condition|;
name|pos
operator|++
control|)
block|{
if|if
condition|(
name|pos
operator|!=
literal|0
condition|)
name|serDDL
operator|=
name|serDDL
operator|.
name|concat
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|serDDL
operator|=
name|serDDL
operator|.
name|concat
argument_list|(
name|schema
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|serDDL
operator|=
name|serDDL
operator|.
name|concat
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
name|serDDL
operator|=
name|serDDL
operator|.
name|concat
argument_list|(
name|schema
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|serDDL
operator|=
name|serDDL
operator|.
name|concat
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
name|dsp
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_DDL
argument_list|,
name|serDDL
argument_list|)
expr_stmt|;
comment|// Need a new DynamicSerDe instance - re-initialization is not supported.
name|ds
operator|=
operator|new
name|DynamicSerDe
argument_list|()
expr_stmt|;
name|ds
operator|.
name|initialize
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
name|dsp
argument_list|)
expr_stmt|;
name|o
operator|=
name|ds
operator|.
name|deserialize
argument_list|(
operator|new
name|BytesWritable
argument_list|(
name|row
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

