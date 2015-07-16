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
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|Utilities
import|;
end_import

begin_comment
comment|/**  * NOTE: before LLAP branch merge, there's no LLAP code here.  * There used to be a global static map of IOContext-s inside IOContext (Hive style!).  * Unfortunately, due to variety of factors, this is now a giant fustercluck.  * 1) Spark doesn't apparently care about multiple inputs, but has multiple threads, so one  *    threadlocal IOContext was added for it.  * 2) LLAP has lots of tasks in the same process so globals no longer cut it either.  * 3) However, Tez runs 2+ threads for one task (e.g. TezTaskEventRouter and TezChild), and these  *    surprisingly enough need the same context. Tez, in its infinite wisdom, doesn't allow them  *    to communicate in any way nor provide any shared context.  * So we are going to...  * 1) Keep the good ol' global map for MR and Tez. Hive style!  * 2) Keep the threadlocal for Spark. Hive style!  * 3) Create inheritable (TADA!) threadlocal with attemptId, only set in LLAP; that will propagate  *    to all the little Tez threads, and we will keep a map per attempt. Hive style squared!  */
end_comment

begin_class
specifier|public
class|class
name|IOContextMap
block|{
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_CONTEXT
init|=
literal|""
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|IOContextMap
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Used for Tez and MR */
specifier|private
specifier|static
specifier|final
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|IOContext
argument_list|>
name|globalMap
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|IOContext
argument_list|>
argument_list|()
decl_stmt|;
comment|/** Used for Spark */
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|IOContext
argument_list|>
name|sparkThreadLocal
init|=
operator|new
name|ThreadLocal
argument_list|<
name|IOContext
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|IOContext
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|IOContext
argument_list|()
return|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|static
name|IOContext
name|get
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
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
name|HIVE_EXECUTION_ENGINE
argument_list|)
operator|.
name|equals
argument_list|(
literal|"spark"
argument_list|)
condition|)
block|{
return|return
name|sparkThreadLocal
operator|.
name|get
argument_list|()
return|;
block|}
name|String
name|inputName
init|=
name|conf
operator|.
name|get
argument_list|(
name|Utilities
operator|.
name|INPUT_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|inputName
operator|==
literal|null
condition|)
block|{
name|inputName
operator|=
name|DEFAULT_CONTEXT
expr_stmt|;
block|}
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|IOContext
argument_list|>
name|map
decl_stmt|;
name|map
operator|=
name|globalMap
expr_stmt|;
name|IOContext
name|ioContext
init|=
name|map
operator|.
name|get
argument_list|(
name|inputName
argument_list|)
decl_stmt|;
if|if
condition|(
name|ioContext
operator|!=
literal|null
condition|)
return|return
name|ioContext
return|;
name|ioContext
operator|=
operator|new
name|IOContext
argument_list|()
expr_stmt|;
name|IOContext
name|oldContext
init|=
name|map
operator|.
name|putIfAbsent
argument_list|(
name|inputName
argument_list|,
name|ioContext
argument_list|)
decl_stmt|;
return|return
operator|(
name|oldContext
operator|==
literal|null
operator|)
condition|?
name|ioContext
else|:
name|oldContext
return|;
block|}
specifier|public
specifier|static
name|void
name|clear
parameter_list|()
block|{
name|sparkThreadLocal
operator|.
name|remove
argument_list|()
expr_stmt|;
name|globalMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

