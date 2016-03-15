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
name|FileStatus
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
name|io
operator|.
name|Writable
import|;
end_import

begin_class
specifier|public
specifier|final
class|class
name|SyntheticFileId
implements|implements
name|Writable
block|{
specifier|private
name|long
name|pathHash
decl_stmt|;
specifier|private
name|long
name|modTime
decl_stmt|;
specifier|private
name|long
name|length
decl_stmt|;
comment|/** Writable ctor. */
specifier|public
name|SyntheticFileId
parameter_list|()
block|{   }
specifier|public
name|SyntheticFileId
parameter_list|(
name|Path
name|path
parameter_list|,
name|long
name|len
parameter_list|,
name|long
name|modificationTime
parameter_list|)
block|{
name|this
operator|.
name|pathHash
operator|=
name|hashCode
argument_list|(
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|modTime
operator|=
name|modificationTime
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|len
expr_stmt|;
block|}
specifier|public
name|SyntheticFileId
parameter_list|(
name|FileStatus
name|file
parameter_list|)
block|{
name|this
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
argument_list|,
name|file
operator|.
name|getLen
argument_list|()
argument_list|,
name|file
operator|.
name|getModificationTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"["
operator|+
name|pathHash
operator|+
literal|", "
operator|+
name|modTime
operator|+
literal|", "
operator|+
name|length
operator|+
literal|"]"
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
specifier|final
name|int
name|prime
init|=
literal|31
decl_stmt|;
name|int
name|result
init|=
name|prime
operator|+
call|(
name|int
call|)
argument_list|(
name|length
operator|^
operator|(
name|length
operator|>>>
literal|32
operator|)
argument_list|)
decl_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
call|(
name|int
call|)
argument_list|(
name|modTime
operator|^
operator|(
name|modTime
operator|>>>
literal|32
operator|)
argument_list|)
expr_stmt|;
return|return
name|prime
operator|*
name|result
operator|+
call|(
name|int
call|)
argument_list|(
name|pathHash
operator|^
operator|(
name|pathHash
operator|>>>
literal|32
operator|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|SyntheticFileId
operator|)
condition|)
return|return
literal|false
return|;
name|SyntheticFileId
name|other
init|=
operator|(
name|SyntheticFileId
operator|)
name|obj
decl_stmt|;
return|return
name|length
operator|==
name|other
operator|.
name|length
operator|&&
name|modTime
operator|==
name|other
operator|.
name|modTime
operator|&&
name|pathHash
operator|==
name|other
operator|.
name|pathHash
return|;
block|}
specifier|private
name|long
name|hashCode
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|long
name|h
init|=
literal|0
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
name|path
operator|.
name|length
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|h
operator|=
literal|1223
operator|*
name|h
operator|+
name|path
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
return|return
name|h
return|;
block|}
comment|/** Length allows for some backward compatibility wrt field addition. */
specifier|private
specifier|static
specifier|final
name|short
name|THREE_LONGS
init|=
literal|24
decl_stmt|;
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
name|out
operator|.
name|writeShort
argument_list|(
name|THREE_LONGS
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|pathHash
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|modTime
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|length
argument_list|)
expr_stmt|;
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
name|short
name|len
init|=
name|in
operator|.
name|readShort
argument_list|()
decl_stmt|;
if|if
condition|(
name|len
operator|<
name|THREE_LONGS
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Need at least "
operator|+
name|THREE_LONGS
operator|+
literal|" bytes"
argument_list|)
throw|;
name|pathHash
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|modTime
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|length
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|int
name|extraBytes
init|=
name|len
operator|-
name|THREE_LONGS
decl_stmt|;
if|if
condition|(
name|extraBytes
operator|>
literal|0
condition|)
block|{
name|in
operator|.
name|skipBytes
argument_list|(
name|extraBytes
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

