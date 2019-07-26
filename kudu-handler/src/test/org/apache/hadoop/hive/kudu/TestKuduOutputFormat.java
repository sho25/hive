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
name|kudu
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
name|hive
operator|.
name|kudu
operator|.
name|KuduOutputFormat
operator|.
name|KuduRecordWriter
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
name|JobConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
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
name|kudu
operator|.
name|client
operator|.
name|CreateTableOptions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|client
operator|.
name|KuduClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|client
operator|.
name|KuduException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|client
operator|.
name|KuduScanner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|client
operator|.
name|KuduTable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|client
operator|.
name|PartialRow
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|client
operator|.
name|RowResult
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|kudu
operator|.
name|test
operator|.
name|KuduTestHarness
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
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
operator|.
name|UTF_8
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
name|kudu
operator|.
name|KuduStorageHandler
operator|.
name|KUDU_MASTER_ADDRS_KEY
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
name|kudu
operator|.
name|KuduStorageHandler
operator|.
name|KUDU_TABLE_NAME_KEY
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
name|kudu
operator|.
name|KuduTestUtils
operator|.
name|getAllTypesSchema
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
name|containsString
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
name|fail
import|;
end_import

begin_comment
comment|/**  * Tests for the KuduOutputFormat implementation.  */
end_comment

begin_class
specifier|public
class|class
name|TestKuduOutputFormat
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"default.TestKuduOutputFormat"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Schema
name|SCHEMA
init|=
name|getAllTypesSchema
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Configuration
name|BASE_CONF
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Properties
name|TBL_PROPS
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|NOW_MS
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|KuduTestHarness
name|harness
init|=
operator|new
name|KuduTestHarness
argument_list|()
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
comment|// Set the base configuration values.
name|BASE_CONF
operator|.
name|set
argument_list|(
name|KUDU_MASTER_ADDRS_KEY
argument_list|,
name|harness
operator|.
name|getMasterAddressesAsString
argument_list|()
argument_list|)
expr_stmt|;
name|TBL_PROPS
operator|.
name|setProperty
argument_list|(
name|KUDU_TABLE_NAME_KEY
argument_list|,
name|TABLE_NAME
argument_list|)
expr_stmt|;
comment|// Create the test Kudu table.
name|CreateTableOptions
name|options
init|=
operator|new
name|CreateTableOptions
argument_list|()
operator|.
name|setRangePartitionColumns
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
literal|"key"
argument_list|)
argument_list|)
decl_stmt|;
name|harness
operator|.
name|getClient
argument_list|()
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|SCHEMA
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGoodRow
parameter_list|()
throws|throws
name|Exception
block|{
name|KuduOutputFormat
name|outputFormat
init|=
operator|new
name|KuduOutputFormat
argument_list|()
decl_stmt|;
name|KuduRecordWriter
name|writer
init|=
operator|(
name|KuduRecordWriter
operator|)
name|outputFormat
operator|.
name|getHiveRecordWriter
argument_list|(
operator|new
name|JobConf
argument_list|(
name|BASE_CONF
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
name|TBL_PROPS
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// Write a good row.
try|try
block|{
name|PartialRow
name|row
init|=
name|SCHEMA
operator|.
name|newPartialRow
argument_list|()
decl_stmt|;
name|row
operator|.
name|addByte
argument_list|(
literal|"key"
argument_list|,
operator|(
name|byte
operator|)
literal|1
argument_list|)
expr_stmt|;
name|row
operator|.
name|addShort
argument_list|(
literal|"int16"
argument_list|,
operator|(
name|short
operator|)
literal|1
argument_list|)
expr_stmt|;
name|row
operator|.
name|addInt
argument_list|(
literal|"int32"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|row
operator|.
name|addLong
argument_list|(
literal|"int64"
argument_list|,
literal|1L
argument_list|)
expr_stmt|;
name|row
operator|.
name|addBoolean
argument_list|(
literal|"bool"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|row
operator|.
name|addFloat
argument_list|(
literal|"float"
argument_list|,
literal|1.1f
argument_list|)
expr_stmt|;
name|row
operator|.
name|addDouble
argument_list|(
literal|"double"
argument_list|,
literal|1.1d
argument_list|)
expr_stmt|;
name|row
operator|.
name|addString
argument_list|(
literal|"string"
argument_list|,
literal|"one"
argument_list|)
expr_stmt|;
name|row
operator|.
name|addBinary
argument_list|(
literal|"binary"
argument_list|,
literal|"one"
operator|.
name|getBytes
argument_list|(
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|row
operator|.
name|addTimestamp
argument_list|(
literal|"timestamp"
argument_list|,
operator|new
name|Timestamp
argument_list|(
name|NOW_MS
argument_list|)
argument_list|)
expr_stmt|;
name|row
operator|.
name|addDecimal
argument_list|(
literal|"decimal"
argument_list|,
operator|new
name|BigDecimal
argument_list|(
literal|"1.111"
argument_list|)
argument_list|)
expr_stmt|;
name|row
operator|.
name|setNull
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
comment|// Not setting the "default" column.
name|KuduWritable
name|writable
init|=
operator|new
name|KuduWritable
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|writer
operator|.
name|write
argument_list|(
name|writable
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|writer
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
comment|// Verify the written row.
name|KuduClient
name|client
init|=
name|harness
operator|.
name|getClient
argument_list|()
decl_stmt|;
name|KuduTable
name|table
init|=
name|client
operator|.
name|openTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|KuduScanner
name|scanner
init|=
name|client
operator|.
name|newScannerBuilder
argument_list|(
name|table
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RowResult
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|RowResult
name|result
range|:
name|scanner
control|)
block|{
name|results
operator|.
name|add
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|RowResult
name|result
init|=
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
name|result
operator|.
name|getByte
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|short
operator|)
literal|1
argument_list|,
name|result
operator|.
name|getShort
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|getInt
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|result
operator|.
name|getLong
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getBoolean
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1.1f
argument_list|,
name|result
operator|.
name|getFloat
argument_list|(
literal|5
argument_list|)
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1.1d
argument_list|,
name|result
operator|.
name|getDouble
argument_list|(
literal|6
argument_list|)
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"one"
argument_list|,
name|result
operator|.
name|getString
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"one"
argument_list|,
operator|new
name|String
argument_list|(
name|result
operator|.
name|getBinaryCopy
argument_list|(
literal|8
argument_list|)
argument_list|,
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NOW_MS
argument_list|,
name|result
operator|.
name|getTimestamp
argument_list|(
literal|9
argument_list|)
operator|.
name|getTime
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|"1.111"
argument_list|)
argument_list|,
name|result
operator|.
name|getDecimal
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|isNull
argument_list|(
literal|11
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|getInt
argument_list|(
literal|12
argument_list|)
argument_list|)
expr_stmt|;
comment|// default.
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBadRow
parameter_list|()
throws|throws
name|Exception
block|{
name|KuduOutputFormat
name|outputFormat
init|=
operator|new
name|KuduOutputFormat
argument_list|()
decl_stmt|;
name|KuduRecordWriter
name|writer
init|=
operator|(
name|KuduRecordWriter
operator|)
name|outputFormat
operator|.
name|getHiveRecordWriter
argument_list|(
operator|new
name|JobConf
argument_list|(
name|BASE_CONF
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
name|TBL_PROPS
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// Write an empty row.
try|try
block|{
name|PartialRow
name|row
init|=
name|SCHEMA
operator|.
name|newPartialRow
argument_list|()
decl_stmt|;
name|KuduWritable
name|writable
init|=
operator|new
name|KuduWritable
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|writer
operator|.
name|write
argument_list|(
name|writable
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KuduException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Primary key column key is not set"
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|writer
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMissingTable
parameter_list|()
throws|throws
name|Exception
block|{
name|KuduOutputFormat
name|outputFormat
init|=
operator|new
name|KuduOutputFormat
argument_list|()
decl_stmt|;
name|Properties
name|tblProps
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
try|try
block|{
name|outputFormat
operator|.
name|getHiveRecordWriter
argument_list|(
operator|new
name|JobConf
argument_list|(
name|BASE_CONF
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
name|tblProps
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should fail on missing table"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"kudu.table_name is not set"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBadTable
parameter_list|()
throws|throws
name|Exception
block|{
name|KuduOutputFormat
name|outputFormat
init|=
operator|new
name|KuduOutputFormat
argument_list|()
decl_stmt|;
name|Properties
name|tblProps
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|tblProps
operator|.
name|setProperty
argument_list|(
name|KUDU_TABLE_NAME_KEY
argument_list|,
literal|"default.notatable"
argument_list|)
expr_stmt|;
try|try
block|{
name|outputFormat
operator|.
name|getHiveRecordWriter
argument_list|(
operator|new
name|JobConf
argument_list|(
name|BASE_CONF
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
name|tblProps
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should fail on a bad table"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Kudu table does not exist: default.notatable"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

