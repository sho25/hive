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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|metastore
operator|.
name|ObjectStore
import|;
end_import

begin_class
class|class
name|MetaToolTaskExecuteJDOQLQuery
extends|extends
name|MetaToolTask
block|{
annotation|@
name|Override
name|void
name|execute
parameter_list|()
block|{
name|String
name|query
init|=
name|getCl
argument_list|()
operator|.
name|getJDOQLQuery
argument_list|()
decl_stmt|;
if|if
condition|(
name|query
operator|.
name|toLowerCase
argument_list|()
operator|.
name|trim
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"select"
argument_list|)
condition|)
block|{
name|executeJDOQLSelect
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|query
operator|.
name|toLowerCase
argument_list|()
operator|.
name|trim
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"update"
argument_list|)
condition|)
block|{
name|executeJDOQLUpdate
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"HiveMetaTool:Unsupported statement type, only select and update supported"
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|executeJDOQLSelect
parameter_list|(
name|String
name|query
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Executing query: "
operator|+
name|query
argument_list|)
expr_stmt|;
try|try
init|(
name|ObjectStore
operator|.
name|QueryWrapper
name|queryWrapper
init|=
operator|new
name|ObjectStore
operator|.
name|QueryWrapper
argument_list|()
init|)
block|{
name|Collection
argument_list|<
name|?
argument_list|>
name|result
init|=
name|getObjectStore
argument_list|()
operator|.
name|executeJDOQLSelect
argument_list|(
name|query
argument_list|,
name|queryWrapper
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
name|Iterator
argument_list|<
name|?
argument_list|>
name|iter
init|=
name|result
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Object
name|o
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|o
operator|.
name|toString
argument_list|()
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
literal|"Encountered error during executeJDOQLSelect"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|executeJDOQLUpdate
parameter_list|(
name|String
name|query
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Executing query: "
operator|+
name|query
argument_list|)
expr_stmt|;
name|long
name|numUpdated
init|=
name|getObjectStore
argument_list|()
operator|.
name|executeJDOQLUpdate
argument_list|(
name|query
argument_list|)
decl_stmt|;
if|if
condition|(
name|numUpdated
operator|>=
literal|0
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Number of records updated: "
operator|+
name|numUpdated
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Encountered error during executeJDOQL - commit of JDO transaction failed."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

