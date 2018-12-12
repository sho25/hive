begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metastore
operator|.
name|tools
operator|.
name|metatool
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_class
class|class
name|MetaToolTaskListFSRoot
extends|extends
name|MetaToolTask
block|{
annotation|@
name|Override
name|void
name|execute
parameter_list|()
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|hdfsRoots
init|=
name|getObjectStore
argument_list|()
operator|.
name|listFSRoots
argument_list|()
decl_stmt|;
if|if
condition|(
name|hdfsRoots
operator|!=
literal|null
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Listing FS Roots.."
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|s
range|:
name|hdfsRoots
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Encountered error during listFSRoot"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

