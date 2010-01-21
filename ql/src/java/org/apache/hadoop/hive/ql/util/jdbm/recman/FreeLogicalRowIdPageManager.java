begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/**  * JDBM LICENSE v1.00  *  * Redistribution and use of this software and associated documentation  * ("Software"), with or without modification, are permitted provided  * that the following conditions are met:  *  * 1. Redistributions of source code must retain copyright  *    statements and notices.  Redistributions must also contain a  *    copy of this document.  *  * 2. Redistributions in binary form must reproduce the  *    above copyright notice, this list of conditions and the  *    following disclaimer in the documentation and/or other  *    materials provided with the distribution.  *  * 3. The name "JDBM" must not be used to endorse or promote  *    products derived from this Software without prior written  *    permission of Cees de Groot.  For written permission,  *    please contact cg@cdegroot.com.  *  * 4. Products derived from this Software may not be called "JDBM"  *    nor may "JDBM" appear in their names without prior written  *    permission of Cees de Groot.   *  * 5. Due credit should be given to the JDBM Project  *    (http://jdbm.sourceforge.net/).  *  * THIS SOFTWARE IS PROVIDED BY THE JDBM PROJECT AND CONTRIBUTORS  * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT  * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL  * CEES DE GROOT OR ANY CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED  * OF THE POSSIBILITY OF SUCH DAMAGE.  *  * Copyright 2000 (C) Cees de Groot. All Rights Reserved.  * Contributions are Copyright (C) 2000 by their associated contributors.  *  * $Id: FreeLogicalRowIdPageManager.java,v 1.1 2000/05/06 00:00:31 boisvert Exp $  */
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
name|IOException
import|;
end_import

begin_comment
comment|/**  * This class manages free Logical rowid pages and provides methods to free and  * allocate Logical rowids on a high level.  */
end_comment

begin_class
specifier|final
class|class
name|FreeLogicalRowIdPageManager
block|{
comment|// our record file
specifier|private
specifier|final
name|RecordFile
name|file
decl_stmt|;
comment|// our page manager
specifier|private
specifier|final
name|PageManager
name|pageman
decl_stmt|;
comment|/**    * Creates a new instance using the indicated record file and page manager.    */
name|FreeLogicalRowIdPageManager
parameter_list|(
name|RecordFile
name|file
parameter_list|,
name|PageManager
name|pageman
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
name|this
operator|.
name|pageman
operator|=
name|pageman
expr_stmt|;
block|}
comment|/**    * Returns a free Logical rowid, or null if nothing was found.    */
name|Location
name|get
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Loop through the free Logical rowid list until we find
comment|// the first rowid.
name|Location
name|retval
init|=
literal|null
decl_stmt|;
name|PageCursor
name|curs
init|=
operator|new
name|PageCursor
argument_list|(
name|pageman
argument_list|,
name|Magic
operator|.
name|FREELOGIDS_PAGE
argument_list|)
decl_stmt|;
while|while
condition|(
name|curs
operator|.
name|next
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|FreeLogicalRowIdPage
name|fp
init|=
name|FreeLogicalRowIdPage
operator|.
name|getFreeLogicalRowIdPageView
argument_list|(
name|file
operator|.
name|get
argument_list|(
name|curs
operator|.
name|getCurrent
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|slot
init|=
name|fp
operator|.
name|getFirstAllocated
argument_list|()
decl_stmt|;
if|if
condition|(
name|slot
operator|!=
operator|-
literal|1
condition|)
block|{
comment|// got one!
name|retval
operator|=
operator|new
name|Location
argument_list|(
name|fp
operator|.
name|get
argument_list|(
name|slot
argument_list|)
argument_list|)
expr_stmt|;
name|fp
operator|.
name|free
argument_list|(
name|slot
argument_list|)
expr_stmt|;
if|if
condition|(
name|fp
operator|.
name|getCount
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// page became empty - free it
name|file
operator|.
name|release
argument_list|(
name|curs
operator|.
name|getCurrent
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|pageman
operator|.
name|free
argument_list|(
name|Magic
operator|.
name|FREELOGIDS_PAGE
argument_list|,
name|curs
operator|.
name|getCurrent
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|file
operator|.
name|release
argument_list|(
name|curs
operator|.
name|getCurrent
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
return|return
name|retval
return|;
block|}
else|else
block|{
comment|// no luck, go to next page
name|file
operator|.
name|release
argument_list|(
name|curs
operator|.
name|getCurrent
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Puts the indicated rowid on the free list    */
name|void
name|put
parameter_list|(
name|Location
name|rowid
parameter_list|)
throws|throws
name|IOException
block|{
name|PhysicalRowId
name|free
init|=
literal|null
decl_stmt|;
name|PageCursor
name|curs
init|=
operator|new
name|PageCursor
argument_list|(
name|pageman
argument_list|,
name|Magic
operator|.
name|FREELOGIDS_PAGE
argument_list|)
decl_stmt|;
name|long
name|freePage
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|curs
operator|.
name|next
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|freePage
operator|=
name|curs
operator|.
name|getCurrent
argument_list|()
expr_stmt|;
name|BlockIo
name|curBlock
init|=
name|file
operator|.
name|get
argument_list|(
name|freePage
argument_list|)
decl_stmt|;
name|FreeLogicalRowIdPage
name|fp
init|=
name|FreeLogicalRowIdPage
operator|.
name|getFreeLogicalRowIdPageView
argument_list|(
name|curBlock
argument_list|)
decl_stmt|;
name|int
name|slot
init|=
name|fp
operator|.
name|getFirstFree
argument_list|()
decl_stmt|;
if|if
condition|(
name|slot
operator|!=
operator|-
literal|1
condition|)
block|{
name|free
operator|=
name|fp
operator|.
name|alloc
argument_list|(
name|slot
argument_list|)
expr_stmt|;
break|break;
block|}
name|file
operator|.
name|release
argument_list|(
name|curBlock
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|free
operator|==
literal|null
condition|)
block|{
comment|// No more space on the free list, add a page.
name|freePage
operator|=
name|pageman
operator|.
name|allocate
argument_list|(
name|Magic
operator|.
name|FREELOGIDS_PAGE
argument_list|)
expr_stmt|;
name|BlockIo
name|curBlock
init|=
name|file
operator|.
name|get
argument_list|(
name|freePage
argument_list|)
decl_stmt|;
name|FreeLogicalRowIdPage
name|fp
init|=
name|FreeLogicalRowIdPage
operator|.
name|getFreeLogicalRowIdPageView
argument_list|(
name|curBlock
argument_list|)
decl_stmt|;
name|free
operator|=
name|fp
operator|.
name|alloc
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|free
operator|.
name|setBlock
argument_list|(
name|rowid
operator|.
name|getBlock
argument_list|()
argument_list|)
expr_stmt|;
name|free
operator|.
name|setOffset
argument_list|(
name|rowid
operator|.
name|getOffset
argument_list|()
argument_list|)
expr_stmt|;
name|file
operator|.
name|release
argument_list|(
name|freePage
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

