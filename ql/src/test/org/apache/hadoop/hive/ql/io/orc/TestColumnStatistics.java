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
name|io
operator|.
name|orc
package|;
end_package

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|Assert
operator|.
name|assertEquals
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
name|FileOutputStream
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
name|List
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
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|io
operator|.
name|DateWritable
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
name|hadoop
operator|.
name|io
operator|.
name|BytesWritable
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
name|Text
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
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestName
import|;
end_import

begin_comment
comment|/**  * Test ColumnStatisticsImpl for ORC.  */
end_comment

begin_class
specifier|public
class|class
name|TestColumnStatistics
block|{
annotation|@
name|Test
specifier|public
name|void
name|testLongMerge
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectInspector
name|inspector
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|javaIntObjectInspector
decl_stmt|;
name|ColumnStatisticsImpl
name|stats1
init|=
name|ColumnStatisticsImpl
operator|.
name|create
argument_list|(
name|inspector
argument_list|)
decl_stmt|;
name|ColumnStatisticsImpl
name|stats2
init|=
name|ColumnStatisticsImpl
operator|.
name|create
argument_list|(
name|inspector
argument_list|)
decl_stmt|;
name|stats1
operator|.
name|updateInteger
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|updateInteger
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|stats2
operator|.
name|updateInteger
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|stats2
operator|.
name|updateInteger
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|merge
argument_list|(
name|stats2
argument_list|)
expr_stmt|;
name|IntegerColumnStatistics
name|typed
init|=
operator|(
name|IntegerColumnStatistics
operator|)
name|stats1
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|typed
operator|.
name|getMinimum
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|typed
operator|.
name|getMaximum
argument_list|()
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|reset
argument_list|()
expr_stmt|;
name|stats1
operator|.
name|updateInteger
argument_list|(
operator|-
literal|10
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|updateInteger
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|merge
argument_list|(
name|stats2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|10
argument_list|,
name|typed
operator|.
name|getMinimum
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10000
argument_list|,
name|typed
operator|.
name|getMaximum
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDoubleMerge
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectInspector
name|inspector
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|javaDoubleObjectInspector
decl_stmt|;
name|ColumnStatisticsImpl
name|stats1
init|=
name|ColumnStatisticsImpl
operator|.
name|create
argument_list|(
name|inspector
argument_list|)
decl_stmt|;
name|ColumnStatisticsImpl
name|stats2
init|=
name|ColumnStatisticsImpl
operator|.
name|create
argument_list|(
name|inspector
argument_list|)
decl_stmt|;
name|stats1
operator|.
name|updateDouble
argument_list|(
literal|10.0
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|updateDouble
argument_list|(
literal|100.0
argument_list|)
expr_stmt|;
name|stats2
operator|.
name|updateDouble
argument_list|(
literal|1.0
argument_list|)
expr_stmt|;
name|stats2
operator|.
name|updateDouble
argument_list|(
literal|1000.0
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|merge
argument_list|(
name|stats2
argument_list|)
expr_stmt|;
name|DoubleColumnStatistics
name|typed
init|=
operator|(
name|DoubleColumnStatistics
operator|)
name|stats1
decl_stmt|;
name|assertEquals
argument_list|(
literal|1.0
argument_list|,
name|typed
operator|.
name|getMinimum
argument_list|()
argument_list|,
literal|0.001
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1000.0
argument_list|,
name|typed
operator|.
name|getMaximum
argument_list|()
argument_list|,
literal|0.001
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|reset
argument_list|()
expr_stmt|;
name|stats1
operator|.
name|updateDouble
argument_list|(
operator|-
literal|10
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|updateDouble
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|merge
argument_list|(
name|stats2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|10
argument_list|,
name|typed
operator|.
name|getMinimum
argument_list|()
argument_list|,
literal|0.001
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10000
argument_list|,
name|typed
operator|.
name|getMaximum
argument_list|()
argument_list|,
literal|0.001
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStringMerge
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectInspector
name|inspector
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
decl_stmt|;
name|ColumnStatisticsImpl
name|stats1
init|=
name|ColumnStatisticsImpl
operator|.
name|create
argument_list|(
name|inspector
argument_list|)
decl_stmt|;
name|ColumnStatisticsImpl
name|stats2
init|=
name|ColumnStatisticsImpl
operator|.
name|create
argument_list|(
name|inspector
argument_list|)
decl_stmt|;
name|stats1
operator|.
name|updateString
argument_list|(
operator|new
name|Text
argument_list|(
literal|"bob"
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|updateString
argument_list|(
operator|new
name|Text
argument_list|(
literal|"david"
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|updateString
argument_list|(
operator|new
name|Text
argument_list|(
literal|"charles"
argument_list|)
argument_list|)
expr_stmt|;
name|stats2
operator|.
name|updateString
argument_list|(
operator|new
name|Text
argument_list|(
literal|"anne"
argument_list|)
argument_list|)
expr_stmt|;
name|stats2
operator|.
name|updateString
argument_list|(
operator|new
name|Text
argument_list|(
literal|"erin"
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|merge
argument_list|(
name|stats2
argument_list|)
expr_stmt|;
name|StringColumnStatistics
name|typed
init|=
operator|(
name|StringColumnStatistics
operator|)
name|stats1
decl_stmt|;
name|assertEquals
argument_list|(
literal|"anne"
argument_list|,
name|typed
operator|.
name|getMinimum
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"erin"
argument_list|,
name|typed
operator|.
name|getMaximum
argument_list|()
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|reset
argument_list|()
expr_stmt|;
name|stats1
operator|.
name|updateString
argument_list|(
operator|new
name|Text
argument_list|(
literal|"aaa"
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|updateString
argument_list|(
operator|new
name|Text
argument_list|(
literal|"zzz"
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|merge
argument_list|(
name|stats2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"aaa"
argument_list|,
name|typed
operator|.
name|getMinimum
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"zzz"
argument_list|,
name|typed
operator|.
name|getMaximum
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDateMerge
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectInspector
name|inspector
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|javaDateObjectInspector
decl_stmt|;
name|ColumnStatisticsImpl
name|stats1
init|=
name|ColumnStatisticsImpl
operator|.
name|create
argument_list|(
name|inspector
argument_list|)
decl_stmt|;
name|ColumnStatisticsImpl
name|stats2
init|=
name|ColumnStatisticsImpl
operator|.
name|create
argument_list|(
name|inspector
argument_list|)
decl_stmt|;
name|stats1
operator|.
name|updateDate
argument_list|(
operator|new
name|DateWritable
argument_list|(
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|updateDate
argument_list|(
operator|new
name|DateWritable
argument_list|(
literal|100
argument_list|)
argument_list|)
expr_stmt|;
name|stats2
operator|.
name|updateDate
argument_list|(
operator|new
name|DateWritable
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|stats2
operator|.
name|updateDate
argument_list|(
operator|new
name|DateWritable
argument_list|(
literal|2000
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|merge
argument_list|(
name|stats2
argument_list|)
expr_stmt|;
name|DateColumnStatistics
name|typed
init|=
operator|(
name|DateColumnStatistics
operator|)
name|stats1
decl_stmt|;
name|assertEquals
argument_list|(
operator|new
name|DateWritable
argument_list|(
literal|10
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|typed
operator|.
name|getMinimum
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|DateWritable
argument_list|(
literal|2000
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|typed
operator|.
name|getMaximum
argument_list|()
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|reset
argument_list|()
expr_stmt|;
name|stats1
operator|.
name|updateDate
argument_list|(
operator|new
name|DateWritable
argument_list|(
operator|-
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|updateDate
argument_list|(
operator|new
name|DateWritable
argument_list|(
literal|10000
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|merge
argument_list|(
name|stats2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|DateWritable
argument_list|(
operator|-
literal|10
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|typed
operator|.
name|getMinimum
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|DateWritable
argument_list|(
literal|10000
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|typed
operator|.
name|getMaximum
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTimestampMerge
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectInspector
name|inspector
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|javaTimestampObjectInspector
decl_stmt|;
name|ColumnStatisticsImpl
name|stats1
init|=
name|ColumnStatisticsImpl
operator|.
name|create
argument_list|(
name|inspector
argument_list|)
decl_stmt|;
name|ColumnStatisticsImpl
name|stats2
init|=
name|ColumnStatisticsImpl
operator|.
name|create
argument_list|(
name|inspector
argument_list|)
decl_stmt|;
name|stats1
operator|.
name|updateTimestamp
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|updateTimestamp
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|100
argument_list|)
argument_list|)
expr_stmt|;
name|stats2
operator|.
name|updateTimestamp
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|stats2
operator|.
name|updateTimestamp
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|merge
argument_list|(
name|stats2
argument_list|)
expr_stmt|;
name|TimestampColumnStatistics
name|typed
init|=
operator|(
name|TimestampColumnStatistics
operator|)
name|stats1
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|typed
operator|.
name|getMinimum
argument_list|()
operator|.
name|getTime
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|typed
operator|.
name|getMaximum
argument_list|()
operator|.
name|getTime
argument_list|()
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|reset
argument_list|()
expr_stmt|;
name|stats1
operator|.
name|updateTimestamp
argument_list|(
operator|new
name|Timestamp
argument_list|(
operator|-
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|updateTimestamp
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|10000
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|merge
argument_list|(
name|stats2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|10
argument_list|,
name|typed
operator|.
name|getMinimum
argument_list|()
operator|.
name|getTime
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10000
argument_list|,
name|typed
operator|.
name|getMaximum
argument_list|()
operator|.
name|getTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalMerge
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectInspector
name|inspector
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|javaHiveDecimalObjectInspector
decl_stmt|;
name|ColumnStatisticsImpl
name|stats1
init|=
name|ColumnStatisticsImpl
operator|.
name|create
argument_list|(
name|inspector
argument_list|)
decl_stmt|;
name|ColumnStatisticsImpl
name|stats2
init|=
name|ColumnStatisticsImpl
operator|.
name|create
argument_list|(
name|inspector
argument_list|)
decl_stmt|;
name|stats1
operator|.
name|updateDecimal
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|updateDecimal
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|100
argument_list|)
argument_list|)
expr_stmt|;
name|stats2
operator|.
name|updateDecimal
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|stats2
operator|.
name|updateDecimal
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|merge
argument_list|(
name|stats2
argument_list|)
expr_stmt|;
name|DecimalColumnStatistics
name|typed
init|=
operator|(
name|DecimalColumnStatistics
operator|)
name|stats1
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|typed
operator|.
name|getMinimum
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|typed
operator|.
name|getMaximum
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|reset
argument_list|()
expr_stmt|;
name|stats1
operator|.
name|updateDecimal
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|-
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|updateDecimal
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|10000
argument_list|)
argument_list|)
expr_stmt|;
name|stats1
operator|.
name|merge
argument_list|(
name|stats2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|10
argument_list|,
name|typed
operator|.
name|getMinimum
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10000
argument_list|,
name|typed
operator|.
name|getMaximum
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|SimpleStruct
block|{
name|BytesWritable
name|bytes1
decl_stmt|;
name|Text
name|string1
decl_stmt|;
name|SimpleStruct
parameter_list|(
name|BytesWritable
name|b1
parameter_list|,
name|String
name|s1
parameter_list|)
block|{
name|this
operator|.
name|bytes1
operator|=
name|b1
expr_stmt|;
if|if
condition|(
name|s1
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|string1
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|string1
operator|=
operator|new
name|Text
argument_list|(
name|s1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|Path
name|workDir
init|=
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|,
literal|"target"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"test"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|Configuration
name|conf
decl_stmt|;
name|FileSystem
name|fs
decl_stmt|;
name|Path
name|testFilePath
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|testCaseName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|openFileSystem
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|fs
operator|.
name|setWorkingDirectory
argument_list|(
name|workDir
argument_list|)
expr_stmt|;
name|testFilePath
operator|=
operator|new
name|Path
argument_list|(
literal|"TestOrcFile."
operator|+
name|testCaseName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|".orc"
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|testFilePath
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|BytesWritable
name|bytes
parameter_list|(
name|int
modifier|...
name|items
parameter_list|)
block|{
name|BytesWritable
name|result
init|=
operator|new
name|BytesWritable
argument_list|()
decl_stmt|;
name|result
operator|.
name|setSize
argument_list|(
name|items
operator|.
name|length
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
name|items
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|result
operator|.
name|getBytes
argument_list|()
index|[
name|i
index|]
operator|=
operator|(
name|byte
operator|)
name|items
index|[
name|i
index|]
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHasNull
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectInspector
name|inspector
decl_stmt|;
synchronized|synchronized
init|(
name|TestOrcFile
operator|.
name|class
init|)
block|{
name|inspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|SimpleStruct
operator|.
name|class
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
expr_stmt|;
block|}
name|Writer
name|writer
init|=
name|OrcFile
operator|.
name|createWriter
argument_list|(
name|testFilePath
argument_list|,
name|OrcFile
operator|.
name|writerOptions
argument_list|(
name|conf
argument_list|)
operator|.
name|inspector
argument_list|(
name|inspector
argument_list|)
operator|.
name|rowIndexStride
argument_list|(
literal|1000
argument_list|)
operator|.
name|stripeSize
argument_list|(
literal|10000
argument_list|)
operator|.
name|bufferSize
argument_list|(
literal|10000
argument_list|)
argument_list|)
decl_stmt|;
comment|// STRIPE 1
comment|// RG1
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|writer
operator|.
name|addRow
argument_list|(
operator|new
name|SimpleStruct
argument_list|(
name|bytes
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
argument_list|,
literal|"RG1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// RG2
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|writer
operator|.
name|addRow
argument_list|(
operator|new
name|SimpleStruct
argument_list|(
name|bytes
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// RG3
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|writer
operator|.
name|addRow
argument_list|(
operator|new
name|SimpleStruct
argument_list|(
name|bytes
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
argument_list|,
literal|"RG3"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// RG4
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|writer
operator|.
name|addRow
argument_list|(
operator|new
name|SimpleStruct
argument_list|(
name|bytes
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// RG5
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|writer
operator|.
name|addRow
argument_list|(
operator|new
name|SimpleStruct
argument_list|(
name|bytes
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// STRIPE 2
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5000
condition|;
name|i
operator|++
control|)
block|{
name|writer
operator|.
name|addRow
argument_list|(
operator|new
name|SimpleStruct
argument_list|(
name|bytes
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// STRIPE 3
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5000
condition|;
name|i
operator|++
control|)
block|{
name|writer
operator|.
name|addRow
argument_list|(
operator|new
name|SimpleStruct
argument_list|(
name|bytes
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
argument_list|,
literal|"STRIPE-3"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// STRIPE 4
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5000
condition|;
name|i
operator|++
control|)
block|{
name|writer
operator|.
name|addRow
argument_list|(
operator|new
name|SimpleStruct
argument_list|(
name|bytes
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|Reader
name|reader
init|=
name|OrcFile
operator|.
name|createReader
argument_list|(
name|testFilePath
argument_list|,
name|OrcFile
operator|.
name|readerOptions
argument_list|(
name|conf
argument_list|)
operator|.
name|filesystem
argument_list|(
name|fs
argument_list|)
argument_list|)
decl_stmt|;
comment|// check the file level stats
name|ColumnStatistics
index|[]
name|stats
init|=
name|reader
operator|.
name|getStatistics
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|20000
argument_list|,
name|stats
index|[
literal|0
index|]
operator|.
name|getNumberOfValues
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20000
argument_list|,
name|stats
index|[
literal|1
index|]
operator|.
name|getNumberOfValues
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|7000
argument_list|,
name|stats
index|[
literal|2
index|]
operator|.
name|getNumberOfValues
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|stats
index|[
literal|0
index|]
operator|.
name|hasNull
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|stats
index|[
literal|1
index|]
operator|.
name|hasNull
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|stats
index|[
literal|2
index|]
operator|.
name|hasNull
argument_list|()
argument_list|)
expr_stmt|;
comment|// check the stripe level stats
name|List
argument_list|<
name|StripeStatistics
argument_list|>
name|stripeStats
init|=
name|reader
operator|.
name|getStripeStatistics
argument_list|()
decl_stmt|;
comment|// stripe 1 stats
name|StripeStatistics
name|ss1
init|=
name|stripeStats
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ColumnStatistics
name|ss1_cs1
init|=
name|ss1
operator|.
name|getColumnStatistics
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|ColumnStatistics
name|ss1_cs2
init|=
name|ss1
operator|.
name|getColumnStatistics
argument_list|()
index|[
literal|1
index|]
decl_stmt|;
name|ColumnStatistics
name|ss1_cs3
init|=
name|ss1
operator|.
name|getColumnStatistics
argument_list|()
index|[
literal|2
index|]
decl_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|ss1_cs1
operator|.
name|hasNull
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|ss1_cs2
operator|.
name|hasNull
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|ss1_cs3
operator|.
name|hasNull
argument_list|()
argument_list|)
expr_stmt|;
comment|// stripe 2 stats
name|StripeStatistics
name|ss2
init|=
name|stripeStats
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ColumnStatistics
name|ss2_cs1
init|=
name|ss2
operator|.
name|getColumnStatistics
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|ColumnStatistics
name|ss2_cs2
init|=
name|ss2
operator|.
name|getColumnStatistics
argument_list|()
index|[
literal|1
index|]
decl_stmt|;
name|ColumnStatistics
name|ss2_cs3
init|=
name|ss2
operator|.
name|getColumnStatistics
argument_list|()
index|[
literal|2
index|]
decl_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|ss2_cs1
operator|.
name|hasNull
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|ss2_cs2
operator|.
name|hasNull
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|ss2_cs3
operator|.
name|hasNull
argument_list|()
argument_list|)
expr_stmt|;
comment|// stripe 3 stats
name|StripeStatistics
name|ss3
init|=
name|stripeStats
operator|.
name|get
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|ColumnStatistics
name|ss3_cs1
init|=
name|ss3
operator|.
name|getColumnStatistics
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|ColumnStatistics
name|ss3_cs2
init|=
name|ss3
operator|.
name|getColumnStatistics
argument_list|()
index|[
literal|1
index|]
decl_stmt|;
name|ColumnStatistics
name|ss3_cs3
init|=
name|ss3
operator|.
name|getColumnStatistics
argument_list|()
index|[
literal|2
index|]
decl_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|ss3_cs1
operator|.
name|hasNull
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|ss3_cs2
operator|.
name|hasNull
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|ss3_cs3
operator|.
name|hasNull
argument_list|()
argument_list|)
expr_stmt|;
comment|// stripe 4 stats
name|StripeStatistics
name|ss4
init|=
name|stripeStats
operator|.
name|get
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|ColumnStatistics
name|ss4_cs1
init|=
name|ss4
operator|.
name|getColumnStatistics
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|ColumnStatistics
name|ss4_cs2
init|=
name|ss4
operator|.
name|getColumnStatistics
argument_list|()
index|[
literal|1
index|]
decl_stmt|;
name|ColumnStatistics
name|ss4_cs3
init|=
name|ss4
operator|.
name|getColumnStatistics
argument_list|()
index|[
literal|2
index|]
decl_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|ss4_cs1
operator|.
name|hasNull
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|ss4_cs2
operator|.
name|hasNull
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|ss4_cs3
operator|.
name|hasNull
argument_list|()
argument_list|)
expr_stmt|;
comment|// Test file dump
name|PrintStream
name|origOut
init|=
name|System
operator|.
name|out
decl_stmt|;
name|String
name|outputFilename
init|=
literal|"orc-file-has-null.out"
decl_stmt|;
name|FileOutputStream
name|myOut
init|=
operator|new
name|FileOutputStream
argument_list|(
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|outputFilename
argument_list|)
decl_stmt|;
comment|// replace stdout and run command
name|System
operator|.
name|setOut
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|myOut
argument_list|)
argument_list|)
expr_stmt|;
name|FileDump
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
name|testFilePath
operator|.
name|toString
argument_list|()
block|,
literal|"--rowindex=2"
block|}
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
name|System
operator|.
name|setOut
argument_list|(
name|origOut
argument_list|)
expr_stmt|;
name|TestFileDump
operator|.
name|checkOutput
argument_list|(
name|outputFilename
argument_list|,
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|outputFilename
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

