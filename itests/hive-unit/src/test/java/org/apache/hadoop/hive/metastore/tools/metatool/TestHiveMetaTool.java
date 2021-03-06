begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

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
name|metastore
operator|.
name|tools
operator|.
name|metatool
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintStream
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
name|HashMap
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
name|api
operator|.
name|Database
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
name|SerDeInfo
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
name|StorageDescriptor
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
name|Table
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
name|serdeConstants
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
name|avro
operator|.
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
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
name|util
operator|.
name|StringUtils
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
name|TException
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
name|assertTrue
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
name|assertEquals
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
name|After
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
comment|/** Integration tests for the HiveMetaTool program. */
end_comment

begin_class
specifier|public
class|class
name|TestHiveMetaTool
block|{
specifier|private
specifier|static
specifier|final
name|String
name|DB_NAME
init|=
literal|"TestHiveMetaToolDB"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"simpleTbl"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LOCATION
init|=
literal|"hdfs://nn.example.com/"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|NEW_LOCATION
init|=
literal|"hdfs://nn-ha-uri/"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PATH
init|=
literal|"warehouse/hive/ab.avsc"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|AVRO_URI
init|=
name|LOCATION
operator|+
name|PATH
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|NEW_AVRO_URI
init|=
name|NEW_LOCATION
operator|+
name|PATH
decl_stmt|;
specifier|private
name|HiveMetaStoreClient
name|client
decl_stmt|;
specifier|private
name|OutputStream
name|os
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|os
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|System
operator|.
name|setOut
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|os
argument_list|)
argument_list|)
expr_stmt|;
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|(
name|HiveMetaTool
operator|.
name|class
argument_list|)
decl_stmt|;
name|client
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|createDatabase
argument_list|()
expr_stmt|;
name|createTable
argument_list|()
expr_stmt|;
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Unable to setup the hive metatool test"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|Exception
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|createDatabase
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|client
operator|.
name|getAllDatabases
argument_list|()
operator|.
name|contains
argument_list|(
name|DB_NAME
argument_list|)
condition|)
block|{
name|client
operator|.
name|dropDatabase
argument_list|(
name|DB_NAME
argument_list|)
expr_stmt|;
block|}
name|Database
name|db
init|=
operator|new
name|Database
argument_list|()
decl_stmt|;
name|db
operator|.
name|setName
argument_list|(
name|DB_NAME
argument_list|)
expr_stmt|;
name|client
operator|.
name|createDatabase
argument_list|(
name|db
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|createTable
parameter_list|()
throws|throws
name|Exception
block|{
name|Table
name|tbl
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|tbl
operator|.
name|setDbName
argument_list|(
name|DB_NAME
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setTableName
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|parameters
operator|.
name|put
argument_list|(
name|AvroTableProperties
operator|.
name|SCHEMA_URL
operator|.
name|getPropName
argument_list|()
argument_list|,
name|AVRO_URI
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setParameters
argument_list|(
name|parameters
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|fields
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"name"
argument_list|,
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|fields
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"income"
argument_list|,
name|serdeConstants
operator|.
name|INT_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
name|sd
operator|.
name|setCols
argument_list|(
name|fields
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setParameters
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setSerdeInfo
argument_list|(
operator|new
name|SerDeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|setParameters
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
name|AvroTableProperties
operator|.
name|SCHEMA_URL
operator|.
name|getPropName
argument_list|()
argument_list|,
name|AVRO_URI
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setSd
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|client
operator|.
name|createTable
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListFSRoot
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveMetaTool
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-listFSRoot"
block|}
argument_list|)
expr_stmt|;
name|String
name|out
init|=
name|os
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|out
operator|+
literal|" doesn't contain "
operator|+
name|client
operator|.
name|getDatabase
argument_list|(
name|DB_NAME
argument_list|)
operator|.
name|getLocationUri
argument_list|()
argument_list|,
name|out
operator|.
name|contains
argument_list|(
name|client
operator|.
name|getDatabase
argument_list|(
name|DB_NAME
argument_list|)
operator|.
name|getLocationUri
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExecuteJDOQL
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveMetaTool
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-executeJDOQL"
block|,
literal|"select locationUri from org.apache.hadoop.hive.metastore.model.MDatabase"
block|}
argument_list|)
expr_stmt|;
name|String
name|out
init|=
name|os
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|out
operator|+
literal|" doesn't contain "
operator|+
name|client
operator|.
name|getDatabase
argument_list|(
name|DB_NAME
argument_list|)
operator|.
name|getLocationUri
argument_list|()
argument_list|,
name|out
operator|.
name|contains
argument_list|(
name|client
operator|.
name|getDatabase
argument_list|(
name|DB_NAME
argument_list|)
operator|.
name|getLocationUri
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateFSRootLocation
parameter_list|()
throws|throws
name|Exception
block|{
name|checkAvroSchemaURLProps
argument_list|(
name|AVRO_URI
argument_list|)
expr_stmt|;
name|HiveMetaTool
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-updateLocation"
block|,
name|NEW_LOCATION
block|,
name|LOCATION
block|,
literal|"-tablePropKey"
block|,
literal|"avro.schema.url"
block|}
argument_list|)
expr_stmt|;
name|checkAvroSchemaURLProps
argument_list|(
name|NEW_AVRO_URI
argument_list|)
expr_stmt|;
name|HiveMetaTool
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-updateLocation"
block|,
name|LOCATION
block|,
name|NEW_LOCATION
block|,
literal|"-tablePropKey"
block|,
literal|"avro.schema.url"
block|}
argument_list|)
expr_stmt|;
name|checkAvroSchemaURLProps
argument_list|(
name|AVRO_URI
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkAvroSchemaURLProps
parameter_list|(
name|String
name|expectedUri
parameter_list|)
throws|throws
name|TException
block|{
name|Table
name|table
init|=
name|client
operator|.
name|getTable
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedUri
argument_list|,
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|AvroTableProperties
operator|.
name|SCHEMA_URL
operator|.
name|getPropName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedUri
argument_list|,
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|AvroTableProperties
operator|.
name|SCHEMA_URL
operator|.
name|getPropName
argument_list|()
argument_list|)
argument_list|)
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
try|try
block|{
name|client
operator|.
name|dropTable
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|client
operator|.
name|dropDatabase
argument_list|(
name|DB_NAME
argument_list|)
expr_stmt|;
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Unable to close metastore"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|Exception
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

