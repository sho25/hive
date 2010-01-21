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
name|objectinspector
operator|.
name|ObjectInspector
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|ExprNodeEvaluator
block|{
comment|/**    * Initialize should be called once and only once. Return the ObjectInspector    * for the return value, given the rowInspector.    */
specifier|public
specifier|abstract
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Evaluate the expression given the row. This method should use the    * rowInspector passed in from initialize to inspect the row object. The    * return value will be inspected by the return value of initialize.    */
specifier|public
specifier|abstract
name|Object
name|evaluate
parameter_list|(
name|Object
name|row
parameter_list|)
throws|throws
name|HiveException
function_decl|;
block|}
end_class

end_unit

