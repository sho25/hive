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
name|common
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryMXBean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryNotificationInfo
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryPoolMXBean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryType
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryUsage
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|NotificationEmitter
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|NotificationListener
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|ListenerNotFoundException
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

begin_comment
comment|/**  * Class that monitors memory usage and notifies the listeners when a certain of threshold of memory is used  * after GC (collection usage).  */
end_comment

begin_class
specifier|public
class|class
name|HeapMemoryMonitor
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
name|HeapMemoryMonitor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// notifies when memory usage is 70% after GC
specifier|private
specifier|static
specifier|final
name|double
name|DEFAULT_THRESHOLD
init|=
literal|0.7d
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|MemoryPoolMXBean
name|tenuredGenPool
init|=
name|getTenuredGenPool
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|double
name|threshold
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Listener
argument_list|>
name|listeners
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|NotificationListener
name|notificationListener
decl_stmt|;
specifier|public
interface|interface
name|Listener
block|{
name|void
name|memoryUsageAboveThreshold
parameter_list|(
name|long
name|usedMemory
parameter_list|,
name|long
name|maxMemory
parameter_list|)
function_decl|;
block|}
specifier|public
name|HeapMemoryMonitor
parameter_list|(
name|double
name|threshold
parameter_list|)
block|{
name|this
operator|.
name|threshold
operator|=
name|threshold
operator|<=
literal|0.0d
operator|||
name|threshold
operator|>
literal|1.0d
condition|?
name|DEFAULT_THRESHOLD
else|:
name|threshold
expr_stmt|;
name|setupTenuredGenPoolThreshold
argument_list|(
name|tenuredGenPool
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setupTenuredGenPoolThreshold
parameter_list|(
specifier|final
name|MemoryPoolMXBean
name|tenuredGenPool
parameter_list|)
block|{
if|if
condition|(
name|tenuredGenPool
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|MemoryPoolMXBean
name|pool
range|:
name|ManagementFactory
operator|.
name|getMemoryPoolMXBeans
argument_list|()
control|)
block|{
specifier|final
name|long
name|memoryThreshold
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|floor
argument_list|(
name|pool
operator|.
name|getUsage
argument_list|()
operator|.
name|getMax
argument_list|()
operator|*
name|threshold
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|isTenured
init|=
name|isTenured
argument_list|(
name|pool
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|isTenured
condition|)
block|{
continue|continue;
block|}
comment|// set memory threshold on memory used after GC
specifier|final
name|boolean
name|isCollectionUsageThresholdSupported
init|=
name|pool
operator|.
name|isCollectionUsageThresholdSupported
argument_list|()
decl_stmt|;
if|if
condition|(
name|isCollectionUsageThresholdSupported
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting collection usage threshold to {}"
argument_list|,
name|memoryThreshold
argument_list|)
expr_stmt|;
name|pool
operator|.
name|setCollectionUsageThreshold
argument_list|(
name|memoryThreshold
argument_list|)
expr_stmt|;
return|return;
block|}
else|else
block|{
comment|// if collection usage threshold is not support, worst case set memory threshold on memory usage (before GC)
specifier|final
name|boolean
name|isUsageThresholdSupported
init|=
name|pool
operator|.
name|isUsageThresholdSupported
argument_list|()
decl_stmt|;
if|if
condition|(
name|isUsageThresholdSupported
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting usage threshold to {}"
argument_list|,
name|memoryThreshold
argument_list|)
expr_stmt|;
name|pool
operator|.
name|setUsageThreshold
argument_list|(
name|memoryThreshold
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
block|}
block|}
specifier|private
specifier|static
name|MemoryPoolMXBean
name|getTenuredGenPool
parameter_list|()
block|{
for|for
control|(
name|MemoryPoolMXBean
name|pool
range|:
name|ManagementFactory
operator|.
name|getMemoryPoolMXBeans
argument_list|()
control|)
block|{
specifier|final
name|String
name|vendor
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.vendor"
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|isTenured
init|=
name|isTenured
argument_list|(
name|pool
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|isTenured
condition|)
block|{
continue|continue;
block|}
specifier|final
name|boolean
name|isCollectionUsageThresholdSupported
init|=
name|pool
operator|.
name|isCollectionUsageThresholdSupported
argument_list|()
decl_stmt|;
if|if
condition|(
name|isCollectionUsageThresholdSupported
condition|)
block|{
return|return
name|pool
return|;
block|}
else|else
block|{
specifier|final
name|boolean
name|isUsageThresholdSupported
init|=
name|pool
operator|.
name|isUsageThresholdSupported
argument_list|()
decl_stmt|;
if|if
condition|(
name|isUsageThresholdSupported
condition|)
block|{
return|return
name|pool
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"{} vendor does not support isCollectionUsageThresholdSupported() and isUsageThresholdSupported()"
operator|+
literal|" for tenured memory pool '{}'."
argument_list|,
name|vendor
argument_list|,
name|pool
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|boolean
name|isTenured
parameter_list|(
name|MemoryPoolMXBean
name|memoryPoolMXBean
parameter_list|)
block|{
if|if
condition|(
name|memoryPoolMXBean
operator|.
name|getType
argument_list|()
operator|!=
name|MemoryType
operator|.
name|HEAP
condition|)
block|{
return|return
literal|false
return|;
block|}
name|String
name|name
init|=
name|memoryPoolMXBean
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
name|name
operator|.
name|equals
argument_list|(
literal|"CMS Old Gen"
argument_list|)
comment|// CMS
operator|||
name|name
operator|.
name|equals
argument_list|(
literal|"PS Old Gen"
argument_list|)
comment|// Parallel GC
operator|||
name|name
operator|.
name|equals
argument_list|(
literal|"G1 Old Gen"
argument_list|)
comment|// G1GC
comment|// other vendors like IBM, Azul etc. use different names
operator|||
name|name
operator|.
name|equals
argument_list|(
literal|"Old Space"
argument_list|)
operator|||
name|name
operator|.
name|equals
argument_list|(
literal|"Tenured Gen"
argument_list|)
operator|||
name|name
operator|.
name|equals
argument_list|(
literal|"Java heap"
argument_list|)
operator|||
name|name
operator|.
name|equals
argument_list|(
literal|"GenPauseless Old Gen"
argument_list|)
return|;
block|}
specifier|public
name|void
name|registerListener
parameter_list|(
specifier|final
name|Listener
name|listener
parameter_list|)
block|{
name|listeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MemoryUsage
name|getTenuredGenMemoryUsage
parameter_list|()
block|{
if|if
condition|(
name|tenuredGenPool
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|tenuredGenPool
operator|.
name|getUsage
argument_list|()
return|;
block|}
specifier|public
name|void
name|start
parameter_list|()
block|{
comment|// unsupported if null
if|if
condition|(
name|tenuredGenPool
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|MemoryMXBean
name|mxBean
init|=
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
decl_stmt|;
name|NotificationEmitter
name|emitter
init|=
operator|(
name|NotificationEmitter
operator|)
name|mxBean
decl_stmt|;
name|notificationListener
operator|=
parameter_list|(
name|n
parameter_list|,
name|hb
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|n
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
name|MemoryNotificationInfo
operator|.
name|MEMORY_COLLECTION_THRESHOLD_EXCEEDED
argument_list|)
condition|)
block|{
name|long
name|maxMemory
init|=
name|tenuredGenPool
operator|.
name|getUsage
argument_list|()
operator|.
name|getMax
argument_list|()
decl_stmt|;
name|long
name|usedMemory
init|=
name|tenuredGenPool
operator|.
name|getUsage
argument_list|()
operator|.
name|getUsed
argument_list|()
decl_stmt|;
for|for
control|(
name|Listener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|memoryUsageAboveThreshold
argument_list|(
name|usedMemory
argument_list|,
name|maxMemory
argument_list|)
expr_stmt|;
block|}
block|}
block|}
expr_stmt|;
name|emitter
operator|.
name|addNotificationListener
argument_list|(
name|notificationListener
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|notificationListener
operator|!=
literal|null
condition|)
block|{
name|MemoryMXBean
name|mxBean
init|=
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
decl_stmt|;
name|NotificationEmitter
name|emitter
init|=
operator|(
name|NotificationEmitter
operator|)
name|mxBean
decl_stmt|;
try|try
block|{
name|emitter
operator|.
name|removeNotificationListener
argument_list|(
name|notificationListener
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ListenerNotFoundException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to remove HeapMemoryMonitor notification listener from MemoryMXBean"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

