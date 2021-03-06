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

begin_comment
comment|/**  * Demux operator descriptor implementation.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Demux Operator"
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
class|class
name|DemuxDesc
extends|extends
name|AbstractOperatorDesc
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
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|newTagToOldTag
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|newTagToChildIndex
decl_stmt|;
specifier|private
name|List
argument_list|<
name|TableDesc
argument_list|>
name|keysSerializeInfos
decl_stmt|;
specifier|private
name|List
argument_list|<
name|TableDesc
argument_list|>
name|valuesSerializeInfos
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|childIndexToOriginalNumParents
decl_stmt|;
specifier|public
name|DemuxDesc
parameter_list|()
block|{   }
specifier|public
name|DemuxDesc
parameter_list|(
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|newTagToOldTag
parameter_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|newTagToChildIndex
parameter_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|childIndexToOriginalNumParents
parameter_list|,
name|List
argument_list|<
name|TableDesc
argument_list|>
name|keysSerializeInfos
parameter_list|,
name|List
argument_list|<
name|TableDesc
argument_list|>
name|valuesSerializeInfos
parameter_list|)
block|{
name|this
operator|.
name|newTagToOldTag
operator|=
name|newTagToOldTag
expr_stmt|;
name|this
operator|.
name|newTagToChildIndex
operator|=
name|newTagToChildIndex
expr_stmt|;
name|this
operator|.
name|childIndexToOriginalNumParents
operator|=
name|childIndexToOriginalNumParents
expr_stmt|;
name|this
operator|.
name|keysSerializeInfos
operator|=
name|keysSerializeInfos
expr_stmt|;
name|this
operator|.
name|valuesSerializeInfos
operator|=
name|valuesSerializeInfos
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|TableDesc
argument_list|>
name|getKeysSerializeInfos
parameter_list|()
block|{
return|return
name|keysSerializeInfos
return|;
block|}
specifier|public
name|void
name|setKeysSerializeInfos
parameter_list|(
name|List
argument_list|<
name|TableDesc
argument_list|>
name|keysSerializeInfos
parameter_list|)
block|{
name|this
operator|.
name|keysSerializeInfos
operator|=
name|keysSerializeInfos
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|TableDesc
argument_list|>
name|getValuesSerializeInfos
parameter_list|()
block|{
return|return
name|valuesSerializeInfos
return|;
block|}
specifier|public
name|void
name|setValuesSerializeInfos
parameter_list|(
name|List
argument_list|<
name|TableDesc
argument_list|>
name|valuesSerializeInfos
parameter_list|)
block|{
name|this
operator|.
name|valuesSerializeInfos
operator|=
name|valuesSerializeInfos
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|getNewTagToOldTag
parameter_list|()
block|{
return|return
name|newTagToOldTag
return|;
block|}
specifier|public
name|void
name|setNewTagToOldTag
parameter_list|(
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|newTagToOldTag
parameter_list|)
block|{
name|this
operator|.
name|newTagToOldTag
operator|=
name|newTagToOldTag
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|getNewTagToChildIndex
parameter_list|()
block|{
return|return
name|newTagToChildIndex
return|;
block|}
specifier|public
name|void
name|setNewTagToChildIndex
parameter_list|(
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|newTagToChildIndex
parameter_list|)
block|{
name|this
operator|.
name|newTagToChildIndex
operator|=
name|newTagToChildIndex
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|getChildIndexToOriginalNumParents
parameter_list|()
block|{
return|return
name|childIndexToOriginalNumParents
return|;
block|}
specifier|public
name|void
name|setChildIndexToOriginalNumParents
parameter_list|(
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|childIndexToOriginalNumParents
parameter_list|)
block|{
name|this
operator|.
name|childIndexToOriginalNumParents
operator|=
name|childIndexToOriginalNumParents
expr_stmt|;
block|}
block|}
end_class

end_unit

