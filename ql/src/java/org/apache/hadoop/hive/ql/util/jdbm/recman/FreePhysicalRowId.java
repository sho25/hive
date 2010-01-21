begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/**  * JDBM LICENSE v1.00  *  * Redistribution and use of this software and associated documentation  * ("Software"), with or without modification, are permitted provided  * that the following conditions are met:  *  * 1. Redistributions of source code must retain copyright  *    statements and notices.  Redistributions must also contain a  *    copy of this document.  *  * 2. Redistributions in binary form must reproduce the  *    above copyright notice, this list of conditions and the  *    following disclaimer in the documentation and/or other  *    materials provided with the distribution.  *  * 3. The name "JDBM" must not be used to endorse or promote  *    products derived from this Software without prior written  *    permission of Cees de Groot.  For written permission,  *    please contact cg@cdegroot.com.  *  * 4. Products derived from this Software may not be called "JDBM"  *    nor may "JDBM" appear in their names without prior written  *    permission of Cees de Groot.   *  * 5. Due credit should be given to the JDBM Project  *    (http://jdbm.sourceforge.net/).  *  * THIS SOFTWARE IS PROVIDED BY THE JDBM PROJECT AND CONTRIBUTORS  * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT  * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL  * CEES DE GROOT OR ANY CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED  * OF THE POSSIBILITY OF SUCH DAMAGE.  *  * Copyright 2000 (C) Cees de Groot. All Rights Reserved.  * Contributions are Copyright (C) 2000 by their associated contributors.  *  * $Id: FreePhysicalRowId.java,v 1.1 2000/05/06 00:00:31 boisvert Exp $  */
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

begin_comment
comment|/**  * This class extends the physical rowid with a size value to indicated the size  * of a free rowid on the free rowid list.  */
end_comment

begin_class
specifier|final
class|class
name|FreePhysicalRowId
extends|extends
name|PhysicalRowId
block|{
comment|// offsets
specifier|private
specifier|static
specifier|final
name|short
name|O_SIZE
init|=
name|PhysicalRowId
operator|.
name|SIZE
decl_stmt|;
comment|// int size
specifier|static
specifier|final
name|short
name|SIZE
init|=
name|O_SIZE
operator|+
name|Magic
operator|.
name|SZ_INT
decl_stmt|;
comment|/**    * Constructs a physical rowid from the indicated data starting at the    * indicated position.    */
name|FreePhysicalRowId
parameter_list|(
name|BlockIo
name|block
parameter_list|,
name|short
name|pos
parameter_list|)
block|{
name|super
argument_list|(
name|block
argument_list|,
name|pos
argument_list|)
expr_stmt|;
block|}
comment|/** Returns the size */
name|int
name|getSize
parameter_list|()
block|{
return|return
name|block
operator|.
name|readInt
argument_list|(
name|pos
operator|+
name|O_SIZE
argument_list|)
return|;
block|}
comment|/** Sets the size */
name|void
name|setSize
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|block
operator|.
name|writeInt
argument_list|(
name|pos
operator|+
name|O_SIZE
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

