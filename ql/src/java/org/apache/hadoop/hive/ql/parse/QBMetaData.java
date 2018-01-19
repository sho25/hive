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
name|parse
package|;
end_package

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
name|Map
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
name|plan
operator|.
name|DynamicPartitionCtx
import|;
end_import

begin_comment
comment|/**  * Implementation of the metadata information related to a query block.  *  **/
end_comment

begin_class
specifier|public
class|class
name|QBMetaData
block|{
specifier|public
specifier|static
specifier|final
name|int
name|DEST_INVALID
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEST_TABLE
init|=
literal|1
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEST_PARTITION
init|=
literal|2
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEST_DFS_FILE
init|=
literal|3
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEST_REDUCE
init|=
literal|4
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEST_LOCAL_FILE
init|=
literal|5
decl_stmt|;
specifier|private
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|Table
argument_list|>
name|aliasToTable
decl_stmt|;
specifier|private
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|Table
argument_list|>
name|nameToDestTable
decl_stmt|;
specifier|private
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|Partition
argument_list|>
name|nameToDestPartition
decl_stmt|;
specifier|private
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|nameToDestFile
decl_stmt|;
specifier|private
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|nameToDestType
decl_stmt|;
specifier|private
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|aliasToPartSpec
decl_stmt|;
specifier|private
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|DynamicPartitionCtx
argument_list|>
name|aliasToDPCtx
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
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
name|QBMetaData
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|QBMetaData
parameter_list|()
block|{
comment|// Must be deterministic order map - see HIVE-8707
name|aliasToTable
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Table
argument_list|>
argument_list|()
expr_stmt|;
name|nameToDestTable
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Table
argument_list|>
argument_list|()
expr_stmt|;
name|nameToDestPartition
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Partition
argument_list|>
argument_list|()
expr_stmt|;
name|nameToDestFile
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|nameToDestType
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
comment|// Must be deterministic order maps - see HIVE-8707
name|aliasToPartSpec
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|aliasToDPCtx
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|DynamicPartitionCtx
argument_list|>
argument_list|()
expr_stmt|;
block|}
comment|// All getXXX needs toLowerCase() because they are directly called from
comment|// SemanticAnalyzer
comment|// All setXXX does not need it because they are called from QB which already
comment|// lowercases
comment|// the aliases.
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|Table
argument_list|>
name|getAliasToTable
parameter_list|()
block|{
return|return
name|aliasToTable
return|;
block|}
specifier|public
name|Table
name|getTableForAlias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|aliasToTable
operator|.
name|get
argument_list|(
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|void
name|setSrcForAlias
parameter_list|(
name|String
name|alias
parameter_list|,
name|Table
name|tab
parameter_list|)
block|{
name|aliasToTable
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|tab
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setDestForAlias
parameter_list|(
name|String
name|alias
parameter_list|,
name|Table
name|tab
parameter_list|)
block|{
name|nameToDestType
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|DEST_TABLE
argument_list|)
argument_list|)
expr_stmt|;
name|nameToDestTable
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|tab
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setDestForAlias
parameter_list|(
name|String
name|alias
parameter_list|,
name|Partition
name|part
parameter_list|)
block|{
name|nameToDestType
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|DEST_PARTITION
argument_list|)
argument_list|)
expr_stmt|;
name|nameToDestPartition
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|part
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setDestForAlias
parameter_list|(
name|String
name|alias
parameter_list|,
name|String
name|fname
parameter_list|,
name|boolean
name|isDfsFile
parameter_list|)
block|{
name|nameToDestType
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|isDfsFile
condition|?
name|Integer
operator|.
name|valueOf
argument_list|(
name|DEST_DFS_FILE
argument_list|)
else|:
name|Integer
operator|.
name|valueOf
argument_list|(
name|DEST_LOCAL_FILE
argument_list|)
argument_list|)
expr_stmt|;
name|nameToDestFile
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|fname
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Integer
name|getDestTypeForAlias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|nameToDestType
operator|.
name|get
argument_list|(
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param alias this is actually dest name, like insclause-0    */
specifier|public
name|Table
name|getDestTableForAlias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|nameToDestTable
operator|.
name|get
argument_list|(
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Table
argument_list|>
name|getNameToDestTable
parameter_list|()
block|{
return|return
name|nameToDestTable
return|;
block|}
specifier|public
name|Partition
name|getDestPartitionForAlias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|nameToDestPartition
operator|.
name|get
argument_list|(
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Partition
argument_list|>
name|getNameToDestPartition
parameter_list|()
block|{
return|return
name|nameToDestPartition
return|;
block|}
specifier|public
name|String
name|getDestFileForAlias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|nameToDestFile
operator|.
name|get
argument_list|(
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|Table
name|getSrcForAlias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|aliasToTable
operator|.
name|get
argument_list|(
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartSpecForAlias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|aliasToPartSpec
operator|.
name|get
argument_list|(
name|alias
argument_list|)
return|;
block|}
specifier|public
name|void
name|setPartSpecForAlias
parameter_list|(
name|String
name|alias
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|aliasToPartSpec
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|partSpec
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setDPCtx
parameter_list|(
name|String
name|alias
parameter_list|,
name|DynamicPartitionCtx
name|dpCtx
parameter_list|)
block|{
name|aliasToDPCtx
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|dpCtx
argument_list|)
expr_stmt|;
block|}
specifier|public
name|DynamicPartitionCtx
name|getDPCtx
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|aliasToDPCtx
operator|.
name|get
argument_list|(
name|alias
argument_list|)
return|;
block|}
block|}
end_class

end_unit

