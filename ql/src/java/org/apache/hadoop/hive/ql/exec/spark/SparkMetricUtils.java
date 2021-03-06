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
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileSystem
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
name|spark
operator|.
name|TaskContext
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
import|;
end_import

begin_comment
comment|/**  * Utility class for update Spark-level metrics.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|SparkMetricUtils
block|{
specifier|private
name|SparkMetricUtils
parameter_list|()
block|{
comment|// Do nothing
block|}
specifier|public
specifier|static
name|void
name|updateSparkRecordsWrittenMetrics
parameter_list|(
name|long
name|numRows
parameter_list|)
block|{
name|TaskContext
name|taskContext
init|=
name|TaskContext
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|taskContext
operator|!=
literal|null
operator|&&
name|numRows
operator|>
literal|0
condition|)
block|{
name|taskContext
operator|.
name|taskMetrics
argument_list|()
operator|.
name|outputMetrics
argument_list|()
operator|.
name|setRecordsWritten
argument_list|(
name|numRows
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|updateSparkBytesWrittenMetrics
parameter_list|(
name|Logger
name|log
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
index|[]
name|commitPaths
parameter_list|)
block|{
name|AtomicLong
name|bytesWritten
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
name|Arrays
operator|.
name|stream
argument_list|(
name|commitPaths
argument_list|)
operator|.
name|parallel
argument_list|()
operator|.
name|forEach
argument_list|(
name|path
lambda|->
block|{
try|try
block|{
name|bytesWritten
operator|.
name|addAndGet
argument_list|(
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
operator|.
name|getLen
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|log
operator|.
name|debug
argument_list|(
literal|"Unable to collect stats for file: "
operator|+
name|path
operator|+
literal|" output metrics may be inaccurate"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
if|if
condition|(
name|bytesWritten
operator|.
name|get
argument_list|()
operator|>
literal|0
condition|)
block|{
name|TaskContext
operator|.
name|get
argument_list|()
operator|.
name|taskMetrics
argument_list|()
operator|.
name|outputMetrics
argument_list|()
operator|.
name|setBytesWritten
argument_list|(
name|bytesWritten
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

