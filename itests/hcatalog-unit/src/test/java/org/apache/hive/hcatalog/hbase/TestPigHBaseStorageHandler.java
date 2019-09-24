begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|hbase
package|;
end_package

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
name|net
operator|.
name|URI
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
name|Iterator
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
name|hbase
operator|.
name|TableName
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
name|hbase
operator|.
name|client
operator|.
name|Admin
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
name|hbase
operator|.
name|client
operator|.
name|Connection
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
name|hbase
operator|.
name|client
operator|.
name|ConnectionFactory
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
name|hbase
operator|.
name|client
operator|.
name|Put
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
name|hbase
operator|.
name|client
operator|.
name|Result
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
name|hbase
operator|.
name|client
operator|.
name|ResultScanner
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
name|hbase
operator|.
name|client
operator|.
name|Scan
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
name|hbase
operator|.
name|client
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
name|hbase
operator|.
name|util
operator|.
name|Bytes
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|DriverFactory
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
name|IDriver
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
name|mapreduce
operator|.
name|HCatBaseTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|PigServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|data
operator|.
name|DataType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|data
operator|.
name|Tuple
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|impl
operator|.
name|logicalLayer
operator|.
name|schema
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
name|pig
operator|.
name|impl
operator|.
name|logicalLayer
operator|.
name|schema
operator|.
name|Schema
operator|.
name|FieldSchema
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

begin_class
specifier|public
class|class
name|TestPigHBaseStorageHandler
extends|extends
name|SkeletonHBaseTest
block|{
specifier|private
specifier|static
name|HiveConf
name|hcatConf
decl_stmt|;
specifier|private
specifier|static
name|IDriver
name|driver
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testFamily"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|QUALIFIER1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier1"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|QUALIFIER2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier2"
argument_list|)
decl_stmt|;
specifier|public
name|void
name|Initialize
parameter_list|()
throws|throws
name|Exception
block|{
name|hcatConf
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
comment|//hcatConf.set(ConfVars.SEMANTIC_ANALYZER_HOOK.varname,
comment|//		HCatSemanticAnalyzer.class.getName());
name|URI
name|fsuri
init|=
name|getFileSystem
argument_list|()
operator|.
name|getUri
argument_list|()
decl_stmt|;
name|Path
name|whPath
init|=
operator|new
name|Path
argument_list|(
name|fsuri
operator|.
name|getScheme
argument_list|()
argument_list|,
name|fsuri
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|getTestDir
argument_list|()
argument_list|)
decl_stmt|;
name|hcatConf
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
name|hcatConf
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
name|hcatConf
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
name|hcatConf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
operator|.
name|varname
argument_list|,
name|whPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
argument_list|)
expr_stmt|;
comment|//Add hbase properties
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|el
range|:
name|getHbaseConf
argument_list|()
control|)
block|{
if|if
condition|(
name|el
operator|.
name|getKey
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"hbase."
argument_list|)
condition|)
block|{
name|hcatConf
operator|.
name|set
argument_list|(
name|el
operator|.
name|getKey
argument_list|()
argument_list|,
name|el
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|driver
operator|=
name|DriverFactory
operator|.
name|newDriver
argument_list|(
name|hcatConf
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hcatConf
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|populateHBaseTable
parameter_list|(
name|String
name|tName
parameter_list|,
name|Connection
name|connection
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Put
argument_list|>
name|myPuts
init|=
name|generatePuts
argument_list|(
name|tName
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
literal|null
decl_stmt|;
try|try
block|{
name|table
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tName
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|myPuts
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|List
argument_list|<
name|Put
argument_list|>
name|generatePuts
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Put
argument_list|>
name|myPuts
decl_stmt|;
name|myPuts
operator|=
operator|new
name|ArrayList
argument_list|<
name|Put
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER1
argument_list|,
literal|1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"textA-"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER2
argument_list|,
literal|1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"textB-"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|myPuts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
return|return
name|myPuts
return|;
block|}
specifier|public
specifier|static
name|void
name|createTestDataFile
parameter_list|(
name|String
name|filename
parameter_list|)
throws|throws
name|IOException
block|{
name|FileWriter
name|writer
init|=
literal|null
decl_stmt|;
name|int
name|LOOP_SIZE
init|=
literal|10
decl_stmt|;
name|float
name|f
init|=
operator|-
literal|100.1f
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
name|writer
operator|.
name|write
argument_list|(
name|i
operator|+
literal|"\t"
operator|+
operator|(
name|f
operator|+
name|i
operator|)
operator|+
literal|"\t"
operator|+
literal|"textB-"
operator|+
name|i
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
annotation|@
name|Test
specifier|public
name|void
name|testPigHBaseSchema
parameter_list|()
throws|throws
name|Exception
block|{
name|Initialize
argument_list|()
expr_stmt|;
name|String
name|tableName
init|=
name|newTableName
argument_list|(
literal|"MyTable"
argument_list|)
decl_stmt|;
name|String
name|databaseName
init|=
name|newTableName
argument_list|(
literal|"MyDatabase"
argument_list|)
decl_stmt|;
comment|//Table name will be lower case unless specified by hbase.table.name property
name|String
name|hbaseTableName
init|=
literal|"testTable"
decl_stmt|;
name|String
name|db_dir
init|=
name|HCatUtil
operator|.
name|makePathASafeFileName
argument_list|(
name|getTestDir
argument_list|()
operator|+
literal|"/hbasedb"
argument_list|)
decl_stmt|;
name|String
name|dbQuery
init|=
literal|"CREATE DATABASE IF NOT EXISTS "
operator|+
name|databaseName
operator|+
literal|" LOCATION '"
operator|+
name|db_dir
operator|+
literal|"'"
decl_stmt|;
name|String
name|deleteQuery
init|=
literal|"DROP TABLE "
operator|+
name|databaseName
operator|+
literal|"."
operator|+
name|tableName
decl_stmt|;
name|String
name|tableQuery
init|=
literal|"CREATE TABLE "
operator|+
name|databaseName
operator|+
literal|"."
operator|+
name|tableName
operator|+
literal|"(key float, testqualifier1 string, testqualifier2 int) STORED BY "
operator|+
literal|"'org.apache.hadoop.hive.hbase.HBaseStorageHandler'"
operator|+
literal|" WITH SERDEPROPERTIES ('hbase.columns.mapping'=':key,testFamily:testQualifier1,testFamily:testQualifier2')"
operator|+
literal|" TBLPROPERTIES ('hbase.table.name'='"
operator|+
name|hbaseTableName
operator|+
literal|"')"
decl_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|deleteQuery
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|dbQuery
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|tableQuery
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
literal|null
decl_stmt|;
name|Admin
name|hAdmin
init|=
literal|null
decl_stmt|;
name|boolean
name|doesTableExist
init|=
literal|false
decl_stmt|;
try|try
block|{
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|getHbaseConf
argument_list|()
argument_list|)
expr_stmt|;
name|hAdmin
operator|=
name|connection
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|doesTableExist
operator|=
name|hAdmin
operator|.
name|tableExists
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|hbaseTableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|hAdmin
operator|!=
literal|null
condition|)
block|{
name|hAdmin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|connection
operator|!=
literal|null
condition|)
block|{
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|doesTableExist
argument_list|)
expr_stmt|;
name|PigServer
name|server
init|=
name|HCatBaseTest
operator|.
name|createPigServer
argument_list|(
literal|false
argument_list|,
name|hcatConf
operator|.
name|getAllProperties
argument_list|()
argument_list|)
decl_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"A = load '"
operator|+
name|databaseName
operator|+
literal|"."
operator|+
name|tableName
operator|+
literal|"' using org.apache.hive.hcatalog.pig.HCatLoader();"
argument_list|)
expr_stmt|;
name|Schema
name|dumpedASchema
init|=
name|server
operator|.
name|dumpSchema
argument_list|(
literal|"A"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fields
init|=
name|dumpedASchema
operator|.
name|getFields
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|fields
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|DataType
operator|.
name|FLOAT
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|type
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"key"
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|DataType
operator|.
name|CHARARRAY
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|type
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"testQualifier1"
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|DataType
operator|.
name|INTEGER
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|type
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"testQualifier2"
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPigFilterProjection
parameter_list|()
throws|throws
name|Exception
block|{
name|Initialize
argument_list|()
expr_stmt|;
name|String
name|tableName
init|=
name|newTableName
argument_list|(
literal|"MyTable"
argument_list|)
decl_stmt|;
name|String
name|databaseName
init|=
name|newTableName
argument_list|(
literal|"MyDatabase"
argument_list|)
decl_stmt|;
comment|//Table name will be lower case unless specified by hbase.table.name property
name|String
name|hbaseTableName
init|=
operator|(
name|databaseName
operator|+
literal|"."
operator|+
name|tableName
operator|)
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
name|String
name|db_dir
init|=
name|HCatUtil
operator|.
name|makePathASafeFileName
argument_list|(
name|getTestDir
argument_list|()
operator|+
literal|"/hbasedb"
argument_list|)
decl_stmt|;
name|String
name|dbQuery
init|=
literal|"CREATE DATABASE IF NOT EXISTS "
operator|+
name|databaseName
operator|+
literal|" LOCATION '"
operator|+
name|db_dir
operator|+
literal|"'"
decl_stmt|;
name|String
name|deleteQuery
init|=
literal|"DROP TABLE "
operator|+
name|databaseName
operator|+
literal|"."
operator|+
name|tableName
decl_stmt|;
name|String
name|tableQuery
init|=
literal|"CREATE TABLE "
operator|+
name|databaseName
operator|+
literal|"."
operator|+
name|tableName
operator|+
literal|"(key int, testqualifier1 string, testqualifier2 string) STORED BY "
operator|+
literal|"'org.apache.hadoop.hive.hbase.HBaseStorageHandler'"
operator|+
literal|" WITH SERDEPROPERTIES ('hbase.columns.mapping'=':key,testFamily:testQualifier1,testFamily:testQualifier2')"
operator|+
literal|" TBLPROPERTIES ('hbase.table.default.storage.type'='binary')"
decl_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|deleteQuery
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|dbQuery
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|tableQuery
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
literal|null
decl_stmt|;
name|Admin
name|hAdmin
init|=
literal|null
decl_stmt|;
name|Table
name|table
init|=
literal|null
decl_stmt|;
name|ResultScanner
name|scanner
init|=
literal|null
decl_stmt|;
name|boolean
name|doesTableExist
init|=
literal|false
decl_stmt|;
try|try
block|{
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|getHbaseConf
argument_list|()
argument_list|)
expr_stmt|;
name|hAdmin
operator|=
name|connection
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|doesTableExist
operator|=
name|hAdmin
operator|.
name|tableExists
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|hbaseTableName
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|doesTableExist
argument_list|)
expr_stmt|;
name|populateHBaseTable
argument_list|(
name|hbaseTableName
argument_list|,
name|connection
argument_list|)
expr_stmt|;
name|table
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|hbaseTableName
argument_list|)
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testFamily"
argument_list|)
argument_list|)
expr_stmt|;
name|scanner
operator|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|scanner
operator|!=
literal|null
condition|)
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|hAdmin
operator|!=
literal|null
condition|)
block|{
name|hAdmin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|connection
operator|!=
literal|null
condition|)
block|{
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
name|int
name|index
init|=
literal|1
decl_stmt|;
name|PigServer
name|server
init|=
name|HCatBaseTest
operator|.
name|createPigServer
argument_list|(
literal|false
argument_list|,
name|hcatConf
operator|.
name|getAllProperties
argument_list|()
argument_list|)
decl_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"A = load '"
operator|+
name|databaseName
operator|+
literal|"."
operator|+
name|tableName
operator|+
literal|"' using org.apache.hive.hcatalog.pig.HCatLoader();"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"B = filter A by key< 5;"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"C = foreach B generate key,testqualifier2;"
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|Tuple
argument_list|>
name|itr
init|=
name|server
operator|.
name|openIterator
argument_list|(
literal|"C"
argument_list|)
decl_stmt|;
comment|//verify if the filter is correct and returns 2 rows and contains 2 columns and the contents match
while|while
condition|(
name|itr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Tuple
name|t
init|=
name|itr
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|t
operator|.
name|size
argument_list|()
operator|==
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getClass
argument_list|()
operator|==
name|Integer
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|index
argument_list|,
name|t
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getClass
argument_list|()
operator|==
name|String
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"textB-"
operator|+
name|index
argument_list|,
name|t
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|index
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|index
operator|-
literal|1
argument_list|,
literal|4
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPigPopulation
parameter_list|()
throws|throws
name|Exception
block|{
name|Initialize
argument_list|()
expr_stmt|;
name|String
name|tableName
init|=
name|newTableName
argument_list|(
literal|"MyTable"
argument_list|)
decl_stmt|;
name|String
name|databaseName
init|=
name|newTableName
argument_list|(
literal|"MyDatabase"
argument_list|)
decl_stmt|;
comment|//Table name will be lower case unless specified by hbase.table.name property
name|String
name|hbaseTableName
init|=
operator|(
name|databaseName
operator|+
literal|"."
operator|+
name|tableName
operator|)
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
name|String
name|db_dir
init|=
name|HCatUtil
operator|.
name|makePathASafeFileName
argument_list|(
name|getTestDir
argument_list|()
operator|+
literal|"/hbasedb"
argument_list|)
decl_stmt|;
name|String
name|POPTXT_FILE_NAME
init|=
name|db_dir
operator|+
literal|"testfile.txt"
decl_stmt|;
name|float
name|f
init|=
operator|-
literal|100.1f
decl_stmt|;
name|String
name|dbQuery
init|=
literal|"CREATE DATABASE IF NOT EXISTS "
operator|+
name|databaseName
operator|+
literal|" LOCATION '"
operator|+
name|db_dir
operator|+
literal|"'"
decl_stmt|;
name|String
name|deleteQuery
init|=
literal|"DROP TABLE "
operator|+
name|databaseName
operator|+
literal|"."
operator|+
name|tableName
decl_stmt|;
name|String
name|tableQuery
init|=
literal|"CREATE TABLE "
operator|+
name|databaseName
operator|+
literal|"."
operator|+
name|tableName
operator|+
literal|"(key int, testqualifier1 float, testqualifier2 string) STORED BY "
operator|+
literal|"'org.apache.hadoop.hive.hbase.HBaseStorageHandler'"
operator|+
literal|" WITH SERDEPROPERTIES ('hbase.columns.mapping'=':key,testFamily:testQualifier1,testFamily:testQualifier2')"
operator|+
literal|" TBLPROPERTIES ('hbase.table.default.storage.type'='binary')"
decl_stmt|;
name|String
name|selectQuery
init|=
literal|"SELECT * from "
operator|+
name|databaseName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"."
operator|+
name|tableName
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|deleteQuery
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|dbQuery
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|tableQuery
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
literal|null
decl_stmt|;
name|Admin
name|hAdmin
init|=
literal|null
decl_stmt|;
name|Table
name|table
init|=
literal|null
decl_stmt|;
name|ResultScanner
name|scanner
init|=
literal|null
decl_stmt|;
name|boolean
name|doesTableExist
init|=
literal|false
decl_stmt|;
try|try
block|{
name|connection
operator|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|getHbaseConf
argument_list|()
argument_list|)
expr_stmt|;
name|hAdmin
operator|=
name|connection
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|doesTableExist
operator|=
name|hAdmin
operator|.
name|tableExists
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|hbaseTableName
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|doesTableExist
argument_list|)
expr_stmt|;
name|createTestDataFile
argument_list|(
name|POPTXT_FILE_NAME
argument_list|)
expr_stmt|;
name|PigServer
name|server
init|=
name|HCatBaseTest
operator|.
name|createPigServer
argument_list|(
literal|false
argument_list|,
name|hcatConf
operator|.
name|getAllProperties
argument_list|()
argument_list|)
decl_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"A = load '"
operator|+
name|POPTXT_FILE_NAME
operator|+
literal|"' using PigStorage() as (key:int, testqualifier1:float, testqualifier2:chararray);"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"B = filter A by (key> 2) AND (key< 8) ;"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"store B into '"
operator|+
name|databaseName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"."
operator|+
name|tableName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"' using  org.apache.hive.hcatalog.pig.HCatStorer();"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"C = load '"
operator|+
name|databaseName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"."
operator|+
name|tableName
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"' using org.apache.hive.hcatalog.pig.HCatLoader();"
argument_list|)
expr_stmt|;
comment|// Schema should be same
name|Schema
name|dumpedBSchema
init|=
name|server
operator|.
name|dumpSchema
argument_list|(
literal|"C"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fields
init|=
name|dumpedBSchema
operator|.
name|getFields
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|fields
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|DataType
operator|.
name|INTEGER
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|type
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"key"
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|DataType
operator|.
name|FLOAT
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|type
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"testQualifier1"
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|DataType
operator|.
name|CHARARRAY
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|type
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"testQualifier2"
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
comment|//Query the hbase table and check the key is valid and only 5  are present
name|table
operator|=
name|connection
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|hbaseTableName
argument_list|)
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testFamily"
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|familyNameBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testFamily"
argument_list|)
decl_stmt|;
name|scanner
operator|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|int
name|index
init|=
literal|3
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|result
range|:
name|scanner
control|)
block|{
comment|//key is correct
name|assertEquals
argument_list|(
name|index
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|//first column exists
name|assertTrue
argument_list|(
name|result
operator|.
name|containsColumn
argument_list|(
name|familyNameBytes
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|//value is correct
name|assertEquals
argument_list|(
operator|(
name|index
operator|+
name|f
operator|)
argument_list|,
name|Bytes
operator|.
name|toFloat
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|familyNameBytes
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier1"
argument_list|)
argument_list|)
argument_list|)
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|//second column exists
name|assertTrue
argument_list|(
name|result
operator|.
name|containsColumn
argument_list|(
name|familyNameBytes
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|//value is correct
name|assertEquals
argument_list|(
operator|(
literal|"textB-"
operator|+
name|index
operator|)
operator|.
name|toString
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|result
operator|.
name|getValue
argument_list|(
name|familyNameBytes
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier2"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|index
operator|++
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
comment|// 5 rows should be returned
name|assertEquals
argument_list|(
name|count
argument_list|,
literal|5
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|scanner
operator|!=
literal|null
condition|)
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|hAdmin
operator|!=
literal|null
condition|)
block|{
name|hAdmin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|connection
operator|!=
literal|null
condition|)
block|{
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|//Check if hive returns results correctly
name|driver
operator|.
name|run
argument_list|(
name|selectQuery
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|result
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
name|result
argument_list|)
expr_stmt|;
comment|//Query using the hive command line
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|String
argument_list|>
name|itr
init|=
name|result
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|3
init|;
name|i
operator|<=
literal|7
condition|;
name|i
operator|++
control|)
block|{
name|String
name|tokens
index|[]
init|=
name|itr
operator|.
name|next
argument_list|()
operator|.
name|split
argument_list|(
literal|"\\s+"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|Integer
operator|.
name|parseInt
argument_list|(
name|tokens
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
operator|+
name|f
argument_list|,
name|Float
operator|.
name|parseFloat
argument_list|(
name|tokens
index|[
literal|1
index|]
argument_list|)
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
literal|"textB-"
operator|+
name|i
operator|)
operator|.
name|toString
argument_list|()
argument_list|,
name|tokens
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
block|}
comment|//delete the table from the database
name|driver
operator|.
name|run
argument_list|(
name|deleteQuery
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

