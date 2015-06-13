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
name|Metrics
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
name|HashMap
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
name|MalformedObjectNameException
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

begin_comment
comment|/**  * This class may eventually get superseded by org.apache.hadoop.hive.common.metrics2.Metrics.  *  * Metrics Subsystem  - allows exposure of a number of named parameters/counters  *                      via jmx, intended to be used as a static subsystem  *  *                      Has a couple of primary ways it can be used:  *                      (i) Using the set and get methods to set and get named parameters  *                      (ii) Using the incrementCounter method to increment and set named  *                      parameters in one go, rather than having to make a get and then a set.  *                      (iii) Using the startScope and endScope methods to start and end  *                      named "scopes" that record the number of times they've been  *                      instantiated and amount of time(in milliseconds) spent inside  *                      the scopes.  */
end_comment

begin_class
specifier|public
class|class
name|LegacyMetrics
implements|implements
name|Metrics
block|{
specifier|private
name|LegacyMetrics
parameter_list|()
block|{
comment|// block
block|}
comment|/**    * MetricsScope : A class that encapsulates an idea of a metered scope.    * Instantiating a named scope and then closing it exposes two counters:    *   (i) a "number of calls" counter (&lt;name&gt;.n ), and    *  (ii) a "number of msecs spent between scope open and close" counter. (&lt;name&gt;.t)    */
specifier|public
specifier|static
class|class
name|MetricsScope
block|{
specifier|final
name|LegacyMetrics
name|metrics
decl_stmt|;
specifier|final
name|String
name|name
decl_stmt|;
specifier|final
name|String
name|numCounter
decl_stmt|;
specifier|final
name|String
name|timeCounter
decl_stmt|;
specifier|final
name|String
name|avgTimeCounter
decl_stmt|;
specifier|private
name|boolean
name|isOpen
init|=
literal|false
decl_stmt|;
specifier|private
name|Long
name|startTime
init|=
literal|null
decl_stmt|;
comment|/**      * Instantiates a named scope - intended to only be called by Metrics, so locally scoped.      * @param name - name of the variable      * @throws IOException      */
specifier|private
name|MetricsScope
parameter_list|(
name|String
name|name
parameter_list|,
name|LegacyMetrics
name|metrics
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|numCounter
operator|=
name|name
operator|+
literal|".n"
expr_stmt|;
name|this
operator|.
name|timeCounter
operator|=
name|name
operator|+
literal|".t"
expr_stmt|;
name|this
operator|.
name|avgTimeCounter
operator|=
name|name
operator|+
literal|".avg_t"
expr_stmt|;
name|open
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Long
name|getNumCounter
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|(
name|Long
operator|)
name|metrics
operator|.
name|get
argument_list|(
name|numCounter
argument_list|)
return|;
block|}
specifier|public
name|Long
name|getTimeCounter
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|(
name|Long
operator|)
name|metrics
operator|.
name|get
argument_list|(
name|timeCounter
argument_list|)
return|;
block|}
comment|/**      * Opens scope, and makes note of the time started, increments run counter      * @throws IOException      *      */
specifier|public
name|void
name|open
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|isOpen
condition|)
block|{
name|isOpen
operator|=
literal|true
expr_stmt|;
name|startTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Scope named "
operator|+
name|name
operator|+
literal|" is not closed, cannot be opened."
argument_list|)
throw|;
block|}
block|}
comment|/**      * Closes scope, and records the time taken      * @throws IOException      */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|isOpen
condition|)
block|{
name|Long
name|endTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
synchronized|synchronized
init|(
name|metrics
init|)
block|{
name|Long
name|num
init|=
name|metrics
operator|.
name|incrementCounter
argument_list|(
name|numCounter
argument_list|)
decl_stmt|;
name|Long
name|time
init|=
name|metrics
operator|.
name|incrementCounter
argument_list|(
name|timeCounter
argument_list|,
name|endTime
operator|-
name|startTime
argument_list|)
decl_stmt|;
if|if
condition|(
name|num
operator|!=
literal|null
operator|&&
name|time
operator|!=
literal|null
condition|)
block|{
name|metrics
operator|.
name|set
argument_list|(
name|avgTimeCounter
argument_list|,
name|Double
operator|.
name|valueOf
argument_list|(
name|time
operator|.
name|doubleValue
argument_list|()
operator|/
name|num
operator|.
name|doubleValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Scope named "
operator|+
name|name
operator|+
literal|" is not open, cannot be closed."
argument_list|)
throw|;
block|}
name|isOpen
operator|=
literal|false
expr_stmt|;
block|}
comment|/**      * Closes scope if open, and reopens it      * @throws IOException      */
specifier|public
name|void
name|reopen
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|isOpen
condition|)
block|{
name|close
argument_list|()
expr_stmt|;
block|}
name|open
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|MetricsMBean
name|metrics
init|=
operator|new
name|MetricsMBeanImpl
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ObjectName
name|oname
decl_stmt|;
static|static
block|{
try|try
block|{
name|oname
operator|=
operator|new
name|ObjectName
argument_list|(
literal|"org.apache.hadoop.hive.common.metrics:type=MetricsMBean"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MalformedObjectNameException
name|mone
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|mone
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|HashMap
argument_list|<
name|String
argument_list|,
name|MetricsScope
argument_list|>
argument_list|>
name|threadLocalScopes
init|=
operator|new
name|ThreadLocal
argument_list|<
name|HashMap
argument_list|<
name|String
argument_list|,
name|MetricsScope
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|HashMap
argument_list|<
name|String
argument_list|,
name|MetricsScope
argument_list|>
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|MetricsScope
argument_list|>
argument_list|()
return|;
block|}
block|}
decl_stmt|;
specifier|public
name|LegacyMetrics
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
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
name|mbs
operator|.
name|registerMBean
argument_list|(
name|metrics
argument_list|,
name|oname
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Long
name|incrementCounter
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|incrementCounter
argument_list|(
name|name
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
literal|1
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|Long
name|incrementCounter
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|increment
parameter_list|)
throws|throws
name|IOException
block|{
name|Long
name|value
decl_stmt|;
synchronized|synchronized
init|(
name|metrics
init|)
block|{
if|if
condition|(
operator|!
name|metrics
operator|.
name|hasKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|value
operator|=
name|Long
operator|.
name|valueOf
argument_list|(
name|increment
argument_list|)
expr_stmt|;
name|set
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|value
operator|=
operator|(
operator|(
name|Long
operator|)
name|get
argument_list|(
name|name
argument_list|)
operator|)
operator|+
name|increment
expr_stmt|;
name|set
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|value
return|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|metrics
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Object
name|get
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|metrics
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|public
name|void
name|startScope
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|threadLocalScopes
operator|.
name|get
argument_list|()
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|threadLocalScopes
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|(
name|name
argument_list|)
operator|.
name|open
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|threadLocalScopes
operator|.
name|get
argument_list|()
operator|.
name|put
argument_list|(
name|name
argument_list|,
operator|new
name|MetricsScope
argument_list|(
name|name
argument_list|,
name|this
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|MetricsScope
name|getScope
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|threadLocalScopes
operator|.
name|get
argument_list|()
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|threadLocalScopes
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No metrics scope named "
operator|+
name|name
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|endScope
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|threadLocalScopes
operator|.
name|get
argument_list|()
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|threadLocalScopes
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|(
name|name
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Resets the static context state to initial.    * Used primarily for testing purposes.    *    * Note that threadLocalScopes ThreadLocal is *not* cleared in this call.    */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|Exception
block|{
synchronized|synchronized
init|(
name|metrics
init|)
block|{
name|MBeanServer
name|mbs
init|=
name|ManagementFactory
operator|.
name|getPlatformMBeanServer
argument_list|()
decl_stmt|;
if|if
condition|(
name|mbs
operator|.
name|isRegistered
argument_list|(
name|oname
argument_list|)
condition|)
block|{
name|mbs
operator|.
name|unregisterMBean
argument_list|(
name|oname
argument_list|)
expr_stmt|;
block|}
name|metrics
operator|.
name|clear
argument_list|()
expr_stmt|;
name|threadLocalScopes
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

