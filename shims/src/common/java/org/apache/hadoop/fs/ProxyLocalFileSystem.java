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
name|fs
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
name|java
operator|.
name|net
operator|.
name|URI
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
name|conf
operator|.
name|Configuration
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
name|Shell
import|;
end_import

begin_comment
comment|/****************************************************************  * A Proxy for LocalFileSystem  *  * Serves uri's corresponding to 'pfile:///' namespace with using  * a LocalFileSystem  *****************************************************************/
end_comment

begin_class
specifier|public
class|class
name|ProxyLocalFileSystem
extends|extends
name|FilterFileSystem
block|{
specifier|protected
name|LocalFileSystem
name|localFs
decl_stmt|;
specifier|public
name|ProxyLocalFileSystem
parameter_list|()
block|{
name|localFs
operator|=
operator|new
name|LocalFileSystem
argument_list|()
expr_stmt|;
block|}
specifier|public
name|ProxyLocalFileSystem
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unsupported Constructor"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|URI
name|name
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// create a proxy for the local filesystem
comment|// the scheme/authority serving as the proxy is derived
comment|// from the supplied URI
name|String
name|scheme
init|=
name|name
operator|.
name|getScheme
argument_list|()
decl_stmt|;
name|String
name|nameUriString
init|=
name|name
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|Shell
operator|.
name|WINDOWS
condition|)
block|{
comment|// Replace the encoded backward slash with forward slash
comment|// Remove the windows drive letter
comment|// replace the '=' with special string '------' to handle the unsupported char '=' in windows.
name|nameUriString
operator|=
name|nameUriString
operator|.
name|replaceAll
argument_list|(
literal|"%5C"
argument_list|,
literal|"/"
argument_list|)
operator|.
name|replaceFirst
argument_list|(
literal|"/[c-zC-Z]:"
argument_list|,
literal|"/"
argument_list|)
operator|.
name|replaceFirst
argument_list|(
literal|"^[c-zC-Z]:"
argument_list|,
literal|""
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|"="
argument_list|,
literal|"------"
argument_list|)
expr_stmt|;
name|name
operator|=
name|URI
operator|.
name|create
argument_list|(
name|nameUriString
argument_list|)
expr_stmt|;
block|}
name|String
name|authority
init|=
name|name
operator|.
name|getAuthority
argument_list|()
operator|!=
literal|null
condition|?
name|name
operator|.
name|getAuthority
argument_list|()
else|:
literal|""
decl_stmt|;
name|String
name|proxyUriString
init|=
name|nameUriString
operator|+
literal|"://"
operator|+
name|authority
operator|+
literal|"/"
decl_stmt|;
name|fs
operator|=
operator|new
name|ProxyFileSystem
argument_list|(
name|localFs
argument_list|,
name|URI
operator|.
name|create
argument_list|(
name|proxyUriString
argument_list|)
argument_list|)
expr_stmt|;
name|fs
operator|.
name|initialize
argument_list|(
name|name
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

