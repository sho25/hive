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
name|index
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
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
name|ql
operator|.
name|io
operator|.
name|HiveInputFormat
operator|.
name|HiveInputSplit
import|;
end_import

begin_class
specifier|public
specifier|final
class|class
name|MockInputFile
block|{
specifier|private
specifier|final
name|String
name|path
decl_stmt|;
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|HiveInputSplit
argument_list|>
name|splits
decl_stmt|;
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|HiveInputSplit
argument_list|>
name|selectedSplits
decl_stmt|;
specifier|private
name|MockInputFile
parameter_list|(
name|String
name|path
parameter_list|,
name|List
argument_list|<
name|HiveInputSplit
argument_list|>
name|splits
parameter_list|,
name|List
argument_list|<
name|HiveInputSplit
argument_list|>
name|selectedSplits
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|splits
operator|=
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|splits
argument_list|)
expr_stmt|;
name|this
operator|.
name|selectedSplits
operator|=
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|selectedSplits
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getPath
parameter_list|()
block|{
return|return
name|path
return|;
block|}
specifier|public
name|List
argument_list|<
name|HiveInputSplit
argument_list|>
name|getSplits
parameter_list|()
block|{
return|return
name|splits
return|;
block|}
specifier|public
name|List
argument_list|<
name|HiveInputSplit
argument_list|>
name|getSelectedSplits
parameter_list|()
block|{
return|return
name|selectedSplits
return|;
block|}
specifier|public
specifier|static
name|PathStep
name|builder
parameter_list|()
block|{
return|return
operator|new
name|MockInputFileBuilder
argument_list|()
return|;
block|}
specifier|public
specifier|static
interface|interface
name|PathStep
block|{
name|DefaultSplitLengthStep
name|path
parameter_list|(
name|String
name|path
parameter_list|)
function_decl|;
block|}
specifier|public
specifier|static
interface|interface
name|DefaultSplitLengthStep
extends|extends
name|SplitStep
block|{
name|SplitStep
name|defaultSplitLength
parameter_list|(
name|long
name|defaultSplitLength
parameter_list|)
function_decl|;
block|}
specifier|public
specifier|static
interface|interface
name|SplitStep
block|{
name|SplitStep
name|split
parameter_list|()
function_decl|;
name|SplitStep
name|selectedSplit
parameter_list|()
function_decl|;
name|LastSplitStep
name|split
parameter_list|(
name|long
name|lastSplitSize
parameter_list|)
function_decl|;
name|LastSplitStep
name|selectedSplit
parameter_list|(
name|long
name|lastSplitSize
parameter_list|)
function_decl|;
name|MockInputFile
name|build
parameter_list|()
function_decl|;
block|}
specifier|public
specifier|static
interface|interface
name|LastSplitStep
block|{
name|MockInputFile
name|build
parameter_list|()
function_decl|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|MockInputFileBuilder
implements|implements
name|PathStep
implements|,
name|SplitStep
implements|,
name|LastSplitStep
implements|,
name|DefaultSplitLengthStep
block|{
specifier|private
name|String
name|path
decl_stmt|;
specifier|private
name|long
name|defaultSplitSize
init|=
name|SplitFilterTestCase
operator|.
name|DEFAULT_SPLIT_SIZE
decl_stmt|;
empty_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|HiveInputSplit
argument_list|>
name|splits
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|HiveInputSplit
argument_list|>
name|selectedSplits
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|long
name|position
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|DefaultSplitLengthStep
name|path
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|SplitStep
name|split
parameter_list|()
block|{
name|nextSplit
argument_list|(
name|defaultSplitSize
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|LastSplitStep
name|split
parameter_list|(
name|long
name|lastSplitSize
parameter_list|)
block|{
name|nextSplit
argument_list|(
name|lastSplitSize
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|SplitStep
name|selectedSplit
parameter_list|()
block|{
name|selectedSplits
operator|.
name|add
argument_list|(
name|nextSplit
argument_list|(
name|defaultSplitSize
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|LastSplitStep
name|selectedSplit
parameter_list|(
name|long
name|lastSplitSize
parameter_list|)
block|{
name|selectedSplits
operator|.
name|add
argument_list|(
name|nextSplit
argument_list|(
name|lastSplitSize
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|SplitStep
name|defaultSplitLength
parameter_list|(
name|long
name|defaultSplitLength
parameter_list|)
block|{
name|this
operator|.
name|defaultSplitSize
operator|=
name|defaultSplitLength
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|private
name|HiveInputSplit
name|nextSplit
parameter_list|(
name|long
name|splitSize
parameter_list|)
block|{
name|HiveInputSplit
name|split
init|=
name|MockHiveInputSplits
operator|.
name|createMockSplit
argument_list|(
name|path
argument_list|,
name|position
argument_list|,
name|splitSize
argument_list|)
decl_stmt|;
name|position
operator|+=
name|splitSize
expr_stmt|;
name|splits
operator|.
name|add
argument_list|(
name|split
argument_list|)
expr_stmt|;
return|return
name|split
return|;
block|}
annotation|@
name|Override
specifier|public
name|MockInputFile
name|build
parameter_list|()
block|{
return|return
operator|new
name|MockInputFile
argument_list|(
name|path
argument_list|,
name|splits
argument_list|,
name|selectedSplits
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

