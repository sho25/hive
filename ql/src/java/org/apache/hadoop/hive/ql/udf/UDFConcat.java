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
name|Text
import|;
end_import

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"concat"
argument_list|,
name|value
operator|=
literal|"_FUNC_(str1, str2, ... strN) - returns the concatenation of str1, str2, ... strN"
argument_list|,
name|extended
operator|=
literal|"Returns NULL if any argument is NULL.\n"
operator|+
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_('abc', 'def') FROM src LIMIT 1;\n"
operator|+
literal|"  'abcdef'"
argument_list|)
specifier|public
class|class
name|UDFConcat
extends|extends
name|UDF
block|{
specifier|public
name|UDFConcat
parameter_list|()
block|{   }
name|Text
name|text
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
modifier|...
name|args
parameter_list|)
block|{
name|text
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|Text
name|arg
range|:
name|args
control|)
block|{
if|if
condition|(
name|arg
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|text
operator|.
name|append
argument_list|(
name|arg
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|arg
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|text
return|;
block|}
block|}
end_class

end_unit

