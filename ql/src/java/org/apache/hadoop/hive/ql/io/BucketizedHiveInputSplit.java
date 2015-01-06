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
name|io
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|io
operator|.
name|HiveInputFormat
operator|.
name|HiveInputSplit
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
name|mapred
operator|.
name|FileSplit
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
name|mapred
operator|.
name|InputSplit
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * HiveInputSplit encapsulates an InputSplit with its corresponding  * inputFormatClass. The reason that it derives from FileSplit is to make sure  * "map.input.file" in MapTask.  */
end_comment

begin_class
specifier|public
class|class
name|BucketizedHiveInputSplit
extends|extends
name|HiveInputSplit
block|{
specifier|protected
name|InputSplit
index|[]
name|inputSplits
decl_stmt|;
specifier|protected
name|String
name|inputFormatClassName
decl_stmt|;
specifier|public
name|String
name|getInputFormatClassName
parameter_list|()
block|{
return|return
name|inputFormatClassName
return|;
block|}
specifier|public
name|void
name|setInputFormatClassName
parameter_list|(
name|String
name|inputFormatClassName
parameter_list|)
block|{
name|this
operator|.
name|inputFormatClassName
operator|=
name|inputFormatClassName
expr_stmt|;
block|}
specifier|public
name|BucketizedHiveInputSplit
parameter_list|()
block|{
comment|// This is the only public constructor of FileSplit
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|BucketizedHiveInputSplit
parameter_list|(
name|InputSplit
index|[]
name|inputSplits
parameter_list|,
name|String
name|inputFormatClassName
parameter_list|)
block|{
comment|// This is the only public constructor of FileSplit
name|super
argument_list|()
expr_stmt|;
assert|assert
operator|(
name|inputSplits
operator|!=
literal|null
operator|&&
name|inputSplits
operator|.
name|length
operator|>
literal|0
operator|)
assert|;
name|this
operator|.
name|inputSplits
operator|=
name|inputSplits
expr_stmt|;
name|this
operator|.
name|inputFormatClassName
operator|=
name|inputFormatClassName
expr_stmt|;
block|}
specifier|public
name|int
name|getNumSplits
parameter_list|()
block|{
return|return
name|inputSplits
operator|.
name|length
return|;
block|}
specifier|public
name|InputSplit
name|getSplit
parameter_list|(
name|int
name|idx
parameter_list|)
block|{
assert|assert
operator|(
name|idx
operator|>=
literal|0
operator|&&
name|idx
operator|<
name|inputSplits
operator|.
name|length
operator|)
assert|;
return|return
name|inputSplits
index|[
name|idx
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|inputFormatClassName
parameter_list|()
block|{
return|return
name|inputFormatClassName
return|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|getPath
parameter_list|()
block|{
if|if
condition|(
name|inputSplits
operator|!=
literal|null
operator|&&
name|inputSplits
operator|.
name|length
operator|>
literal|0
operator|&&
name|inputSplits
index|[
literal|0
index|]
operator|instanceof
name|FileSplit
condition|)
block|{
return|return
operator|(
operator|(
name|FileSplit
operator|)
name|inputSplits
index|[
literal|0
index|]
operator|)
operator|.
name|getPath
argument_list|()
return|;
block|}
return|return
operator|new
name|Path
argument_list|(
literal|""
argument_list|)
return|;
block|}
comment|/** The position of the first byte in the file to process. */
annotation|@
name|Override
specifier|public
name|long
name|getStart
parameter_list|()
block|{
if|if
condition|(
name|inputSplits
operator|!=
literal|null
operator|&&
name|inputSplits
operator|.
name|length
operator|>
literal|0
operator|&&
name|inputSplits
index|[
literal|0
index|]
operator|instanceof
name|FileSplit
condition|)
block|{
return|return
operator|(
operator|(
name|FileSplit
operator|)
name|inputSplits
index|[
literal|0
index|]
operator|)
operator|.
name|getStart
argument_list|()
return|;
block|}
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
name|inputSplits
operator|!=
literal|null
operator|&&
name|inputSplits
operator|.
name|length
operator|>
literal|0
condition|)
block|{
return|return
name|inputFormatClassName
operator|+
literal|":"
operator|+
name|inputSplits
index|[
literal|0
index|]
operator|.
name|toString
argument_list|()
return|;
block|}
return|return
name|inputFormatClassName
operator|+
literal|":null"
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLength
parameter_list|()
block|{
name|long
name|r
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|inputSplits
operator|!=
literal|null
condition|)
block|{
try|try
block|{
for|for
control|(
name|InputSplit
name|inputSplit
range|:
name|inputSplits
control|)
block|{
name|r
operator|+=
name|inputSplit
operator|.
name|getLength
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|r
return|;
block|}
specifier|public
name|long
name|getLength
parameter_list|(
name|int
name|idx
parameter_list|)
block|{
if|if
condition|(
name|inputSplits
operator|!=
literal|null
condition|)
block|{
try|try
block|{
return|return
name|inputSplits
index|[
name|idx
index|]
operator|.
name|getLength
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getLocations
parameter_list|()
throws|throws
name|IOException
block|{
assert|assert
operator|(
name|inputSplits
operator|!=
literal|null
operator|&&
name|inputSplits
operator|.
name|length
operator|>
literal|0
operator|)
assert|;
return|return
name|inputSplits
index|[
literal|0
index|]
operator|.
name|getLocations
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|inputSplitClassName
init|=
name|in
operator|.
name|readUTF
argument_list|()
decl_stmt|;
name|int
name|numSplits
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|inputSplits
operator|=
operator|new
name|InputSplit
index|[
name|numSplits
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numSplits
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|inputSplits
index|[
name|i
index|]
operator|=
operator|(
name|InputSplit
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|conf
operator|.
name|getClassByName
argument_list|(
name|inputSplitClassName
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot create an instance of InputSplit class = "
operator|+
name|inputSplitClassName
operator|+
literal|":"
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
name|inputSplits
index|[
name|i
index|]
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|inputFormatClassName
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
operator|(
name|inputSplits
operator|!=
literal|null
operator|&&
name|inputSplits
operator|.
name|length
operator|>
literal|0
operator|)
assert|;
name|out
operator|.
name|writeUTF
argument_list|(
name|inputSplits
index|[
literal|0
index|]
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|inputSplits
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|InputSplit
name|inputSplit
range|:
name|inputSplits
control|)
block|{
name|inputSplit
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeUTF
argument_list|(
name|inputFormatClassName
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

