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
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Vector
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|lib
operator|.
name|Node
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
name|conf
operator|.
name|Configuration
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
name|plan
operator|.
name|explain
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
name|exprNodeDesc
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
name|mapredWork
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
name|LongWritable
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
name|OutputCollector
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
name|Reporter
import|;
end_import

begin_comment
comment|/**  * Base operator implementation  **/
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|Operator
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
implements|implements
name|Serializable
implements|,
name|Node
block|{
comment|// Bean methods
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|childOperators
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|parentOperators
decl_stmt|;
specifier|private
specifier|static
name|int
name|seqId
decl_stmt|;
comment|// It can be opimized later so that an operator operator (init/close) is performed
comment|// only after that operation has been performed on all the parents. This will require
comment|// initializing the whole tree in all the mappers (which might be required for mappers
comment|// spanning multiple files anyway, in future)
specifier|public
specifier|static
enum|enum
name|State
block|{
name|UNINIT
block|,
name|INIT
block|,
name|CLOSE
block|}
empty_stmt|;
specifier|transient
specifier|private
name|State
name|state
init|=
name|State
operator|.
name|UNINIT
decl_stmt|;
static|static
block|{
name|seqId
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|Operator
parameter_list|()
block|{
name|id
operator|=
name|String
operator|.
name|valueOf
argument_list|(
name|seqId
operator|++
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an operator with a reporter.    * @param reporter Used to report progress of certain operators.    */
specifier|public
name|Operator
parameter_list|(
name|Reporter
name|reporter
parameter_list|)
block|{
name|this
operator|.
name|reporter
operator|=
name|reporter
expr_stmt|;
name|id
operator|=
name|String
operator|.
name|valueOf
argument_list|(
name|seqId
operator|++
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setChildOperators
parameter_list|(
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|childOperators
parameter_list|)
block|{
name|this
operator|.
name|childOperators
operator|=
name|childOperators
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|getChildOperators
parameter_list|()
block|{
return|return
name|childOperators
return|;
block|}
comment|/**    * Implements the getChildren function for the Node Interface.    */
specifier|public
name|Vector
argument_list|<
name|Node
argument_list|>
name|getChildren
parameter_list|()
block|{
if|if
condition|(
name|getChildOperators
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Vector
argument_list|<
name|Node
argument_list|>
name|ret_vec
init|=
operator|new
name|Vector
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
range|:
name|getChildOperators
argument_list|()
control|)
block|{
name|ret_vec
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
return|return
name|ret_vec
return|;
block|}
specifier|public
name|void
name|setParentOperators
parameter_list|(
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|parentOperators
parameter_list|)
block|{
name|this
operator|.
name|parentOperators
operator|=
name|parentOperators
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|getParentOperators
parameter_list|()
block|{
return|return
name|parentOperators
return|;
block|}
specifier|protected
name|T
name|conf
decl_stmt|;
specifier|protected
name|boolean
name|done
decl_stmt|;
specifier|public
name|void
name|setConf
parameter_list|(
name|T
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|explain
specifier|public
name|T
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|public
name|boolean
name|getDone
parameter_list|()
block|{
return|return
name|done
return|;
block|}
specifier|public
name|void
name|setDone
parameter_list|(
name|boolean
name|done
parameter_list|)
block|{
name|this
operator|.
name|done
operator|=
name|done
expr_stmt|;
block|}
comment|// non-bean fields needed during compilation
specifier|transient
specifier|private
name|RowSchema
name|rowSchema
decl_stmt|;
specifier|public
name|void
name|setSchema
parameter_list|(
name|RowSchema
name|rowSchema
parameter_list|)
block|{
name|this
operator|.
name|rowSchema
operator|=
name|rowSchema
expr_stmt|;
block|}
specifier|public
name|RowSchema
name|getSchema
parameter_list|()
block|{
return|return
name|rowSchema
return|;
block|}
comment|// non-bean ..
specifier|transient
specifier|protected
name|HashMap
argument_list|<
name|Enum
argument_list|<
name|?
argument_list|>
argument_list|,
name|LongWritable
argument_list|>
name|statsMap
init|=
operator|new
name|HashMap
argument_list|<
name|Enum
argument_list|<
name|?
argument_list|>
argument_list|,
name|LongWritable
argument_list|>
argument_list|()
decl_stmt|;
specifier|transient
specifier|protected
name|OutputCollector
name|out
decl_stmt|;
specifier|transient
specifier|protected
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
empty_stmt|;
specifier|transient
specifier|protected
name|mapredWork
name|gWork
decl_stmt|;
specifier|transient
specifier|protected
name|String
name|alias
decl_stmt|;
specifier|transient
specifier|protected
name|String
name|joinAlias
decl_stmt|;
specifier|transient
specifier|protected
name|Reporter
name|reporter
decl_stmt|;
specifier|transient
specifier|protected
name|String
name|id
decl_stmt|;
comment|/**    * A map of output column name to input expression map. This is used by optimizer    * and built during semantic analysis    * contains only key elements for reduce sink and group by op    */
specifier|protected
specifier|transient
name|Map
argument_list|<
name|String
argument_list|,
name|exprNodeDesc
argument_list|>
name|colExprMap
decl_stmt|;
specifier|public
name|void
name|setId
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
specifier|public
name|String
name|getid
parameter_list|()
block|{
return|return
name|id
return|;
block|}
specifier|public
name|void
name|setOutputCollector
parameter_list|(
name|OutputCollector
name|out
parameter_list|)
block|{
name|this
operator|.
name|out
operator|=
name|out
expr_stmt|;
comment|// the collector is same across all operators
if|if
condition|(
name|childOperators
operator|==
literal|null
condition|)
return|return;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
range|:
name|childOperators
control|)
block|{
name|op
operator|.
name|setOutputCollector
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Operators often need access to global variables. This allows    * us to put global config information in the root configuration    * object and have that be accessible to all the operators in the    * tree.    */
specifier|public
name|void
name|setMapredWork
parameter_list|(
name|mapredWork
name|gWork
parameter_list|)
block|{
name|this
operator|.
name|gWork
operator|=
name|gWork
expr_stmt|;
if|if
condition|(
name|childOperators
operator|==
literal|null
condition|)
return|return;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
range|:
name|childOperators
control|)
block|{
name|op
operator|.
name|setMapredWork
argument_list|(
name|gWork
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Store the alias this operator is working on behalf of    */
specifier|public
name|void
name|setAlias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
name|this
operator|.
name|alias
operator|=
name|alias
expr_stmt|;
if|if
condition|(
name|childOperators
operator|==
literal|null
condition|)
return|return;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
range|:
name|childOperators
control|)
block|{
name|op
operator|.
name|setAlias
argument_list|(
name|alias
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Store the join alias this operator is working on behalf of    */
specifier|public
name|void
name|setJoinAlias
parameter_list|(
name|String
name|joinAlias
parameter_list|)
block|{
name|this
operator|.
name|joinAlias
operator|=
name|joinAlias
expr_stmt|;
if|if
condition|(
name|childOperators
operator|==
literal|null
condition|)
return|return;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
range|:
name|childOperators
control|)
block|{
name|op
operator|.
name|setJoinAlias
argument_list|(
name|joinAlias
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Map
argument_list|<
name|Enum
argument_list|<
name|?
argument_list|>
argument_list|,
name|Long
argument_list|>
name|getStats
parameter_list|()
block|{
name|HashMap
argument_list|<
name|Enum
argument_list|<
name|?
argument_list|>
argument_list|,
name|Long
argument_list|>
name|ret
init|=
operator|new
name|HashMap
argument_list|<
name|Enum
argument_list|<
name|?
argument_list|>
argument_list|,
name|Long
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Enum
argument_list|<
name|?
argument_list|>
name|one
range|:
name|statsMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|ret
operator|.
name|put
argument_list|(
name|one
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|statsMap
operator|.
name|get
argument_list|(
name|one
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|ret
operator|)
return|;
block|}
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|hconf
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|state
operator|==
name|state
operator|.
name|INIT
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Already Initialized"
argument_list|)
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Initializing Self"
argument_list|)
expr_stmt|;
name|this
operator|.
name|reporter
operator|=
name|reporter
expr_stmt|;
if|if
condition|(
name|childOperators
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Initializing children:"
argument_list|)
expr_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
range|:
name|childOperators
control|)
block|{
name|op
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
block|}
name|state
operator|=
name|State
operator|.
name|INIT
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Initialization Done"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|abstract
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|// If a operator wants to do some work at the beginning of a group
specifier|public
name|void
name|startGroup
parameter_list|()
throws|throws
name|HiveException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting group"
argument_list|)
expr_stmt|;
if|if
condition|(
name|childOperators
operator|==
literal|null
condition|)
return|return;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting group for children:"
argument_list|)
expr_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
range|:
name|childOperators
control|)
name|op
operator|.
name|startGroup
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Start group Done"
argument_list|)
expr_stmt|;
block|}
comment|// If a operator wants to do some work at the beginning of a group
specifier|public
name|void
name|endGroup
parameter_list|()
throws|throws
name|HiveException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Ending group"
argument_list|)
expr_stmt|;
if|if
condition|(
name|childOperators
operator|==
literal|null
condition|)
return|return;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Ending group for children:"
argument_list|)
expr_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
range|:
name|childOperators
control|)
name|op
operator|.
name|endGroup
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"End group Done"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|state
operator|==
name|state
operator|.
name|CLOSE
condition|)
return|return;
try|try
block|{
name|logStats
argument_list|()
expr_stmt|;
if|if
condition|(
name|childOperators
operator|==
literal|null
condition|)
return|return;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
range|:
name|childOperators
control|)
block|{
name|op
operator|.
name|close
argument_list|(
name|abort
argument_list|)
expr_stmt|;
block|}
name|state
operator|=
name|State
operator|.
name|CLOSE
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
comment|/**    * Unlike other operator interfaces which are called from map or reduce task,    * jobClose is called from the jobclient side once the job has completed    *    * @param conf Configuration with with which job was submitted    * @param success whether the job was completed successfully or not    */
specifier|public
name|void
name|jobClose
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|boolean
name|success
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|childOperators
operator|==
literal|null
condition|)
return|return;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
range|:
name|childOperators
control|)
block|{
name|op
operator|.
name|jobClose
argument_list|(
name|conf
argument_list|,
name|success
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|forward
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|(
name|childOperators
operator|==
literal|null
operator|)
operator|||
operator|(
name|getDone
argument_list|()
operator|)
condition|)
block|{
return|return;
block|}
comment|// if all children are done, this operator is also done
name|boolean
name|isDone
init|=
literal|true
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|o
range|:
name|childOperators
control|)
block|{
if|if
condition|(
operator|!
name|o
operator|.
name|getDone
argument_list|()
condition|)
block|{
name|isDone
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|isDone
condition|)
block|{
name|setDone
argument_list|(
name|isDone
argument_list|)
expr_stmt|;
return|return;
block|}
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|o
range|:
name|childOperators
control|)
block|{
name|o
operator|.
name|process
argument_list|(
name|row
argument_list|,
name|rowInspector
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|resetStats
parameter_list|()
block|{
for|for
control|(
name|Enum
argument_list|<
name|?
argument_list|>
name|e
range|:
name|statsMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|statsMap
operator|.
name|get
argument_list|(
name|e
argument_list|)
operator|.
name|set
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
interface|interface
name|OperatorFunc
block|{
specifier|public
name|void
name|func
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
parameter_list|)
function_decl|;
block|}
specifier|public
name|void
name|preorderMap
parameter_list|(
name|OperatorFunc
name|opFunc
parameter_list|)
block|{
name|opFunc
operator|.
name|func
argument_list|(
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
name|childOperators
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|o
range|:
name|childOperators
control|)
block|{
name|o
operator|.
name|preorderMap
argument_list|(
name|opFunc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|logStats
parameter_list|()
block|{
for|for
control|(
name|Enum
argument_list|<
name|?
argument_list|>
name|e
range|:
name|statsMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|e
operator|.
name|toString
argument_list|()
operator|+
literal|":"
operator|+
name|statsMap
operator|.
name|get
argument_list|(
name|e
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Implements the getName function for the Node Interface.    * @return the name of the operator    */
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
operator|new
name|String
argument_list|(
literal|"OP"
argument_list|)
return|;
block|}
comment|/**    * Returns a map of output column name to input expression map    * Note that currently it returns only key columns for ReduceSink and GroupBy operators    * @return null if the operator doesn't change columns    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|exprNodeDesc
argument_list|>
name|getColumnExprMap
parameter_list|()
block|{
return|return
name|colExprMap
return|;
block|}
specifier|public
name|void
name|setColumnExprMap
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|exprNodeDesc
argument_list|>
name|colExprMap
parameter_list|)
block|{
name|this
operator|.
name|colExprMap
operator|=
name|colExprMap
expr_stmt|;
block|}
specifier|public
name|String
name|dump
parameter_list|()
block|{
name|StringBuilder
name|s
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|s
operator|.
name|append
argument_list|(
literal|"<"
operator|+
name|getName
argument_list|()
operator|+
literal|">"
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
literal|"Id ="
operator|+
name|id
argument_list|)
expr_stmt|;
if|if
condition|(
name|childOperators
operator|!=
literal|null
condition|)
block|{
name|s
operator|.
name|append
argument_list|(
literal|"<Children>"
argument_list|)
expr_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|o
range|:
name|childOperators
control|)
block|{
name|s
operator|.
name|append
argument_list|(
name|o
operator|.
name|dump
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|s
operator|.
name|append
argument_list|(
literal|"<\\Children>"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|parentOperators
operator|!=
literal|null
condition|)
block|{
name|s
operator|.
name|append
argument_list|(
literal|"<Parent>"
argument_list|)
expr_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|o
range|:
name|parentOperators
control|)
block|{
name|s
operator|.
name|append
argument_list|(
literal|"Id = "
operator|+
name|o
operator|.
name|id
operator|+
literal|" "
argument_list|)
expr_stmt|;
block|}
name|s
operator|.
name|append
argument_list|(
literal|"<\\Parent>"
argument_list|)
expr_stmt|;
block|}
name|s
operator|.
name|append
argument_list|(
literal|"<\\"
operator|+
name|getName
argument_list|()
operator|+
literal|">"
argument_list|)
expr_stmt|;
return|return
name|s
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

