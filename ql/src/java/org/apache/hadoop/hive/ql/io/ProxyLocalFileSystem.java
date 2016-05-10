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
name|fs
operator|.
name|LocalFileSystem
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
comment|/**  * This class is to workaround existing issues on LocalFileSystem.  */
end_comment

begin_class
specifier|public
class|class
name|ProxyLocalFileSystem
extends|extends
name|LocalFileSystem
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|rename
parameter_list|(
name|Path
name|src
parameter_list|,
name|Path
name|dst
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Make sure for existing destination we return false as per FileSystem api contract
return|return
name|super
operator|.
name|isFile
argument_list|(
name|dst
argument_list|)
condition|?
literal|false
else|:
name|super
operator|.
name|rename
argument_list|(
name|src
argument_list|,
name|dst
argument_list|)
return|;
block|}
block|}
end_class

end_unit

