begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/**  * JDBM LICENSE v1.00  *  * Redistribution and use of this software and associated documentation  * ("Software"), with or without modification, are permitted provided  * that the following conditions are met:  *  * 1. Redistributions of source code must retain copyright  *    statements and notices.  Redistributions must also contain a  *    copy of this document.  *  * 2. Redistributions in binary form must reproduce the  *    above copyright notice, this list of conditions and the  *    following disclaimer in the documentation and/or other  *    materials provided with the distribution.  *  * 3. The name "JDBM" must not be used to endorse or promote  *    products derived from this Software without prior written  *    permission of Cees de Groot.  For written permission,  *    please contact cg@cdegroot.com.  *  * 4. Products derived from this Software may not be called "JDBM"  *    nor may "JDBM" appear in their names without prior written  *    permission of Cees de Groot.   *  * 5. Due credit should be given to the JDBM Project  *    (http://jdbm.sourceforge.net/).  *  * THIS SOFTWARE IS PROVIDED BY THE JDBM PROJECT AND CONTRIBUTORS  * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT  * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL  * CEES DE GROOT OR ANY CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED  * OF THE POSSIBILITY OF SUCH DAMAGE.  *  * Copyright 2000 (C) Cees de Groot. All Rights Reserved.  * Contributions are Copyright (C) 2000 by their associated contributors.  *  * $Id: PageManager.java,v 1.3 2005/06/25 23:12:32 doomdark Exp $  */
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
name|util
operator|.
name|jdbm
operator|.
name|recman
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  *  This class manages the linked lists of pages that make up a file.  */
end_comment

begin_class
specifier|final
class|class
name|PageManager
block|{
comment|// our record file
specifier|private
name|RecordFile
name|file
decl_stmt|;
comment|// header data
specifier|private
name|FileHeader
name|header
decl_stmt|;
specifier|private
name|BlockIo
name|headerBuf
decl_stmt|;
comment|/**      *  Creates a new page manager using the indicated record file.      */
name|PageManager
parameter_list|(
name|RecordFile
name|file
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|file
operator|=
name|file
expr_stmt|;
comment|// check the file header. If the magic is 0, we assume a new
comment|// file. Note that we hold on to the file header node.
name|headerBuf
operator|=
name|file
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|headerBuf
operator|.
name|readShort
argument_list|(
literal|0
argument_list|)
operator|==
literal|0
condition|)
name|header
operator|=
operator|new
name|FileHeader
argument_list|(
name|headerBuf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
else|else
name|header
operator|=
operator|new
name|FileHeader
argument_list|(
name|headerBuf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**      *  Allocates a page of the indicated type. Returns recid of the      *  page.      */
name|long
name|allocate
parameter_list|(
name|short
name|type
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|type
operator|==
name|Magic
operator|.
name|FREE_PAGE
condition|)
throw|throw
operator|new
name|Error
argument_list|(
literal|"allocate of free page?"
argument_list|)
throw|;
comment|// do we have something on the free list?
name|long
name|retval
init|=
name|header
operator|.
name|getFirstOf
argument_list|(
name|Magic
operator|.
name|FREE_PAGE
argument_list|)
decl_stmt|;
name|boolean
name|isNew
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|retval
operator|!=
literal|0
condition|)
block|{
comment|// yes. Point to it and make the next of that page the
comment|// new first free page.
name|header
operator|.
name|setFirstOf
argument_list|(
name|Magic
operator|.
name|FREE_PAGE
argument_list|,
name|getNext
argument_list|(
name|retval
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// nope. make a new record
name|retval
operator|=
name|header
operator|.
name|getLastOf
argument_list|(
name|Magic
operator|.
name|FREE_PAGE
argument_list|)
expr_stmt|;
if|if
condition|(
name|retval
operator|==
literal|0
condition|)
comment|// very new file - allocate record #1
name|retval
operator|=
literal|1
expr_stmt|;
name|header
operator|.
name|setLastOf
argument_list|(
name|Magic
operator|.
name|FREE_PAGE
argument_list|,
name|retval
operator|+
literal|1
argument_list|)
expr_stmt|;
name|isNew
operator|=
literal|true
expr_stmt|;
block|}
comment|// Cool. We have a record, add it to the correct list
name|BlockIo
name|buf
init|=
name|file
operator|.
name|get
argument_list|(
name|retval
argument_list|)
decl_stmt|;
name|PageHeader
name|pageHdr
init|=
name|isNew
condition|?
operator|new
name|PageHeader
argument_list|(
name|buf
argument_list|,
name|type
argument_list|)
else|:
name|PageHeader
operator|.
name|getView
argument_list|(
name|buf
argument_list|)
decl_stmt|;
name|long
name|oldLast
init|=
name|header
operator|.
name|getLastOf
argument_list|(
name|type
argument_list|)
decl_stmt|;
comment|// Clean data.
name|System
operator|.
name|arraycopy
argument_list|(
name|RecordFile
operator|.
name|cleanData
argument_list|,
literal|0
argument_list|,
name|buf
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|RecordFile
operator|.
name|BLOCK_SIZE
argument_list|)
expr_stmt|;
name|pageHdr
operator|.
name|setType
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|pageHdr
operator|.
name|setPrev
argument_list|(
name|oldLast
argument_list|)
expr_stmt|;
name|pageHdr
operator|.
name|setNext
argument_list|(
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|oldLast
operator|==
literal|0
condition|)
comment|// This was the first one of this type
name|header
operator|.
name|setFirstOf
argument_list|(
name|type
argument_list|,
name|retval
argument_list|)
expr_stmt|;
name|header
operator|.
name|setLastOf
argument_list|(
name|type
argument_list|,
name|retval
argument_list|)
expr_stmt|;
name|file
operator|.
name|release
argument_list|(
name|retval
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// If there's a previous, fix up its pointer
if|if
condition|(
name|oldLast
operator|!=
literal|0
condition|)
block|{
name|buf
operator|=
name|file
operator|.
name|get
argument_list|(
name|oldLast
argument_list|)
expr_stmt|;
name|pageHdr
operator|=
name|PageHeader
operator|.
name|getView
argument_list|(
name|buf
argument_list|)
expr_stmt|;
name|pageHdr
operator|.
name|setNext
argument_list|(
name|retval
argument_list|)
expr_stmt|;
name|file
operator|.
name|release
argument_list|(
name|oldLast
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// remove the view, we have modified the type.
name|buf
operator|.
name|setView
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return
name|retval
return|;
block|}
comment|/**      *  Frees a page of the indicated type.      */
name|void
name|free
parameter_list|(
name|short
name|type
parameter_list|,
name|long
name|recid
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|type
operator|==
name|Magic
operator|.
name|FREE_PAGE
condition|)
throw|throw
operator|new
name|Error
argument_list|(
literal|"free free page?"
argument_list|)
throw|;
if|if
condition|(
name|recid
operator|==
literal|0
condition|)
throw|throw
operator|new
name|Error
argument_list|(
literal|"free header page?"
argument_list|)
throw|;
comment|// get the page and read next and previous pointers
name|BlockIo
name|buf
init|=
name|file
operator|.
name|get
argument_list|(
name|recid
argument_list|)
decl_stmt|;
name|PageHeader
name|pageHdr
init|=
name|PageHeader
operator|.
name|getView
argument_list|(
name|buf
argument_list|)
decl_stmt|;
name|long
name|prev
init|=
name|pageHdr
operator|.
name|getPrev
argument_list|()
decl_stmt|;
name|long
name|next
init|=
name|pageHdr
operator|.
name|getNext
argument_list|()
decl_stmt|;
comment|// put the page at the front of the free list.
name|pageHdr
operator|.
name|setType
argument_list|(
name|Magic
operator|.
name|FREE_PAGE
argument_list|)
expr_stmt|;
name|pageHdr
operator|.
name|setNext
argument_list|(
name|header
operator|.
name|getFirstOf
argument_list|(
name|Magic
operator|.
name|FREE_PAGE
argument_list|)
argument_list|)
expr_stmt|;
name|pageHdr
operator|.
name|setPrev
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|header
operator|.
name|setFirstOf
argument_list|(
name|Magic
operator|.
name|FREE_PAGE
argument_list|,
name|recid
argument_list|)
expr_stmt|;
name|file
operator|.
name|release
argument_list|(
name|recid
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// remove the page from its old list
if|if
condition|(
name|prev
operator|!=
literal|0
condition|)
block|{
name|buf
operator|=
name|file
operator|.
name|get
argument_list|(
name|prev
argument_list|)
expr_stmt|;
name|pageHdr
operator|=
name|PageHeader
operator|.
name|getView
argument_list|(
name|buf
argument_list|)
expr_stmt|;
name|pageHdr
operator|.
name|setNext
argument_list|(
name|next
argument_list|)
expr_stmt|;
name|file
operator|.
name|release
argument_list|(
name|prev
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|header
operator|.
name|setFirstOf
argument_list|(
name|type
argument_list|,
name|next
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|next
operator|!=
literal|0
condition|)
block|{
name|buf
operator|=
name|file
operator|.
name|get
argument_list|(
name|next
argument_list|)
expr_stmt|;
name|pageHdr
operator|=
name|PageHeader
operator|.
name|getView
argument_list|(
name|buf
argument_list|)
expr_stmt|;
name|pageHdr
operator|.
name|setPrev
argument_list|(
name|prev
argument_list|)
expr_stmt|;
name|file
operator|.
name|release
argument_list|(
name|next
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|header
operator|.
name|setLastOf
argument_list|(
name|type
argument_list|,
name|prev
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      *  Returns the page following the indicated block      */
name|long
name|getNext
parameter_list|(
name|long
name|block
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|PageHeader
operator|.
name|getView
argument_list|(
name|file
operator|.
name|get
argument_list|(
name|block
argument_list|)
argument_list|)
operator|.
name|getNext
argument_list|()
return|;
block|}
finally|finally
block|{
name|file
operator|.
name|release
argument_list|(
name|block
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      *  Returns the page before the indicated block      */
name|long
name|getPrev
parameter_list|(
name|long
name|block
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|PageHeader
operator|.
name|getView
argument_list|(
name|file
operator|.
name|get
argument_list|(
name|block
argument_list|)
argument_list|)
operator|.
name|getPrev
argument_list|()
return|;
block|}
finally|finally
block|{
name|file
operator|.
name|release
argument_list|(
name|block
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      *  Returns the first page on the indicated list.      */
name|long
name|getFirst
parameter_list|(
name|short
name|type
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|header
operator|.
name|getFirstOf
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**      *  Returns the last page on the indicated list.      */
name|long
name|getLast
parameter_list|(
name|short
name|type
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|header
operator|.
name|getLastOf
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**      *  Commit all pending (in-memory) data by flushing the page manager.      *  This forces a flush of all outstanding blocks (this it's an implicit      *  {@link RecordFile#commit} as well).      */
name|void
name|commit
parameter_list|()
throws|throws
name|IOException
block|{
comment|// write the header out
name|file
operator|.
name|release
argument_list|(
name|headerBuf
argument_list|)
expr_stmt|;
name|file
operator|.
name|commit
argument_list|()
expr_stmt|;
comment|// and obtain it again
name|headerBuf
operator|=
name|file
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|header
operator|=
operator|new
name|FileHeader
argument_list|(
name|headerBuf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**      *  Flushes the page manager. This forces a flush of all outstanding      *  blocks (this it's an implicit {@link RecordFile#commit} as well).      */
name|void
name|rollback
parameter_list|()
throws|throws
name|IOException
block|{
comment|// release header
name|file
operator|.
name|discard
argument_list|(
name|headerBuf
argument_list|)
expr_stmt|;
name|file
operator|.
name|rollback
argument_list|()
expr_stmt|;
comment|// and obtain it again
name|headerBuf
operator|=
name|file
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|headerBuf
operator|.
name|readShort
argument_list|(
literal|0
argument_list|)
operator|==
literal|0
condition|)
name|header
operator|=
operator|new
name|FileHeader
argument_list|(
name|headerBuf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
else|else
name|header
operator|=
operator|new
name|FileHeader
argument_list|(
name|headerBuf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**      *  Closes the page manager. This flushes the page manager and releases      *  the lock on the header.      */
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|file
operator|.
name|release
argument_list|(
name|headerBuf
argument_list|)
expr_stmt|;
name|file
operator|.
name|commit
argument_list|()
expr_stmt|;
name|headerBuf
operator|=
literal|null
expr_stmt|;
name|header
operator|=
literal|null
expr_stmt|;
name|file
operator|=
literal|null
expr_stmt|;
block|}
comment|/**      *  Returns the file header.      */
name|FileHeader
name|getFileHeader
parameter_list|()
block|{
return|return
name|header
return|;
block|}
block|}
end_class

end_unit

