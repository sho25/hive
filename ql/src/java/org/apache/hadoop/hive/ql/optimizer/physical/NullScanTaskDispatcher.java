begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|collections
operator|.
name|CollectionUtils
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
name|common
operator|.
name|StringInternUtils
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
name|NullScanFileSystem
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
name|io
operator|.
name|ZeroRowsInputFormat
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
name|SemanticDispatcher
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
name|SemanticGraphWalker
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
name|SemanticNodeProcessor
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
name|PreOrderOnceWalker
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
name|SemanticRule
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

begin_comment
comment|/**  * Iterate over all tasks one by one and removes all input paths from task if  * conditions as defined in rules match.  */
end_comment

begin_class
specifier|public
class|class
name|NullScanTaskDispatcher
implements|implements
name|SemanticDispatcher
block|{
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|NullScanTaskDispatcher
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|PhysicalContext
name|physicalContext
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|SemanticRule
argument_list|,
name|SemanticNodeProcessor
argument_list|>
name|rules
decl_stmt|;
specifier|public
name|NullScanTaskDispatcher
parameter_list|(
name|PhysicalContext
name|context
parameter_list|,
name|Map
argument_list|<
name|SemanticRule
argument_list|,
name|SemanticNodeProcessor
argument_list|>
name|rules
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
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
parameter_list|,
name|Path
name|path
parameter_list|)
block|{
if|if
condition|(
name|desc
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|boolean
name|isEmpty
init|=
literal|false
decl_stmt|;
try|try
block|{
name|isEmpty
operator|=
name|Utilities
operator|.
name|isEmptyPath
argument_list|(
name|physicalContext
operator|.
name|getConf
argument_list|()
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Cannot determine if the table is empty"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|desc
operator|.
name|setInputFileFormatClass
argument_list|(
name|isEmpty
condition|?
name|ZeroRowsInputFormat
operator|.
name|class
else|:
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
return|return
name|desc
return|;
block|}
specifier|private
name|void
name|processAlias
parameter_list|(
name|MapWork
name|work
parameter_list|,
name|Path
name|path
parameter_list|,
name|Collection
argument_list|<
name|String
argument_list|>
name|aliasesAffected
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|aliases
parameter_list|)
block|{
comment|// the aliases that are allowed to map to a null scan.
name|Collection
argument_list|<
name|String
argument_list|>
name|allowed
init|=
name|aliasesAffected
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|a
lambda|->
name|aliases
operator|.
name|contains
argument_list|(
name|a
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|allowed
operator|.
name|isEmpty
argument_list|()
condition|)
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
operator|.
name|clone
argument_list|()
decl_stmt|;
name|PartitionDesc
name|newPartition
init|=
name|changePartitionToMetadataOnly
argument_list|(
name|partDesc
argument_list|,
name|path
argument_list|)
decl_stmt|;
comment|// Prefix partition with something to avoid it being a hidden file.
name|Path
name|fakePath
init|=
operator|new
name|Path
argument_list|(
name|NullScanFileSystem
operator|.
name|getBase
argument_list|()
operator|+
name|newPartition
operator|.
name|getTableName
argument_list|()
operator|+
literal|"/part"
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
name|StringInternUtils
operator|.
name|internUriStringsInPath
argument_list|(
name|fakePath
argument_list|)
expr_stmt|;
name|work
operator|.
name|addPathToPartitionInfo
argument_list|(
name|fakePath
argument_list|,
name|newPartition
argument_list|)
expr_stmt|;
name|work
operator|.
name|addPathToAlias
argument_list|(
name|fakePath
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|allowed
argument_list|)
argument_list|)
expr_stmt|;
name|aliasesAffected
operator|.
name|removeAll
argument_list|(
name|allowed
argument_list|)
expr_stmt|;
if|if
condition|(
name|aliasesAffected
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|work
operator|.
name|removePathToAlias
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|work
operator|.
name|removePathToPartitionInfo
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|processAlias
parameter_list|(
name|MapWork
name|work
parameter_list|,
name|Set
argument_list|<
name|TableScanOperator
argument_list|>
name|tableScans
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|aliases
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|TableScanOperator
name|tso
range|:
name|tableScans
control|)
block|{
comment|// use LinkedHashMap<String, Operator<? extends OperatorDesc>>
comment|// getAliasToWork() should not apply this for non-native table
if|if
condition|(
name|tso
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
operator|.
name|getStorageHandler
argument_list|()
operator|!=
literal|null
condition|)
block|{
continue|continue;
block|}
name|String
name|alias
init|=
name|getAliasForTableScanOperator
argument_list|(
name|work
argument_list|,
name|tso
argument_list|)
decl_stmt|;
name|aliases
operator|.
name|add
argument_list|(
name|alias
argument_list|)
expr_stmt|;
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
block|}
comment|// group path alias according to work
name|Map
argument_list|<
name|Path
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|candidates
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|work
operator|.
name|getPaths
argument_list|()
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|aliasesAffected
init|=
name|work
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|CollectionUtils
operator|.
name|isNotEmpty
argument_list|(
name|aliasesAffected
argument_list|)
condition|)
block|{
name|candidates
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|aliasesAffected
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|Entry
argument_list|<
name|Path
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|candidates
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|processAlias
argument_list|(
name|work
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
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
literal|"[{}:/#\\?, ]+"
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
argument_list|>
name|task
init|=
operator|(
name|Task
argument_list|<
name|?
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
name|List
argument_list|<
name|MapWork
argument_list|>
name|mapWorks
init|=
operator|new
name|ArrayList
argument_list|<
name|MapWork
argument_list|>
argument_list|(
name|task
operator|.
name|getMapWork
argument_list|()
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|mapWorks
argument_list|,
operator|new
name|Comparator
argument_list|<
name|MapWork
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|MapWork
name|o1
parameter_list|,
name|MapWork
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|getName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
for|for
control|(
name|MapWork
name|mapWork
range|:
name|mapWorks
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Looking at: {}"
argument_list|,
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
name|isEmpty
argument_list|()
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
name|debug
argument_list|(
literal|"Looking for table scans where optimization is applicable"
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest
comment|// matching rule and passes the context along
name|SemanticDispatcher
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
name|SemanticGraphWalker
name|ogw
init|=
operator|new
name|PreOrderOnceWalker
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
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Get the top Nodes for this task
name|Collection
argument_list|<
name|TableScanOperator
argument_list|>
name|topOps
init|=
name|parseContext
operator|.
name|getTopOps
argument_list|()
operator|.
name|values
argument_list|()
decl_stmt|;
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
name|topOps
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
name|int
name|scanTableSize
init|=
name|walkerCtx
operator|.
name|getMetadataOnlyTableScans
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found {} null table scans"
argument_list|,
name|scanTableSize
argument_list|)
expr_stmt|;
if|if
condition|(
name|scanTableSize
operator|>
literal|0
condition|)
block|{
name|processAlias
argument_list|(
name|mapWork
argument_list|,
name|walkerCtx
operator|.
name|getMetadataOnlyTableScans
argument_list|()
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

