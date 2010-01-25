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
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"lpad"
argument_list|,
name|value
operator|=
literal|"_FUNC_(str, len, pad) - Returns str, left-padded with pad to a "
operator|+
literal|"length of len"
argument_list|,
name|extended
operator|=
literal|"If str is longer than len, the return value is shortened to "
operator|+
literal|"len characters.\n"
operator|+
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_('hi', 5, '??') FROM src LIMIT 1;\n"
operator|+
literal|"  '???hi'"
operator|+
literal|"> SELECT _FUNC_('hi', 1, '??') FROM src LIMIT 1;\n"
operator|+
literal|"  'h'"
argument_list|)
specifier|public
class|class
name|UDFLpad
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
parameter_list|,
name|Text
name|pad
parameter_list|)
block|{
if|if
condition|(
name|s
operator|==
literal|null
operator|||
name|n
operator|==
literal|null
operator|||
name|pad
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
decl_stmt|;
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
name|byte
index|[]
name|txt
init|=
name|s
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|byte
index|[]
name|padTxt
init|=
name|pad
operator|.
name|getBytes
argument_list|()
decl_stmt|;
comment|// The length of the padding needed
name|int
name|pos
init|=
name|Math
operator|.
name|max
argument_list|(
name|len
operator|-
name|s
operator|.
name|getLength
argument_list|()
argument_list|,
literal|0
argument_list|)
decl_stmt|;
comment|// Copy the padding
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|pos
condition|;
name|i
operator|+=
name|pad
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
name|pad
operator|.
name|getLength
argument_list|()
operator|&&
name|j
operator|<
name|pos
operator|-
name|i
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
name|padTxt
index|[
name|j
index|]
expr_stmt|;
block|}
block|}
comment|// Copy the text
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|pos
operator|+
name|i
operator|<
name|len
operator|&&
name|i
operator|<
name|s
operator|.
name|getLength
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|data
index|[
name|pos
operator|+
name|i
index|]
operator|=
name|txt
index|[
name|i
index|]
expr_stmt|;
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

