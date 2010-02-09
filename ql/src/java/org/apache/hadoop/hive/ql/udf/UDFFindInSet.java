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
comment|/**  * UDFFindInSet.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"find_in_set"
argument_list|,
name|value
operator|=
literal|"_FUNC_(str,str_array) - Returns the first occurrence "
operator|+
literal|" of str in str_array where str_array is a comma-delimited string."
operator|+
literal|" Returns null if either argument is null."
operator|+
literal|" Returns 0 if the first argument has any commas."
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_('ab','abc,b,ab,c,def') FROM src LIMIT 1;\n"
operator|+
literal|"  3\n"
operator|+
literal|"> SELECT * FROM src1 WHERE NOT _FUNC_(key,'311,128,345,956')=0;\n"
operator|+
literal|"  311  val_311\n"
operator|+
literal|"  128"
argument_list|)
specifier|public
class|class
name|UDFFindInSet
extends|extends
name|UDF
block|{
specifier|private
specifier|final
name|IntWritable
name|result
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
specifier|public
name|IntWritable
name|evaluate
parameter_list|(
name|Text
name|s
parameter_list|,
name|Text
name|txtarray
parameter_list|)
block|{
if|if
condition|(
name|s
operator|==
literal|null
operator|||
name|txtarray
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|byte
index|[]
name|search_bytes
init|=
name|s
operator|.
name|getBytes
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
name|s
operator|.
name|getLength
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|search_bytes
index|[
name|i
index|]
operator|==
literal|','
condition|)
block|{
name|result
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
name|byte
index|[]
name|data
init|=
name|txtarray
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|int
name|search_length
init|=
name|s
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|int
name|cur_pos_in_array
init|=
literal|0
decl_stmt|;
name|int
name|cur_length
init|=
literal|0
decl_stmt|;
name|boolean
name|matching
init|=
literal|true
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
name|txtarray
operator|.
name|getLength
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|data
index|[
name|i
index|]
operator|==
literal|','
condition|)
block|{
name|cur_pos_in_array
operator|++
expr_stmt|;
if|if
condition|(
name|matching
operator|&&
name|cur_length
operator|==
name|search_length
condition|)
block|{
name|result
operator|.
name|set
argument_list|(
name|cur_pos_in_array
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
else|else
block|{
name|matching
operator|=
literal|true
expr_stmt|;
name|cur_length
operator|=
literal|0
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|cur_length
operator|+
literal|1
operator|<=
name|search_length
condition|)
block|{
if|if
condition|(
operator|!
name|matching
operator|||
name|search_bytes
index|[
name|cur_length
index|]
operator|!=
name|data
index|[
name|i
index|]
condition|)
block|{
name|matching
operator|=
literal|false
expr_stmt|;
block|}
block|}
else|else
block|{
name|matching
operator|=
literal|false
expr_stmt|;
block|}
name|cur_length
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
name|matching
operator|&&
name|cur_length
operator|==
name|search_length
condition|)
block|{
name|cur_pos_in_array
operator|++
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
name|cur_pos_in_array
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
else|else
block|{
name|result
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
block|}
end_class

end_unit

