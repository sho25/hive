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
name|util
operator|.
name|List
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
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|typeinfo
operator|.
name|TypeInfoFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * VectorGroupByDesc.  *  * Extra parameters beyond MapJoinDesc just for the vector map join operators.  *  * We don't extend MapJoinDesc because the base OperatorDesc doesn't support  * clone and adding it is a lot work for little gain.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinDesc
extends|extends
name|AbstractVectorDesc
block|{
specifier|private
specifier|static
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
specifier|static
enum|enum
name|HashTableImplementationType
block|{
name|NONE
block|,
name|OPTIMIZED
block|,
name|FAST
block|}
specifier|public
specifier|static
enum|enum
name|HashTableKind
block|{
name|NONE
block|,
name|HASH_SET
block|,
name|HASH_MULTISET
block|,
name|HASH_MAP
block|}
specifier|public
specifier|static
enum|enum
name|HashTableKeyType
block|{
name|NONE
block|,
name|BOOLEAN
block|,
name|BYTE
block|,
name|SHORT
block|,
name|INT
block|,
name|LONG
block|,
name|STRING
block|,
name|MULTI_KEY
block|;
specifier|public
name|PrimitiveTypeInfo
name|getPrimitiveTypeInfo
parameter_list|()
block|{
switch|switch
condition|(
name|this
condition|)
block|{
case|case
name|BOOLEAN
case|:
return|return
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
return|;
case|case
name|BYTE
case|:
return|return
name|TypeInfoFactory
operator|.
name|byteTypeInfo
return|;
case|case
name|INT
case|:
return|return
name|TypeInfoFactory
operator|.
name|intTypeInfo
return|;
case|case
name|LONG
case|:
return|return
name|TypeInfoFactory
operator|.
name|longTypeInfo
return|;
case|case
name|NONE
case|:
return|return
name|TypeInfoFactory
operator|.
name|voidTypeInfo
return|;
case|case
name|SHORT
case|:
return|return
name|TypeInfoFactory
operator|.
name|shortTypeInfo
return|;
case|case
name|STRING
case|:
return|return
name|TypeInfoFactory
operator|.
name|stringTypeInfo
return|;
case|case
name|MULTI_KEY
case|:
default|default:
return|return
literal|null
return|;
block|}
block|}
block|}
specifier|public
specifier|static
enum|enum
name|OperatorVariation
block|{
name|NONE
block|,
name|INNER_BIG_ONLY
block|,
name|INNER
block|,
name|LEFT_SEMI
block|,
name|OUTER
block|}
specifier|private
name|HashTableImplementationType
name|hashTableImplementationType
decl_stmt|;
specifier|private
name|HashTableKind
name|hashTableKind
decl_stmt|;
specifier|private
name|HashTableKeyType
name|hashTableKeyType
decl_stmt|;
specifier|private
name|OperatorVariation
name|operatorVariation
decl_stmt|;
specifier|private
name|boolean
name|minMaxEnabled
decl_stmt|;
specifier|private
name|VectorMapJoinInfo
name|vectorMapJoinInfo
decl_stmt|;
specifier|public
name|VectorMapJoinDesc
parameter_list|()
block|{
name|hashTableImplementationType
operator|=
name|HashTableImplementationType
operator|.
name|NONE
expr_stmt|;
name|hashTableKind
operator|=
name|HashTableKind
operator|.
name|NONE
expr_stmt|;
name|hashTableKeyType
operator|=
name|HashTableKeyType
operator|.
name|NONE
expr_stmt|;
name|operatorVariation
operator|=
name|OperatorVariation
operator|.
name|NONE
expr_stmt|;
name|minMaxEnabled
operator|=
literal|false
expr_stmt|;
name|vectorMapJoinInfo
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|VectorMapJoinDesc
parameter_list|(
name|VectorMapJoinDesc
name|clone
parameter_list|)
block|{
name|this
operator|.
name|hashTableImplementationType
operator|=
name|clone
operator|.
name|hashTableImplementationType
expr_stmt|;
name|this
operator|.
name|hashTableKind
operator|=
name|clone
operator|.
name|hashTableKind
expr_stmt|;
name|this
operator|.
name|hashTableKeyType
operator|=
name|clone
operator|.
name|hashTableKeyType
expr_stmt|;
name|this
operator|.
name|operatorVariation
operator|=
name|clone
operator|.
name|operatorVariation
expr_stmt|;
name|this
operator|.
name|minMaxEnabled
operator|=
name|clone
operator|.
name|minMaxEnabled
expr_stmt|;
name|this
operator|.
name|vectorMapJoinInfo
operator|=
name|clone
operator|.
name|vectorMapJoinInfo
expr_stmt|;
block|}
specifier|public
name|HashTableImplementationType
name|hashTableImplementationType
parameter_list|()
block|{
return|return
name|hashTableImplementationType
return|;
block|}
specifier|public
name|void
name|setHashTableImplementationType
parameter_list|(
name|HashTableImplementationType
name|hashTableImplementationType
parameter_list|)
block|{
name|this
operator|.
name|hashTableImplementationType
operator|=
name|hashTableImplementationType
expr_stmt|;
block|}
specifier|public
name|HashTableKind
name|hashTableKind
parameter_list|()
block|{
return|return
name|hashTableKind
return|;
block|}
specifier|public
name|void
name|setHashTableKind
parameter_list|(
name|HashTableKind
name|hashTableKind
parameter_list|)
block|{
name|this
operator|.
name|hashTableKind
operator|=
name|hashTableKind
expr_stmt|;
block|}
specifier|public
name|HashTableKeyType
name|hashTableKeyType
parameter_list|()
block|{
return|return
name|hashTableKeyType
return|;
block|}
specifier|public
name|void
name|setHashTableKeyType
parameter_list|(
name|HashTableKeyType
name|hashTableKeyType
parameter_list|)
block|{
name|this
operator|.
name|hashTableKeyType
operator|=
name|hashTableKeyType
expr_stmt|;
block|}
specifier|public
name|OperatorVariation
name|operatorVariation
parameter_list|()
block|{
return|return
name|operatorVariation
return|;
block|}
specifier|public
name|void
name|setOperatorVariation
parameter_list|(
name|OperatorVariation
name|operatorVariation
parameter_list|)
block|{
name|this
operator|.
name|operatorVariation
operator|=
name|operatorVariation
expr_stmt|;
block|}
specifier|public
name|boolean
name|minMaxEnabled
parameter_list|()
block|{
return|return
name|minMaxEnabled
return|;
block|}
specifier|public
name|void
name|setMinMaxEnabled
parameter_list|(
name|boolean
name|minMaxEnabled
parameter_list|)
block|{
name|this
operator|.
name|minMaxEnabled
operator|=
name|minMaxEnabled
expr_stmt|;
block|}
specifier|public
name|void
name|setVectorMapJoinInfo
parameter_list|(
name|VectorMapJoinInfo
name|vectorMapJoinInfo
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|vectorMapJoinInfo
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|vectorMapJoinInfo
operator|=
name|vectorMapJoinInfo
expr_stmt|;
block|}
specifier|public
name|VectorMapJoinInfo
name|getVectorMapJoinInfo
parameter_list|()
block|{
return|return
name|vectorMapJoinInfo
return|;
block|}
specifier|private
name|boolean
name|isVectorizationMapJoinNativeEnabled
decl_stmt|;
specifier|private
name|String
name|engine
decl_stmt|;
specifier|private
name|boolean
name|oneMapJoinCondition
decl_stmt|;
specifier|private
name|boolean
name|hasNullSafes
decl_stmt|;
specifier|private
name|boolean
name|isFastHashTableEnabled
decl_stmt|;
specifier|private
name|boolean
name|isHybridHashJoin
decl_stmt|;
specifier|private
name|boolean
name|supportsKeyTypes
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|notSupportedKeyTypes
decl_stmt|;
specifier|private
name|boolean
name|isEmptyKey
decl_stmt|;
specifier|private
name|boolean
name|smallTableExprVectorizes
decl_stmt|;
specifier|public
name|void
name|setIsVectorizationMapJoinNativeEnabled
parameter_list|(
name|boolean
name|isVectorizationMapJoinNativeEnabled
parameter_list|)
block|{
name|this
operator|.
name|isVectorizationMapJoinNativeEnabled
operator|=
name|isVectorizationMapJoinNativeEnabled
expr_stmt|;
block|}
specifier|public
name|boolean
name|getIsVectorizationMapJoinNativeEnabled
parameter_list|()
block|{
return|return
name|isVectorizationMapJoinNativeEnabled
return|;
block|}
specifier|public
name|void
name|setEngine
parameter_list|(
name|String
name|engine
parameter_list|)
block|{
name|this
operator|.
name|engine
operator|=
name|engine
expr_stmt|;
block|}
specifier|public
name|String
name|getEngine
parameter_list|()
block|{
return|return
name|engine
return|;
block|}
specifier|public
name|void
name|setOneMapJoinCondition
parameter_list|(
name|boolean
name|oneMapJoinCondition
parameter_list|)
block|{
name|this
operator|.
name|oneMapJoinCondition
operator|=
name|oneMapJoinCondition
expr_stmt|;
block|}
specifier|public
name|boolean
name|getOneMapJoinCondition
parameter_list|()
block|{
return|return
name|oneMapJoinCondition
return|;
block|}
specifier|public
name|void
name|setHasNullSafes
parameter_list|(
name|boolean
name|hasNullSafes
parameter_list|)
block|{
name|this
operator|.
name|hasNullSafes
operator|=
name|hasNullSafes
expr_stmt|;
block|}
specifier|public
name|boolean
name|getHasNullSafes
parameter_list|()
block|{
return|return
name|hasNullSafes
return|;
block|}
specifier|public
name|void
name|setSupportsKeyTypes
parameter_list|(
name|boolean
name|supportsKeyTypes
parameter_list|)
block|{
name|this
operator|.
name|supportsKeyTypes
operator|=
name|supportsKeyTypes
expr_stmt|;
block|}
specifier|public
name|boolean
name|getSupportsKeyTypes
parameter_list|()
block|{
return|return
name|supportsKeyTypes
return|;
block|}
specifier|public
name|void
name|setNotSupportedKeyTypes
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|notSupportedKeyTypes
parameter_list|)
block|{
name|this
operator|.
name|notSupportedKeyTypes
operator|=
name|notSupportedKeyTypes
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getNotSupportedKeyTypes
parameter_list|()
block|{
return|return
name|notSupportedKeyTypes
return|;
block|}
specifier|public
name|void
name|setIsEmptyKey
parameter_list|(
name|boolean
name|isEmptyKey
parameter_list|)
block|{
name|this
operator|.
name|isEmptyKey
operator|=
name|isEmptyKey
expr_stmt|;
block|}
specifier|public
name|boolean
name|getIsEmptyKey
parameter_list|()
block|{
return|return
name|isEmptyKey
return|;
block|}
specifier|public
name|void
name|setSmallTableExprVectorizes
parameter_list|(
name|boolean
name|smallTableExprVectorizes
parameter_list|)
block|{
name|this
operator|.
name|smallTableExprVectorizes
operator|=
name|smallTableExprVectorizes
expr_stmt|;
block|}
specifier|public
name|boolean
name|getSmallTableExprVectorizes
parameter_list|()
block|{
return|return
name|smallTableExprVectorizes
return|;
block|}
specifier|public
name|void
name|setIsFastHashTableEnabled
parameter_list|(
name|boolean
name|isFastHashTableEnabled
parameter_list|)
block|{
name|this
operator|.
name|isFastHashTableEnabled
operator|=
name|isFastHashTableEnabled
expr_stmt|;
block|}
specifier|public
name|boolean
name|getIsFastHashTableEnabled
parameter_list|()
block|{
return|return
name|isFastHashTableEnabled
return|;
block|}
specifier|public
name|void
name|setIsHybridHashJoin
parameter_list|(
name|boolean
name|isHybridHashJoin
parameter_list|)
block|{
name|this
operator|.
name|isHybridHashJoin
operator|=
name|isHybridHashJoin
expr_stmt|;
block|}
specifier|public
name|boolean
name|getIsHybridHashJoin
parameter_list|()
block|{
return|return
name|isHybridHashJoin
return|;
block|}
block|}
end_class

end_unit

