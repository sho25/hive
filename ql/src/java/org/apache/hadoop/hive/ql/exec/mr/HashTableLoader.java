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
name|mr
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectInputStream
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
name|Arrays
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
name|filecache
operator|.
name|DistributedCache
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
name|HashTableSinkOperator
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
name|MapJoinOperator
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
name|exec
operator|.
name|Operator
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
name|TemporaryHashSinkOperator
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
name|MapJoinTableContainer
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
name|MapJoinDesc
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
name|OperatorDesc
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
name|shims
operator|.
name|ShimLoader
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

begin_comment
comment|/**  * HashTableLoader for MR loads the hashtable for MapJoins from local disk (hashtables  * are distributed by using the DistributedCache.  *  */
end_comment

begin_class
specifier|public
class|class
name|HashTableLoader
implements|implements
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
name|HashTableLoader
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
name|MapJoinOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|ExecMapperContext
name|context
decl_stmt|;
specifier|private
name|Configuration
name|hconf
decl_stmt|;
specifier|private
name|MapJoinOperator
name|joinOp
decl_stmt|;
specifier|private
name|MapJoinDesc
name|desc
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|ExecMapperContext
name|context
parameter_list|,
name|MapredContext
name|mrContext
parameter_list|,
name|Configuration
name|hconf
parameter_list|,
name|MapJoinOperator
name|joinOp
parameter_list|)
block|{
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|hconf
operator|=
name|hconf
expr_stmt|;
name|this
operator|.
name|joinOp
operator|=
name|joinOp
expr_stmt|;
name|this
operator|.
name|desc
operator|=
name|joinOp
operator|.
name|getConf
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|load
parameter_list|(
name|MapJoinTableContainer
index|[]
name|mapJoinTables
parameter_list|,
name|MapJoinTableContainerSerDe
index|[]
name|mapJoinTableSerdes
parameter_list|)
throws|throws
name|HiveException
block|{
name|String
name|currentInputPath
init|=
name|context
operator|.
name|getCurrentInputPath
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"******* Load from HashTable for input file: "
operator|+
name|currentInputPath
argument_list|)
expr_stmt|;
name|MapredLocalWork
name|localWork
init|=
name|context
operator|.
name|getLocalWork
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
name|localWork
operator|.
name|getDirectFetchOp
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|loadDirectly
argument_list|(
name|mapJoinTables
argument_list|,
name|currentInputPath
argument_list|)
expr_stmt|;
block|}
name|Path
name|baseDir
init|=
name|getBaseDir
argument_list|(
name|localWork
argument_list|)
decl_stmt|;
if|if
condition|(
name|baseDir
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|String
name|fileName
init|=
name|localWork
operator|.
name|getBucketFileName
argument_list|(
name|currentInputPath
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|mapJoinTables
operator|.
name|length
condition|;
name|pos
operator|++
control|)
block|{
if|if
condition|(
name|pos
operator|==
name|desc
operator|.
name|getPosBigTable
argument_list|()
operator|||
name|mapJoinTables
index|[
name|pos
index|]
operator|!=
literal|null
condition|)
block|{
continue|continue;
block|}
name|Path
name|path
init|=
name|Utilities
operator|.
name|generatePath
argument_list|(
name|baseDir
argument_list|,
name|desc
operator|.
name|getDumpFilePrefix
argument_list|()
argument_list|,
operator|(
name|byte
operator|)
name|pos
argument_list|,
name|fileName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"\tLoad back 1 hashtable file from tmp file uri:"
operator|+
name|path
argument_list|)
expr_stmt|;
name|ObjectInputStream
name|in
init|=
operator|new
name|ObjectInputStream
argument_list|(
operator|new
name|BufferedInputStream
argument_list|(
operator|new
name|FileInputStream
argument_list|(
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|,
literal|4096
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|mapJoinTables
index|[
name|pos
index|]
operator|=
name|mapJoinTableSerdes
index|[
name|pos
index|]
operator|.
name|load
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
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
specifier|private
name|Path
name|getBaseDir
parameter_list|(
name|MapredLocalWork
name|localWork
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|isLocalMode
argument_list|(
name|hconf
argument_list|)
condition|)
block|{
return|return
name|localWork
operator|.
name|getTmpPath
argument_list|()
return|;
block|}
name|Path
index|[]
name|localArchives
init|=
name|DistributedCache
operator|.
name|getLocalCacheArchives
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
if|if
condition|(
name|localArchives
operator|!=
literal|null
condition|)
block|{
name|String
name|stageID
init|=
name|localWork
operator|.
name|getStageID
argument_list|()
decl_stmt|;
name|String
name|suffix
init|=
name|Utilities
operator|.
name|generateTarFileName
argument_list|(
name|stageID
argument_list|)
decl_stmt|;
name|FileSystem
name|localFs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|localArchives
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|Path
name|archive
init|=
name|localArchives
index|[
name|j
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|archive
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
name|suffix
argument_list|)
condition|)
block|{
continue|continue;
block|}
return|return
name|archive
operator|.
name|makeQualified
argument_list|(
name|localFs
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|loadDirectly
parameter_list|(
name|MapJoinTableContainer
index|[]
name|mapJoinTables
parameter_list|,
name|String
name|inputFileName
parameter_list|)
throws|throws
name|Exception
block|{
name|MapredLocalWork
name|localWork
init|=
name|context
operator|.
name|getLocalWork
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|directWorks
init|=
name|localWork
operator|.
name|getDirectFetchOp
argument_list|()
operator|.
name|get
argument_list|(
name|joinOp
argument_list|)
decl_stmt|;
if|if
condition|(
name|directWorks
operator|==
literal|null
operator|||
name|directWorks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|JobConf
name|job
init|=
operator|new
name|JobConf
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
name|MapredLocalTask
name|localTask
init|=
operator|new
name|MapredLocalTask
argument_list|(
name|localWork
argument_list|,
name|job
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HashTableSinkOperator
name|sink
init|=
operator|new
name|TemporaryHashSinkOperator
argument_list|(
name|desc
argument_list|)
decl_stmt|;
name|sink
operator|.
name|setParentOperators
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|(
name|directWorks
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|operator
range|:
name|directWorks
control|)
block|{
if|if
condition|(
name|operator
operator|!=
literal|null
condition|)
block|{
name|operator
operator|.
name|setChildOperators
argument_list|(
name|Arrays
operator|.
expr|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|>
name|asList
argument_list|(
name|sink
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|localTask
operator|.
name|setExecContext
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|localTask
operator|.
name|startForward
argument_list|(
name|inputFileName
argument_list|)
expr_stmt|;
name|MapJoinTableContainer
index|[]
name|tables
init|=
name|sink
operator|.
name|getMapJoinTables
argument_list|()
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
name|sink
operator|.
name|getNumParent
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|sink
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|mapJoinTables
index|[
name|i
index|]
operator|=
name|tables
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
name|Arrays
operator|.
name|fill
argument_list|(
name|tables
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

