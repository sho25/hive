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
name|common
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
name|fs
operator|.
name|FileSystem
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

begin_comment
comment|/**  * Collection of file manipulation utilities common across Hive  */
end_comment

begin_class
specifier|public
class|class
name|FileUtils
block|{
comment|/**    * Variant of Path.makeQualified that qualifies the input path against the    * default file system indicated by the configuration    *     * This does not require a FileSystem handle in most cases - only requires the    * Filesystem URI. This saves the cost of opening the Filesystem - which can    * involve RPCs - as well as cause errors    *     * @param path    *          path to be fully qualified    * @param conf    *          Configuration file    * @return path qualified relative to default file system    */
specifier|public
specifier|static
name|Path
name|makeQualified
parameter_list|(
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|path
operator|.
name|isAbsolute
argument_list|()
condition|)
block|{
comment|// in this case we need to get the working directory
comment|// and this requires a FileSystem handle. So revert to
comment|// original method.
return|return
name|path
operator|.
name|makeQualified
argument_list|(
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
argument_list|)
return|;
block|}
name|URI
name|fsUri
init|=
name|FileSystem
operator|.
name|getDefaultUri
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|URI
name|pathUri
init|=
name|path
operator|.
name|toUri
argument_list|()
decl_stmt|;
name|String
name|scheme
init|=
name|pathUri
operator|.
name|getScheme
argument_list|()
decl_stmt|;
name|String
name|authority
init|=
name|pathUri
operator|.
name|getAuthority
argument_list|()
decl_stmt|;
if|if
condition|(
name|scheme
operator|!=
literal|null
operator|&&
operator|(
name|authority
operator|!=
literal|null
operator|||
name|fsUri
operator|.
name|getAuthority
argument_list|()
operator|==
literal|null
operator|)
condition|)
block|{
return|return
name|path
return|;
block|}
if|if
condition|(
name|scheme
operator|==
literal|null
condition|)
block|{
name|scheme
operator|=
name|fsUri
operator|.
name|getScheme
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|authority
operator|==
literal|null
condition|)
block|{
name|authority
operator|=
name|fsUri
operator|.
name|getAuthority
argument_list|()
expr_stmt|;
if|if
condition|(
name|authority
operator|==
literal|null
condition|)
block|{
name|authority
operator|=
literal|""
expr_stmt|;
block|}
block|}
return|return
operator|new
name|Path
argument_list|(
name|scheme
operator|+
literal|":"
operator|+
literal|"//"
operator|+
name|authority
operator|+
name|pathUri
operator|.
name|getPath
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

