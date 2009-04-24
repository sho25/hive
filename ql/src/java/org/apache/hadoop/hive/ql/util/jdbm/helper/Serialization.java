begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/**  * JDBM LICENSE v1.00  *  * Redistribution and use of this software and associated documentation  * ("Software"), with or without modification, are permitted provided  * that the following conditions are met:  *  * 1. Redistributions of source code must retain copyright  *    statements and notices.  Redistributions must also contain a  *    copy of this document.  *  * 2. Redistributions in binary form must reproduce the  *    above copyright notice, this list of conditions and the  *    following disclaimer in the documentation and/or other  *    materials provided with the distribution.  *  * 3. The name "JDBM" must not be used to endorse or promote  *    products derived from this Software without prior written  *    permission of Cees de Groot.  For written permission,  *    please contact cg@cdegroot.com.  *  * 4. Products derived from this Software may not be called "JDBM"  *    nor may "JDBM" appear in their names without prior written  *    permission of Cees de Groot.  *  * 5. Due credit should be given to the JDBM Project  *    (http://jdbm.sourceforge.net/).  *  * THIS SOFTWARE IS PROVIDED BY THE JDBM PROJECT AND CONTRIBUTORS  * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT  * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL  * CEES DE GROOT OR ANY CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED  * OF THE POSSIBILITY OF SUCH DAMAGE.  *  * Copyright 2001 (C) Alex Boisvert. All Rights Reserved.  * Contributions are Copyright (C) 2001 by their associated contributors.  *  */
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
name|helper
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|java
operator|.
name|io
operator|.
name|ObjectInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectOutputStream
import|;
end_import

begin_comment
comment|/**  * Serialization-related utility methods.  *  * @author<a href="mailto:boisvert@intalio.com">Alex Boisvert</a>  * @version $Id: Serialization.java,v 1.1 2002/05/31 06:33:20 boisvert Exp $  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|Serialization
block|{
comment|/**      * Serialize the object into a byte array.      */
specifier|public
specifier|static
name|byte
index|[]
name|serialize
parameter_list|(
name|Object
name|obj
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteArrayOutputStream
name|baos
decl_stmt|;
name|ObjectOutputStream
name|oos
decl_stmt|;
name|baos
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|oos
operator|=
operator|new
name|ObjectOutputStream
argument_list|(
name|baos
argument_list|)
expr_stmt|;
name|oos
operator|.
name|writeObject
argument_list|(
name|obj
argument_list|)
expr_stmt|;
name|oos
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|baos
operator|.
name|toByteArray
argument_list|()
return|;
block|}
comment|/**      * Deserialize an object from a byte array      */
specifier|public
specifier|static
name|Object
name|deserialize
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|)
throws|throws
name|ClassNotFoundException
throws|,
name|IOException
block|{
name|ByteArrayInputStream
name|bais
decl_stmt|;
name|ObjectInputStream
name|ois
decl_stmt|;
name|bais
operator|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|buf
argument_list|)
expr_stmt|;
name|ois
operator|=
operator|new
name|ObjectInputStream
argument_list|(
name|bais
argument_list|)
expr_stmt|;
return|return
name|ois
operator|.
name|readObject
argument_list|()
return|;
block|}
block|}
end_class

end_unit

