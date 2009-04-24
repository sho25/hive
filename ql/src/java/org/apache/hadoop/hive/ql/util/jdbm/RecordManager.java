begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/**  * JDBM LICENSE v1.00  *  * Redistribution and use of this software and associated documentation  * ("Software"), with or without modification, are permitted provided  * that the following conditions are met:  *  * 1. Redistributions of source code must retain copyright  *    statements and notices.  Redistributions must also contain a  *    copy of this document.  *  * 2. Redistributions in binary form must reproduce the  *    above copyright notice, this list of conditions and the  *    following disclaimer in the documentation and/or other  *    materials provided with the distribution.  *  * 3. The name "JDBM" must not be used to endorse or promote  *    products derived from this Software without prior written  *    permission of Cees de Groot.  For written permission,  *    please contact cg@cdegroot.com.  *  * 4. Products derived from this Software may not be called "JDBM"  *    nor may "JDBM" appear in their names without prior written  *    permission of Cees de Groot.  *  * 5. Due credit should be given to the JDBM Project  *    (http://jdbm.sourceforge.net/).  *  * THIS SOFTWARE IS PROVIDED BY THE JDBM PROJECT AND CONTRIBUTORS  * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT  * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL  * CEES DE GROOT OR ANY CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED  * OF THE POSSIBILITY OF SUCH DAMAGE.  *  * Copyright 2000 (C) Cees de Groot. All Rights Reserved.  * Copyright 2000-2001 (C) Alex Boisvert. All Rights Reserved.  * Contributions are Copyright (C) 2000 by their associated contributors.  *  * $Id: RecordManager.java,v 1.3 2005/06/25 23:12:31 doomdark Exp $  */
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
name|util
operator|.
name|jdbm
operator|.
name|helper
operator|.
name|Serializer
import|;
end_import

begin_comment
comment|/**  *  An interface to manages records, which are uninterpreted blobs of data.  *<p>  *  The set of record operations is simple: fetch, insert, update and delete.  *  Each record is identified using a "rowid" and contains a byte[] data block.  *  Rowids are returned on inserts and you can store them someplace safe  *  to be able to get  back to them.  Data blocks can be as long as you wish,  *  and may have lengths different from the original when updating.  *  * @author<a href="mailto:boisvert@intalio.com">Alex Boisvert</a>  * @author<a href="cg@cdegroot.com">Cees de Groot</a>  * @version $Id: RecordManager.java,v 1.3 2005/06/25 23:12:31 doomdark Exp $  */
end_comment

begin_interface
specifier|public
interface|interface
name|RecordManager
block|{
comment|/**      * Reserved slot for name directory.      */
specifier|public
specifier|static
specifier|final
name|int
name|NAME_DIRECTORY_ROOT
init|=
literal|0
decl_stmt|;
comment|/**      *  Inserts a new record using standard java object serialization.      *      *  @param obj the object for the new record.      *  @return the rowid for the new record.      *  @throws IOException when one of the underlying I/O operations fails.      */
specifier|public
specifier|abstract
name|long
name|insert
parameter_list|(
name|Object
name|obj
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      *  Inserts a new record using a custom serializer.      *      *  @param obj the object for the new record.      *  @param serializer a custom serializer      *  @return the rowid for the new record.      *  @throws IOException when one of the underlying I/O operations fails.      */
specifier|public
specifier|abstract
name|long
name|insert
parameter_list|(
name|Object
name|obj
parameter_list|,
name|Serializer
name|serializer
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      *  Deletes a record.      *      *  @param recid the rowid for the record that should be deleted.      *  @throws IOException when one of the underlying I/O operations fails.      */
specifier|public
specifier|abstract
name|void
name|delete
parameter_list|(
name|long
name|recid
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      *  Updates a record using standard java object serialization.      *      *  @param recid the recid for the record that is to be updated.      *  @param obj the new object for the record.      *  @throws IOException when one of the underlying I/O operations fails.      */
specifier|public
specifier|abstract
name|void
name|update
parameter_list|(
name|long
name|recid
parameter_list|,
name|Object
name|obj
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      *  Updates a record using a custom serializer.      *      *  @param recid the recid for the record that is to be updated.      *  @param obj the new object for the record.      *  @param serializer a custom serializer      *  @throws IOException when one of the underlying I/O operations fails.      */
specifier|public
specifier|abstract
name|void
name|update
parameter_list|(
name|long
name|recid
parameter_list|,
name|Object
name|obj
parameter_list|,
name|Serializer
name|serializer
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      *  Fetches a record using standard java object serialization.      *      *  @param recid the recid for the record that must be fetched.      *  @return the object contained in the record.      *  @throws IOException when one of the underlying I/O operations fails.      */
specifier|public
specifier|abstract
name|Object
name|fetch
parameter_list|(
name|long
name|recid
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      *  Fetches a record using a custom serializer.      *      *  @param recid the recid for the record that must be fetched.      *  @param serializer a custom serializer      *  @return the object contained in the record.      *  @throws IOException when one of the underlying I/O operations fails.      */
specifier|public
specifier|abstract
name|Object
name|fetch
parameter_list|(
name|long
name|recid
parameter_list|,
name|Serializer
name|serializer
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      *  Closes the record manager.      *      *  @throws IOException when one of the underlying I/O operations fails.      */
specifier|public
specifier|abstract
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      *  Returns the number of slots available for "root" rowids. These slots      *  can be used to store special rowids, like rowids that point to      *  other rowids. Root rowids are useful for bootstrapping access to      *  a set of data.      */
specifier|public
specifier|abstract
name|int
name|getRootCount
parameter_list|()
function_decl|;
comment|/**      *  Returns the indicated root rowid.      *      *  @see #getRootCount      */
specifier|public
specifier|abstract
name|long
name|getRoot
parameter_list|(
name|int
name|id
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      *  Sets the indicated root rowid.      *      *  @see #getRootCount      */
specifier|public
specifier|abstract
name|void
name|setRoot
parameter_list|(
name|int
name|id
parameter_list|,
name|long
name|rowid
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Commit (make persistent) all changes since beginning of transaction.      */
specifier|public
specifier|abstract
name|void
name|commit
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Rollback (cancel) all changes since beginning of transaction.      */
specifier|public
specifier|abstract
name|void
name|rollback
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Obtain the record id of a named object. Returns 0 if named object      * doesn't exist.      */
specifier|public
specifier|abstract
name|long
name|getNamedObject
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Set the record id of a named object.      */
specifier|public
specifier|abstract
name|void
name|setNamedObject
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|recid
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

