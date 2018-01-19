begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Set
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
name|metadata
operator|.
name|Partition
import|;
end_import

begin_class
specifier|public
class|class
name|BucketJoinProcCtx
implements|implements
name|NodeProcessorCtx
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
name|BucketJoinProcCtx
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|JoinOperator
argument_list|>
name|rejectedJoinOps
init|=
operator|new
name|HashSet
argument_list|<
name|JoinOperator
argument_list|>
argument_list|()
decl_stmt|;
comment|// The set of join operators which can be converted to a bucketed map join
specifier|private
name|Set
argument_list|<
name|JoinOperator
argument_list|>
name|convertedJoinOps
init|=
operator|new
name|HashSet
argument_list|<
name|JoinOperator
argument_list|>
argument_list|()
decl_stmt|;
comment|// In checking if a mapjoin can be converted to bucket mapjoin,
comment|// some join alias could be changed: alias -> newAlias
specifier|private
specifier|transient
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|aliasToNewAliasMap
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|tblAliasToNumberOfBucketsInEachPartition
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
name|tblAliasToBucketedFilePathsInEachPartition
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Partition
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|bigTblPartsToBucketFileNames
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Partition
argument_list|,
name|Integer
argument_list|>
name|bigTblPartsToBucketNumber
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|joinAliases
decl_stmt|;
specifier|private
name|String
name|baseBigAlias
decl_stmt|;
specifier|private
name|boolean
name|bigTablePartitioned
decl_stmt|;
specifier|public
name|BucketJoinProcCtx
parameter_list|(
name|HiveConf
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
specifier|public
name|HiveConf
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|public
name|Set
argument_list|<
name|JoinOperator
argument_list|>
name|getRejectedJoinOps
parameter_list|()
block|{
return|return
name|rejectedJoinOps
return|;
block|}
specifier|public
name|Set
argument_list|<
name|JoinOperator
argument_list|>
name|getConvertedJoinOps
parameter_list|()
block|{
return|return
name|convertedJoinOps
return|;
block|}
specifier|public
name|void
name|setRejectedJoinOps
parameter_list|(
name|Set
argument_list|<
name|JoinOperator
argument_list|>
name|rejectedJoinOps
parameter_list|)
block|{
name|this
operator|.
name|rejectedJoinOps
operator|=
name|rejectedJoinOps
expr_stmt|;
block|}
specifier|public
name|void
name|setConvertedJoinOps
parameter_list|(
name|Set
argument_list|<
name|JoinOperator
argument_list|>
name|setOfConvertedJoins
parameter_list|)
block|{
name|this
operator|.
name|convertedJoinOps
operator|=
name|setOfConvertedJoins
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|getTblAliasToNumberOfBucketsInEachPartition
parameter_list|()
block|{
return|return
name|tblAliasToNumberOfBucketsInEachPartition
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
name|getTblAliasToBucketedFilePathsInEachPartition
parameter_list|()
block|{
return|return
name|tblAliasToBucketedFilePathsInEachPartition
return|;
block|}
specifier|public
name|Map
argument_list|<
name|Partition
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getBigTblPartsToBucketFileNames
parameter_list|()
block|{
return|return
name|bigTblPartsToBucketFileNames
return|;
block|}
specifier|public
name|Map
argument_list|<
name|Partition
argument_list|,
name|Integer
argument_list|>
name|getBigTblPartsToBucketNumber
parameter_list|()
block|{
return|return
name|bigTblPartsToBucketNumber
return|;
block|}
specifier|public
name|void
name|setTblAliasToNumberOfBucketsInEachPartition
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|tblAliasToNumberOfBucketsInEachPartition
parameter_list|)
block|{
name|this
operator|.
name|tblAliasToNumberOfBucketsInEachPartition
operator|=
name|tblAliasToNumberOfBucketsInEachPartition
expr_stmt|;
block|}
specifier|public
name|void
name|setTblAliasToBucketedFilePathsInEachPartition
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
name|tblAliasToBucketedFilePathsInEachPartition
parameter_list|)
block|{
name|this
operator|.
name|tblAliasToBucketedFilePathsInEachPartition
operator|=
name|tblAliasToBucketedFilePathsInEachPartition
expr_stmt|;
block|}
specifier|public
name|void
name|setBigTblPartsToBucketFileNames
parameter_list|(
name|Map
argument_list|<
name|Partition
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|bigTblPartsToBucketFileNames
parameter_list|)
block|{
name|this
operator|.
name|bigTblPartsToBucketFileNames
operator|=
name|bigTblPartsToBucketFileNames
expr_stmt|;
block|}
specifier|public
name|void
name|setBigTblPartsToBucketNumber
parameter_list|(
name|Map
argument_list|<
name|Partition
argument_list|,
name|Integer
argument_list|>
name|bigTblPartsToBucketNumber
parameter_list|)
block|{
name|this
operator|.
name|bigTblPartsToBucketNumber
operator|=
name|bigTblPartsToBucketNumber
expr_stmt|;
block|}
specifier|public
name|void
name|setJoinAliases
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|joinAliases
parameter_list|)
block|{
name|this
operator|.
name|joinAliases
operator|=
name|joinAliases
expr_stmt|;
block|}
specifier|public
name|void
name|setBaseBigAlias
parameter_list|(
name|String
name|baseBigAlias
parameter_list|)
block|{
name|this
operator|.
name|baseBigAlias
operator|=
name|baseBigAlias
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getJoinAliases
parameter_list|()
block|{
return|return
name|joinAliases
return|;
block|}
specifier|public
name|String
name|getBaseBigAlias
parameter_list|()
block|{
return|return
name|baseBigAlias
return|;
block|}
specifier|public
name|boolean
name|isBigTablePartitioned
parameter_list|()
block|{
return|return
name|bigTablePartitioned
return|;
block|}
specifier|public
name|void
name|setBigTablePartitioned
parameter_list|(
name|boolean
name|bigTablePartitioned
parameter_list|)
block|{
name|this
operator|.
name|bigTablePartitioned
operator|=
name|bigTablePartitioned
expr_stmt|;
block|}
specifier|public
name|void
name|setAliasToNewAliasMap
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|aliasToNewAliasMap
parameter_list|)
block|{
name|this
operator|.
name|aliasToNewAliasMap
operator|=
name|aliasToNewAliasMap
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getAliasToNewAliasMap
parameter_list|()
block|{
return|return
name|aliasToNewAliasMap
return|;
block|}
block|}
end_class

end_unit

