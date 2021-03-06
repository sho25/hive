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

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Merge Join Operator"
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
name|CommonMergeJoinDesc
extends|extends
name|MapJoinDesc
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
name|int
name|numBuckets
decl_stmt|;
specifier|private
name|int
name|mapJoinConversionPos
decl_stmt|;
name|CommonMergeJoinDesc
parameter_list|()
block|{   }
specifier|public
name|CommonMergeJoinDesc
parameter_list|(
name|int
name|numBuckets
parameter_list|,
name|int
name|mapJoinConversionPos
parameter_list|,
name|MapJoinDesc
name|joinDesc
parameter_list|)
block|{
name|super
argument_list|(
name|joinDesc
argument_list|)
expr_stmt|;
name|this
operator|.
name|numBuckets
operator|=
name|numBuckets
expr_stmt|;
name|this
operator|.
name|mapJoinConversionPos
operator|=
name|mapJoinConversionPos
expr_stmt|;
block|}
specifier|public
name|int
name|getNumBuckets
parameter_list|()
block|{
return|return
name|numBuckets
return|;
block|}
specifier|public
name|int
name|getBigTablePosition
parameter_list|()
block|{
return|return
name|mapJoinConversionPos
return|;
block|}
specifier|public
name|void
name|setBigTablePosition
parameter_list|(
name|int
name|pos
parameter_list|)
block|{
name|mapJoinConversionPos
operator|=
name|pos
expr_stmt|;
block|}
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
if|if
condition|(
name|super
operator|.
name|isSame
argument_list|(
name|other
argument_list|)
condition|)
block|{
name|CommonMergeJoinDesc
name|otherDesc
init|=
operator|(
name|CommonMergeJoinDesc
operator|)
name|other
decl_stmt|;
return|return
name|getNumBuckets
argument_list|()
operator|==
name|otherDesc
operator|.
name|getNumBuckets
argument_list|()
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

