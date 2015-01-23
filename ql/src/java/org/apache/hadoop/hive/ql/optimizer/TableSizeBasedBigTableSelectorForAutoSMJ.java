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
name|SemanticException
import|;
end_import

begin_comment
comment|/*  * This is a pluggable policy to chose the candidate map-join table for converting a join to a  * sort merge join. The largest table is chosen based on the size of the tables.  */
end_comment

begin_class
specifier|public
class|class
name|TableSizeBasedBigTableSelectorForAutoSMJ
extends|extends
name|SizeBasedBigTableSelectorForAutoSMJ
implements|implements
name|BigTableSelectorForAutoSMJ
block|{
specifier|public
name|int
name|getBigTablePosition
parameter_list|(
name|ParseContext
name|parseCtx
parameter_list|,
name|JoinOperator
name|joinOp
parameter_list|,
name|Set
argument_list|<
name|Integer
argument_list|>
name|bigTableCandidates
parameter_list|)
throws|throws
name|SemanticException
block|{
name|int
name|bigTablePos
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|maxSize
init|=
operator|-
literal|1
decl_stmt|;
name|HiveConf
name|conf
init|=
name|parseCtx
operator|.
name|getConf
argument_list|()
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|TableScanOperator
argument_list|>
name|topOps
init|=
operator|new
name|ArrayList
argument_list|<
name|TableScanOperator
argument_list|>
argument_list|()
decl_stmt|;
name|getListTopOps
argument_list|(
name|joinOp
argument_list|,
name|topOps
argument_list|)
expr_stmt|;
name|int
name|currentPos
init|=
literal|0
decl_stmt|;
for|for
control|(
name|TableScanOperator
name|topOp
range|:
name|topOps
control|)
block|{
if|if
condition|(
name|topOp
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
operator|!
name|bigTableCandidates
operator|.
name|contains
argument_list|(
name|currentPos
argument_list|)
condition|)
block|{
name|currentPos
operator|++
expr_stmt|;
continue|continue;
block|}
name|Table
name|table
init|=
name|topOp
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
decl_stmt|;
name|long
name|currentSize
init|=
literal|0
decl_stmt|;
if|if
condition|(
operator|!
name|table
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
name|currentSize
operator|=
name|getSize
argument_list|(
name|conf
argument_list|,
name|table
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// For partitioned tables, get the size of all the partitions
name|PrunedPartitionList
name|partsList
init|=
name|PartitionPruner
operator|.
name|prune
argument_list|(
name|topOp
argument_list|,
name|parseCtx
argument_list|,
literal|null
argument_list|)
decl_stmt|;
for|for
control|(
name|Partition
name|part
range|:
name|partsList
operator|.
name|getNotDeniedPartns
argument_list|()
control|)
block|{
name|currentSize
operator|+=
name|getSize
argument_list|(
name|conf
argument_list|,
name|part
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|currentSize
operator|>
name|maxSize
condition|)
block|{
name|maxSize
operator|=
name|currentSize
expr_stmt|;
name|bigTablePos
operator|=
name|currentPos
expr_stmt|;
block|}
name|currentPos
operator|++
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|bigTablePos
return|;
block|}
block|}
end_class

end_unit

