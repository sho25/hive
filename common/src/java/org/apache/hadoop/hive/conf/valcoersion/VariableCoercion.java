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
name|conf
operator|.
name|valcoersion
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
name|conf
operator|.
name|Configuration
import|;
end_import

begin_comment
comment|/**  * VariableCoercions are used to enforce rules related to system variables.  * These rules may transform the value of system properties returned by the  * {@link org.apache.hadoop.hive.conf.SystemVariables SystemVariables} utility class  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VariableCoercion
block|{
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|public
name|VariableCoercion
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
comment|/**    * Coerce the original value of the variable    * @param originalValue the unmodified value    * @return transformed value    */
specifier|public
specifier|abstract
name|String
name|getCoerced
parameter_list|(
name|String
name|originalValue
parameter_list|)
function_decl|;
block|}
end_class

end_unit

