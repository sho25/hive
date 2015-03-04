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
operator|.
name|tez
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|ReentrantLock
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
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
name|ObjectCache
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
name|ObjectCacheFactory
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
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_comment
comment|/**  * Access to the Object cache from Tez, along with utility methods for accessing specific Keys.  */
end_comment

begin_class
specifier|public
class|class
name|TezCacheAccess
block|{
specifier|private
name|TezCacheAccess
parameter_list|(
name|ObjectCache
name|cache
parameter_list|,
name|String
name|qId
parameter_list|)
block|{
name|this
operator|.
name|qId
operator|=
name|qId
expr_stmt|;
name|this
operator|.
name|cache
operator|=
name|cache
expr_stmt|;
block|}
specifier|private
name|ObjectCache
name|cache
decl_stmt|;
specifier|private
name|String
name|qId
decl_stmt|;
specifier|public
specifier|static
name|TezCacheAccess
name|createInstance
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|ObjectCache
name|cache
init|=
name|ObjectCacheFactory
operator|.
name|getCache
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|String
name|qId
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|)
decl_stmt|;
return|return
operator|new
name|TezCacheAccess
argument_list|(
name|cache
argument_list|,
name|qId
argument_list|)
return|;
block|}
specifier|private
specifier|static
specifier|final
name|String
name|CACHED_INPUT_KEY
init|=
literal|"CACHED_INPUTS"
decl_stmt|;
specifier|private
specifier|final
name|ReentrantLock
name|cachedInputLock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|get
parameter_list|()
throws|throws
name|HiveException
block|{
return|return
operator|(
name|Set
argument_list|<
name|String
argument_list|>
operator|)
name|cache
operator|.
name|retrieve
argument_list|(
name|CACHED_INPUT_KEY
argument_list|,
operator|new
name|Callable
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
specifier|public
name|Object
name|call
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|newSetFromMap
argument_list|(
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isInputCached
parameter_list|(
name|String
name|inputName
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
operator|.
name|cachedInputLock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|get
argument_list|()
operator|.
name|contains
argument_list|(
name|qId
operator|+
name|inputName
argument_list|)
return|;
block|}
finally|finally
block|{
name|this
operator|.
name|cachedInputLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|registerCachedInput
parameter_list|(
name|String
name|inputName
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
operator|.
name|cachedInputLock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|get
argument_list|()
operator|.
name|add
argument_list|(
name|qId
operator|+
name|inputName
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|cachedInputLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

