begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|spark
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|Path
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
name|conf
operator|.
name|HiveConf
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
name|exec
operator|.
name|persistence
operator|.
name|MapJoinTableContainer
import|;
end_import

begin_class
specifier|public
class|class
name|SmallTableCache
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SmallTableCache
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ConcurrentHashMap
argument_list|<
name|Path
argument_list|,
name|MapJoinTableContainer
argument_list|>
name|tableContainerMap
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|Path
argument_list|,
name|MapJoinTableContainer
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|volatile
name|String
name|queryId
decl_stmt|;
comment|/**    * Check if this is a new query. If so, clean up the cache    * that is for the previous query, and reset the current query id.    */
specifier|public
specifier|static
name|void
name|initialize
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|currentQueryId
init|=
name|conf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
operator|.
name|varname
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|currentQueryId
operator|.
name|equals
argument_list|(
name|queryId
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|tableContainerMap
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
synchronized|synchronized
init|(
name|tableContainerMap
init|)
block|{
if|if
condition|(
operator|!
name|currentQueryId
operator|.
name|equals
argument_list|(
name|queryId
argument_list|)
operator|&&
operator|!
name|tableContainerMap
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|MapJoinTableContainer
name|tableContainer
range|:
name|tableContainerMap
operator|.
name|values
argument_list|()
control|)
block|{
name|tableContainer
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|tableContainerMap
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Cleaned up small table cache for query "
operator|+
name|queryId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|queryId
operator|=
name|currentQueryId
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|cache
parameter_list|(
name|Path
name|path
parameter_list|,
name|MapJoinTableContainer
name|tableContainer
parameter_list|)
block|{
if|if
condition|(
name|tableContainerMap
operator|.
name|putIfAbsent
argument_list|(
name|path
argument_list|,
name|tableContainer
argument_list|)
operator|==
literal|null
operator|&&
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Cached small table file "
operator|+
name|path
operator|+
literal|" for query "
operator|+
name|queryId
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|MapJoinTableContainer
name|get
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|MapJoinTableContainer
name|tableContainer
init|=
name|tableContainerMap
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableContainer
operator|!=
literal|null
operator|&&
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Loaded small table file "
operator|+
name|path
operator|+
literal|" from cache for query "
operator|+
name|queryId
argument_list|)
expr_stmt|;
block|}
return|return
name|tableContainer
return|;
block|}
block|}
end_class

end_unit

