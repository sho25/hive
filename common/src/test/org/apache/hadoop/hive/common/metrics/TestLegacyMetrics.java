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
operator|.
name|metrics
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
name|util
operator|.
name|concurrent
operator|.
name|Callable
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
name|ExecutorService
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
name|Executors
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
name|TimeUnit
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|Attribute
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanAttributeInfo
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanInfo
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanOperationInfo
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanServer
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|ObjectName
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
name|common
operator|.
name|metrics
operator|.
name|common
operator|.
name|MetricsFactory
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
name|common
operator|.
name|metrics
operator|.
name|common
operator|.
name|MetricsScope
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
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|TestLegacyMetrics
block|{
specifier|private
specifier|static
specifier|final
name|String
name|scopeName
init|=
literal|"foo"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|periodMs
init|=
literal|50L
decl_stmt|;
specifier|private
specifier|static
name|LegacyMetrics
name|metrics
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|MetricsFactory
operator|.
name|close
argument_list|()
expr_stmt|;
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METRICS_CLASS
argument_list|,
name|LegacyMetrics
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
name|MetricsFactory
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|metrics
operator|=
operator|(
name|LegacyMetrics
operator|)
name|MetricsFactory
operator|.
name|getInstance
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|after
parameter_list|()
throws|throws
name|Exception
block|{
name|MetricsFactory
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMetricsMBean
parameter_list|()
throws|throws
name|Exception
block|{
name|MBeanServer
name|mbs
init|=
name|ManagementFactory
operator|.
name|getPlatformMBeanServer
argument_list|()
decl_stmt|;
specifier|final
name|ObjectName
name|oname
init|=
operator|new
name|ObjectName
argument_list|(
literal|"org.apache.hadoop.hive.common.metrics:type=MetricsMBean"
argument_list|)
decl_stmt|;
name|MBeanInfo
name|mBeanInfo
init|=
name|mbs
operator|.
name|getMBeanInfo
argument_list|(
name|oname
argument_list|)
decl_stmt|;
comment|// check implementation class:
name|assertEquals
argument_list|(
name|MetricsMBeanImpl
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|mBeanInfo
operator|.
name|getClassName
argument_list|()
argument_list|)
expr_stmt|;
comment|// check reset operation:
name|MBeanOperationInfo
index|[]
name|oops
init|=
name|mBeanInfo
operator|.
name|getOperations
argument_list|()
decl_stmt|;
name|boolean
name|resetFound
init|=
literal|false
decl_stmt|;
for|for
control|(
name|MBeanOperationInfo
name|op
range|:
name|oops
control|)
block|{
if|if
condition|(
literal|"reset"
operator|.
name|equals
argument_list|(
name|op
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|resetFound
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
name|resetFound
argument_list|)
expr_stmt|;
comment|// add metric with a non-null value:
name|Attribute
name|attr
init|=
operator|new
name|Attribute
argument_list|(
literal|"fooMetric"
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
operator|-
literal|77
argument_list|)
argument_list|)
decl_stmt|;
name|mbs
operator|.
name|setAttribute
argument_list|(
name|oname
argument_list|,
name|attr
argument_list|)
expr_stmt|;
name|mBeanInfo
operator|=
name|mbs
operator|.
name|getMBeanInfo
argument_list|(
name|oname
argument_list|)
expr_stmt|;
name|MBeanAttributeInfo
index|[]
name|attrinuteInfos
init|=
name|mBeanInfo
operator|.
name|getAttributes
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|attrinuteInfos
operator|.
name|length
argument_list|)
expr_stmt|;
name|boolean
name|attrFound
init|=
literal|false
decl_stmt|;
for|for
control|(
name|MBeanAttributeInfo
name|info
range|:
name|attrinuteInfos
control|)
block|{
if|if
condition|(
literal|"fooMetric"
operator|.
name|equals
argument_list|(
name|info
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|assertEquals
argument_list|(
literal|"java.lang.Long"
argument_list|,
name|info
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|info
operator|.
name|isReadable
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|info
operator|.
name|isWritable
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|info
operator|.
name|isIs
argument_list|()
argument_list|)
expr_stmt|;
name|attrFound
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
name|attrFound
argument_list|)
expr_stmt|;
comment|// check metric value:
name|Object
name|v
init|=
name|mbs
operator|.
name|getAttribute
argument_list|(
name|oname
argument_list|,
literal|"fooMetric"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
operator|-
literal|77
argument_list|)
argument_list|,
name|v
argument_list|)
expr_stmt|;
comment|// reset the bean:
name|Object
name|result
init|=
name|mbs
operator|.
name|invoke
argument_list|(
name|oname
argument_list|,
literal|"reset"
argument_list|,
operator|new
name|Object
index|[
literal|0
index|]
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|result
argument_list|)
expr_stmt|;
comment|// the metric value must be zeroed:
name|v
operator|=
name|mbs
operator|.
name|getAttribute
argument_list|(
name|oname
argument_list|,
literal|"fooMetric"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
literal|0
argument_list|)
argument_list|,
name|v
argument_list|)
expr_stmt|;
block|}
specifier|private
parameter_list|<
name|T
parameter_list|>
name|void
name|expectIOE
parameter_list|(
name|Callable
argument_list|<
name|T
argument_list|>
name|c
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|T
name|t
init|=
name|c
operator|.
name|call
argument_list|()
decl_stmt|;
name|fail
argument_list|(
literal|"IOE expected but ["
operator|+
name|t
operator|+
literal|"] was returned."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
comment|// ok, expected
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScopeSingleThread
parameter_list|()
throws|throws
name|Exception
block|{
name|metrics
operator|.
name|startStoredScope
argument_list|(
name|scopeName
argument_list|)
expr_stmt|;
specifier|final
name|LegacyMetrics
operator|.
name|LegacyMetricsScope
name|fooScope
init|=
operator|(
name|LegacyMetrics
operator|.
name|LegacyMetricsScope
operator|)
name|metrics
operator|.
name|getStoredScope
argument_list|(
name|scopeName
argument_list|)
decl_stmt|;
comment|// the time and number counters become available only after the 1st
comment|// scope close:
name|expectIOE
argument_list|(
operator|new
name|Callable
argument_list|<
name|Long
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Long
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|Long
name|num
init|=
name|fooScope
operator|.
name|getNumCounter
argument_list|()
decl_stmt|;
return|return
name|num
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|expectIOE
argument_list|(
operator|new
name|Callable
argument_list|<
name|Long
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Long
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|Long
name|time
init|=
name|fooScope
operator|.
name|getTimeCounter
argument_list|()
decl_stmt|;
return|return
name|time
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// cannot open scope that is already open:
name|expectIOE
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|fooScope
operator|.
name|open
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|fooScope
argument_list|,
name|metrics
operator|.
name|getStoredScope
argument_list|(
name|scopeName
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|periodMs
operator|+
literal|1
argument_list|)
expr_stmt|;
comment|// 1st close:
comment|// closing of open scope should be ok:
name|metrics
operator|.
name|endStoredScope
argument_list|(
name|scopeName
argument_list|)
expr_stmt|;
name|expectIOE
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|metrics
operator|.
name|endStoredScope
argument_list|(
name|scopeName
argument_list|)
expr_stmt|;
comment|// closing of closed scope not allowed
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
literal|1
argument_list|)
argument_list|,
name|fooScope
operator|.
name|getNumCounter
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|long
name|t1
init|=
name|fooScope
operator|.
name|getTimeCounter
argument_list|()
operator|.
name|longValue
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|t1
operator|>
name|periodMs
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|fooScope
argument_list|,
name|metrics
operator|.
name|getStoredScope
argument_list|(
name|scopeName
argument_list|)
argument_list|)
expr_stmt|;
comment|// opening allowed after closing:
name|metrics
operator|.
name|startStoredScope
argument_list|(
name|scopeName
argument_list|)
expr_stmt|;
comment|// opening of already open scope not allowed:
name|expectIOE
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|metrics
operator|.
name|startStoredScope
argument_list|(
name|scopeName
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
literal|1
argument_list|)
argument_list|,
name|fooScope
operator|.
name|getNumCounter
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t1
argument_list|,
name|fooScope
operator|.
name|getTimeCounter
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|fooScope
argument_list|,
name|metrics
operator|.
name|getStoredScope
argument_list|(
name|scopeName
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|periodMs
operator|+
literal|1
argument_list|)
expr_stmt|;
comment|// Reopening (close + open) allowed in opened state:
name|fooScope
operator|.
name|reopen
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
literal|2
argument_list|)
argument_list|,
name|fooScope
operator|.
name|getNumCounter
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fooScope
operator|.
name|getTimeCounter
argument_list|()
operator|.
name|longValue
argument_list|()
operator|>
literal|2
operator|*
name|periodMs
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|periodMs
operator|+
literal|1
argument_list|)
expr_stmt|;
comment|// 3rd close:
name|fooScope
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
literal|3
argument_list|)
argument_list|,
name|fooScope
operator|.
name|getNumCounter
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fooScope
operator|.
name|getTimeCounter
argument_list|()
operator|.
name|longValue
argument_list|()
operator|>
literal|3
operator|*
name|periodMs
argument_list|)
expr_stmt|;
name|Double
name|avgT
init|=
operator|(
name|Double
operator|)
name|metrics
operator|.
name|get
argument_list|(
literal|"foo.avg_t"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|avgT
operator|.
name|doubleValue
argument_list|()
operator|>
name|periodMs
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScopeConcurrency
parameter_list|()
throws|throws
name|Exception
block|{
name|metrics
operator|.
name|startStoredScope
argument_list|(
name|scopeName
argument_list|)
expr_stmt|;
name|LegacyMetrics
operator|.
name|LegacyMetricsScope
name|fooScope
init|=
operator|(
name|LegacyMetrics
operator|.
name|LegacyMetricsScope
operator|)
name|metrics
operator|.
name|getStoredScope
argument_list|(
name|scopeName
argument_list|)
decl_stmt|;
specifier|final
name|int
name|threads
init|=
literal|10
decl_stmt|;
name|ExecutorService
name|executorService
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|threads
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|threads
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|n
init|=
name|i
decl_stmt|;
name|executorService
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|testScopeImpl
argument_list|(
name|n
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|executorService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|executorService
operator|.
name|awaitTermination
argument_list|(
name|periodMs
operator|*
literal|3
operator|*
name|threads
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
expr_stmt|;
name|fooScope
operator|=
operator|(
name|LegacyMetrics
operator|.
name|LegacyMetricsScope
operator|)
name|metrics
operator|.
name|getStoredScope
argument_list|(
name|scopeName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
literal|3
operator|*
name|threads
argument_list|)
argument_list|,
name|fooScope
operator|.
name|getNumCounter
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fooScope
operator|.
name|getTimeCounter
argument_list|()
operator|.
name|longValue
argument_list|()
operator|>
literal|3
operator|*
name|periodMs
operator|*
name|threads
argument_list|)
expr_stmt|;
name|Double
name|avgT
init|=
operator|(
name|Double
operator|)
name|metrics
operator|.
name|get
argument_list|(
literal|"foo.avg_t"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|avgT
operator|.
name|doubleValue
argument_list|()
operator|>
name|periodMs
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|endStoredScope
argument_list|(
name|scopeName
argument_list|)
expr_stmt|;
block|}
name|void
name|testScopeImpl
parameter_list|(
name|int
name|n
parameter_list|)
throws|throws
name|Exception
block|{
name|metrics
operator|.
name|startStoredScope
argument_list|(
name|scopeName
argument_list|)
expr_stmt|;
specifier|final
name|LegacyMetrics
operator|.
name|LegacyMetricsScope
name|fooScope
init|=
operator|(
name|LegacyMetrics
operator|.
name|LegacyMetricsScope
operator|)
name|metrics
operator|.
name|getStoredScope
argument_list|(
name|scopeName
argument_list|)
decl_stmt|;
comment|// cannot open scope that is already open:
name|expectIOE
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|fooScope
operator|.
name|open
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|fooScope
argument_list|,
name|metrics
operator|.
name|getStoredScope
argument_list|(
name|scopeName
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|periodMs
operator|+
literal|1
argument_list|)
expr_stmt|;
comment|// 1st close:
name|metrics
operator|.
name|endStoredScope
argument_list|(
name|scopeName
argument_list|)
expr_stmt|;
comment|// closing of open scope should be ok.
name|assertTrue
argument_list|(
name|fooScope
operator|.
name|getNumCounter
argument_list|()
operator|.
name|longValue
argument_list|()
operator|>=
literal|1
argument_list|)
expr_stmt|;
specifier|final
name|long
name|t1
init|=
name|fooScope
operator|.
name|getTimeCounter
argument_list|()
operator|.
name|longValue
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|t1
operator|>
name|periodMs
argument_list|)
expr_stmt|;
name|expectIOE
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|metrics
operator|.
name|endStoredScope
argument_list|(
name|scopeName
argument_list|)
expr_stmt|;
comment|// closing of closed scope not allowed
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|fooScope
argument_list|,
name|metrics
operator|.
name|getStoredScope
argument_list|(
name|scopeName
argument_list|)
argument_list|)
expr_stmt|;
comment|// opening allowed after closing:
name|metrics
operator|.
name|startStoredScope
argument_list|(
name|scopeName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fooScope
operator|.
name|getNumCounter
argument_list|()
operator|.
name|longValue
argument_list|()
operator|>=
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fooScope
operator|.
name|getTimeCounter
argument_list|()
operator|.
name|longValue
argument_list|()
operator|>=
name|t1
argument_list|)
expr_stmt|;
comment|// opening of already open scope not allowed:
name|expectIOE
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|metrics
operator|.
name|startStoredScope
argument_list|(
name|scopeName
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|fooScope
argument_list|,
name|metrics
operator|.
name|getStoredScope
argument_list|(
name|scopeName
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|periodMs
operator|+
literal|1
argument_list|)
expr_stmt|;
comment|// Reopening (close + open) allowed in opened state:
name|fooScope
operator|.
name|reopen
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|fooScope
operator|.
name|getNumCounter
argument_list|()
operator|.
name|longValue
argument_list|()
operator|>=
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fooScope
operator|.
name|getTimeCounter
argument_list|()
operator|.
name|longValue
argument_list|()
operator|>
literal|2
operator|*
name|periodMs
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|periodMs
operator|+
literal|1
argument_list|)
expr_stmt|;
comment|// 3rd close:
name|fooScope
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|fooScope
operator|.
name|getNumCounter
argument_list|()
operator|.
name|longValue
argument_list|()
operator|>=
literal|3
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fooScope
operator|.
name|getTimeCounter
argument_list|()
operator|.
name|longValue
argument_list|()
operator|>
literal|3
operator|*
name|periodMs
argument_list|)
expr_stmt|;
name|Double
name|avgT
init|=
operator|(
name|Double
operator|)
name|metrics
operator|.
name|get
argument_list|(
literal|"foo.avg_t"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|avgT
operator|.
name|doubleValue
argument_list|()
operator|>
name|periodMs
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

