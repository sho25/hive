begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ql
operator|.
name|metadata
package|;
end_package

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
name|LinkedList
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
name|DB
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
name|ql
operator|.
name|io
operator|.
name|IgnoreKeyTextOutputFormat
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
name|thrift
operator|.
name|test
operator|.
name|Complex
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
name|MetadataTypedColumnsetSerDe
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
name|ThriftDeserializer
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
name|mapred
operator|.
name|SequenceFileInputFormat
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
name|mapred
operator|.
name|SequenceFileOutputFormat
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
name|mapred
operator|.
name|TextInputFormat
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
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
import|;
end_import

begin_class
specifier|public
class|class
name|TestHive
extends|extends
name|TestCase
block|{
specifier|private
name|Hive
name|hm
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
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
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|hm
operator|=
name|Hive
operator|.
name|get
argument_list|(
name|hiveConf
argument_list|)
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
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Unable to initialize Hive Metastore using configruation: \n "
operator|+
name|hiveConf
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|Hive
operator|.
name|closeCurrent
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
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Unable to close Hive Metastore using configruation: \n "
operator|+
name|hiveConf
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|public
name|void
name|testTable
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
comment|// create a simple table and test create, drop, get
name|String
name|tableName
init|=
literal|"table_for_testtable"
decl_stmt|;
try|try
block|{
name|this
operator|.
name|hm
operator|.
name|dropTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e1
parameter_list|)
block|{
name|e1
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Unable to drop table"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|Table
name|tbl
init|=
operator|new
name|Table
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fields
init|=
name|tbl
operator|.
name|getCols
argument_list|()
decl_stmt|;
name|fields
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"col1"
argument_list|,
name|Constants
operator|.
name|INT_TYPE_NAME
argument_list|,
literal|"int -- first column"
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
literal|"col2"
argument_list|,
name|Constants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|"string -- second column"
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
literal|"col3"
argument_list|,
name|Constants
operator|.
name|DOUBLE_TYPE_NAME
argument_list|,
literal|"double -- thrift column"
argument_list|)
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setFields
argument_list|(
name|fields
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setOutputFormatClass
argument_list|(
name|IgnoreKeyTextOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setInputFormatClass
argument_list|(
name|SequenceFileInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setProperty
argument_list|(
literal|"comment"
argument_list|,
literal|"this is a test table created as part junit tests"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
init|=
name|tbl
operator|.
name|getBucketCols
argument_list|()
decl_stmt|;
name|bucketCols
operator|.
name|add
argument_list|(
literal|"col1"
argument_list|)
expr_stmt|;
try|try
block|{
name|tbl
operator|.
name|setBucketCols
argument_list|(
name|bucketCols
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Unable to set bucket column for table: "
operator|+
name|tableName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
decl_stmt|;
name|partCols
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"ds"
argument_list|,
name|Constants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|"partition column, date but in string format as date type is not yet supported in QL"
argument_list|)
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setPartCols
argument_list|(
name|partCols
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setNumBuckets
argument_list|(
operator|(
name|short
operator|)
literal|512
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setOwner
argument_list|(
literal|"pchakka"
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setRetention
argument_list|(
literal|10
argument_list|)
expr_stmt|;
comment|// set output format parameters (these are not supported by QL but only for demo purposes)
name|tbl
operator|.
name|setSerdeParam
argument_list|(
name|Constants
operator|.
name|FIELD_DELIM
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setSerdeParam
argument_list|(
name|Constants
operator|.
name|LINE_DELIM
argument_list|,
literal|"\n"
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setSerdeParam
argument_list|(
name|Constants
operator|.
name|MAPKEY_DELIM
argument_list|,
literal|"3"
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setSerdeParam
argument_list|(
name|Constants
operator|.
name|COLLECTION_DELIM
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setSerdeParam
argument_list|(
name|Constants
operator|.
name|FIELD_DELIM
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setSerializationLib
argument_list|(
name|MetadataTypedColumnsetSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// create table
try|try
block|{
name|hm
operator|.
name|createTable
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Unable to create table: "
operator|+
name|tableName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|// get table
name|Table
name|ft
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ft
operator|=
name|hm
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|ft
operator|.
name|checkValidity
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Table names didn't match for table: "
operator|+
name|tableName
argument_list|,
name|tbl
operator|.
name|getName
argument_list|()
argument_list|,
name|ft
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Table owners didn't match for table: "
operator|+
name|tableName
argument_list|,
name|tbl
operator|.
name|getOwner
argument_list|()
argument_list|,
name|ft
operator|.
name|getOwner
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Table retention didn't match for table: "
operator|+
name|tableName
argument_list|,
name|tbl
operator|.
name|getRetention
argument_list|()
argument_list|,
name|ft
operator|.
name|getRetention
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Data location is not set correctly"
argument_list|,
name|DB
operator|.
name|getDefaultTablePath
argument_list|(
name|tableName
argument_list|,
name|this
operator|.
name|hiveConf
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|ft
operator|.
name|getDataLocation
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// now that URI is set correctly, set the original table's uri and then compare the two tables
name|tbl
operator|.
name|setDataLocation
argument_list|(
name|ft
operator|.
name|getDataLocation
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Tables  doesn't match: "
operator|+
name|tableName
argument_list|,
name|ft
operator|.
name|getTTable
argument_list|()
operator|.
name|equals
argument_list|(
name|tbl
operator|.
name|getTTable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Serde is not set correctly"
argument_list|,
name|tbl
operator|.
name|getDeserializer
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|ft
operator|.
name|getDeserializer
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"SerializationLib is not set correctly"
argument_list|,
name|tbl
operator|.
name|getSerializationLib
argument_list|()
argument_list|,
name|MetadataTypedColumnsetSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Unable to fetch table correctly: "
operator|+
name|tableName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|hm
operator|.
name|dropTable
argument_list|(
name|tableName
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Table
name|ft2
init|=
name|hm
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"Unable to drop table "
argument_list|,
name|ft2
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
literal|"Unable to drop table: "
operator|+
name|tableName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
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
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"testTable failed"
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
comment|/**    * Tests create and fetch of a thrift based table    * @throws Throwable     */
specifier|public
name|void
name|testThriftTable
parameter_list|()
throws|throws
name|Throwable
block|{
name|String
name|tableName
init|=
literal|"table_for_test_thrifttable"
decl_stmt|;
try|try
block|{
try|try
block|{
name|this
operator|.
name|hm
operator|.
name|dropTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e1
parameter_list|)
block|{
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
name|e1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Unable to drop table"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|Table
name|tbl
init|=
operator|new
name|Table
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|tbl
operator|.
name|setInputFormatClass
argument_list|(
name|SequenceFileInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setOutputFormatClass
argument_list|(
name|SequenceFileOutputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setSerializationLib
argument_list|(
name|ThriftDeserializer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setSerdeParam
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_CLASS
argument_list|,
name|Complex
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setSerdeParam
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
name|TBinaryProtocol
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|hm
operator|.
name|createTable
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
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
name|assertTrue
argument_list|(
literal|"Unable to create table: "
operator|+
name|tableName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|// get table
name|Table
name|ft
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ft
operator|=
name|hm
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"Unable to fetch table"
argument_list|,
name|ft
argument_list|)
expr_stmt|;
name|ft
operator|.
name|checkValidity
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Table names didn't match for table: "
operator|+
name|tableName
argument_list|,
name|tbl
operator|.
name|getName
argument_list|()
argument_list|,
name|ft
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Table owners didn't match for table: "
operator|+
name|tableName
argument_list|,
name|tbl
operator|.
name|getOwner
argument_list|()
argument_list|,
name|ft
operator|.
name|getOwner
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Table retention didn't match for table: "
operator|+
name|tableName
argument_list|,
name|tbl
operator|.
name|getRetention
argument_list|()
argument_list|,
name|ft
operator|.
name|getRetention
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Data location is not set correctly"
argument_list|,
name|DB
operator|.
name|getDefaultTablePath
argument_list|(
name|tableName
argument_list|,
name|this
operator|.
name|hiveConf
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|ft
operator|.
name|getDataLocation
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// now that URI is set correctly, set the original table's uri and then compare the two tables
name|tbl
operator|.
name|setDataLocation
argument_list|(
name|ft
operator|.
name|getDataLocation
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Tables  doesn't match: "
operator|+
name|tableName
argument_list|,
name|ft
operator|.
name|getTTable
argument_list|()
operator|.
name|equals
argument_list|(
name|tbl
operator|.
name|getTTable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"SerializationLib is not set correctly"
argument_list|,
name|tbl
operator|.
name|getSerializationLib
argument_list|()
argument_list|,
name|ThriftDeserializer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Serde is not set correctly"
argument_list|,
name|tbl
operator|.
name|getDeserializer
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|ft
operator|.
name|getDeserializer
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
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
name|assertTrue
argument_list|(
literal|"Unable to fetch table correctly: "
operator|+
name|tableName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|hm
operator|.
name|dropTable
argument_list|(
name|tableName
argument_list|)
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
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"testThriftTable() failed"
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|private
specifier|static
name|Table
name|createTestTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|HiveException
block|{
name|Table
name|tbl
init|=
operator|new
name|Table
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|tbl
operator|.
name|getTTable
argument_list|()
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setInputFormatClass
argument_list|(
name|SequenceFileInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setOutputFormatClass
argument_list|(
name|SequenceFileOutputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setSerializationLib
argument_list|(
name|ThriftDeserializer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setSerdeParam
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_CLASS
argument_list|,
name|Complex
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setSerdeParam
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
name|TBinaryProtocol
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|tbl
return|;
block|}
specifier|public
name|void
name|testGetAndDropTables
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|String
name|dbName
init|=
literal|"db_for_testgettables"
decl_stmt|;
name|String
name|table1Name
init|=
literal|"table1"
decl_stmt|;
name|hm
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|hm
operator|.
name|createDatabase
argument_list|(
name|dbName
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|ts
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|ts
operator|.
name|add
argument_list|(
name|table1Name
argument_list|)
expr_stmt|;
name|ts
operator|.
name|add
argument_list|(
literal|"table2"
argument_list|)
expr_stmt|;
name|Table
name|tbl1
init|=
name|createTestTable
argument_list|(
name|dbName
argument_list|,
name|ts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|hm
operator|.
name|createTable
argument_list|(
name|tbl1
argument_list|)
expr_stmt|;
name|Table
name|tbl2
init|=
name|createTestTable
argument_list|(
name|dbName
argument_list|,
name|ts
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|hm
operator|.
name|createTable
argument_list|(
name|tbl2
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fts
init|=
name|hm
operator|.
name|getTablesForDb
argument_list|(
name|dbName
argument_list|,
literal|".*"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ts
argument_list|,
name|fts
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|fts
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|fts
operator|=
name|hm
operator|.
name|getTablesForDb
argument_list|(
name|dbName
argument_list|,
literal|".*1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|fts
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|fts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|//also test getting a table from a specific db
name|Table
name|table1
init|=
name|hm
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|table1Name
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|table1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|table1Name
argument_list|,
name|table1
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|table1
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|//and test dropping this specific table
name|hm
operator|.
name|dropTable
argument_list|(
name|dbName
argument_list|,
name|table1Name
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|table1
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|hm
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|)
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
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"testGetTables() failed"
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|public
name|void
name|testPartition
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|String
name|tableName
init|=
literal|"table_for_testpartition"
decl_stmt|;
try|try
block|{
name|hm
operator|.
name|dropTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
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
name|assertTrue
argument_list|(
literal|"Unable to drop table: "
operator|+
name|tableName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|LinkedList
argument_list|<
name|String
argument_list|>
name|cols
init|=
operator|new
name|LinkedList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"key"
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
literal|"value"
argument_list|)
expr_stmt|;
name|LinkedList
argument_list|<
name|String
argument_list|>
name|part_cols
init|=
operator|new
name|LinkedList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|part_cols
operator|.
name|add
argument_list|(
literal|"ds"
argument_list|)
expr_stmt|;
name|part_cols
operator|.
name|add
argument_list|(
literal|"hr"
argument_list|)
expr_stmt|;
try|try
block|{
name|hm
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|cols
argument_list|,
name|part_cols
argument_list|,
name|TextInputFormat
operator|.
name|class
argument_list|,
name|IgnoreKeyTextOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
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
name|assertTrue
argument_list|(
literal|"Unable to create table: "
operator|+
name|tableName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|Table
name|tbl
init|=
literal|null
decl_stmt|;
try|try
block|{
name|tbl
operator|=
name|hm
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
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
name|assertTrue
argument_list|(
literal|"Unable to fetch table: "
operator|+
name|tableName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|part_spec
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|part_spec
operator|.
name|clear
argument_list|()
expr_stmt|;
name|part_spec
operator|.
name|put
argument_list|(
literal|"ds"
argument_list|,
literal|"2008-04-08"
argument_list|)
expr_stmt|;
name|part_spec
operator|.
name|put
argument_list|(
literal|"hr"
argument_list|,
literal|"12"
argument_list|)
expr_stmt|;
try|try
block|{
name|hm
operator|.
name|createPartition
argument_list|(
name|tbl
argument_list|,
name|part_spec
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
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
name|assertTrue
argument_list|(
literal|"Unable to create parition for table: "
operator|+
name|tableName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|hm
operator|.
name|dropTable
argument_list|(
name|tableName
argument_list|)
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
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"testPartition() failed"
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
block|}
end_class

end_unit

