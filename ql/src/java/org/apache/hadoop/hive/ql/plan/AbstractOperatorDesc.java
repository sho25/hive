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
name|PTFUtils
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
name|stats
operator|.
name|StatsCollectionContext
import|;
end_import

begin_class
specifier|public
class|class
name|AbstractOperatorDesc
implements|implements
name|OperatorDesc
block|{
specifier|protected
name|boolean
name|vectorMode
init|=
literal|false
decl_stmt|;
comment|// Extra parameters only for vectorization.
specifier|protected
name|VectorDesc
name|vectorDesc
decl_stmt|;
specifier|protected
name|Statistics
name|statistics
decl_stmt|;
specifier|protected
specifier|transient
name|OpTraits
name|opTraits
decl_stmt|;
specifier|protected
specifier|transient
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|opProps
decl_stmt|;
specifier|protected
name|long
name|memNeeded
init|=
literal|0
decl_stmt|;
specifier|protected
name|long
name|memAvailable
init|=
literal|0
decl_stmt|;
specifier|protected
name|String
name|runtimeStatsTmpDir
decl_stmt|;
annotation|@
name|Override
annotation|@
name|Explain
argument_list|(
name|skipHeader
operator|=
literal|true
argument_list|,
name|displayName
operator|=
literal|"Statistics"
argument_list|)
specifier|public
name|Statistics
name|getStatistics
parameter_list|()
block|{
return|return
name|statistics
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|skipHeader
operator|=
literal|true
argument_list|,
name|displayName
operator|=
literal|"Statistics"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|}
argument_list|)
specifier|public
name|String
name|getUserLevelStatistics
parameter_list|()
block|{
return|return
name|statistics
operator|.
name|toUserLevelExplainString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setStatistics
parameter_list|(
name|Statistics
name|statistics
parameter_list|)
block|{
name|this
operator|.
name|statistics
operator|=
name|statistics
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|clone
parameter_list|()
throws|throws
name|CloneNotSupportedException
block|{
throw|throw
operator|new
name|CloneNotSupportedException
argument_list|(
literal|"clone not supported"
argument_list|)
throw|;
block|}
specifier|public
name|boolean
name|getVectorMode
parameter_list|()
block|{
return|return
name|vectorMode
return|;
block|}
specifier|public
name|void
name|setVectorMode
parameter_list|(
name|boolean
name|vm
parameter_list|)
block|{
name|this
operator|.
name|vectorMode
operator|=
name|vm
expr_stmt|;
block|}
specifier|public
name|void
name|setVectorDesc
parameter_list|(
name|VectorDesc
name|vectorDesc
parameter_list|)
block|{
name|this
operator|.
name|vectorDesc
operator|=
name|vectorDesc
expr_stmt|;
block|}
specifier|public
name|VectorDesc
name|getVectorDesc
parameter_list|()
block|{
return|return
name|vectorDesc
return|;
block|}
annotation|@
name|Override
specifier|public
name|OpTraits
name|getTraits
parameter_list|()
block|{
return|return
name|opTraits
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setTraits
parameter_list|(
name|OpTraits
name|opTraits
parameter_list|)
block|{
name|this
operator|.
name|opTraits
operator|=
name|opTraits
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getOpProps
parameter_list|()
block|{
return|return
name|opProps
return|;
block|}
specifier|public
name|void
name|setOpProps
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
parameter_list|)
block|{
name|this
operator|.
name|opProps
operator|=
name|props
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMemoryNeeded
parameter_list|()
block|{
return|return
name|memNeeded
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setMemoryNeeded
parameter_list|(
name|long
name|memNeeded
parameter_list|)
block|{
name|this
operator|.
name|memNeeded
operator|=
name|memNeeded
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMaxMemoryAvailable
parameter_list|()
block|{
return|return
name|memAvailable
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setMaxMemoryAvailable
parameter_list|(
specifier|final
name|long
name|memoryAvailble
parameter_list|)
block|{
name|this
operator|.
name|memAvailable
operator|=
name|memoryAvailble
expr_stmt|;
block|}
specifier|public
name|String
name|getRuntimeStatsTmpDir
parameter_list|()
block|{
return|return
name|runtimeStatsTmpDir
return|;
block|}
specifier|public
name|void
name|setRuntimeStatsTmpDir
parameter_list|(
name|String
name|runtimeStatsTmpDir
parameter_list|)
block|{
name|this
operator|.
name|runtimeStatsTmpDir
operator|=
name|runtimeStatsTmpDir
expr_stmt|;
block|}
comment|/**    * The default implementation delegates to {@link #equals(Object)}. Intended to be    * overridden by sub classes.    */
annotation|@
name|Override
specifier|public
name|boolean
name|isSame
parameter_list|(
name|OperatorDesc
name|other
parameter_list|)
block|{
return|return
name|equals
argument_list|(
name|other
argument_list|)
return|;
block|}
block|}
end_class

end_unit

