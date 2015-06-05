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
name|udf
operator|.
name|generic
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDF
operator|.
name|DeferredJavaObject
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDF
operator|.
name|DeferredObject
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
name|io
operator|.
name|TimestampWritable
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
name|IntWritable
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

begin_class
specifier|public
class|class
name|TestGenericUDFQuarter
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testQuarterStr
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFQuarter
name|udf
init|=
operator|new
name|GenericUDFQuarter
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
name|ObjectInspector
index|[]
name|arguments
init|=
block|{
name|valueOI0
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
comment|// date str
name|runAndVerifyStr
argument_list|(
literal|"2014-01-10"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2014-02-10"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2014-03-31"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2014-04-02"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2014-05-28"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2016-06-03"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2016-07-28"
argument_list|,
literal|3
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2016-08-29"
argument_list|,
literal|3
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2016-09-29"
argument_list|,
literal|3
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2016-10-29"
argument_list|,
literal|4
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2016-11-29"
argument_list|,
literal|4
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2016-12-29"
argument_list|,
literal|4
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// wrong date str
name|runAndVerifyStr
argument_list|(
literal|"2016-03-35"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2014-01-32"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"01/14/2014"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// negative Unix time
name|runAndVerifyStr
argument_list|(
literal|"1966-01-01"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"1966-03-31"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"1966-04-01"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"1966-12-31"
argument_list|,
literal|4
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// ts str
name|runAndVerifyStr
argument_list|(
literal|"2014-01-01 00:00:00"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2014-02-10 15:23:00"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2014-03-31 15:23:00"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2014-04-02 15:23:00"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2014-05-28 15:23:00"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2016-06-03 15:23:00"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2016-07-28 15:23:00"
argument_list|,
literal|3
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2016-08-29 15:23:00"
argument_list|,
literal|3
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2016-09-29 15:23:00"
argument_list|,
literal|3
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2016-10-29 15:23:00"
argument_list|,
literal|4
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2016-11-29 15:23:00"
argument_list|,
literal|4
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2016-12-31 23:59:59.999"
argument_list|,
literal|4
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// wrong date str
name|runAndVerifyStr
argument_list|(
literal|"2016-03-35 15:23:00"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"2014-01-32 15:23:00"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"01/14/2014 15:23:00"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// negative Unix time
name|runAndVerifyStr
argument_list|(
literal|"1966-01-01 00:00:00"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"1966-03-31 23:59:59.999"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"1966-04-01 00:00:00"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"1966-12-31 23:59:59.999"
argument_list|,
literal|4
argument_list|,
name|udf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testQuarterDt
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFQuarter
name|udf
init|=
operator|new
name|GenericUDFQuarter
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDateObjectInspector
decl_stmt|;
name|ObjectInspector
index|[]
name|arguments
init|=
block|{
name|valueOI0
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
comment|// positive Unix time
name|runAndVerifyDt
argument_list|(
literal|"2014-01-01"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyDt
argument_list|(
literal|"2014-02-10"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyDt
argument_list|(
literal|"2014-03-31"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyDt
argument_list|(
literal|"2014-04-02"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyDt
argument_list|(
literal|"2014-05-28"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyDt
argument_list|(
literal|"2016-06-03"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyDt
argument_list|(
literal|"2016-07-28"
argument_list|,
literal|3
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyDt
argument_list|(
literal|"2016-08-29"
argument_list|,
literal|3
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyDt
argument_list|(
literal|"2016-09-29"
argument_list|,
literal|3
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyDt
argument_list|(
literal|"2016-10-29"
argument_list|,
literal|4
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyDt
argument_list|(
literal|"2016-11-29"
argument_list|,
literal|4
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyDt
argument_list|(
literal|"2016-12-31"
argument_list|,
literal|4
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// negative Unix time
name|runAndVerifyDt
argument_list|(
literal|"1966-01-01"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyDt
argument_list|(
literal|"1966-03-31"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyDt
argument_list|(
literal|"1966-04-01"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyDt
argument_list|(
literal|"1966-12-31"
argument_list|,
literal|4
argument_list|,
name|udf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testQuarterTs
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFQuarter
name|udf
init|=
operator|new
name|GenericUDFQuarter
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableTimestampObjectInspector
decl_stmt|;
name|ObjectInspector
index|[]
name|arguments
init|=
block|{
name|valueOI0
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
comment|// positive Unix time
name|runAndVerifyTs
argument_list|(
literal|"2014-01-01 00:00:00"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyTs
argument_list|(
literal|"2014-02-10 15:23:00"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyTs
argument_list|(
literal|"2014-03-31 15:23:00"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyTs
argument_list|(
literal|"2014-04-02 15:23:00"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyTs
argument_list|(
literal|"2014-05-28 15:23:00"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyTs
argument_list|(
literal|"2016-06-03 15:23:00"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyTs
argument_list|(
literal|"2016-07-28 15:23:00"
argument_list|,
literal|3
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyTs
argument_list|(
literal|"2016-08-29 15:23:00"
argument_list|,
literal|3
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyTs
argument_list|(
literal|"2016-09-29 15:23:00"
argument_list|,
literal|3
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyTs
argument_list|(
literal|"2016-10-29 15:23:00"
argument_list|,
literal|4
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyTs
argument_list|(
literal|"2016-11-29 15:23:00"
argument_list|,
literal|4
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyTs
argument_list|(
literal|"2016-12-31 23:59:59.999"
argument_list|,
literal|4
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// negative Unix time
name|runAndVerifyTs
argument_list|(
literal|"1966-01-01 00:00:00"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyTs
argument_list|(
literal|"1966-03-31 23:59:59"
argument_list|,
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyTs
argument_list|(
literal|"1966-04-01 00:00:00"
argument_list|,
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyTs
argument_list|(
literal|"1966-12-31 23:59:59.999"
argument_list|,
literal|4
argument_list|,
name|udf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runAndVerifyStr
parameter_list|(
name|String
name|str
parameter_list|,
name|Integer
name|expResult
parameter_list|,
name|GenericUDF
name|udf
parameter_list|)
throws|throws
name|HiveException
block|{
name|DeferredObject
name|valueObj0
init|=
operator|new
name|DeferredJavaObject
argument_list|(
name|str
operator|!=
literal|null
condition|?
operator|new
name|Text
argument_list|(
name|str
argument_list|)
else|:
literal|null
argument_list|)
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
name|valueObj0
block|}
decl_stmt|;
name|IntWritable
name|output
init|=
operator|(
name|IntWritable
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|expResult
operator|==
literal|null
condition|)
block|{
name|assertNull
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNotNull
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"quarter() test "
argument_list|,
name|expResult
operator|.
name|intValue
argument_list|()
argument_list|,
name|output
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|runAndVerifyDt
parameter_list|(
name|String
name|str
parameter_list|,
name|Integer
name|expResult
parameter_list|,
name|GenericUDF
name|udf
parameter_list|)
throws|throws
name|HiveException
block|{
name|DeferredObject
name|valueObj0
init|=
operator|new
name|DeferredJavaObject
argument_list|(
name|str
operator|!=
literal|null
condition|?
operator|new
name|DateWritable
argument_list|(
name|Date
operator|.
name|valueOf
argument_list|(
name|str
argument_list|)
argument_list|)
else|:
literal|null
argument_list|)
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
name|valueObj0
block|}
decl_stmt|;
name|IntWritable
name|output
init|=
operator|(
name|IntWritable
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|expResult
operator|==
literal|null
condition|)
block|{
name|assertNull
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNotNull
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"quarter() test "
argument_list|,
name|expResult
operator|.
name|intValue
argument_list|()
argument_list|,
name|output
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|runAndVerifyTs
parameter_list|(
name|String
name|str
parameter_list|,
name|Integer
name|expResult
parameter_list|,
name|GenericUDF
name|udf
parameter_list|)
throws|throws
name|HiveException
block|{
name|DeferredObject
name|valueObj0
init|=
operator|new
name|DeferredJavaObject
argument_list|(
name|str
operator|!=
literal|null
condition|?
operator|new
name|TimestampWritable
argument_list|(
name|Timestamp
operator|.
name|valueOf
argument_list|(
name|str
argument_list|)
argument_list|)
else|:
literal|null
argument_list|)
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
name|valueObj0
block|}
decl_stmt|;
name|IntWritable
name|output
init|=
operator|(
name|IntWritable
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|expResult
operator|==
literal|null
condition|)
block|{
name|assertNull
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNotNull
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"quarter() test "
argument_list|,
name|expResult
operator|.
name|intValue
argument_list|()
argument_list|,
name|output
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

