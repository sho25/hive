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
operator|.
name|generic
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
name|ql
operator|.
name|parse
operator|.
name|SemanticException
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
name|typeinfo
operator|.
name|TypeInfo
import|;
end_import

begin_comment
comment|/**  * A Generic User-defined aggregation function (GenericUDAF) for the use with   * Hive.  *   * GenericUDAFResolver is used at compile time.  We use GenericUDAFResolver to  * find out the GenericUDAFEvaluator for the parameter types.  *   */
end_comment

begin_interface
specifier|public
interface|interface
name|GenericUDAFResolver
block|{
comment|/** Get the evaluator for the parameter types.    *      *  The reason that this function returns an object instead of a class    *  is because it's possible that the object needs some configuration     *  (that can be serialized).  In that case the class of the object has     *  to implement the Serializable interface.  At execution time, we will     *  deserialize the object from the plan and use it to evaluate the    *  aggregations.    *      *  If the class of the object does not implement Serializable, then    *  we will create a new instance of the class at execution time.    *      *  @param parameters  The types of the parameters. We need the type     *         information to know which evaluator class to use.      */
name|GenericUDAFEvaluator
name|getEvaluator
parameter_list|(
name|TypeInfo
index|[]
name|parameters
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
block|}
end_interface

end_unit

