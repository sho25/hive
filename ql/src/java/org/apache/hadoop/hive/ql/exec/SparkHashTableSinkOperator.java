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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|Future
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
name|io
operator|.
name|FileExistsException
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
name|FileStatus
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
name|ql
operator|.
name|exec
operator|.
name|persistence
operator|.
name|MapJoinPersistableTableContainer
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
name|persistence
operator|.
name|MapJoinTableContainerSerDe
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
name|plan
operator|.
name|BucketMapJoinContext
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
name|plan
operator|.
name|MapredLocalWork
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
name|plan
operator|.
name|SparkBucketMapJoinContext
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
name|plan
operator|.
name|SparkHashTableSinkDesc
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
name|plan
operator|.
name|api
operator|.
name|OperatorType
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
import|;
end_import

begin_class
specifier|public
class|class
name|SparkHashTableSinkOperator
extends|extends
name|TerminalOperator
argument_list|<
name|SparkHashTableSinkDesc
argument_list|>
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|int
name|MIN_REPLICATION
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|final
name|String
name|CLASS_NAME
init|=
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|PerfLogger
name|perfLogger
init|=
name|SessionState
operator|.
name|getPerfLogger
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SparkHashTableSinkOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HashTableSinkOperator
name|htsOperator
decl_stmt|;
specifier|public
name|SparkHashTableSinkOperator
parameter_list|()
block|{
name|htsOperator
operator|=
operator|new
name|HashTableSinkOperator
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Collection
argument_list|<
name|Future
argument_list|<
name|?
argument_list|>
argument_list|>
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|Collection
argument_list|<
name|Future
argument_list|<
name|?
argument_list|>
argument_list|>
name|result
init|=
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|inputOIs
init|=
operator|new
name|ObjectInspector
index|[
name|conf
operator|.
name|getTagLength
argument_list|()
index|]
decl_stmt|;
name|byte
name|tag
init|=
name|conf
operator|.
name|getTag
argument_list|()
decl_stmt|;
name|inputOIs
index|[
name|tag
index|]
operator|=
name|inputObjInspectors
index|[
literal|0
index|]
expr_stmt|;
name|conf
operator|.
name|setTagOrder
argument_list|(
operator|new
name|Byte
index|[]
block|{
name|tag
block|}
argument_list|)
expr_stmt|;
name|htsOperator
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|htsOperator
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|,
name|inputOIs
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Ignore the tag passed in, which should be 0, not what we want
name|htsOperator
operator|.
name|process
argument_list|(
name|row
argument_list|,
name|conf
operator|.
name|getTag
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|closeOp
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|MapJoinPersistableTableContainer
index|[]
name|mapJoinTables
init|=
name|htsOperator
operator|.
name|mapJoinTables
decl_stmt|;
name|byte
name|tag
init|=
name|conf
operator|.
name|getTag
argument_list|()
decl_stmt|;
if|if
condition|(
name|mapJoinTables
operator|==
literal|null
operator|||
name|mapJoinTables
operator|.
name|length
operator|<
name|tag
operator|||
name|mapJoinTables
index|[
name|tag
index|]
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"mapJoinTable is null"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|abort
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Aborting, skip dumping side-table for tag: "
operator|+
name|tag
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|String
name|method
init|=
name|PerfLogger
operator|.
name|SPARK_FLUSH_HASHTABLE
operator|+
name|getName
argument_list|()
decl_stmt|;
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|method
argument_list|)
expr_stmt|;
try|try
block|{
name|flushToFile
argument_list|(
name|mapJoinTables
index|[
name|tag
index|]
argument_list|,
name|tag
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|method
argument_list|)
expr_stmt|;
block|}
block|}
name|super
operator|.
name|closeOp
argument_list|(
name|abort
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|void
name|flushToFile
parameter_list|(
name|MapJoinPersistableTableContainer
name|tableContainer
parameter_list|,
name|byte
name|tag
parameter_list|)
throws|throws
name|Exception
block|{
name|MapredLocalWork
name|localWork
init|=
name|getExecContext
argument_list|()
operator|.
name|getLocalWork
argument_list|()
decl_stmt|;
name|BucketMapJoinContext
name|mapJoinCtx
init|=
name|localWork
operator|.
name|getBucketMapjoinContext
argument_list|()
decl_stmt|;
name|Path
name|inputPath
init|=
name|getExecContext
argument_list|()
operator|.
name|getCurrentInputPath
argument_list|()
decl_stmt|;
name|String
name|bigInputPath
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|inputPath
operator|!=
literal|null
operator|&&
name|mapJoinCtx
operator|!=
literal|null
condition|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|aliases
init|=
operator|(
operator|(
name|SparkBucketMapJoinContext
operator|)
name|mapJoinCtx
operator|)
operator|.
name|getPosToAliasMap
argument_list|()
operator|.
name|get
argument_list|(
operator|(
name|int
operator|)
name|tag
argument_list|)
decl_stmt|;
name|bigInputPath
operator|=
name|mapJoinCtx
operator|.
name|getMappingBigFile
argument_list|(
name|aliases
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|,
name|inputPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// get tmp file URI
name|Path
name|tmpURI
init|=
name|localWork
operator|.
name|getTmpHDFSPath
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Temp URI for side table: "
operator|+
name|tmpURI
argument_list|)
expr_stmt|;
comment|// get current bucket file name
name|String
name|fileName
init|=
name|localWork
operator|.
name|getBucketFileName
argument_list|(
name|bigInputPath
argument_list|)
decl_stmt|;
comment|// get the tmp URI path; it will be a hdfs path if not local mode
name|String
name|dumpFilePrefix
init|=
name|conf
operator|.
name|getDumpFilePrefix
argument_list|()
decl_stmt|;
name|Path
name|path
init|=
name|Utilities
operator|.
name|generatePath
argument_list|(
name|tmpURI
argument_list|,
name|dumpFilePrefix
argument_list|,
name|tag
argument_list|,
name|fileName
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|htsOperator
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|short
name|replication
init|=
name|fs
operator|.
name|getDefaultReplication
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|path
argument_list|)
expr_stmt|;
comment|// Create the folder and its parents if not there
while|while
condition|(
literal|true
condition|)
block|{
name|path
operator|=
operator|new
name|Path
argument_list|(
name|path
argument_list|,
name|getOperatorId
argument_list|()
operator|+
literal|"-"
operator|+
name|Math
operator|.
name|abs
argument_list|(
name|Utilities
operator|.
name|randGen
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
comment|// This will guarantee file name uniqueness.
if|if
condition|(
name|fs
operator|.
name|createNewFile
argument_list|(
name|path
argument_list|)
condition|)
block|{
break|break;
block|}
block|}
catch|catch
parameter_list|(
name|FileExistsException
name|e
parameter_list|)
block|{
comment|// No problem, use a new name
block|}
block|}
comment|// TODO find out numOfPartitions for the big table
name|int
name|numOfPartitions
init|=
name|replication
decl_stmt|;
name|replication
operator|=
operator|(
name|short
operator|)
name|Math
operator|.
name|max
argument_list|(
name|MIN_REPLICATION
argument_list|,
name|numOfPartitions
argument_list|)
expr_stmt|;
name|htsOperator
operator|.
name|console
operator|.
name|printInfo
argument_list|(
name|Utilities
operator|.
name|now
argument_list|()
operator|+
literal|"\tDump the side-table for tag: "
operator|+
name|tag
operator|+
literal|" with group count: "
operator|+
name|tableContainer
operator|.
name|size
argument_list|()
operator|+
literal|" into file: "
operator|+
name|path
argument_list|)
expr_stmt|;
comment|// get the hashtable file and path
name|OutputStream
name|os
init|=
literal|null
decl_stmt|;
name|ObjectOutputStream
name|out
init|=
literal|null
decl_stmt|;
try|try
block|{
name|os
operator|=
name|fs
operator|.
name|create
argument_list|(
name|path
argument_list|,
name|replication
argument_list|)
expr_stmt|;
name|out
operator|=
operator|new
name|ObjectOutputStream
argument_list|(
operator|new
name|BufferedOutputStream
argument_list|(
name|os
argument_list|,
literal|4096
argument_list|)
argument_list|)
expr_stmt|;
name|MapJoinTableContainerSerDe
name|mapJoinTableSerde
init|=
name|htsOperator
operator|.
name|mapJoinTableSerdes
index|[
name|tag
index|]
decl_stmt|;
name|mapJoinTableSerde
operator|.
name|persist
argument_list|(
name|out
argument_list|,
name|tableContainer
argument_list|)
expr_stmt|;
name|FileStatus
name|status
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|htsOperator
operator|.
name|console
operator|.
name|printInfo
argument_list|(
name|Utilities
operator|.
name|now
argument_list|()
operator|+
literal|"\tUploaded 1 File to: "
operator|+
name|path
operator|+
literal|" ("
operator|+
name|status
operator|.
name|getLen
argument_list|()
operator|+
literal|" bytes)"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Failed to dump the side-table, remove the partial file
try|try
block|{
name|fs
operator|.
name|delete
argument_list|(
name|path
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Got exception in deleting partial side-table dump for tag: "
operator|+
name|tag
operator|+
literal|", file "
operator|+
name|path
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
throw|throw
name|e
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|out
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|os
operator|!=
literal|null
condition|)
block|{
name|os
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
name|tableContainer
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|/**    * Implements the getName function for the Node Interface.    *    * @return the name of the operator    */
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|HashTableSinkOperator
operator|.
name|getOperatorName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperatorType
name|getType
parameter_list|()
block|{
return|return
name|OperatorType
operator|.
name|HASHTABLESINK
return|;
block|}
block|}
end_class

end_unit

