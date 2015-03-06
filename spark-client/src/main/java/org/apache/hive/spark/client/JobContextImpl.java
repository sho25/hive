begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|java
operator|.
name|util
operator|.
name|Map
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
name|CopyOnWriteArrayList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|counter
operator|.
name|SparkCounters
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaFutureAction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaSparkContext
import|;
end_import

begin_class
class|class
name|JobContextImpl
implements|implements
name|JobContext
block|{
specifier|private
specifier|final
name|JavaSparkContext
name|sc
decl_stmt|;
specifier|private
specifier|final
name|ThreadLocal
argument_list|<
name|MonitorCallback
argument_list|>
name|monitorCb
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|JavaFutureAction
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
name|monitoredJobs
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|addedJars
decl_stmt|;
specifier|private
specifier|final
name|File
name|localTmpDir
decl_stmt|;
specifier|public
name|JobContextImpl
parameter_list|(
name|JavaSparkContext
name|sc
parameter_list|,
name|File
name|localTmpDir
parameter_list|)
block|{
name|this
operator|.
name|sc
operator|=
name|sc
expr_stmt|;
name|this
operator|.
name|monitorCb
operator|=
operator|new
name|ThreadLocal
argument_list|<
name|MonitorCallback
argument_list|>
argument_list|()
expr_stmt|;
name|monitoredJobs
operator|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|JavaFutureAction
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|addedJars
operator|=
operator|new
name|CopyOnWriteArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|localTmpDir
operator|=
name|localTmpDir
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|JavaSparkContext
name|sc
parameter_list|()
block|{
return|return
name|sc
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|JavaFutureAction
argument_list|<
name|T
argument_list|>
name|monitor
parameter_list|(
name|JavaFutureAction
argument_list|<
name|T
argument_list|>
name|job
parameter_list|,
name|SparkCounters
name|sparkCounters
parameter_list|,
name|Set
argument_list|<
name|Integer
argument_list|>
name|cachedRDDIds
parameter_list|)
block|{
name|monitorCb
operator|.
name|get
argument_list|()
operator|.
name|call
argument_list|(
name|job
argument_list|,
name|sparkCounters
argument_list|,
name|cachedRDDIds
argument_list|)
expr_stmt|;
return|return
name|job
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|JavaFutureAction
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
name|getMonitoredJobs
parameter_list|()
block|{
return|return
name|monitoredJobs
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAddedJars
parameter_list|()
block|{
return|return
name|addedJars
return|;
block|}
annotation|@
name|Override
specifier|public
name|File
name|getLocalTmpDir
parameter_list|()
block|{
return|return
name|localTmpDir
return|;
block|}
name|void
name|setMonitorCb
parameter_list|(
name|MonitorCallback
name|cb
parameter_list|)
block|{
name|monitorCb
operator|.
name|set
argument_list|(
name|cb
argument_list|)
expr_stmt|;
block|}
name|void
name|stop
parameter_list|()
block|{
name|monitoredJobs
operator|.
name|clear
argument_list|()
expr_stmt|;
name|sc
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

