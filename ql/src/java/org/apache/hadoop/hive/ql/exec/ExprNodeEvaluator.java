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
name|exec
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
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|ExprNodeEvaluator
block|{
comment|/**    * @return plain old java object    **/
specifier|public
specifier|abstract
name|Object
name|evaluateToObject
parameter_list|(
name|HiveObject
name|row
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * @return encapsulated Hive Object    **/
specifier|public
specifier|abstract
name|HiveObject
name|evaluate
parameter_list|(
name|HiveObject
name|row
parameter_list|)
throws|throws
name|HiveException
function_decl|;
block|}
end_class

end_unit

