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
name|plan
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
name|plan
operator|.
name|Explain
operator|.
name|Level
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
name|Explain
operator|.
name|Vectorization
import|;
end_import

begin_comment
comment|/**  * MapredLocalWork.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Map Reduce Local Work"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|,
name|vectorization
operator|=
name|Vectorization
operator|.
name|SUMMARY_PATH
argument_list|)
specifier|public
class|class
name|MapredLocalWork
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|LinkedHashMap
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
name|aliasToWork
decl_stmt|;
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|FetchWork
argument_list|>
name|aliasToFetchWork
decl_stmt|;
specifier|private
name|boolean
name|inputFileChangeSensitive
decl_stmt|;
specifier|private
name|BucketMapJoinContext
name|bucketMapjoinContext
decl_stmt|;
specifier|private
name|Path
name|tmpPath
decl_stmt|;
specifier|private
name|String
name|stageID
decl_stmt|;
comment|// Temp HDFS path for Spark HashTable sink
specifier|private
name|Path
name|tmpHDFSPath
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|dummyParentOp
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|MapJoinOperator
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|>
name|directFetchOp
decl_stmt|;
specifier|private
name|boolean
name|hasStagedAlias
decl_stmt|;
specifier|public
name|MapredLocalWork
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|LinkedHashMap
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
argument_list|,
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|FetchWork
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|dummyParentOp
operator|=
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
expr_stmt|;
name|this
operator|.
name|directFetchOp
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|MapJoinOperator
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|MapredLocalWork
parameter_list|(
specifier|final
name|LinkedHashMap
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
name|aliasToWork
parameter_list|,
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|FetchWork
argument_list|>
name|aliasToFetchWork
parameter_list|)
block|{
name|this
operator|.
name|aliasToWork
operator|=
name|aliasToWork
expr_stmt|;
name|this
operator|.
name|aliasToFetchWork
operator|=
name|aliasToFetchWork
expr_stmt|;
block|}
specifier|public
name|MapredLocalWork
parameter_list|(
name|MapredLocalWork
name|clone
parameter_list|)
block|{
name|this
operator|.
name|tmpPath
operator|=
name|clone
operator|.
name|tmpPath
expr_stmt|;
name|this
operator|.
name|inputFileChangeSensitive
operator|=
name|clone
operator|.
name|inputFileChangeSensitive
expr_stmt|;
block|}
specifier|public
name|void
name|setDummyParentOp
parameter_list|(
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|op
parameter_list|)
block|{
name|this
operator|.
name|dummyParentOp
operator|=
name|op
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|getDummyParentOp
parameter_list|()
block|{
return|return
name|dummyParentOp
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Alias -> Map Local Operator Tree"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|LinkedHashMap
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
name|getAliasToWork
parameter_list|()
block|{
return|return
name|aliasToWork
return|;
block|}
specifier|public
name|String
name|getStageID
parameter_list|()
block|{
return|return
name|stageID
return|;
block|}
specifier|public
name|void
name|setStageID
parameter_list|(
name|String
name|stageID
parameter_list|)
block|{
name|this
operator|.
name|stageID
operator|=
name|stageID
expr_stmt|;
block|}
specifier|public
name|void
name|setAliasToWork
parameter_list|(
specifier|final
name|LinkedHashMap
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
name|aliasToWork
parameter_list|)
block|{
name|this
operator|.
name|aliasToWork
operator|=
name|aliasToWork
expr_stmt|;
block|}
comment|/**    * @return the aliasToFetchWork    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Alias -> Map Local Tables"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|FetchWork
argument_list|>
name|getAliasToFetchWork
parameter_list|()
block|{
return|return
name|aliasToFetchWork
return|;
block|}
comment|/**    * @param aliasToFetchWork    *          the aliasToFetchWork to set    */
specifier|public
name|void
name|setAliasToFetchWork
parameter_list|(
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|FetchWork
argument_list|>
name|aliasToFetchWork
parameter_list|)
block|{
name|this
operator|.
name|aliasToFetchWork
operator|=
name|aliasToFetchWork
expr_stmt|;
block|}
specifier|public
name|boolean
name|getInputFileChangeSensitive
parameter_list|()
block|{
return|return
name|inputFileChangeSensitive
return|;
block|}
specifier|public
name|void
name|setInputFileChangeSensitive
parameter_list|(
name|boolean
name|inputFileChangeSensitive
parameter_list|)
block|{
name|this
operator|.
name|inputFileChangeSensitive
operator|=
name|inputFileChangeSensitive
expr_stmt|;
block|}
specifier|public
name|void
name|deriveExplainAttributes
parameter_list|()
block|{
if|if
condition|(
name|bucketMapjoinContext
operator|!=
literal|null
condition|)
block|{
name|bucketMapjoinContext
operator|.
name|deriveBucketMapJoinMapping
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|FetchWork
name|fetchWork
range|:
name|aliasToFetchWork
operator|.
name|values
argument_list|()
control|)
block|{
name|PlanUtils
operator|.
name|configureInputJobPropertiesForStorageHandler
argument_list|(
name|fetchWork
operator|.
name|getTblDesc
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Bucket Mapjoin Context"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|BucketMapJoinContext
name|getBucketMapjoinContextExplain
parameter_list|()
block|{
return|return
name|bucketMapjoinContext
operator|!=
literal|null
operator|&&
name|bucketMapjoinContext
operator|.
name|getBucketFileNameMapping
argument_list|()
operator|!=
literal|null
condition|?
name|bucketMapjoinContext
else|:
literal|null
return|;
block|}
specifier|public
name|BucketMapJoinContext
name|getBucketMapjoinContext
parameter_list|()
block|{
return|return
name|bucketMapjoinContext
return|;
block|}
specifier|public
name|void
name|setBucketMapjoinContext
parameter_list|(
name|BucketMapJoinContext
name|bucketMapjoinContext
parameter_list|)
block|{
name|this
operator|.
name|bucketMapjoinContext
operator|=
name|bucketMapjoinContext
expr_stmt|;
block|}
specifier|public
name|BucketMapJoinContext
name|copyPartSpecMappingOnly
parameter_list|()
block|{
if|if
condition|(
name|bucketMapjoinContext
operator|!=
literal|null
operator|&&
name|bucketMapjoinContext
operator|.
name|getBigTablePartSpecToFileMapping
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|BucketMapJoinContext
name|context
init|=
operator|new
name|BucketMapJoinContext
argument_list|()
decl_stmt|;
name|context
operator|.
name|setBigTablePartSpecToFileMapping
argument_list|(
name|bucketMapjoinContext
operator|.
name|getBigTablePartSpecToFileMapping
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|context
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|void
name|setTmpPath
parameter_list|(
name|Path
name|tmpPath
parameter_list|)
block|{
name|this
operator|.
name|tmpPath
operator|=
name|tmpPath
expr_stmt|;
block|}
specifier|public
name|Path
name|getTmpPath
parameter_list|()
block|{
return|return
name|tmpPath
return|;
block|}
specifier|public
name|void
name|setTmpHDFSPath
parameter_list|(
name|Path
name|tmpPath
parameter_list|)
block|{
name|this
operator|.
name|tmpHDFSPath
operator|=
name|tmpPath
expr_stmt|;
block|}
specifier|public
name|Path
name|getTmpHDFSPath
parameter_list|()
block|{
return|return
name|tmpHDFSPath
return|;
block|}
specifier|public
name|String
name|getBucketFileName
parameter_list|(
name|String
name|bigFileName
parameter_list|)
block|{
if|if
condition|(
operator|!
name|inputFileChangeSensitive
operator|||
name|bigFileName
operator|==
literal|null
operator|||
name|bigFileName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|"-"
return|;
block|}
name|String
name|fileName
init|=
name|getFileName
argument_list|(
name|bigFileName
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucketMapjoinContext
operator|!=
literal|null
condition|)
block|{
name|fileName
operator|=
name|bucketMapjoinContext
operator|.
name|createFileName
argument_list|(
name|bigFileName
argument_list|,
name|fileName
argument_list|)
expr_stmt|;
block|}
return|return
name|fileName
return|;
block|}
specifier|private
name|String
name|getFileName
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|int
name|last_separator
init|=
name|path
operator|.
name|lastIndexOf
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
decl_stmt|;
if|if
condition|(
name|last_separator
operator|<
literal|0
condition|)
block|{
return|return
name|path
return|;
block|}
return|return
name|path
operator|.
name|substring
argument_list|(
name|last_separator
operator|+
literal|1
argument_list|)
return|;
block|}
specifier|public
name|MapredLocalWork
name|extractDirectWorks
parameter_list|(
name|Map
argument_list|<
name|MapJoinOperator
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|>
name|directWorks
parameter_list|)
block|{
name|MapredLocalWork
name|newLocalWork
init|=
operator|new
name|MapredLocalWork
argument_list|()
decl_stmt|;
name|newLocalWork
operator|.
name|setTmpPath
argument_list|(
name|tmpPath
argument_list|)
expr_stmt|;
name|newLocalWork
operator|.
name|setInputFileChangeSensitive
argument_list|(
name|inputFileChangeSensitive
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|validWorks
init|=
name|getDirectWorks
argument_list|(
name|directWorks
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|validWorks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// all small aliases are staged.. no need full bucket context
name|newLocalWork
operator|.
name|setBucketMapjoinContext
argument_list|(
name|copyPartSpecMappingOnly
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|newLocalWork
return|;
block|}
name|newLocalWork
operator|.
name|directFetchOp
operator|=
operator|new
name|HashMap
argument_list|<
name|MapJoinOperator
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|>
argument_list|(
name|directWorks
argument_list|)
expr_stmt|;
name|newLocalWork
operator|.
name|aliasToWork
operator|=
operator|new
name|LinkedHashMap
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
name|newLocalWork
operator|.
name|aliasToFetchWork
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|FetchWork
argument_list|>
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|works
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|(
name|aliasToWork
argument_list|)
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
argument_list|>
argument_list|>
name|entry
range|:
name|works
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|alias
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|boolean
name|notStaged
init|=
name|validWorks
operator|.
name|contains
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|newLocalWork
operator|.
name|aliasToWork
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|notStaged
condition|?
name|aliasToWork
operator|.
name|remove
argument_list|(
name|alias
argument_list|)
else|:
literal|null
argument_list|)
expr_stmt|;
name|newLocalWork
operator|.
name|aliasToFetchWork
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|notStaged
condition|?
name|aliasToFetchWork
operator|.
name|remove
argument_list|(
name|alias
argument_list|)
else|:
literal|null
argument_list|)
expr_stmt|;
block|}
comment|// copy full bucket context
name|newLocalWork
operator|.
name|setBucketMapjoinContext
argument_list|(
name|getBucketMapjoinContext
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|newLocalWork
return|;
block|}
specifier|private
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|getDirectWorks
parameter_list|(
name|Collection
argument_list|<
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
name|values
parameter_list|)
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|operators
init|=
operator|new
name|HashSet
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|works
range|:
name|values
control|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|work
range|:
name|works
control|)
block|{
if|if
condition|(
name|work
operator|!=
literal|null
condition|)
block|{
name|operators
operator|.
name|add
argument_list|(
name|work
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|operators
return|;
block|}
specifier|public
name|void
name|setDirectFetchOp
parameter_list|(
name|Map
argument_list|<
name|MapJoinOperator
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|>
name|op
parameter_list|)
block|{
name|this
operator|.
name|directFetchOp
operator|=
name|op
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|MapJoinOperator
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|>
name|getDirectFetchOp
parameter_list|()
block|{
return|return
name|directFetchOp
return|;
block|}
specifier|public
name|boolean
name|hasStagedAlias
parameter_list|()
block|{
return|return
name|hasStagedAlias
return|;
block|}
specifier|public
name|void
name|setHasStagedAlias
parameter_list|(
name|boolean
name|hasStagedAlias
parameter_list|)
block|{
name|this
operator|.
name|hasStagedAlias
operator|=
name|hasStagedAlias
expr_stmt|;
block|}
block|}
end_class

end_unit

