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
name|HashMap
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

begin_comment
comment|/**  * Map Join operator Descriptor implementation.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"HashTable Sink Operator"
argument_list|)
specifier|public
class|class
name|HashTableSinkDesc
extends|extends
name|JoinDesc
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
comment|// used to handle skew join
specifier|private
name|boolean
name|handleSkewJoin
init|=
literal|false
decl_stmt|;
specifier|private
name|int
name|skewKeyDefinition
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|bigKeysDirMap
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|>
name|smallKeysDirMap
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|TableDesc
argument_list|>
name|skewKeysValuesTables
decl_stmt|;
comment|// alias to key mapping
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|exprs
decl_stmt|;
comment|// alias to filter mapping
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|filters
decl_stmt|;
comment|// used for create joinOutputObjectInspector
specifier|protected
name|List
argument_list|<
name|String
argument_list|>
name|outputColumnNames
decl_stmt|;
comment|// key:column output name, value:tag
specifier|private
specifier|transient
name|Map
argument_list|<
name|String
argument_list|,
name|Byte
argument_list|>
name|reversedExprs
decl_stmt|;
comment|// No outer join involved
specifier|protected
name|boolean
name|noOuterJoin
decl_stmt|;
specifier|protected
name|JoinCondDesc
index|[]
name|conds
decl_stmt|;
specifier|protected
name|Byte
index|[]
name|tagOrder
decl_stmt|;
specifier|private
name|TableDesc
name|keyTableDesc
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|keys
decl_stmt|;
specifier|private
name|TableDesc
name|keyTblDesc
decl_stmt|;
specifier|private
name|List
argument_list|<
name|TableDesc
argument_list|>
name|valueTblDescs
decl_stmt|;
specifier|private
name|List
argument_list|<
name|TableDesc
argument_list|>
name|valueTblFilteredDescs
decl_stmt|;
specifier|private
name|int
name|posBigTable
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|retainList
decl_stmt|;
specifier|private
specifier|transient
name|BucketMapJoinContext
name|bucketMapjoinContext
decl_stmt|;
specifier|private
name|float
name|hashtableMemoryUsage
decl_stmt|;
comment|//map join dump file name
specifier|private
name|String
name|dumpFilePrefix
decl_stmt|;
specifier|public
name|HashTableSinkDesc
parameter_list|()
block|{
name|bucketMapjoinContext
operator|=
operator|new
name|BucketMapJoinContext
argument_list|()
expr_stmt|;
block|}
specifier|public
name|HashTableSinkDesc
parameter_list|(
name|MapJoinDesc
name|clone
parameter_list|)
block|{
name|this
operator|.
name|bigKeysDirMap
operator|=
name|clone
operator|.
name|getBigKeysDirMap
argument_list|()
expr_stmt|;
name|this
operator|.
name|conds
operator|=
name|clone
operator|.
name|getConds
argument_list|()
expr_stmt|;
name|this
operator|.
name|exprs
operator|=
name|clone
operator|.
name|getExprs
argument_list|()
expr_stmt|;
name|this
operator|.
name|handleSkewJoin
operator|=
name|clone
operator|.
name|getHandleSkewJoin
argument_list|()
expr_stmt|;
name|this
operator|.
name|keyTableDesc
operator|=
name|clone
operator|.
name|getKeyTableDesc
argument_list|()
expr_stmt|;
name|this
operator|.
name|noOuterJoin
operator|=
name|clone
operator|.
name|getNoOuterJoin
argument_list|()
expr_stmt|;
name|this
operator|.
name|outputColumnNames
operator|=
name|clone
operator|.
name|getOutputColumnNames
argument_list|()
expr_stmt|;
name|this
operator|.
name|reversedExprs
operator|=
name|clone
operator|.
name|getReversedExprs
argument_list|()
expr_stmt|;
name|this
operator|.
name|skewKeyDefinition
operator|=
name|clone
operator|.
name|getSkewKeyDefinition
argument_list|()
expr_stmt|;
name|this
operator|.
name|skewKeysValuesTables
operator|=
name|clone
operator|.
name|getSkewKeysValuesTables
argument_list|()
expr_stmt|;
name|this
operator|.
name|smallKeysDirMap
operator|=
name|clone
operator|.
name|getSmallKeysDirMap
argument_list|()
expr_stmt|;
name|this
operator|.
name|tagOrder
operator|=
name|clone
operator|.
name|getTagOrder
argument_list|()
expr_stmt|;
name|this
operator|.
name|filters
operator|=
name|clone
operator|.
name|getFilters
argument_list|()
expr_stmt|;
name|this
operator|.
name|keys
operator|=
name|clone
operator|.
name|getKeys
argument_list|()
expr_stmt|;
name|this
operator|.
name|keyTblDesc
operator|=
name|clone
operator|.
name|getKeyTblDesc
argument_list|()
expr_stmt|;
name|this
operator|.
name|valueTblDescs
operator|=
name|clone
operator|.
name|getValueTblDescs
argument_list|()
expr_stmt|;
name|this
operator|.
name|valueTblFilteredDescs
operator|=
name|clone
operator|.
name|getValueFilteredTblDescs
argument_list|()
expr_stmt|;
name|this
operator|.
name|posBigTable
operator|=
name|clone
operator|.
name|getPosBigTable
argument_list|()
expr_stmt|;
name|this
operator|.
name|retainList
operator|=
name|clone
operator|.
name|getRetainList
argument_list|()
expr_stmt|;
name|this
operator|.
name|dumpFilePrefix
operator|=
name|clone
operator|.
name|getDumpFilePrefix
argument_list|()
expr_stmt|;
name|this
operator|.
name|bucketMapjoinContext
operator|=
operator|new
name|BucketMapJoinContext
argument_list|(
name|clone
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|initRetainExprList
parameter_list|()
block|{
name|retainList
operator|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|Set
argument_list|<
name|Entry
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
argument_list|>
name|set
init|=
name|exprs
operator|.
name|entrySet
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
argument_list|>
name|setIter
init|=
name|set
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|setIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Entry
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|current
init|=
name|setIter
operator|.
name|next
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
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
name|current
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|retainList
operator|.
name|put
argument_list|(
name|current
operator|.
name|getKey
argument_list|()
argument_list|,
name|list
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|float
name|getHashtableMemoryUsage
parameter_list|()
block|{
return|return
name|hashtableMemoryUsage
return|;
block|}
specifier|public
name|void
name|setHashtableMemoryUsage
parameter_list|(
name|float
name|hashtableMemoryUsage
parameter_list|)
block|{
name|this
operator|.
name|hashtableMemoryUsage
operator|=
name|hashtableMemoryUsage
expr_stmt|;
block|}
comment|/**    * @return the dumpFilePrefix    */
specifier|public
name|String
name|getDumpFilePrefix
parameter_list|()
block|{
return|return
name|dumpFilePrefix
return|;
block|}
comment|/**    * @param dumpFilePrefix    *          the dumpFilePrefix to set    */
specifier|public
name|void
name|setDumpFilePrefix
parameter_list|(
name|String
name|dumpFilePrefix
parameter_list|)
block|{
name|this
operator|.
name|dumpFilePrefix
operator|=
name|dumpFilePrefix
expr_stmt|;
block|}
specifier|public
name|boolean
name|isHandleSkewJoin
parameter_list|()
block|{
return|return
name|handleSkewJoin
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setHandleSkewJoin
parameter_list|(
name|boolean
name|handleSkewJoin
parameter_list|)
block|{
name|this
operator|.
name|handleSkewJoin
operator|=
name|handleSkewJoin
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getSkewKeyDefinition
parameter_list|()
block|{
return|return
name|skewKeyDefinition
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setSkewKeyDefinition
parameter_list|(
name|int
name|skewKeyDefinition
parameter_list|)
block|{
name|this
operator|.
name|skewKeyDefinition
operator|=
name|skewKeyDefinition
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|getBigKeysDirMap
parameter_list|()
block|{
return|return
name|bigKeysDirMap
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setBigKeysDirMap
parameter_list|(
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|bigKeysDirMap
parameter_list|)
block|{
name|this
operator|.
name|bigKeysDirMap
operator|=
name|bigKeysDirMap
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|Byte
argument_list|,
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|>
name|getSmallKeysDirMap
parameter_list|()
block|{
return|return
name|smallKeysDirMap
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setSmallKeysDirMap
parameter_list|(
name|Map
argument_list|<
name|Byte
argument_list|,
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|>
name|smallKeysDirMap
parameter_list|)
block|{
name|this
operator|.
name|smallKeysDirMap
operator|=
name|smallKeysDirMap
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|Byte
argument_list|,
name|TableDesc
argument_list|>
name|getSkewKeysValuesTables
parameter_list|()
block|{
return|return
name|skewKeysValuesTables
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setSkewKeysValuesTables
parameter_list|(
name|Map
argument_list|<
name|Byte
argument_list|,
name|TableDesc
argument_list|>
name|skewKeysValuesTables
parameter_list|)
block|{
name|this
operator|.
name|skewKeysValuesTables
operator|=
name|skewKeysValuesTables
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|getExprs
parameter_list|()
block|{
return|return
name|exprs
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setExprs
parameter_list|(
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|exprs
parameter_list|)
block|{
name|this
operator|.
name|exprs
operator|=
name|exprs
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|getFilters
parameter_list|()
block|{
return|return
name|filters
return|;
block|}
specifier|public
name|List
argument_list|<
name|TableDesc
argument_list|>
name|getValueTblFilteredDescs
parameter_list|()
block|{
return|return
name|valueTblFilteredDescs
return|;
block|}
specifier|public
name|void
name|setValueTblFilteredDescs
parameter_list|(
name|List
argument_list|<
name|TableDesc
argument_list|>
name|valueTblFilteredDescs
parameter_list|)
block|{
name|this
operator|.
name|valueTblFilteredDescs
operator|=
name|valueTblFilteredDescs
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setFilters
parameter_list|(
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|filters
parameter_list|)
block|{
name|this
operator|.
name|filters
operator|=
name|filters
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getOutputColumnNames
parameter_list|()
block|{
return|return
name|outputColumnNames
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setOutputColumnNames
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|outputColumnNames
parameter_list|)
block|{
name|this
operator|.
name|outputColumnNames
operator|=
name|outputColumnNames
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Byte
argument_list|>
name|getReversedExprs
parameter_list|()
block|{
return|return
name|reversedExprs
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setReversedExprs
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Byte
argument_list|>
name|reversedExprs
parameter_list|)
block|{
name|this
operator|.
name|reversedExprs
operator|=
name|reversedExprs
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNoOuterJoin
parameter_list|()
block|{
return|return
name|noOuterJoin
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setNoOuterJoin
parameter_list|(
name|boolean
name|noOuterJoin
parameter_list|)
block|{
name|this
operator|.
name|noOuterJoin
operator|=
name|noOuterJoin
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|JoinCondDesc
index|[]
name|getConds
parameter_list|()
block|{
return|return
name|conds
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConds
parameter_list|(
name|JoinCondDesc
index|[]
name|conds
parameter_list|)
block|{
name|this
operator|.
name|conds
operator|=
name|conds
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Byte
index|[]
name|getTagOrder
parameter_list|()
block|{
return|return
name|tagOrder
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setTagOrder
parameter_list|(
name|Byte
index|[]
name|tagOrder
parameter_list|)
block|{
name|this
operator|.
name|tagOrder
operator|=
name|tagOrder
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TableDesc
name|getKeyTableDesc
parameter_list|()
block|{
return|return
name|keyTableDesc
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setKeyTableDesc
parameter_list|(
name|TableDesc
name|keyTableDesc
parameter_list|)
block|{
name|this
operator|.
name|keyTableDesc
operator|=
name|keyTableDesc
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|getRetainList
parameter_list|()
block|{
return|return
name|retainList
return|;
block|}
specifier|public
name|void
name|setRetainList
parameter_list|(
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|retainList
parameter_list|)
block|{
name|this
operator|.
name|retainList
operator|=
name|retainList
expr_stmt|;
block|}
comment|/**    * @return the keys    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"keys"
argument_list|)
specifier|public
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|getKeys
parameter_list|()
block|{
return|return
name|keys
return|;
block|}
comment|/**    * @param keys    *          the keys to set    */
specifier|public
name|void
name|setKeys
parameter_list|(
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|keys
parameter_list|)
block|{
name|this
operator|.
name|keys
operator|=
name|keys
expr_stmt|;
block|}
comment|/**    * @return the position of the big table not in memory    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Position of Big Table"
argument_list|)
specifier|public
name|int
name|getPosBigTable
parameter_list|()
block|{
return|return
name|posBigTable
return|;
block|}
comment|/**    * @param posBigTable    *          the position of the big table not in memory    */
specifier|public
name|void
name|setPosBigTable
parameter_list|(
name|int
name|posBigTable
parameter_list|)
block|{
name|this
operator|.
name|posBigTable
operator|=
name|posBigTable
expr_stmt|;
block|}
comment|/**    * @return the keyTblDesc    */
specifier|public
name|TableDesc
name|getKeyTblDesc
parameter_list|()
block|{
return|return
name|keyTblDesc
return|;
block|}
comment|/**    * @param keyTblDesc    *          the keyTblDesc to set    */
specifier|public
name|void
name|setKeyTblDesc
parameter_list|(
name|TableDesc
name|keyTblDesc
parameter_list|)
block|{
name|this
operator|.
name|keyTblDesc
operator|=
name|keyTblDesc
expr_stmt|;
block|}
comment|/**    * @return the valueTblDescs    */
specifier|public
name|List
argument_list|<
name|TableDesc
argument_list|>
name|getValueTblDescs
parameter_list|()
block|{
return|return
name|valueTblDescs
return|;
block|}
comment|/**    * @param valueTblDescs    *          the valueTblDescs to set    */
specifier|public
name|void
name|setValueTblDescs
parameter_list|(
name|List
argument_list|<
name|TableDesc
argument_list|>
name|valueTblDescs
parameter_list|)
block|{
name|this
operator|.
name|valueTblDescs
operator|=
name|valueTblDescs
expr_stmt|;
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
block|}
end_class

end_unit

