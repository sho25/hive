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

begin_comment
comment|/**  * UDFRepeat.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"repeat"
argument_list|,
name|value
operator|=
literal|"_FUNC_(str, n) - repeat str n times "
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_('123', 2) FROM src LIMIT 1;\n"
operator|+
literal|"  '123123'"
argument_list|)
specifier|public
class|class
name|UDFRepeat
extends|extends
name|UDF
block|{
specifier|private
specifier|final
name|Text
name|result
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
specifier|public
name|Text
name|evaluate
parameter_list|(
name|Text
name|s
parameter_list|,
name|IntWritable
name|n
parameter_list|)
block|{
if|if
condition|(
name|n
operator|==
literal|null
operator|||
name|s
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|len
init|=
name|n
operator|.
name|get
argument_list|()
operator|*
name|s
operator|.
name|getLength
argument_list|()
decl_stmt|;
if|if
condition|(
name|len
operator|<
literal|0
condition|)
block|{
name|len
operator|=
literal|0
expr_stmt|;
block|}
name|byte
index|[]
name|data
init|=
name|result
operator|.
name|getBytes
argument_list|()
decl_stmt|;
if|if
condition|(
name|data
operator|.
name|length
operator|<
name|len
condition|)
block|{
name|data
operator|=
operator|new
name|byte
index|[
name|len
index|]
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|len
condition|;
name|i
operator|+=
name|s
operator|.
name|getLength
argument_list|()
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|s
operator|.
name|getLength
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|data
index|[
name|i
operator|+
name|j
index|]
operator|=
name|s
operator|.
name|getBytes
argument_list|()
index|[
name|j
index|]
expr_stmt|;
block|}
block|}
name|result
operator|.
name|set
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

