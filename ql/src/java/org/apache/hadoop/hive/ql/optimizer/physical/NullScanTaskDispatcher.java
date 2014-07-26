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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Stack
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
name|Task
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
name|HiveIgnoreKeyTextOutputFormat
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
name|OneNullRowInputFormat
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
name|PreOrderWalker
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
name|optimizer
operator|.
name|physical
operator|.
name|MetadataOnlyOptimizer
operator|.
name|WalkerCtx
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
name|MapWork
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
name|serde
operator|.
name|serdeConstants
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
name|NullStructSerDe
import|;
end_import

begin_comment
comment|/**  * Iterate over all tasks one by one and removes all input paths from task if conditions as  * defined in rules match.  */
end_comment

begin_class
specifier|public
class|class
name|NullScanTaskDispatcher
implements|implements
name|Dispatcher
block|{
specifier|private
specifier|final
name|PhysicalContext
name|physicalContext
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|rules
decl_stmt|;
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|NullScanTaskDispatcher
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|NullScanTaskDispatcher
parameter_list|(
name|PhysicalContext
name|context
parameter_list|,
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|rules
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|physicalContext
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|rules
operator|=
name|rules
expr_stmt|;
block|}
specifier|private
name|String
name|getAliasForTableScanOperator
parameter_list|(
name|MapWork
name|work
parameter_list|,
name|TableScanOperator
name|tso
parameter_list|)
block|{
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
name|entry
range|:
name|work
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|==
name|tso
condition|)
block|{
return|return
name|entry
operator|.
name|getKey
argument_list|()
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|PartitionDesc
name|changePartitionToMetadataOnly
parameter_list|(
name|PartitionDesc
name|desc
parameter_list|)
block|{
if|if
condition|(
name|desc
operator|!=
literal|null
condition|)
block|{
name|desc
operator|.
name|setInputFileFormatClass
argument_list|(
name|OneNullRowInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setOutputFileFormatClass
argument_list|(
name|HiveIgnoreKeyTextOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|desc
operator|.
name|getProperties
argument_list|()
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_LIB
argument_list|,
name|NullStructSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|desc
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getPathsForAlias
parameter_list|(
name|MapWork
name|work
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|paths
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|work
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|contains
argument_list|(
name|alias
argument_list|)
condition|)
block|{
name|paths
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|paths
return|;
block|}
specifier|private
name|void
name|processAlias
parameter_list|(
name|MapWork
name|work
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
name|work
operator|.
name|setUseOneNullRowInputFormat
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Change the alias partition desc
name|PartitionDesc
name|aliasPartn
init|=
name|work
operator|.
name|getAliasToPartnInfo
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
name|changePartitionToMetadataOnly
argument_list|(
name|aliasPartn
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|paths
init|=
name|getPathsForAlias
argument_list|(
name|work
argument_list|,
name|alias
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|path
range|:
name|paths
control|)
block|{
name|PartitionDesc
name|partDesc
init|=
name|work
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|PartitionDesc
name|newPartition
init|=
name|changePartitionToMetadataOnly
argument_list|(
name|partDesc
argument_list|)
decl_stmt|;
name|Path
name|fakePath
init|=
operator|new
name|Path
argument_list|(
name|physicalContext
operator|.
name|getContext
argument_list|()
operator|.
name|getMRTmpPath
argument_list|()
operator|+
name|newPartition
operator|.
name|getTableName
argument_list|()
operator|+
name|encode
argument_list|(
name|newPartition
operator|.
name|getPartSpec
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|work
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|remove
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|work
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|put
argument_list|(
name|fakePath
operator|.
name|getName
argument_list|()
argument_list|,
name|newPartition
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|aliases
init|=
name|work
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|remove
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|work
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|put
argument_list|(
name|fakePath
operator|.
name|getName
argument_list|()
argument_list|,
name|aliases
argument_list|)
expr_stmt|;
block|}
block|}
comment|// considered using URLEncoder, but it seemed too much
specifier|private
name|String
name|encode
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
return|return
name|partSpec
operator|.
name|toString
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"[:/#\\?]"
argument_list|,
literal|"_"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|dispatch
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
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
init|=
operator|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|nd
decl_stmt|;
comment|// create a the context for walking operators
name|ParseContext
name|parseContext
init|=
name|physicalContext
operator|.
name|getParseContext
argument_list|()
decl_stmt|;
name|WalkerCtx
name|walkerCtx
init|=
operator|new
name|WalkerCtx
argument_list|()
decl_stmt|;
for|for
control|(
name|MapWork
name|mapWork
range|:
name|task
operator|.
name|getMapWork
argument_list|()
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Looking at: "
operator|+
name|mapWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|topOperators
init|=
name|mapWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|values
argument_list|()
decl_stmt|;
if|if
condition|(
name|topOperators
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No top operators"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Looking for table scans where optimization is applicable"
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest
comment|// matching rule and passes the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
literal|null
argument_list|,
name|rules
argument_list|,
name|walkerCtx
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|PreOrderWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
comment|// Create a list of topOp nodes
name|ArrayList
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
comment|// Get the top Nodes for this task
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|workOperator
range|:
name|topOperators
control|)
block|{
if|if
condition|(
name|parseContext
operator|.
name|getTopOps
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|contains
argument_list|(
name|workOperator
argument_list|)
condition|)
block|{
name|topNodes
operator|.
name|add
argument_list|(
name|workOperator
argument_list|)
expr_stmt|;
block|}
block|}
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|reducer
init|=
name|task
operator|.
name|getReducer
argument_list|(
name|mapWork
argument_list|)
decl_stmt|;
if|if
condition|(
name|reducer
operator|!=
literal|null
condition|)
block|{
name|topNodes
operator|.
name|add
argument_list|(
name|reducer
argument_list|)
expr_stmt|;
block|}
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Found %d null table scans"
argument_list|,
name|walkerCtx
operator|.
name|getMetadataOnlyTableScans
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|TableScanOperator
argument_list|>
name|iterator
init|=
name|walkerCtx
operator|.
name|getMetadataOnlyTableScans
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|TableScanOperator
name|tso
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|tso
operator|.
name|getConf
argument_list|()
operator|.
name|setIsMetadataOnly
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|String
name|alias
init|=
name|getAliasForTableScanOperator
argument_list|(
name|mapWork
argument_list|,
name|tso
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Null table scan for "
operator|+
name|alias
argument_list|)
expr_stmt|;
name|processAlias
argument_list|(
name|mapWork
argument_list|,
name|alias
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

