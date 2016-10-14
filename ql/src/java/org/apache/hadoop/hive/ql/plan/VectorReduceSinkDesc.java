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

begin_comment
comment|/**  * VectorReduceSinkDesc.  *  * Extra parameters beyond ReduceSinkDesc just for the VectorReduceSinkOperator.  *  * We don't extend ReduceSinkDesc because the base OperatorDesc doesn't support  * clone and adding it is a lot work for little gain.  */
end_comment

begin_class
specifier|public
class|class
name|VectorReduceSinkDesc
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
name|ReduceSinkKeyType
block|{
name|NONE
block|,
name|LONG
block|,
name|STRING
block|,
name|MULTI_KEY
block|}
specifier|private
name|ReduceSinkKeyType
name|reduceSinkKeyType
decl_stmt|;
specifier|private
name|VectorReduceSinkInfo
name|vectorReduceSinkInfo
decl_stmt|;
specifier|public
name|VectorReduceSinkDesc
parameter_list|()
block|{
name|reduceSinkKeyType
operator|=
name|ReduceSinkKeyType
operator|.
name|NONE
expr_stmt|;
name|vectorReduceSinkInfo
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|ReduceSinkKeyType
name|reduceSinkKeyType
parameter_list|()
block|{
return|return
name|reduceSinkKeyType
return|;
block|}
specifier|public
name|void
name|setReduceSinkKeyType
parameter_list|(
name|ReduceSinkKeyType
name|reduceSinkKeyType
parameter_list|)
block|{
name|this
operator|.
name|reduceSinkKeyType
operator|=
name|reduceSinkKeyType
expr_stmt|;
block|}
specifier|public
name|void
name|setVectorReduceSinkInfo
parameter_list|(
name|VectorReduceSinkInfo
name|vectorReduceSinkInfo
parameter_list|)
block|{
name|this
operator|.
name|vectorReduceSinkInfo
operator|=
name|vectorReduceSinkInfo
expr_stmt|;
block|}
specifier|public
name|VectorReduceSinkInfo
name|getVectorReduceSinkInfo
parameter_list|()
block|{
return|return
name|vectorReduceSinkInfo
return|;
block|}
specifier|private
name|boolean
name|isVectorizationReduceSinkNativeEnabled
decl_stmt|;
specifier|private
name|String
name|engine
decl_stmt|;
specifier|private
name|boolean
name|acidChange
decl_stmt|;
specifier|private
name|boolean
name|hasBuckets
decl_stmt|;
specifier|private
name|boolean
name|hasTopN
decl_stmt|;
specifier|private
name|boolean
name|useUniformHash
decl_stmt|;
specifier|private
name|boolean
name|hasDistinctColumns
decl_stmt|;
specifier|private
name|boolean
name|isKeyBinarySortable
decl_stmt|;
specifier|private
name|boolean
name|isValueLazyBinary
decl_stmt|;
comment|/*    * The following conditions are for native Vector ReduceSink.    */
specifier|public
name|void
name|setIsVectorizationReduceSinkNativeEnabled
parameter_list|(
name|boolean
name|isVectorizationReduceSinkNativeEnabled
parameter_list|)
block|{
name|this
operator|.
name|isVectorizationReduceSinkNativeEnabled
operator|=
name|isVectorizationReduceSinkNativeEnabled
expr_stmt|;
block|}
specifier|public
name|boolean
name|getIsVectorizationReduceSinkNativeEnabled
parameter_list|()
block|{
return|return
name|isVectorizationReduceSinkNativeEnabled
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
name|setAcidChange
parameter_list|(
name|boolean
name|acidChange
parameter_list|)
block|{
name|this
operator|.
name|acidChange
operator|=
name|acidChange
expr_stmt|;
block|}
specifier|public
name|boolean
name|getAcidChange
parameter_list|()
block|{
return|return
name|acidChange
return|;
block|}
specifier|public
name|void
name|setHasBuckets
parameter_list|(
name|boolean
name|hasBuckets
parameter_list|)
block|{
name|this
operator|.
name|hasBuckets
operator|=
name|hasBuckets
expr_stmt|;
block|}
specifier|public
name|boolean
name|getHasBuckets
parameter_list|()
block|{
return|return
name|hasBuckets
return|;
block|}
specifier|public
name|void
name|setHasTopN
parameter_list|(
name|boolean
name|hasTopN
parameter_list|)
block|{
name|this
operator|.
name|hasTopN
operator|=
name|hasTopN
expr_stmt|;
block|}
specifier|public
name|boolean
name|getHasTopN
parameter_list|()
block|{
return|return
name|hasTopN
return|;
block|}
specifier|public
name|void
name|setUseUniformHash
parameter_list|(
name|boolean
name|useUniformHash
parameter_list|)
block|{
name|this
operator|.
name|useUniformHash
operator|=
name|useUniformHash
expr_stmt|;
block|}
specifier|public
name|boolean
name|getUseUniformHash
parameter_list|()
block|{
return|return
name|useUniformHash
return|;
block|}
specifier|public
name|void
name|setHasDistinctColumns
parameter_list|(
name|boolean
name|hasDistinctColumns
parameter_list|)
block|{
name|this
operator|.
name|hasDistinctColumns
operator|=
name|hasDistinctColumns
expr_stmt|;
block|}
specifier|public
name|boolean
name|getHasDistinctColumns
parameter_list|()
block|{
return|return
name|hasDistinctColumns
return|;
block|}
specifier|public
name|void
name|setIsKeyBinarySortable
parameter_list|(
name|boolean
name|isKeyBinarySortable
parameter_list|)
block|{
name|this
operator|.
name|isKeyBinarySortable
operator|=
name|isKeyBinarySortable
expr_stmt|;
block|}
specifier|public
name|boolean
name|getIsKeyBinarySortable
parameter_list|()
block|{
return|return
name|isKeyBinarySortable
return|;
block|}
specifier|public
name|void
name|setIsValueLazyBinary
parameter_list|(
name|boolean
name|isValueLazyBinary
parameter_list|)
block|{
name|this
operator|.
name|isValueLazyBinary
operator|=
name|isValueLazyBinary
expr_stmt|;
block|}
specifier|public
name|boolean
name|getIsValueLazyBinary
parameter_list|()
block|{
return|return
name|isValueLazyBinary
return|;
block|}
block|}
end_class

end_unit

