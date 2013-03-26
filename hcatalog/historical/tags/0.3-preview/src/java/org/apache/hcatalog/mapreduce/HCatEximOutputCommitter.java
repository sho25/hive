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
name|hcatalog
operator|.
name|mapreduce
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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|java
operator|.
name|util
operator|.
name|Map
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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|Warehouse
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|metastore
operator|.
name|api
operator|.
name|Partition
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
name|metastore
operator|.
name|api
operator|.
name|StorageDescriptor
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
name|metastore
operator|.
name|api
operator|.
name|Table
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
name|parse
operator|.
name|EximUtil
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
name|parse
operator|.
name|SemanticException
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
name|mapreduce
operator|.
name|JobContext
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
name|mapreduce
operator|.
name|JobStatus
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
name|mapreduce
operator|.
name|OutputCommitter
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
name|mapreduce
operator|.
name|TaskAttemptContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|ErrorType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_class
specifier|public
class|class
name|HCatEximOutputCommitter
extends|extends
name|OutputCommitter
block|{
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
name|HCatEximOutputCommitter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|OutputCommitter
name|baseCommitter
decl_stmt|;
specifier|public
name|HCatEximOutputCommitter
parameter_list|(
name|JobContext
name|context
parameter_list|,
name|OutputCommitter
name|baseCommitter
parameter_list|)
block|{
name|this
operator|.
name|baseCommitter
operator|=
name|baseCommitter
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abortTask
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|baseCommitter
operator|.
name|abortTask
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitTask
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|baseCommitter
operator|.
name|commitTask
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|needsTaskCommit
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|baseCommitter
operator|.
name|needsTaskCommit
argument_list|(
name|context
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setupJob
parameter_list|(
name|JobContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|baseCommitter
operator|!=
literal|null
condition|)
block|{
name|baseCommitter
operator|.
name|setupJob
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setupTask
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|baseCommitter
operator|.
name|setupTask
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abortJob
parameter_list|(
name|JobContext
name|jobContext
parameter_list|,
name|JobStatus
operator|.
name|State
name|state
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|baseCommitter
operator|!=
literal|null
condition|)
block|{
name|baseCommitter
operator|.
name|abortJob
argument_list|(
name|jobContext
argument_list|,
name|state
argument_list|)
expr_stmt|;
block|}
name|OutputJobInfo
name|jobInfo
init|=
name|HCatOutputFormat
operator|.
name|getJobInfo
argument_list|(
name|jobContext
argument_list|)
decl_stmt|;
name|Path
name|src
init|=
operator|new
name|Path
argument_list|(
name|jobInfo
operator|.
name|getLocation
argument_list|()
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|src
operator|.
name|getFileSystem
argument_list|(
name|jobContext
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|src
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitJob
parameter_list|(
name|JobContext
name|jobContext
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|baseCommitter
operator|!=
literal|null
condition|)
block|{
name|baseCommitter
operator|.
name|commitJob
argument_list|(
name|jobContext
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|cleanupJob
parameter_list|(
name|JobContext
name|jobContext
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"HCatEximOutputCommitter.cleanup invoked; m.o.d : "
operator|+
name|jobContext
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
literal|"mapred.output.dir"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|baseCommitter
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"baseCommitter.class = "
operator|+
name|baseCommitter
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|baseCommitter
operator|.
name|cleanupJob
argument_list|(
name|jobContext
argument_list|)
expr_stmt|;
block|}
name|OutputJobInfo
name|jobInfo
init|=
name|HCatBaseOutputFormat
operator|.
name|getJobInfo
argument_list|(
name|jobContext
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|jobContext
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
decl_stmt|;
try|try
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
operator|new
name|URI
argument_list|(
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getTable
argument_list|()
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|doCleanup
argument_list|(
name|jobInfo
argument_list|,
name|fs
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|doCleanup
parameter_list|(
name|OutputJobInfo
name|jobInfo
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
throws|,
name|HCatException
block|{
try|try
block|{
name|Table
name|ttable
init|=
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getTable
argument_list|()
decl_stmt|;
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
name|Table
name|table
init|=
operator|new
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
name|Table
argument_list|(
name|ttable
argument_list|)
decl_stmt|;
name|StorageDescriptor
name|tblSD
init|=
name|ttable
operator|.
name|getSd
argument_list|()
decl_stmt|;
name|Path
name|tblPath
init|=
operator|new
name|Path
argument_list|(
name|tblSD
operator|.
name|getLocation
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|tblPath
argument_list|,
literal|"_metadata"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|tpartitions
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Table
argument_list|,
name|List
argument_list|<
name|Partition
argument_list|>
argument_list|>
name|rv
init|=
name|EximUtil
operator|.
name|readMetaData
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
decl_stmt|;
name|tpartitions
operator|=
name|rv
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{       }
name|List
argument_list|<
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
name|Partition
argument_list|>
name|partitions
init|=
operator|new
name|ArrayList
argument_list|<
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
name|Partition
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|tpartitions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Partition
name|tpartition
range|:
name|tpartitions
control|)
block|{
name|partitions
operator|.
name|add
argument_list|(
operator|new
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
name|Partition
argument_list|(
name|table
argument_list|,
name|tpartition
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|table
operator|.
name|getPartitionKeys
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionValues
init|=
name|jobInfo
operator|.
name|getPartitionValues
argument_list|()
decl_stmt|;
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
name|Partition
name|partition
init|=
operator|new
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
name|Partition
argument_list|(
name|table
argument_list|,
name|partitionValues
argument_list|,
operator|new
name|Path
argument_list|(
name|tblPath
argument_list|,
name|Warehouse
operator|.
name|makePartPath
argument_list|(
name|partitionValues
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|partition
operator|.
name|getTPartition
argument_list|()
operator|.
name|setParameters
argument_list|(
name|table
operator|.
name|getParameters
argument_list|()
argument_list|)
expr_stmt|;
name|partitions
operator|.
name|add
argument_list|(
name|partition
argument_list|)
expr_stmt|;
block|}
name|EximUtil
operator|.
name|createExportDump
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
operator|(
name|table
operator|)
argument_list|,
name|partitions
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SemanticException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_PUBLISHING_PARTITION
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_PUBLISHING_PARTITION
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_PUBLISHING_PARTITION
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

