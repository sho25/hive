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
name|exec
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
name|base
operator|.
name|Preconditions
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
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLClassLoader
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_comment
comment|/**  * {@link UDFClassLoader} is used to dynamically register  * udf (and related) jars  *  * This was introducted to fix HIVE-11878  *  * Each session will have its own instance of {@link UDFClassLoader}  * This is to support HiveServer2 where there can be multiple  * active sessions. Addition/removal of jars/resources in one  * session should not affect other sessions.  */
end_comment

begin_class
specifier|public
class|class
name|UDFClassLoader
extends|extends
name|URLClassLoader
block|{
specifier|private
name|boolean
name|isClosed
decl_stmt|;
specifier|public
name|UDFClassLoader
parameter_list|(
name|URL
index|[]
name|urls
parameter_list|)
block|{
name|super
argument_list|(
name|urls
argument_list|)
expr_stmt|;
name|isClosed
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|UDFClassLoader
parameter_list|(
name|URL
index|[]
name|urls
parameter_list|,
name|ClassLoader
name|parent
parameter_list|)
block|{
name|super
argument_list|(
name|urls
argument_list|,
name|parent
argument_list|)
expr_stmt|;
name|isClosed
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addURL
parameter_list|(
name|URL
name|url
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
operator|!
name|isClosed
argument_list|,
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" is already closed"
argument_list|)
expr_stmt|;
name|super
operator|.
name|addURL
argument_list|(
name|url
argument_list|)
expr_stmt|;
block|}
comment|/**    * See {@link URLClassLoader#close}    */
specifier|public
name|boolean
name|isClosed
parameter_list|()
block|{
return|return
name|isClosed
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|isClosed
operator|=
literal|true
expr_stmt|;
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

