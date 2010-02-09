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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|Description
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
name|UDF
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
name|DoubleWritable
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
name|LongWritable
import|;
end_import

begin_comment
comment|/**  * UDFAbs.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"abs"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x) - returns the absolute value of x"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_(0) FROM src LIMIT 1;\n"
operator|+
literal|"  0\n"
operator|+
literal|"> SELECT _FUNC_(-5) FROM src LIMIT 1;\n"
operator|+
literal|"  5"
argument_list|)
specifier|public
class|class
name|UDFAbs
extends|extends
name|UDF
block|{
specifier|private
specifier|final
name|DoubleWritable
name|resultDouble
init|=
operator|new
name|DoubleWritable
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongWritable
name|resultLong
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|IntWritable
name|resultInt
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
specifier|public
name|DoubleWritable
name|evaluate
parameter_list|(
name|DoubleWritable
name|n
parameter_list|)
block|{
if|if
condition|(
name|n
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|resultDouble
operator|.
name|set
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
name|n
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|resultDouble
return|;
block|}
specifier|public
name|LongWritable
name|evaluate
parameter_list|(
name|LongWritable
name|n
parameter_list|)
block|{
if|if
condition|(
name|n
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|resultLong
operator|.
name|set
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
name|n
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|resultLong
return|;
block|}
specifier|public
name|IntWritable
name|evaluate
parameter_list|(
name|IntWritable
name|n
parameter_list|)
block|{
if|if
condition|(
name|n
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|resultInt
operator|.
name|set
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
name|n
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|resultInt
return|;
block|}
block|}
end_class

end_unit

