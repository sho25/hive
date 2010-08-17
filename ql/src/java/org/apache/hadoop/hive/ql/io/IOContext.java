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

begin_comment
comment|/**  * IOContext basically contains the position information of the current  * key/value. For blockCompressed files, isBlockPointer should return true,  * and currentBlockStart refers to the RCFile Block or SequenceFile Block. For  * non compressed files, isBlockPointer should return false, and  * currentBlockStart refers to the beginning offset of the current row,  * nextBlockStart refers the end of current row and beginning of next row.  */
end_comment

begin_class
specifier|public
class|class
name|IOContext
block|{
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|IOContext
argument_list|>
name|threadLocal
init|=
operator|new
name|ThreadLocal
argument_list|<
name|IOContext
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
if|if
condition|(
name|threadLocal
operator|.
name|get
argument_list|()
operator|==
literal|null
condition|)
block|{
name|threadLocal
operator|.
name|set
argument_list|(
operator|new
name|IOContext
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|IOContext
name|get
parameter_list|()
block|{
return|return
name|IOContext
operator|.
name|threadLocal
operator|.
name|get
argument_list|()
return|;
block|}
name|long
name|currentBlockStart
decl_stmt|;
name|long
name|nextBlockStart
decl_stmt|;
name|boolean
name|isBlockPointer
decl_stmt|;
name|boolean
name|ioExceptions
decl_stmt|;
name|String
name|inputFile
decl_stmt|;
specifier|public
name|IOContext
parameter_list|()
block|{
name|this
operator|.
name|currentBlockStart
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|nextBlockStart
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|isBlockPointer
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|ioExceptions
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|long
name|getCurrentBlockStart
parameter_list|()
block|{
return|return
name|currentBlockStart
return|;
block|}
specifier|public
name|void
name|setCurrentBlockStart
parameter_list|(
name|long
name|currentBlockStart
parameter_list|)
block|{
name|this
operator|.
name|currentBlockStart
operator|=
name|currentBlockStart
expr_stmt|;
block|}
specifier|public
name|long
name|getNextBlockStart
parameter_list|()
block|{
return|return
name|nextBlockStart
return|;
block|}
specifier|public
name|void
name|setNextBlockStart
parameter_list|(
name|long
name|nextBlockStart
parameter_list|)
block|{
name|this
operator|.
name|nextBlockStart
operator|=
name|nextBlockStart
expr_stmt|;
block|}
specifier|public
name|boolean
name|isBlockPointer
parameter_list|()
block|{
return|return
name|isBlockPointer
return|;
block|}
specifier|public
name|void
name|setBlockPointer
parameter_list|(
name|boolean
name|isBlockPointer
parameter_list|)
block|{
name|this
operator|.
name|isBlockPointer
operator|=
name|isBlockPointer
expr_stmt|;
block|}
specifier|public
name|String
name|getInputFile
parameter_list|()
block|{
return|return
name|inputFile
return|;
block|}
specifier|public
name|void
name|setInputFile
parameter_list|(
name|String
name|inputFile
parameter_list|)
block|{
name|this
operator|.
name|inputFile
operator|=
name|inputFile
expr_stmt|;
block|}
specifier|public
name|void
name|setIOExceptions
parameter_list|(
name|boolean
name|ioe
parameter_list|)
block|{
name|this
operator|.
name|ioExceptions
operator|=
name|ioe
expr_stmt|;
block|}
specifier|public
name|boolean
name|getIOExceptions
parameter_list|()
block|{
return|return
name|ioExceptions
return|;
block|}
block|}
end_class

end_unit

