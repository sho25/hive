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
name|java
operator|.
name|util
operator|.
name|Arrays
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
comment|/**  * UDFSpace.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"space"
argument_list|,
name|value
operator|=
literal|"_FUNC_(n) - returns n spaces"
argument_list|,
name|extended
operator|=
literal|"Example:\n "
operator|+
literal|"> SELECT _FUNC_(2) FROM src LIMIT 1;\n"
operator|+
literal|"  '  '"
argument_list|)
specifier|public
class|class
name|UDFSpace
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
name|int
name|len
init|=
name|n
operator|.
name|get
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
if|if
condition|(
name|result
operator|.
name|getBytes
argument_list|()
operator|.
name|length
operator|>=
name|len
condition|)
block|{
name|result
operator|.
name|set
argument_list|(
name|result
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|byte
index|[]
name|spaces
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|spaces
argument_list|,
operator|(
name|byte
operator|)
literal|' '
argument_list|)
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
name|spaces
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

