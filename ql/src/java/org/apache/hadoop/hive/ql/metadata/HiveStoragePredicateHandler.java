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
name|metadata
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
name|plan
operator|.
name|ExprNodeDesc
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
name|plan
operator|.
name|ExprNodeGenericFuncDesc
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
name|Deserializer
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_comment
comment|/**  * HiveStoragePredicateHandler is an optional companion to {@link  * HiveStorageHandler}; it should only be implemented by handlers which  * support decomposition of predicates being pushed down into table scans.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HiveStoragePredicateHandler
block|{
comment|/**    * Gives the storage handler a chance to decompose a predicate.  The storage    * handler should analyze the predicate and return the portion of it which    * cannot be evaluated during table access.  For example, if the original    * predicate is<code>x = 2 AND upper(y)='YUM'</code>, the storage handler    * might be able to handle<code>x = 2</code> but leave the "residual"    *<code>upper(y)='YUM'</code> for Hive to deal with.  The breakdown    * need not be non-overlapping; for example, given the    * predicate<code>x LIKE 'a%b'</code>, the storage handler might    * be able to evaluate the prefix search<code>x LIKE 'a%'</code>, leaving    *<code>x LIKE '%b'</code> as the residual.    *    * @param jobConf contains a job configuration matching the one that    * will later be passed to getRecordReader and getSplits    *    * @param deserializer deserializer which will be used when    * fetching rows    *    * @param predicate predicate to be decomposed    *    * @return decomposed form of predicate, or null if no pushdown is    * possible at all    */
specifier|public
name|DecomposedPredicate
name|decomposePredicate
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|Deserializer
name|deserializer
parameter_list|,
name|ExprNodeDesc
name|predicate
parameter_list|)
function_decl|;
comment|/**    * Struct class for returning multiple values from decomposePredicate.    */
specifier|public
specifier|static
class|class
name|DecomposedPredicate
block|{
comment|/**      * Portion of predicate to be evaluated by storage handler.  Hive      * will pass this into the storage handler's input format.      */
specifier|public
name|ExprNodeGenericFuncDesc
name|pushedPredicate
decl_stmt|;
comment|/**      * Serialized format for filter      */
specifier|public
name|Serializable
name|pushedPredicateObject
decl_stmt|;
comment|/**      * Portion of predicate to be post-evaluated by Hive for any rows      * which are returned by storage handler.      */
specifier|public
name|ExprNodeGenericFuncDesc
name|residualPredicate
decl_stmt|;
block|}
block|}
end_interface

end_unit

