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
name|SerDeException
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
name|objectinspector
operator|.
name|ObjectInspector
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
name|objectinspector
operator|.
name|ObjectInspectorFactory
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
name|objectinspector
operator|.
name|StandardStructObjectInspector
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
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|Type
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
name|assertArrayEquals
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
name|fail
import|;
end_import

begin_comment
comment|/**  * Tests for the KuduSerDe implementation.  */
end_comment

begin_class
specifier|public
class|class
name|TestKuduSerDe
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"default.TestKuduSerDe"
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
name|testSerDeRoundTrip
parameter_list|()
throws|throws
name|Exception
block|{
name|KuduSerDe
name|serDe
init|=
operator|new
name|KuduSerDe
argument_list|()
decl_stmt|;
name|serDe
operator|.
name|initialize
argument_list|(
name|BASE_CONF
argument_list|,
name|TBL_PROPS
argument_list|)
expr_stmt|;
name|PartialRow
name|before
init|=
name|SCHEMA
operator|.
name|newPartialRow
argument_list|()
decl_stmt|;
name|before
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
name|before
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
name|before
operator|.
name|addInt
argument_list|(
literal|"int32"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|before
operator|.
name|addLong
argument_list|(
literal|"int64"
argument_list|,
literal|1L
argument_list|)
expr_stmt|;
name|before
operator|.
name|addBoolean
argument_list|(
literal|"bool"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|before
operator|.
name|addFloat
argument_list|(
literal|"float"
argument_list|,
literal|1.1f
argument_list|)
expr_stmt|;
name|before
operator|.
name|addDouble
argument_list|(
literal|"double"
argument_list|,
literal|1.1d
argument_list|)
expr_stmt|;
name|before
operator|.
name|addString
argument_list|(
literal|"string"
argument_list|,
literal|"one"
argument_list|)
expr_stmt|;
name|before
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
name|before
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
name|before
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
name|before
operator|.
name|setNull
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
name|before
operator|.
name|addInt
argument_list|(
literal|"default"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|KuduWritable
name|beforeWritable
init|=
operator|new
name|KuduWritable
argument_list|(
name|before
argument_list|)
decl_stmt|;
name|Object
name|object
init|=
name|serDe
operator|.
name|deserialize
argument_list|(
name|beforeWritable
argument_list|)
decl_stmt|;
comment|// Capitalized `key` field to check for field case insensitivity.
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"KEY"
argument_list|,
literal|"int16"
argument_list|,
literal|"int32"
argument_list|,
literal|"int64"
argument_list|,
literal|"bool"
argument_list|,
literal|"float"
argument_list|,
literal|"double"
argument_list|,
literal|"string"
argument_list|,
literal|"binary"
argument_list|,
literal|"timestamp"
argument_list|,
literal|"decimal"
argument_list|,
literal|"null"
argument_list|,
literal|"default"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|ois
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableByteObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableShortObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableLongObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableFloatObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBinaryObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableTimestampObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableHiveDecimalObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
comment|// the "default" column is not set.
argument_list|)
decl_stmt|;
name|StandardStructObjectInspector
name|objectInspector
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|fieldNames
argument_list|,
name|ois
argument_list|)
decl_stmt|;
name|KuduWritable
name|afterWritable
init|=
name|serDe
operator|.
name|serialize
argument_list|(
name|object
argument_list|,
name|objectInspector
argument_list|)
decl_stmt|;
name|PartialRow
name|after
init|=
name|afterWritable
operator|.
name|getPartialRow
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|SCHEMA
operator|.
name|getColumnCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|SCHEMA
operator|.
name|getColumnByIndex
argument_list|(
name|i
argument_list|)
operator|.
name|getType
argument_list|()
operator|==
name|Type
operator|.
name|BINARY
condition|)
block|{
name|assertArrayEquals
argument_list|(
literal|"Columns not equal at index: "
operator|+
name|i
argument_list|,
name|before
operator|.
name|getBinaryCopy
argument_list|(
name|i
argument_list|)
argument_list|,
name|after
operator|.
name|getBinaryCopy
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|"Columns not equal at index: "
operator|+
name|i
argument_list|,
name|before
operator|.
name|getObject
argument_list|(
name|i
argument_list|)
argument_list|,
name|after
operator|.
name|getObject
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMissingMasters
parameter_list|()
throws|throws
name|Exception
block|{
name|KuduSerDe
name|serDe
init|=
operator|new
name|KuduSerDe
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|BASE_CONF
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_KUDU_MASTER_ADDRESSES_DEFAULT
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|conf
operator|.
name|unset
argument_list|(
name|KUDU_MASTER_ADDRS_KEY
argument_list|)
expr_stmt|;
try|try
block|{
name|serDe
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|TBL_PROPS
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
name|SerDeException
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
literal|"Kudu master addresses are not specified in the table property"
argument_list|)
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
name|KuduSerDe
name|serDe
init|=
operator|new
name|KuduSerDe
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
name|serDe
operator|.
name|initialize
argument_list|(
name|BASE_CONF
argument_list|,
name|tblProps
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
name|SerDeException
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
name|KuduSerDe
name|serDe
init|=
operator|new
name|KuduSerDe
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
name|serDe
operator|.
name|initialize
argument_list|(
name|BASE_CONF
argument_list|,
name|tblProps
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
name|SerDeException
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

