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
name|Map
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
name|InvalidOperationException
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
name|NoSuchObjectException
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
name|metastore
operator|.
name|api
operator|.
name|Type
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
name|tools
operator|.
name|HiveMetaTool
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
name|avro
operator|.
name|AvroContainerInputFormat
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
name|avro
operator|.
name|AvroContainerOutputFormat
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

begin_class
specifier|public
class|class
name|TestHiveMetaTool
extends|extends
name|TestCase
block|{
specifier|private
name|HiveMetaStoreClient
name|client
decl_stmt|;
specifier|private
name|PrintStream
name|originalOut
decl_stmt|;
specifier|private
name|OutputStream
name|os
decl_stmt|;
specifier|private
name|PrintStream
name|ps
decl_stmt|;
specifier|private
name|String
name|locationUri
decl_stmt|;
specifier|private
specifier|final
name|String
name|dbName
init|=
literal|"TestHiveMetaToolDB"
decl_stmt|;
specifier|private
specifier|final
name|String
name|typeName
init|=
literal|"Person"
decl_stmt|;
specifier|private
specifier|final
name|String
name|tblName
init|=
literal|"simpleTbl"
decl_stmt|;
specifier|private
specifier|final
name|String
name|badTblName
init|=
literal|"badSimpleTbl"
decl_stmt|;
specifier|private
name|void
name|dropDatabase
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|client
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{     }
catch|catch
parameter_list|(
name|InvalidOperationException
name|e
parameter_list|)
block|{     }
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
block|}
annotation|@
name|Override
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
try|try
block|{
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
comment|// Setup output stream to redirect output to
name|os
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|ps
operator|=
operator|new
name|PrintStream
argument_list|(
name|os
argument_list|)
expr_stmt|;
comment|// create a dummy database and a couple of dummy tables
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
name|dbName
argument_list|)
expr_stmt|;
name|client
operator|.
name|dropTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
expr_stmt|;
name|client
operator|.
name|dropTable
argument_list|(
name|dbName
argument_list|,
name|badTblName
argument_list|)
expr_stmt|;
name|dropDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|client
operator|.
name|createDatabase
argument_list|(
name|db
argument_list|)
expr_stmt|;
name|locationUri
operator|=
name|db
operator|.
name|getLocationUri
argument_list|()
expr_stmt|;
name|String
name|avroUri
init|=
literal|"hdfs://nn.example.com/warehouse/hive/ab.avsc"
decl_stmt|;
name|String
name|badAvroUri
init|=
operator|new
name|String
argument_list|(
literal|"hdfs:/hive"
argument_list|)
decl_stmt|;
name|client
operator|.
name|dropType
argument_list|(
name|typeName
argument_list|)
expr_stmt|;
name|Type
name|typ1
init|=
operator|new
name|Type
argument_list|()
decl_stmt|;
name|typ1
operator|.
name|setName
argument_list|(
name|typeName
argument_list|)
expr_stmt|;
name|typ1
operator|.
name|setFields
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|typ1
operator|.
name|getFields
argument_list|()
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
name|typ1
operator|.
name|getFields
argument_list|()
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
name|client
operator|.
name|createType
argument_list|(
name|typ1
argument_list|)
expr_stmt|;
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
name|dbName
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setTableName
argument_list|(
name|tblName
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
name|AvroSerdeUtils
operator|.
name|SCHEMA_URL
argument_list|,
name|avroUri
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setParameters
argument_list|(
name|parameters
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
name|tbl
operator|.
name|setSd
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setCols
argument_list|(
name|typ1
operator|.
name|getFields
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setCompressed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setNumBuckets
argument_list|(
literal|1
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
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
literal|"test_param_1"
argument_list|,
literal|"Use this for comments etc"
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setBucketCols
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getBucketCols
argument_list|()
operator|.
name|add
argument_list|(
literal|"name"
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
name|setName
argument_list|(
name|tbl
operator|.
name|getTableName
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
name|getSerdeInfo
argument_list|()
operator|.
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
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
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
name|AvroSerdeUtils
operator|.
name|SCHEMA_URL
argument_list|,
name|avroUri
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|setSerializationLib
argument_list|(
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
name|AvroSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setInputFormat
argument_list|(
name|AvroContainerInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setOutputFormat
argument_list|(
name|AvroContainerOutputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setPartitionKeys
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|.
name|createTable
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
comment|//create a table with bad avro uri
name|tbl
operator|=
operator|new
name|Table
argument_list|()
expr_stmt|;
name|tbl
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setTableName
argument_list|(
name|badTblName
argument_list|)
expr_stmt|;
name|sd
operator|=
operator|new
name|StorageDescriptor
argument_list|()
expr_stmt|;
name|tbl
operator|.
name|setSd
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setCols
argument_list|(
name|typ1
operator|.
name|getFields
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setCompressed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setNumBuckets
argument_list|(
literal|1
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
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
literal|"test_param_1"
argument_list|,
literal|"Use this for comments etc"
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setBucketCols
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getBucketCols
argument_list|()
operator|.
name|add
argument_list|(
literal|"name"
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
name|setName
argument_list|(
name|tbl
operator|.
name|getTableName
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
name|getSerdeInfo
argument_list|()
operator|.
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
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
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
name|AvroSerdeUtils
operator|.
name|SCHEMA_URL
argument_list|,
name|badAvroUri
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|setSerializationLib
argument_list|(
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
name|AvroSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setInputFormat
argument_list|(
name|AvroContainerInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setOutputFormat
argument_list|(
name|AvroContainerOutputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setPartitionKeys
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|.
name|createTable
argument_list|(
name|tbl
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
name|redirectOutputStream
parameter_list|()
block|{
name|originalOut
operator|=
name|System
operator|.
name|out
expr_stmt|;
name|System
operator|.
name|setOut
argument_list|(
name|ps
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|restoreOutputStream
parameter_list|()
block|{
name|System
operator|.
name|setOut
argument_list|(
name|originalOut
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testListFSRoot
parameter_list|()
throws|throws
name|Exception
block|{
name|redirectOutputStream
argument_list|()
expr_stmt|;
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[
literal|1
index|]
decl_stmt|;
name|args
index|[
literal|0
index|]
operator|=
operator|new
name|String
argument_list|(
literal|"-listFSRoot"
argument_list|)
expr_stmt|;
try|try
block|{
name|HiveMetaTool
operator|.
name|main
argument_list|(
name|args
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
name|boolean
name|b
init|=
name|out
operator|.
name|contains
argument_list|(
name|locationUri
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|restoreOutputStream
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Completed testListFSRoot"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testExecuteJDOQL
parameter_list|()
throws|throws
name|Exception
block|{
name|redirectOutputStream
argument_list|()
expr_stmt|;
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[
literal|2
index|]
decl_stmt|;
name|args
index|[
literal|0
index|]
operator|=
operator|new
name|String
argument_list|(
literal|"-executeJDOQL"
argument_list|)
expr_stmt|;
name|args
index|[
literal|1
index|]
operator|=
operator|new
name|String
argument_list|(
literal|"select locationUri from org.apache.hadoop.hive.metastore.model.MDatabase"
argument_list|)
expr_stmt|;
try|try
block|{
name|HiveMetaTool
operator|.
name|main
argument_list|(
name|args
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
name|boolean
name|b
init|=
name|out
operator|.
name|contains
argument_list|(
name|locationUri
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|restoreOutputStream
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Completed testExecuteJDOQL"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testUpdateFSRootLocation
parameter_list|()
throws|throws
name|Exception
block|{
name|redirectOutputStream
argument_list|()
expr_stmt|;
name|String
name|oldLocationUri
init|=
literal|"hdfs://nn.example.com/"
decl_stmt|;
name|String
name|newLocationUri
init|=
literal|"hdfs://nn-ha-uri/"
decl_stmt|;
name|String
name|oldSchemaUri
init|=
literal|"hdfs://nn.example.com/warehouse/hive/ab.avsc"
decl_stmt|;
name|String
name|newSchemaUri
init|=
literal|"hdfs://nn-ha-uri/warehouse/hive/ab.avsc"
decl_stmt|;
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[
literal|5
index|]
decl_stmt|;
name|args
index|[
literal|0
index|]
operator|=
operator|new
name|String
argument_list|(
literal|"-updateLocation"
argument_list|)
expr_stmt|;
name|args
index|[
literal|1
index|]
operator|=
operator|new
name|String
argument_list|(
name|newLocationUri
argument_list|)
expr_stmt|;
name|args
index|[
literal|2
index|]
operator|=
operator|new
name|String
argument_list|(
name|oldLocationUri
argument_list|)
expr_stmt|;
name|args
index|[
literal|3
index|]
operator|=
operator|new
name|String
argument_list|(
literal|"-tablePropKey"
argument_list|)
expr_stmt|;
name|args
index|[
literal|4
index|]
operator|=
operator|new
name|String
argument_list|(
literal|"avro.schema.url"
argument_list|)
expr_stmt|;
try|try
block|{
name|checkAvroSchemaURLProps
argument_list|(
name|client
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
argument_list|,
name|oldSchemaUri
argument_list|)
expr_stmt|;
comment|// perform HA upgrade
name|HiveMetaTool
operator|.
name|main
argument_list|(
name|args
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
name|boolean
name|b
init|=
name|out
operator|.
name|contains
argument_list|(
name|newLocationUri
argument_list|)
decl_stmt|;
name|restoreOutputStream
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|checkAvroSchemaURLProps
argument_list|(
name|client
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
argument_list|,
name|newSchemaUri
argument_list|)
expr_stmt|;
comment|//restore the original HDFS root
name|args
index|[
literal|1
index|]
operator|=
operator|new
name|String
argument_list|(
name|oldLocationUri
argument_list|)
expr_stmt|;
name|args
index|[
literal|2
index|]
operator|=
operator|new
name|String
argument_list|(
name|newLocationUri
argument_list|)
expr_stmt|;
name|redirectOutputStream
argument_list|()
expr_stmt|;
name|HiveMetaTool
operator|.
name|main
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|checkAvroSchemaURLProps
argument_list|(
name|client
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
argument_list|,
name|oldSchemaUri
argument_list|)
expr_stmt|;
name|restoreOutputStream
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|restoreOutputStream
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Completed testUpdateFSRootLocation.."
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|checkAvroSchemaURLProps
parameter_list|(
name|Table
name|table
parameter_list|,
name|String
name|expectedURL
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|expectedURL
argument_list|,
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|AvroSerdeUtils
operator|.
name|SCHEMA_URL
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedURL
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
name|AvroSerdeUtils
operator|.
name|SCHEMA_URL
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
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
name|dbName
argument_list|,
name|tblName
argument_list|)
expr_stmt|;
name|client
operator|.
name|dropTable
argument_list|(
name|dbName
argument_list|,
name|badTblName
argument_list|)
expr_stmt|;
name|dropDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|super
operator|.
name|tearDown
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

