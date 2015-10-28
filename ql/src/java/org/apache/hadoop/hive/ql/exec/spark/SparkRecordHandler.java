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
name|spark
package|;
end_package

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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|MapredContext
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
name|log
operator|.
name|PerfLogger
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
name|session
operator|.
name|SessionState
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
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|OutputCollector
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
name|mapred
operator|.
name|Reporter
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
name|net
operator|.
name|URLClassLoader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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

begin_class
specifier|public
specifier|abstract
class|class
name|SparkRecordHandler
block|{
specifier|protected
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|SparkRecordHandler
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|PerfLogger
name|perfLogger
init|=
name|SessionState
operator|.
name|getPerfLogger
argument_list|()
decl_stmt|;
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
name|SparkRecordHandler
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// used to log memory usage periodically
specifier|protected
specifier|final
name|MemoryMXBean
name|memoryMXBean
init|=
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
decl_stmt|;
specifier|protected
name|JobConf
name|jc
decl_stmt|;
specifier|protected
name|OutputCollector
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|oc
decl_stmt|;
specifier|protected
name|Reporter
name|rp
decl_stmt|;
specifier|protected
name|boolean
name|abort
init|=
literal|false
decl_stmt|;
specifier|private
name|long
name|rowNumber
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|nextLogThreshold
init|=
literal|1
decl_stmt|;
specifier|public
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|void
name|init
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|OutputCollector
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|output
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|Exception
block|{
name|jc
operator|=
name|job
expr_stmt|;
name|MapredContext
operator|.
name|init
argument_list|(
literal|false
argument_list|,
operator|new
name|JobConf
argument_list|(
name|jc
argument_list|)
argument_list|)
expr_stmt|;
name|MapredContext
operator|.
name|get
argument_list|()
operator|.
name|setReporter
argument_list|(
name|reporter
argument_list|)
expr_stmt|;
name|oc
operator|=
name|output
expr_stmt|;
name|rp
operator|=
name|reporter
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"maximum memory = "
operator|+
name|memoryMXBean
operator|.
name|getHeapMemoryUsage
argument_list|()
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"conf classpath = "
operator|+
name|Arrays
operator|.
name|asList
argument_list|(
operator|(
operator|(
name|URLClassLoader
operator|)
name|job
operator|.
name|getClassLoader
argument_list|()
operator|)
operator|.
name|getURLs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"thread classpath = "
operator|+
name|Arrays
operator|.
name|asList
argument_list|(
operator|(
operator|(
name|URLClassLoader
operator|)
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
operator|)
operator|.
name|getURLs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"cannot get classpath: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Process row with key and single value.    */
specifier|public
specifier|abstract
name|void
name|processRow
parameter_list|(
name|Object
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Process row with key and value collection.    */
specifier|public
specifier|abstract
parameter_list|<
name|E
parameter_list|>
name|void
name|processRow
parameter_list|(
name|Object
name|key
parameter_list|,
name|Iterator
argument_list|<
name|E
argument_list|>
name|values
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Logger processed row number and used memory info.    */
specifier|protected
name|void
name|logMemoryInfo
parameter_list|()
block|{
name|rowNumber
operator|++
expr_stmt|;
if|if
condition|(
name|rowNumber
operator|==
name|nextLogThreshold
condition|)
block|{
name|long
name|usedMemory
init|=
name|memoryMXBean
operator|.
name|getHeapMemoryUsage
argument_list|()
operator|.
name|getUsed
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"processing "
operator|+
name|rowNumber
operator|+
literal|" rows: used memory = "
operator|+
name|usedMemory
argument_list|)
expr_stmt|;
name|nextLogThreshold
operator|=
name|getNextLogThreshold
argument_list|(
name|rowNumber
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|abstract
name|void
name|close
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|boolean
name|getDone
parameter_list|()
function_decl|;
comment|/**    * Logger information to be logged at the end.    */
specifier|protected
name|void
name|logCloseInfo
parameter_list|()
block|{
name|long
name|usedMemory
init|=
name|memoryMXBean
operator|.
name|getHeapMemoryUsage
argument_list|()
operator|.
name|getUsed
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"processed "
operator|+
name|rowNumber
operator|+
literal|" rows: used memory = "
operator|+
name|usedMemory
argument_list|)
expr_stmt|;
block|}
specifier|private
name|long
name|getNextLogThreshold
parameter_list|(
name|long
name|currentThreshold
parameter_list|)
block|{
comment|// A very simple counter to keep track of number of rows processed by the
comment|// reducer. It dumps
comment|// every 1 million times, and quickly before that
if|if
condition|(
name|currentThreshold
operator|>=
literal|1000000
condition|)
block|{
return|return
name|currentThreshold
operator|+
literal|1000000
return|;
block|}
return|return
literal|10
operator|*
name|currentThreshold
return|;
block|}
specifier|public
name|boolean
name|isAbort
parameter_list|()
block|{
return|return
name|abort
return|;
block|}
specifier|public
name|void
name|setAbort
parameter_list|(
name|boolean
name|abort
parameter_list|)
block|{
name|this
operator|.
name|abort
operator|=
name|abort
expr_stmt|;
block|}
block|}
end_class

end_unit

