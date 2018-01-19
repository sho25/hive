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
comment|/**  * UDFAscii.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"ascii"
argument_list|,
name|value
operator|=
literal|"_FUNC_(str) - returns the numeric value of the first character"
operator|+
literal|" of str"
argument_list|,
name|extended
operator|=
literal|"Returns 0 if str is empty or NULL if str is NULL\n"
operator|+
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_('222') FROM src LIMIT 1;"
operator|+
literal|"  50\n"
operator|+
literal|"> SELECT _FUNC_(2) FROM src LIMIT 1;\n"
operator|+
literal|"  50"
argument_list|)
specifier|public
class|class
name|UDFAscii
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
parameter_list|)
block|{
if|if
condition|(
name|s
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|s
operator|.
name|getLength
argument_list|()
operator|>
literal|0
condition|)
block|{
name|result
operator|.
name|set
argument_list|(
name|s
operator|.
name|getBytes
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
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
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

