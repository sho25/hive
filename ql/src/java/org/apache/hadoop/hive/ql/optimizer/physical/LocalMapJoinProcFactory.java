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
name|optimizer
operator|.
name|physical
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
name|ArrayList
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
name|Stack
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
name|JDBMDummyOperator
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
name|JDBMSinkOperator
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
name|MapJoinOperator
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
name|Operator
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
name|OperatorFactory
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
name|RowSchema
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
name|TableScanOperator
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|lib
operator|.
name|NodeProcessor
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
name|NodeProcessorCtx
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
name|optimizer
operator|.
name|physical
operator|.
name|MapJoinResolver
operator|.
name|LocalMapJoinProcCtx
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
name|ql
operator|.
name|plan
operator|.
name|JDBMDummyDesc
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
name|JDBMSinkDesc
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
name|PlanUtils
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
name|TableDesc
import|;
end_import

begin_comment
comment|/**  * Node processor factory for skew join resolver.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|LocalMapJoinProcFactory
block|{
specifier|public
specifier|static
name|NodeProcessor
name|getJoinProc
parameter_list|()
block|{
return|return
operator|new
name|LocalMapJoinProcessor
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getMapJoinMapJoinProc
parameter_list|()
block|{
return|return
operator|new
name|MapJoinMapJoinProc
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getDefaultProc
parameter_list|()
block|{
return|return
operator|new
name|NodeProcessor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
literal|null
return|;
block|}
block|}
return|;
block|}
comment|/**    * LocalMapJoinProcessor.    *    */
specifier|public
specifier|static
class|class
name|LocalMapJoinProcessor
implements|implements
name|NodeProcessor
block|{
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|ctx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|LocalMapJoinProcCtx
name|context
init|=
operator|(
name|LocalMapJoinProcCtx
operator|)
name|ctx
decl_stmt|;
if|if
condition|(
operator|!
name|nd
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"MAPJOIN"
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|MapJoinOperator
name|mapJoinOp
init|=
operator|(
name|MapJoinOperator
operator|)
name|nd
decl_stmt|;
comment|//create an new operator: JDBMSinkOperator
name|JDBMSinkDesc
name|jdbmSinkDesc
init|=
operator|new
name|JDBMSinkDesc
argument_list|(
name|mapJoinOp
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|JDBMSinkOperator
name|jdbmSinkOp
init|=
operator|(
name|JDBMSinkOperator
operator|)
name|OperatorFactory
operator|.
name|get
argument_list|(
name|jdbmSinkDesc
argument_list|)
decl_stmt|;
comment|//get the last operator for processing big tables
name|int
name|bigTable
init|=
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getPosBigTable
argument_list|()
decl_stmt|;
name|Byte
index|[]
name|order
init|=
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getTagOrder
argument_list|()
decl_stmt|;
name|int
name|bigTableAlias
init|=
operator|(
name|int
operator|)
name|order
index|[
name|bigTable
index|]
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|bigOp
init|=
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|bigTable
argument_list|)
decl_stmt|;
comment|//the parent ops for jdbmSinkOp
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|smallTablesParentOp
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
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|dummyOperators
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
comment|//get all parents
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|parentsOp
init|=
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|parentsOp
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
name|bigTableAlias
condition|)
block|{
name|smallTablesParentOp
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|parent
init|=
name|parentsOp
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
comment|//let jdbmOp be the child of this parent
name|parent
operator|.
name|replaceChild
argument_list|(
name|mapJoinOp
argument_list|,
name|jdbmSinkOp
argument_list|)
expr_stmt|;
comment|//keep the parent id correct
name|smallTablesParentOp
operator|.
name|add
argument_list|(
name|parent
argument_list|)
expr_stmt|;
comment|//create an new operator: JDBMDummyOpeator, which share the table desc
name|JDBMDummyDesc
name|desc
init|=
operator|new
name|JDBMDummyDesc
argument_list|()
decl_stmt|;
name|JDBMDummyOperator
name|dummyOp
init|=
operator|(
name|JDBMDummyOperator
operator|)
name|OperatorFactory
operator|.
name|get
argument_list|(
name|desc
argument_list|)
decl_stmt|;
name|TableDesc
name|tbl
decl_stmt|;
if|if
condition|(
name|parent
operator|.
name|getSchema
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|parent
operator|instanceof
name|TableScanOperator
condition|)
block|{
name|tbl
operator|=
operator|(
operator|(
name|TableScanOperator
operator|)
name|parent
operator|)
operator|.
name|getTableDesc
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SemanticException
argument_list|()
throw|;
block|}
block|}
else|else
block|{
comment|//get parent schema
name|RowSchema
name|rowSchema
init|=
name|parent
operator|.
name|getSchema
argument_list|()
decl_stmt|;
name|tbl
operator|=
name|PlanUtils
operator|.
name|getIntermediateFileTableDesc
argument_list|(
name|PlanUtils
operator|.
name|getFieldSchemasFromRowSchema
argument_list|(
name|rowSchema
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|dummyOp
operator|.
name|getConf
argument_list|()
operator|.
name|setTbl
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
comment|//let the dummy op  be the parent of mapjoin op
name|mapJoinOp
operator|.
name|replaceParent
argument_list|(
name|parent
argument_list|,
name|dummyOp
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|dummyChildren
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
name|dummyChildren
operator|.
name|add
argument_list|(
name|mapJoinOp
argument_list|)
expr_stmt|;
name|dummyOp
operator|.
name|setChildOperators
argument_list|(
name|dummyChildren
argument_list|)
expr_stmt|;
comment|//add this dummy op to the dummp operator list
name|dummyOperators
operator|.
name|add
argument_list|(
name|dummyOp
argument_list|)
expr_stmt|;
block|}
name|jdbmSinkOp
operator|.
name|setParentOperators
argument_list|(
name|smallTablesParentOp
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
name|dummyOperators
control|)
block|{
name|context
operator|.
name|addDummyParentOp
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
comment|/**    * LocalMapJoinProcessor.    *    */
specifier|public
specifier|static
class|class
name|MapJoinMapJoinProc
implements|implements
name|NodeProcessor
block|{
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|ctx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|LocalMapJoinProcCtx
name|context
init|=
operator|(
name|LocalMapJoinProcCtx
operator|)
name|ctx
decl_stmt|;
if|if
condition|(
operator|!
name|nd
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"MAPJOIN"
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Mapjoin * MapJoin"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|LocalMapJoinProcFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

