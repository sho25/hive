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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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

begin_comment
comment|/**  * Implementation of the SQRT UDF found in many databases.  */
end_comment

begin_class
specifier|public
class|class
name|UDFSqrt
extends|extends
name|UDF
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|UDFSqrt
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|UDFSqrt
parameter_list|()
block|{   }
comment|/**    * Return NULL for NULL or negative inputs; otherwise, return    * the square root.    */
specifier|public
name|Double
name|evaluate
parameter_list|(
name|Double
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|i
operator|<
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
name|Double
operator|.
name|valueOf
argument_list|(
name|Math
operator|.
name|sqrt
argument_list|(
name|i
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

