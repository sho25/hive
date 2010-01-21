begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/**  * JDBM LICENSE v1.00  *  * Redistribution and use of this software and associated documentation  * ("Software"), with or without modification, are permitted provided  * that the following conditions are met:  *  * 1. Redistributions of source code must retain copyright  *    statements and notices.  Redistributions must also contain a  *    copy of this document.  *  * 2. Redistributions in binary form must reproduce the  *    above copyright notice, this list of conditions and the  *    following disclaimer in the documentation and/or other  *    materials provided with the distribution.  *  * 3. The name "JDBM" must not be used to endorse or promote  *    products derived from this Software without prior written  *    permission of Cees de Groot.  For written permission,  *    please contact cg@cdegroot.com.  *  * 4. Products derived from this Software may not be called "JDBM"  *    nor may "JDBM" appear in their names without prior written  *    permission of Cees de Groot.  *  * 5. Due credit should be given to the JDBM Project  *    (http://jdbm.sourceforge.net/).  *  * THIS SOFTWARE IS PROVIDED BY THE JDBM PROJECT AND CONTRIBUTORS  * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT  * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL  * CEES DE GROOT OR ANY CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED  * OF THE POSSIBILITY OF SUCH DAMAGE.  *  * Copyright 2000 (C) Cees de Groot. All Rights Reserved.  * Copyright 2000-2001 (C) Alex Boisvert. All Rights Reserved.  * Contributions are Copyright (C) 2000 by their associated contributors.  *  * $Id: RecordManagerFactory.java,v 1.2 2005/06/25 23:12:31 doomdark Exp $  */
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
name|File
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
name|util
operator|.
name|Properties
import|;
end_import

begin_comment
comment|/**  * This is the factory class to use for instantiating {@link RecordManager}  * instances.  *   * @author<a href="mailto:boisvert@intalio.com">Alex Boisvert</a>  * @author<a href="cg@cdegroot.com">Cees de Groot</a>  * @version $Id: RecordManagerFactory.java,v 1.2 2005/06/25 23:12:31 doomdark  *          Exp $  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|RecordManagerFactory
block|{
comment|/**    * Create a record manager.    *     * @param name    *          Name of the record file.    * @throws IOException    *           if an I/O related exception occurs while creating or opening the    *           record manager.    * @throws UnsupportedOperationException    *           if some options are not supported by the implementation.    * @throws IllegalArgumentException    *           if some options are invalid.    */
specifier|public
specifier|static
name|RecordManager
name|createRecordManager
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createRecordManager
argument_list|(
name|name
argument_list|,
operator|new
name|Properties
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Create a record manager.    *     * @param name    *          Name of the record file.    * @param options    *          Record manager options.    * @throws IOException    *           if an I/O related exception occurs while creating or opening the    *           record manager.    * @throws UnsupportedOperationException    *           if some options are not supported by the implementation.    * @throws IllegalArgumentException    *           if some options are invalid.    */
specifier|public
specifier|static
name|RecordManager
name|createRecordManager
parameter_list|(
name|String
name|name
parameter_list|,
name|Properties
name|options
parameter_list|)
throws|throws
name|IOException
block|{
name|RecordManagerProvider
name|factory
init|=
name|getFactory
argument_list|(
name|options
argument_list|)
decl_stmt|;
return|return
name|factory
operator|.
name|createRecordManager
argument_list|(
name|name
argument_list|,
name|options
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|RecordManager
name|createRecordManager
parameter_list|(
name|File
name|file
parameter_list|,
name|Properties
name|options
parameter_list|)
throws|throws
name|IOException
block|{
name|RecordManagerProvider
name|factory
init|=
name|getFactory
argument_list|(
name|options
argument_list|)
decl_stmt|;
return|return
name|factory
operator|.
name|createRecordManager
argument_list|(
name|file
argument_list|,
name|options
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|RecordManagerProvider
name|getFactory
parameter_list|(
name|Properties
name|options
parameter_list|)
block|{
name|String
name|provider
decl_stmt|;
name|Class
name|clazz
decl_stmt|;
name|RecordManagerProvider
name|factory
decl_stmt|;
name|provider
operator|=
name|options
operator|.
name|getProperty
argument_list|(
name|RecordManagerOptions
operator|.
name|PROVIDER_FACTORY
argument_list|,
literal|"org.apache.hadoop.hive.ql.util.jdbm.recman.Provider"
argument_list|)
expr_stmt|;
try|try
block|{
name|clazz
operator|=
name|Class
operator|.
name|forName
argument_list|(
name|provider
argument_list|)
expr_stmt|;
name|factory
operator|=
operator|(
name|RecordManagerProvider
operator|)
name|clazz
operator|.
name|newInstance
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|except
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid record manager provider: "
operator|+
name|provider
operator|+
literal|"\n["
operator|+
name|except
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|": "
operator|+
name|except
operator|.
name|getMessage
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|factory
return|;
block|}
block|}
end_class

end_unit

