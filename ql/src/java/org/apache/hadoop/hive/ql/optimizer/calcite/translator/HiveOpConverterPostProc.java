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
name|calcite
operator|.
name|translator
package|;
end_package

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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|conf
operator|.
name|HiveConf
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
name|JoinOperator
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
name|ReduceSinkOperator
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
name|DefaultRuleDispatcher
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
name|Dispatcher
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
name|ForwardWalker
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
name|GraphWalker
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
name|lib
operator|.
name|Rule
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
name|RuleRegExp
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
name|Transform
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
name|ParseContext
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
name|OperatorDesc
import|;
end_import

begin_class
specifier|public
class|class
name|HiveOpConverterPostProc
implements|implements
name|Transform
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HiveOpConverterPostProc
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ParseContext
name|pctx
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|aliasToOpInfo
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ParseContext
name|transform
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// 0. We check the conditions to apply this transformation,
comment|//    if we do not meet them we bail out
specifier|final
name|boolean
name|cboEnabled
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|pctx
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_CBO_ENABLED
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|returnPathEnabled
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|pctx
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_CBO_RETPATH_HIVEOP
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|cboSucceeded
init|=
name|pctx
operator|.
name|getContext
argument_list|()
operator|.
name|isCboSucceeded
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|cboEnabled
operator|&&
name|returnPathEnabled
operator|&&
name|cboSucceeded
operator|)
condition|)
block|{
return|return
name|pctx
return|;
block|}
comment|// 1. Initialize aux data structures
name|this
operator|.
name|pctx
operator|=
name|pctx
expr_stmt|;
name|this
operator|.
name|aliasToOpInfo
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
comment|// 2. Trigger transformation
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|opRules
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
argument_list|()
decl_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
name|JoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|JoinAnnotate
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R2"
argument_list|,
name|TableScanOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|TableScanAnnotate
argument_list|()
argument_list|)
expr_stmt|;
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
literal|null
argument_list|,
name|opRules
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|ForwardWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Node
argument_list|>
name|topNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|topNodes
operator|.
name|addAll
argument_list|(
name|pctx
operator|.
name|getTopOps
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|pctx
return|;
block|}
specifier|private
class|class
name|JoinAnnotate
implements|implements
name|NodeProcessor
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
name|JoinOperator
name|joinOp
init|=
operator|(
name|JoinOperator
operator|)
name|nd
decl_stmt|;
comment|// 1. Additional data structures needed for the join optimization
comment|//    through Hive
name|String
index|[]
name|baseSrc
init|=
operator|new
name|String
index|[
name|joinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|String
index|[]
name|rightAliases
init|=
operator|new
name|String
index|[
name|joinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|-
literal|1
index|]
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
name|joinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ReduceSinkOperator
name|rsOp
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|joinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|aliases
init|=
name|rsOp
operator|.
name|getSchema
argument_list|()
operator|.
name|getTableNames
argument_list|()
decl_stmt|;
if|if
condition|(
name|aliases
operator|==
literal|null
operator|||
name|aliases
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"In return path join annotate rule, we find "
operator|+
name|aliases
operator|==
literal|null
condition|?
literal|null
else|:
name|aliases
operator|.
name|size
argument_list|()
operator|+
literal|" aliases for "
operator|+
name|rsOp
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
name|baseSrc
index|[
name|i
index|]
operator|=
name|aliases
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|joinOp
operator|.
name|getConf
argument_list|()
operator|.
name|setLeftAlias
argument_list|(
name|baseSrc
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rightAliases
index|[
name|i
operator|-
literal|1
index|]
operator|=
name|baseSrc
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
name|joinOp
operator|.
name|getConf
argument_list|()
operator|.
name|setBaseSrc
argument_list|(
name|baseSrc
argument_list|)
expr_stmt|;
name|joinOp
operator|.
name|getConf
argument_list|()
operator|.
name|setRightAliases
argument_list|(
name|rightAliases
argument_list|)
expr_stmt|;
name|joinOp
operator|.
name|getConf
argument_list|()
operator|.
name|setAliasToOpInfo
argument_list|(
name|aliasToOpInfo
argument_list|)
expr_stmt|;
comment|// 2. Use self alias
name|Set
argument_list|<
name|String
argument_list|>
name|aliases
init|=
name|joinOp
operator|.
name|getSchema
argument_list|()
operator|.
name|getTableNames
argument_list|()
decl_stmt|;
if|if
condition|(
name|aliases
operator|==
literal|null
operator|||
name|aliases
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"In return path join annotate rule, we find "
operator|+
name|aliases
operator|==
literal|null
condition|?
literal|null
else|:
name|aliases
operator|.
name|size
argument_list|()
operator|+
literal|" aliases for "
operator|+
name|joinOp
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
specifier|final
name|String
name|joinOpAlias
init|=
name|aliases
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
empty_stmt|;
name|aliasToOpInfo
operator|.
name|put
argument_list|(
name|joinOpAlias
argument_list|,
name|joinOp
argument_list|)
expr_stmt|;
comment|// 3. Populate other data structures
name|pctx
operator|.
name|getJoinOps
argument_list|()
operator|.
name|add
argument_list|(
name|joinOp
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|private
class|class
name|TableScanAnnotate
implements|implements
name|NodeProcessor
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
name|TableScanOperator
name|tableScanOp
init|=
operator|(
name|TableScanOperator
operator|)
name|nd
decl_stmt|;
comment|// 1. Get alias from topOps
name|String
name|opAlias
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|topOpEntry
range|:
name|pctx
operator|.
name|getTopOps
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|topOpEntry
operator|.
name|getValue
argument_list|()
operator|==
name|tableScanOp
condition|)
block|{
name|opAlias
operator|=
name|topOpEntry
operator|.
name|getKey
argument_list|()
expr_stmt|;
block|}
block|}
assert|assert
name|opAlias
operator|!=
literal|null
assert|;
comment|// 2. Add alias to 1) aliasToOpInfo and 2) opToAlias
name|aliasToOpInfo
operator|.
name|put
argument_list|(
name|opAlias
argument_list|,
name|tableScanOp
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

