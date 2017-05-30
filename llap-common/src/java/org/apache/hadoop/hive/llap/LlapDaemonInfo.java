begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  *  you may not use this file except in compliance with the License.  *  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|llap
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
name|atomic
operator|.
name|AtomicReference
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|Resource
import|;
end_import

begin_enum
specifier|public
enum|enum
name|LlapDaemonInfo
block|{
name|INSTANCE
block|;
specifier|private
specifier|static
specifier|final
class|class
name|LlapDaemonInfoHolder
block|{
specifier|public
name|LlapDaemonInfoHolder
parameter_list|(
name|int
name|numExecutors
parameter_list|,
name|long
name|executorMemory
parameter_list|,
name|long
name|cacheSize
parameter_list|,
name|boolean
name|isDirectCache
parameter_list|,
name|boolean
name|isLlapIo
parameter_list|,
specifier|final
name|String
name|pid
parameter_list|)
block|{
name|this
operator|.
name|numExecutors
operator|=
name|numExecutors
expr_stmt|;
name|this
operator|.
name|executorMemory
operator|=
name|executorMemory
expr_stmt|;
name|this
operator|.
name|cacheSize
operator|=
name|cacheSize
expr_stmt|;
name|this
operator|.
name|isDirectCache
operator|=
name|isDirectCache
expr_stmt|;
name|this
operator|.
name|isLlapIo
operator|=
name|isLlapIo
expr_stmt|;
name|this
operator|.
name|PID
operator|=
name|pid
expr_stmt|;
block|}
specifier|final
name|int
name|numExecutors
decl_stmt|;
specifier|final
name|long
name|executorMemory
decl_stmt|;
specifier|final
name|long
name|cacheSize
decl_stmt|;
specifier|final
name|boolean
name|isDirectCache
decl_stmt|;
specifier|final
name|boolean
name|isLlapIo
decl_stmt|;
specifier|final
name|String
name|PID
decl_stmt|;
block|}
comment|// add more variables as required
specifier|private
name|AtomicReference
argument_list|<
name|LlapDaemonInfoHolder
argument_list|>
name|dataRef
init|=
operator|new
name|AtomicReference
argument_list|<
name|LlapDaemonInfoHolder
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|void
name|initialize
parameter_list|(
name|String
name|appName
parameter_list|,
name|Configuration
name|daemonConf
parameter_list|)
block|{
name|int
name|numExecutors
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|daemonConf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_NUM_EXECUTORS
argument_list|)
decl_stmt|;
name|long
name|executorMemoryBytes
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|daemonConf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_MEMORY_PER_INSTANCE_MB
argument_list|)
operator|*
literal|1024l
operator|*
literal|1024l
decl_stmt|;
name|long
name|ioMemoryBytes
init|=
name|HiveConf
operator|.
name|getSizeVar
argument_list|(
name|daemonConf
argument_list|,
name|ConfVars
operator|.
name|LLAP_IO_MEMORY_MAX_SIZE
argument_list|)
decl_stmt|;
name|boolean
name|isDirectCache
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|daemonConf
argument_list|,
name|ConfVars
operator|.
name|LLAP_ALLOCATOR_DIRECT
argument_list|)
decl_stmt|;
name|boolean
name|isLlapIo
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|daemonConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_IO_ENABLED
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|String
name|pid
init|=
name|System
operator|.
name|getenv
argument_list|(
literal|"JVM_PID"
argument_list|)
decl_stmt|;
name|initialize
argument_list|(
name|appName
argument_list|,
name|numExecutors
argument_list|,
name|executorMemoryBytes
argument_list|,
name|ioMemoryBytes
argument_list|,
name|isDirectCache
argument_list|,
name|isLlapIo
argument_list|,
name|pid
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|initialize
parameter_list|(
name|String
name|appName
parameter_list|,
name|int
name|numExecutors
parameter_list|,
name|long
name|executorMemoryBytes
parameter_list|,
name|long
name|ioMemoryBytes
parameter_list|,
name|boolean
name|isDirectCache
parameter_list|,
name|boolean
name|isLlapIo
parameter_list|,
specifier|final
name|String
name|pid
parameter_list|)
block|{
name|INSTANCE
operator|.
name|dataRef
operator|.
name|set
argument_list|(
operator|new
name|LlapDaemonInfoHolder
argument_list|(
name|numExecutors
argument_list|,
name|executorMemoryBytes
argument_list|,
name|ioMemoryBytes
argument_list|,
name|isDirectCache
argument_list|,
name|isLlapIo
argument_list|,
name|pid
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isLlap
parameter_list|()
block|{
return|return
name|dataRef
operator|.
name|get
argument_list|()
operator|!=
literal|null
return|;
block|}
specifier|public
name|int
name|getNumExecutors
parameter_list|()
block|{
return|return
name|dataRef
operator|.
name|get
argument_list|()
operator|.
name|numExecutors
return|;
block|}
specifier|public
name|long
name|getExecutorMemory
parameter_list|()
block|{
return|return
name|dataRef
operator|.
name|get
argument_list|()
operator|.
name|executorMemory
return|;
block|}
specifier|public
name|long
name|getMemoryPerExecutor
parameter_list|()
block|{
specifier|final
name|LlapDaemonInfoHolder
name|data
init|=
name|dataRef
operator|.
name|get
argument_list|()
decl_stmt|;
return|return
operator|(
name|getExecutorMemory
argument_list|()
operator|-
operator|-
operator|(
name|data
operator|.
name|isDirectCache
condition|?
literal|0
else|:
name|data
operator|.
name|cacheSize
operator|)
operator|)
operator|/
name|getNumExecutors
argument_list|()
return|;
block|}
specifier|public
name|long
name|getCacheSize
parameter_list|()
block|{
return|return
name|dataRef
operator|.
name|get
argument_list|()
operator|.
name|cacheSize
return|;
block|}
specifier|public
name|boolean
name|isDirectCache
parameter_list|()
block|{
return|return
name|dataRef
operator|.
name|get
argument_list|()
operator|.
name|isDirectCache
return|;
block|}
specifier|public
name|boolean
name|isLlapIo
parameter_list|()
block|{
return|return
name|dataRef
operator|.
name|get
argument_list|()
operator|.
name|isLlapIo
return|;
block|}
specifier|public
name|String
name|getPID
parameter_list|()
block|{
return|return
name|dataRef
operator|.
name|get
argument_list|()
operator|.
name|PID
return|;
block|}
block|}
end_enum

end_unit

