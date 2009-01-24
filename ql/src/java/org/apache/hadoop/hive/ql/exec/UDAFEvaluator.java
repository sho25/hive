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

begin_comment
comment|/**  * Interface that encapsulates the evaluation logic of a UDAF. One evaluator is needed  * for every overloaded form of a UDAF .e.g max and min UDAFs would have evaluators for  * integer, string and other types. On the other hand avg would have an evaluator only  * for the double type.  */
end_comment

begin_interface
specifier|public
interface|interface
name|UDAFEvaluator
block|{
comment|/**    * Initializer. Initializes the state for the evaluator.    */
specifier|public
name|void
name|init
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

