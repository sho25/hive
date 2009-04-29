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
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
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
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|OperatorFactory
block|{
specifier|public
specifier|final
specifier|static
class|class
name|opTuple
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
block|{
specifier|public
name|Class
argument_list|<
name|T
argument_list|>
name|descClass
decl_stmt|;
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|T
argument_list|>
argument_list|>
name|opClass
decl_stmt|;
specifier|public
name|opTuple
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|descClass
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|T
argument_list|>
argument_list|>
name|opClass
parameter_list|)
block|{
name|this
operator|.
name|descClass
operator|=
name|descClass
expr_stmt|;
name|this
operator|.
name|opClass
operator|=
name|opClass
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|ArrayList
argument_list|<
name|opTuple
argument_list|>
name|opvec
decl_stmt|;
static|static
block|{
name|opvec
operator|=
operator|new
name|ArrayList
argument_list|<
name|opTuple
argument_list|>
argument_list|()
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|opTuple
argument_list|<
name|filterDesc
argument_list|>
argument_list|(
name|filterDesc
operator|.
name|class
argument_list|,
name|FilterOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|opTuple
argument_list|<
name|selectDesc
argument_list|>
argument_list|(
name|selectDesc
operator|.
name|class
argument_list|,
name|SelectOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|opTuple
argument_list|<
name|forwardDesc
argument_list|>
argument_list|(
name|forwardDesc
operator|.
name|class
argument_list|,
name|ForwardOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|opTuple
argument_list|<
name|fileSinkDesc
argument_list|>
argument_list|(
name|fileSinkDesc
operator|.
name|class
argument_list|,
name|FileSinkOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|opTuple
argument_list|<
name|collectDesc
argument_list|>
argument_list|(
name|collectDesc
operator|.
name|class
argument_list|,
name|CollectOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|opTuple
argument_list|<
name|scriptDesc
argument_list|>
argument_list|(
name|scriptDesc
operator|.
name|class
argument_list|,
name|ScriptOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|opTuple
argument_list|<
name|reduceSinkDesc
argument_list|>
argument_list|(
name|reduceSinkDesc
operator|.
name|class
argument_list|,
name|ReduceSinkOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|opTuple
argument_list|<
name|extractDesc
argument_list|>
argument_list|(
name|extractDesc
operator|.
name|class
argument_list|,
name|ExtractOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|opTuple
argument_list|<
name|groupByDesc
argument_list|>
argument_list|(
name|groupByDesc
operator|.
name|class
argument_list|,
name|GroupByOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|opTuple
argument_list|<
name|joinDesc
argument_list|>
argument_list|(
name|joinDesc
operator|.
name|class
argument_list|,
name|JoinOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|opTuple
argument_list|<
name|limitDesc
argument_list|>
argument_list|(
name|limitDesc
operator|.
name|class
argument_list|,
name|LimitOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|opTuple
argument_list|<
name|tableScanDesc
argument_list|>
argument_list|(
name|tableScanDesc
operator|.
name|class
argument_list|,
name|TableScanOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|opvec
operator|.
name|add
argument_list|(
operator|new
name|opTuple
argument_list|<
name|unionDesc
argument_list|>
argument_list|(
name|unionDesc
operator|.
name|class
argument_list|,
name|UnionOperator
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|get
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|opClass
parameter_list|)
block|{
for|for
control|(
name|opTuple
name|o
range|:
name|opvec
control|)
block|{
if|if
condition|(
name|o
operator|.
name|descClass
operator|==
name|opClass
condition|)
block|{
try|try
block|{
return|return
operator|(
name|Operator
argument_list|<
name|T
argument_list|>
operator|)
name|o
operator|.
name|opClass
operator|.
name|newInstance
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"No operator for descriptor class "
operator|+
name|opClass
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|get
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|opClass
parameter_list|,
name|RowSchema
name|rwsch
parameter_list|)
block|{
name|Operator
argument_list|<
name|T
argument_list|>
name|ret
init|=
name|get
argument_list|(
name|opClass
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setSchema
argument_list|(
name|rwsch
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
comment|/**    * Returns an operator given the conf and a list of children operators.      */
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|get
parameter_list|(
name|T
name|conf
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
modifier|...
name|oplist
parameter_list|)
block|{
name|Operator
argument_list|<
name|T
argument_list|>
name|ret
init|=
name|get
argument_list|(
operator|(
name|Class
argument_list|<
name|T
argument_list|>
operator|)
name|conf
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|oplist
operator|.
name|length
operator|==
literal|0
condition|)
return|return
operator|(
name|ret
operator|)
return|;
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|clist
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
name|op
range|:
name|oplist
control|)
block|{
name|clist
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
name|ret
operator|.
name|setChildOperators
argument_list|(
name|clist
argument_list|)
expr_stmt|;
comment|// Add this parent to the children
for|for
control|(
name|Operator
name|op
range|:
name|oplist
control|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|parents
init|=
name|op
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
if|if
condition|(
name|parents
operator|==
literal|null
condition|)
block|{
name|parents
operator|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|parents
operator|.
name|add
argument_list|(
name|ret
argument_list|)
expr_stmt|;
name|op
operator|.
name|setParentOperators
argument_list|(
name|parents
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|ret
operator|)
return|;
block|}
comment|/**    * Returns an operator given the conf and a list of children operators.      */
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|get
parameter_list|(
name|T
name|conf
parameter_list|,
name|RowSchema
name|rwsch
parameter_list|,
name|Operator
modifier|...
name|oplist
parameter_list|)
block|{
name|Operator
argument_list|<
name|T
argument_list|>
name|ret
init|=
name|get
argument_list|(
name|conf
argument_list|,
name|oplist
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setSchema
argument_list|(
name|rwsch
argument_list|)
expr_stmt|;
return|return
operator|(
name|ret
operator|)
return|;
block|}
comment|/**    * Returns an operator given the conf and a list of parent operators.      */
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|getAndMakeChild
parameter_list|(
name|T
name|conf
parameter_list|,
name|Operator
modifier|...
name|oplist
parameter_list|)
block|{
name|Operator
argument_list|<
name|T
argument_list|>
name|ret
init|=
name|get
argument_list|(
operator|(
name|Class
argument_list|<
name|T
argument_list|>
operator|)
name|conf
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|oplist
operator|.
name|length
operator|==
literal|0
condition|)
return|return
operator|(
name|ret
operator|)
return|;
comment|// Add the new operator as child of each of the passed in operators
for|for
control|(
name|Operator
name|op
range|:
name|oplist
control|)
block|{
name|List
argument_list|<
name|Operator
argument_list|>
name|children
init|=
name|op
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
if|if
condition|(
name|children
operator|==
literal|null
condition|)
block|{
name|children
operator|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|children
operator|.
name|add
argument_list|(
name|ret
argument_list|)
expr_stmt|;
name|op
operator|.
name|setChildOperators
argument_list|(
name|children
argument_list|)
expr_stmt|;
block|}
comment|// add parents for the newly created operator
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|parent
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
name|op
range|:
name|oplist
control|)
name|parent
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setParentOperators
argument_list|(
name|parent
argument_list|)
expr_stmt|;
return|return
operator|(
name|ret
operator|)
return|;
block|}
comment|/**    * Returns an operator given the conf and a list of parent operators.      */
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
name|Operator
argument_list|<
name|T
argument_list|>
name|getAndMakeChild
parameter_list|(
name|T
name|conf
parameter_list|,
name|RowSchema
name|rwsch
parameter_list|,
name|Operator
modifier|...
name|oplist
parameter_list|)
block|{
name|Operator
argument_list|<
name|T
argument_list|>
name|ret
init|=
name|getAndMakeChild
argument_list|(
name|conf
argument_list|,
name|oplist
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setSchema
argument_list|(
name|rwsch
argument_list|)
expr_stmt|;
return|return
operator|(
name|ret
operator|)
return|;
block|}
block|}
end_class

end_unit

