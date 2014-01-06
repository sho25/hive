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
name|shims
operator|.
name|ShimLoader
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
specifier|public
name|HashTableLoader
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|load
parameter_list|(
name|ExecMapperContext
name|context
parameter_list|,
name|Configuration
name|hconf
parameter_list|,
name|MapJoinDesc
name|desc
parameter_list|,
name|byte
name|posBigTable
parameter_list|,
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
name|baseDir
init|=
literal|null
decl_stmt|;
name|Path
name|currentInputPath
init|=
name|context
operator|.
name|getCurrentInputPath
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"******* Load from HashTable File: input : "
operator|+
name|currentInputPath
argument_list|)
expr_stmt|;
name|String
name|fileName
init|=
name|context
operator|.
name|getLocalWork
argument_list|()
operator|.
name|getBucketFileName
argument_list|(
name|currentInputPath
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
try|try
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
name|baseDir
operator|=
name|context
operator|.
name|getLocalWork
argument_list|()
operator|.
name|getTmpFileURI
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|Path
index|[]
name|localArchives
decl_stmt|;
name|String
name|stageID
init|=
name|context
operator|.
name|getLocalWork
argument_list|()
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
name|localArchives
operator|=
name|DistributedCache
operator|.
name|getLocalCacheArchives
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|Path
name|archive
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
name|archive
operator|=
name|localArchives
index|[
name|j
index|]
expr_stmt|;
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
name|Path
name|archiveLocalLink
init|=
name|archive
operator|.
name|makeQualified
argument_list|(
name|localFs
argument_list|)
decl_stmt|;
name|baseDir
operator|=
name|archiveLocalLink
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
expr_stmt|;
block|}
block|}
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
name|posBigTable
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|baseDir
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"baseDir cannot be null"
argument_list|)
throw|;
block|}
name|String
name|filePath
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
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|filePath
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
block|}
end_class

end_unit

