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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|HashSet
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
name|fs
operator|.
name|ContentSummary
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
name|fs
operator|.
name|Path
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
name|FetchTask
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
name|FileSinkOperator
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
name|FilterOperator
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
name|LimitOperator
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
name|ListSinkOperator
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
name|SelectOperator
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
name|exec
operator|.
name|TaskFactory
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
name|Utilities
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
name|hooks
operator|.
name|ReadEntity
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
name|io
operator|.
name|ContentSummaryInputFormat
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
name|io
operator|.
name|HiveInputFormat
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
name|InputEstimator
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
name|metadata
operator|.
name|HiveStorageHandler
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
name|Partition
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
name|Table
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
name|ppr
operator|.
name|PartitionPruner
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
name|PrunedPartitionList
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
name|QB
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
name|parse
operator|.
name|SplitSample
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
name|FetchWork
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
name|ListSinkDesc
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
name|PartitionDesc
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
name|InputFormat
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

begin_comment
comment|/**  * Tries to convert simple fetch query to single fetch task, which fetches rows directly  * from location of table/partition.  */
end_comment

begin_class
specifier|public
class|class
name|SimpleFetchOptimizer
implements|implements
name|Transform
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SimpleFetchOptimizer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
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
name|topOps
init|=
name|pctx
operator|.
name|getTopOps
argument_list|()
decl_stmt|;
if|if
condition|(
name|pctx
operator|.
name|getQB
argument_list|()
operator|.
name|isSimpleSelectQuery
argument_list|()
operator|&&
name|topOps
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
comment|// no join, no groupby, no distinct, no lateral view, no subq,
comment|// no CTAS or insert, not analyze command, and single sourced.
name|String
name|alias
init|=
operator|(
name|String
operator|)
name|pctx
operator|.
name|getTopOps
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|Operator
name|topOp
init|=
operator|(
name|Operator
operator|)
name|pctx
operator|.
name|getTopOps
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|toArray
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|topOp
operator|instanceof
name|TableScanOperator
condition|)
block|{
try|try
block|{
name|FetchTask
name|fetchTask
init|=
name|optimize
argument_list|(
name|pctx
argument_list|,
name|alias
argument_list|,
operator|(
name|TableScanOperator
operator|)
name|topOp
argument_list|)
decl_stmt|;
if|if
condition|(
name|fetchTask
operator|!=
literal|null
condition|)
block|{
name|pctx
operator|.
name|setFetchTask
argument_list|(
name|fetchTask
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Has to use full name to make sure it does not conflict with
comment|// org.apache.commons.lang.StringUtils
name|LOG
operator|.
name|error
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|SemanticException
condition|)
block|{
throw|throw
operator|(
name|SemanticException
operator|)
name|e
throw|;
block|}
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
return|return
name|pctx
return|;
block|}
comment|// returns non-null FetchTask instance when succeeded
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|FetchTask
name|optimize
parameter_list|(
name|ParseContext
name|pctx
parameter_list|,
name|String
name|alias
parameter_list|,
name|TableScanOperator
name|source
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|mode
init|=
name|HiveConf
operator|.
name|getVar
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
name|HIVEFETCHTASKCONVERSION
argument_list|)
decl_stmt|;
name|boolean
name|aggressive
init|=
literal|"more"
operator|.
name|equals
argument_list|(
name|mode
argument_list|)
decl_stmt|;
name|FetchData
name|fetch
init|=
name|checkTree
argument_list|(
name|aggressive
argument_list|,
name|pctx
argument_list|,
name|alias
argument_list|,
name|source
argument_list|)
decl_stmt|;
if|if
condition|(
name|fetch
operator|!=
literal|null
operator|&&
name|checkThreshold
argument_list|(
name|fetch
argument_list|,
name|pctx
argument_list|)
condition|)
block|{
name|int
name|limit
init|=
name|pctx
operator|.
name|getQB
argument_list|()
operator|.
name|getParseInfo
argument_list|()
operator|.
name|getOuterQueryLimit
argument_list|()
decl_stmt|;
name|FetchWork
name|fetchWork
init|=
name|fetch
operator|.
name|convertToWork
argument_list|()
decl_stmt|;
name|FetchTask
name|fetchTask
init|=
operator|(
name|FetchTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
name|fetchWork
argument_list|,
name|pctx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|fetchWork
operator|.
name|setSink
argument_list|(
name|fetch
operator|.
name|completed
argument_list|(
name|pctx
argument_list|,
name|fetchWork
argument_list|)
argument_list|)
expr_stmt|;
name|fetchWork
operator|.
name|setSource
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|fetchWork
operator|.
name|setLimit
argument_list|(
name|limit
argument_list|)
expr_stmt|;
return|return
name|fetchTask
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|boolean
name|checkThreshold
parameter_list|(
name|FetchData
name|data
parameter_list|,
name|ParseContext
name|pctx
parameter_list|)
throws|throws
name|Exception
block|{
name|long
name|threshold
init|=
name|HiveConf
operator|.
name|getLongVar
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
name|HIVEFETCHTASKCONVERSIONTHRESHOLD
argument_list|)
decl_stmt|;
if|if
condition|(
name|threshold
operator|<
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
name|long
name|remaining
init|=
name|threshold
decl_stmt|;
name|remaining
operator|-=
name|data
operator|.
name|getInputLength
argument_list|(
name|pctx
argument_list|,
name|remaining
argument_list|)
expr_stmt|;
if|if
condition|(
name|remaining
operator|<
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Threshold "
operator|+
name|remaining
operator|+
literal|" exceeded for pseudoMR mode"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
comment|// all we can handle is LimitOperator, FilterOperator SelectOperator and final FS
comment|//
comment|// for non-aggressive mode (minimal)
comment|// 1. samping is not allowed
comment|// 2. for partitioned table, all filters should be targeted to partition column
comment|// 3. SelectOperator should be select star
specifier|private
name|FetchData
name|checkTree
parameter_list|(
name|boolean
name|aggressive
parameter_list|,
name|ParseContext
name|pctx
parameter_list|,
name|String
name|alias
parameter_list|,
name|TableScanOperator
name|ts
parameter_list|)
throws|throws
name|HiveException
block|{
name|SplitSample
name|splitSample
init|=
name|pctx
operator|.
name|getNameToSplitSample
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|aggressive
operator|&&
name|splitSample
operator|!=
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|QB
name|qb
init|=
name|pctx
operator|.
name|getQB
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|aggressive
operator|&&
name|qb
operator|.
name|hasTableSample
argument_list|(
name|alias
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Table
name|table
init|=
name|qb
operator|.
name|getMetaData
argument_list|()
operator|.
name|getAliasToTable
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|table
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
operator|!
name|table
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
return|return
name|checkOperators
argument_list|(
operator|new
name|FetchData
argument_list|(
name|table
argument_list|,
name|splitSample
argument_list|)
argument_list|,
name|ts
argument_list|,
name|aggressive
argument_list|,
literal|false
argument_list|)
return|;
block|}
name|boolean
name|bypassFilter
init|=
literal|false
decl_stmt|;
if|if
condition|(
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
name|HIVEOPTPPD
argument_list|)
condition|)
block|{
name|ExprNodeDesc
name|pruner
init|=
name|pctx
operator|.
name|getOpToPartPruner
argument_list|()
operator|.
name|get
argument_list|(
name|ts
argument_list|)
decl_stmt|;
name|bypassFilter
operator|=
name|PartitionPruner
operator|.
name|onlyContainsPartnCols
argument_list|(
name|table
argument_list|,
name|pruner
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|aggressive
operator|||
name|bypassFilter
condition|)
block|{
name|PrunedPartitionList
name|pruned
init|=
name|pctx
operator|.
name|getPrunedPartitions
argument_list|(
name|alias
argument_list|,
name|ts
argument_list|)
decl_stmt|;
if|if
condition|(
name|aggressive
operator|||
operator|!
name|pruned
operator|.
name|hasUnknownPartitions
argument_list|()
condition|)
block|{
name|bypassFilter
operator|&=
operator|!
name|pruned
operator|.
name|hasUnknownPartitions
argument_list|()
expr_stmt|;
return|return
name|checkOperators
argument_list|(
operator|new
name|FetchData
argument_list|(
name|table
argument_list|,
name|pruned
argument_list|,
name|splitSample
argument_list|)
argument_list|,
name|ts
argument_list|,
name|aggressive
argument_list|,
name|bypassFilter
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|FetchData
name|checkOperators
parameter_list|(
name|FetchData
name|fetch
parameter_list|,
name|TableScanOperator
name|ts
parameter_list|,
name|boolean
name|aggresive
parameter_list|,
name|boolean
name|bypassFilter
parameter_list|)
block|{
if|if
condition|(
name|ts
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Operator
argument_list|<
name|?
argument_list|>
name|op
init|=
name|ts
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
init|;
condition|;
name|op
operator|=
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
control|)
block|{
if|if
condition|(
name|aggresive
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|op
operator|instanceof
name|LimitOperator
operator|||
name|op
operator|instanceof
name|FilterOperator
operator|||
name|op
operator|instanceof
name|SelectOperator
operator|)
condition|)
block|{
break|break;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
operator|(
name|op
operator|instanceof
name|LimitOperator
operator|||
operator|(
name|op
operator|instanceof
name|FilterOperator
operator|&&
name|bypassFilter
operator|)
operator|||
operator|(
name|op
operator|instanceof
name|SelectOperator
operator|&&
operator|(
operator|(
name|SelectOperator
operator|)
name|op
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|isSelectStar
argument_list|()
operator|)
operator|)
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|op
operator|.
name|getChildOperators
argument_list|()
operator|==
literal|null
operator|||
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
if|if
condition|(
name|op
operator|instanceof
name|FileSinkOperator
condition|)
block|{
name|fetch
operator|.
name|scanOp
operator|=
name|ts
expr_stmt|;
name|fetch
operator|.
name|fileSink
operator|=
name|op
expr_stmt|;
return|return
name|fetch
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
class|class
name|FetchData
block|{
specifier|private
specifier|final
name|Table
name|table
decl_stmt|;
specifier|private
specifier|final
name|SplitSample
name|splitSample
decl_stmt|;
specifier|private
specifier|final
name|PrunedPartitionList
name|partsList
decl_stmt|;
specifier|private
specifier|final
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
init|=
operator|new
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
argument_list|()
decl_stmt|;
comment|// source table scan
specifier|private
name|TableScanOperator
name|scanOp
decl_stmt|;
comment|// this is always non-null when conversion is completed
specifier|private
name|Operator
argument_list|<
name|?
argument_list|>
name|fileSink
decl_stmt|;
specifier|private
name|FetchData
parameter_list|(
name|Table
name|table
parameter_list|,
name|SplitSample
name|splitSample
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|partsList
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|splitSample
operator|=
name|splitSample
expr_stmt|;
block|}
specifier|private
name|FetchData
parameter_list|(
name|Table
name|table
parameter_list|,
name|PrunedPartitionList
name|partsList
parameter_list|,
name|SplitSample
name|splitSample
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|partsList
operator|=
name|partsList
expr_stmt|;
name|this
operator|.
name|splitSample
operator|=
name|splitSample
expr_stmt|;
block|}
specifier|private
name|FetchWork
name|convertToWork
parameter_list|()
throws|throws
name|HiveException
block|{
name|inputs
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|table
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
name|inputs
operator|.
name|add
argument_list|(
operator|new
name|ReadEntity
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|path
init|=
name|table
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|FetchWork
name|work
init|=
operator|new
name|FetchWork
argument_list|(
name|path
argument_list|,
name|Utilities
operator|.
name|getTableDesc
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
name|PlanUtils
operator|.
name|configureInputJobPropertiesForStorageHandler
argument_list|(
name|work
operator|.
name|getTblDesc
argument_list|()
argument_list|)
expr_stmt|;
name|work
operator|.
name|setSplitSample
argument_list|(
name|splitSample
argument_list|)
expr_stmt|;
return|return
name|work
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|listP
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|PartitionDesc
argument_list|>
name|partP
init|=
operator|new
name|ArrayList
argument_list|<
name|PartitionDesc
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Partition
name|partition
range|:
name|partsList
operator|.
name|getNotDeniedPartns
argument_list|()
control|)
block|{
name|inputs
operator|.
name|add
argument_list|(
operator|new
name|ReadEntity
argument_list|(
name|partition
argument_list|)
argument_list|)
expr_stmt|;
name|listP
operator|.
name|add
argument_list|(
name|partition
operator|.
name|getPartitionPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|partP
operator|.
name|add
argument_list|(
name|Utilities
operator|.
name|getPartitionDesc
argument_list|(
name|partition
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Table
name|sourceTable
init|=
name|partsList
operator|.
name|getSourceTable
argument_list|()
decl_stmt|;
name|inputs
operator|.
name|add
argument_list|(
operator|new
name|ReadEntity
argument_list|(
name|sourceTable
argument_list|)
argument_list|)
expr_stmt|;
name|TableDesc
name|table
init|=
name|Utilities
operator|.
name|getTableDesc
argument_list|(
name|sourceTable
argument_list|)
decl_stmt|;
name|FetchWork
name|work
init|=
operator|new
name|FetchWork
argument_list|(
name|listP
argument_list|,
name|partP
argument_list|,
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|work
operator|.
name|getPartDesc
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|PartitionDesc
name|part0
init|=
name|work
operator|.
name|getPartDesc
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|PlanUtils
operator|.
name|configureInputJobPropertiesForStorageHandler
argument_list|(
name|part0
operator|.
name|getTableDesc
argument_list|()
argument_list|)
expr_stmt|;
name|work
operator|.
name|setSplitSample
argument_list|(
name|splitSample
argument_list|)
expr_stmt|;
block|}
return|return
name|work
return|;
block|}
comment|// this optimizer is for replacing FS to temp+fetching from temp with
comment|// single direct fetching, which means FS is not needed any more when conversion completed.
comment|// rows forwarded will be received by ListSinkOperator, which is replacing FS
specifier|private
name|ListSinkOperator
name|completed
parameter_list|(
name|ParseContext
name|pctx
parameter_list|,
name|FetchWork
name|work
parameter_list|)
block|{
name|pctx
operator|.
name|getSemanticInputs
argument_list|()
operator|.
name|addAll
argument_list|(
name|inputs
argument_list|)
expr_stmt|;
return|return
name|replaceFSwithLS
argument_list|(
name|fileSink
argument_list|,
name|work
operator|.
name|getSerializationNullFormat
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|long
name|getInputLength
parameter_list|(
name|ParseContext
name|pctx
parameter_list|,
name|long
name|remaining
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|splitSample
operator|!=
literal|null
operator|&&
name|splitSample
operator|.
name|getTotalLength
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|splitSample
operator|.
name|getTotalLength
argument_list|()
return|;
block|}
name|long
name|length
init|=
name|calculateLength
argument_list|(
name|pctx
argument_list|,
name|remaining
argument_list|)
decl_stmt|;
if|if
condition|(
name|splitSample
operator|!=
literal|null
condition|)
block|{
return|return
name|splitSample
operator|.
name|getTargetSize
argument_list|(
name|length
argument_list|)
return|;
block|}
return|return
name|length
return|;
block|}
specifier|private
name|long
name|calculateLength
parameter_list|(
name|ParseContext
name|pctx
parameter_list|,
name|long
name|remaining
parameter_list|)
throws|throws
name|Exception
block|{
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|pctx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|setColumnNameList
argument_list|(
name|jobConf
argument_list|,
name|scanOp
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|setColumnTypeList
argument_list|(
name|jobConf
argument_list|,
name|scanOp
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|HiveStorageHandler
name|handler
init|=
name|table
operator|.
name|getStorageHandler
argument_list|()
decl_stmt|;
if|if
condition|(
name|handler
operator|instanceof
name|InputEstimator
condition|)
block|{
name|InputEstimator
name|estimator
init|=
operator|(
name|InputEstimator
operator|)
name|handler
decl_stmt|;
name|TableDesc
name|tableDesc
init|=
name|Utilities
operator|.
name|getTableDesc
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|PlanUtils
operator|.
name|configureInputJobPropertiesForStorageHandler
argument_list|(
name|tableDesc
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|copyTableJobPropertiesToConf
argument_list|(
name|tableDesc
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
return|return
name|estimator
operator|.
name|estimate
argument_list|(
name|jobConf
argument_list|,
name|scanOp
argument_list|,
name|remaining
argument_list|)
operator|.
name|getTotalLength
argument_list|()
return|;
block|}
if|if
condition|(
name|table
operator|.
name|isNonNative
argument_list|()
condition|)
block|{
return|return
literal|0
return|;
comment|// nothing can be done
block|}
if|if
condition|(
operator|!
name|table
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
return|return
name|getFileLength
argument_list|(
name|jobConf
argument_list|,
name|table
operator|.
name|getPath
argument_list|()
argument_list|,
name|table
operator|.
name|getInputFormatClass
argument_list|()
argument_list|)
return|;
block|}
name|long
name|total
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Partition
name|partition
range|:
name|partsList
operator|.
name|getNotDeniedPartns
argument_list|()
control|)
block|{
name|Path
name|path
init|=
name|partition
operator|.
name|getPartitionPath
argument_list|()
decl_stmt|;
name|total
operator|+=
name|getFileLength
argument_list|(
name|jobConf
argument_list|,
name|path
argument_list|,
name|partition
operator|.
name|getInputFormatClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|total
return|;
block|}
comment|// from Utilities.getInputSummary()
specifier|private
name|long
name|getFileLength
parameter_list|(
name|JobConf
name|conf
parameter_list|,
name|Path
name|path
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|clazz
parameter_list|)
throws|throws
name|IOException
block|{
name|ContentSummary
name|summary
decl_stmt|;
if|if
condition|(
name|ContentSummaryInputFormat
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|clazz
argument_list|)
condition|)
block|{
name|InputFormat
name|input
init|=
name|HiveInputFormat
operator|.
name|getInputFormatFromCache
argument_list|(
name|clazz
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|summary
operator|=
operator|(
operator|(
name|ContentSummaryInputFormat
operator|)
name|input
operator|)
operator|.
name|getContentSummary
argument_list|(
name|path
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|summary
operator|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|getContentSummary
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
return|return
name|summary
operator|.
name|getLength
argument_list|()
return|;
block|}
block|}
specifier|public
specifier|static
name|ListSinkOperator
name|replaceFSwithLS
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|fileSink
parameter_list|,
name|String
name|nullFormat
parameter_list|)
block|{
name|ListSinkOperator
name|sink
init|=
operator|new
name|ListSinkOperator
argument_list|()
decl_stmt|;
name|sink
operator|.
name|setConf
argument_list|(
operator|new
name|ListSinkDesc
argument_list|(
name|nullFormat
argument_list|)
argument_list|)
expr_stmt|;
name|sink
operator|.
name|setParentOperators
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parent
init|=
name|fileSink
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|sink
operator|.
name|getParentOperators
argument_list|()
operator|.
name|add
argument_list|(
name|parent
argument_list|)
expr_stmt|;
name|parent
operator|.
name|replaceChild
argument_list|(
name|fileSink
argument_list|,
name|sink
argument_list|)
expr_stmt|;
name|fileSink
operator|.
name|setParentOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return
name|sink
return|;
block|}
block|}
end_class

end_unit

