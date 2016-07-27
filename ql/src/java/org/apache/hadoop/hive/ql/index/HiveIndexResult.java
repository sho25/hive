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
name|index
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
name|HashMap
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
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|FSDataInputStream
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
name|common
operator|.
name|FileUtils
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
name|serde2
operator|.
name|columnar
operator|.
name|BytesRefWritable
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
name|lazy
operator|.
name|LazySerDeParameters
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
name|io
operator|.
name|Text
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
name|FileSplit
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
name|LineRecordReader
operator|.
name|LineReader
import|;
end_import

begin_comment
comment|/**  * HiveIndexResult parses the input stream from an index query  * to generate a list of file splits to query.  */
end_comment

begin_class
specifier|public
class|class
name|HiveIndexResult
implements|implements
name|IndexResult
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|l4j
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HiveIndexResult
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
comment|// IndexBucket
specifier|static
class|class
name|IBucket
block|{
specifier|private
name|String
name|name
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|SortedSet
argument_list|<
name|Long
argument_list|>
name|offsets
init|=
operator|new
name|TreeSet
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|IBucket
parameter_list|(
name|String
name|n
parameter_list|)
block|{
name|name
operator|=
name|n
expr_stmt|;
block|}
specifier|public
name|void
name|add
parameter_list|(
name|Long
name|offset
parameter_list|)
block|{
name|offsets
operator|.
name|add
argument_list|(
name|offset
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
specifier|public
name|SortedSet
argument_list|<
name|Long
argument_list|>
name|getOffsets
parameter_list|()
block|{
return|return
name|offsets
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|.
name|getClass
argument_list|()
operator|!=
name|this
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
operator|(
operator|(
operator|(
name|IBucket
operator|)
name|obj
operator|)
operator|.
name|name
operator|.
name|compareToIgnoreCase
argument_list|(
name|this
operator|.
name|name
argument_list|)
operator|==
literal|0
operator|)
return|;
block|}
block|}
name|JobConf
name|job
init|=
literal|null
decl_stmt|;
name|BytesRefWritable
index|[]
name|bytesRef
init|=
operator|new
name|BytesRefWritable
index|[
literal|2
index|]
decl_stmt|;
name|boolean
name|ignoreHdfsLoc
init|=
literal|false
decl_stmt|;
specifier|public
name|HiveIndexResult
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|indexFiles
parameter_list|,
name|JobConf
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveException
block|{
name|job
operator|=
name|conf
expr_stmt|;
name|bytesRef
index|[
literal|0
index|]
operator|=
operator|new
name|BytesRefWritable
argument_list|()
expr_stmt|;
name|bytesRef
index|[
literal|1
index|]
operator|=
operator|new
name|BytesRefWritable
argument_list|()
expr_stmt|;
name|ignoreHdfsLoc
operator|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_INDEX_IGNORE_HDFS_LOC
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexFiles
operator|!=
literal|null
operator|&&
name|indexFiles
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|indexFile
range|:
name|indexFiles
control|)
block|{
name|Path
name|indexFilePath
init|=
operator|new
name|Path
argument_list|(
name|indexFile
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|indexFilePath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileStatus
name|indexStat
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|indexFilePath
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexStat
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|FileStatus
index|[]
name|fss
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|indexFilePath
argument_list|,
name|FileUtils
operator|.
name|HIDDEN_FILES_PATH_FILTER
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|f
range|:
name|fss
control|)
block|{
name|paths
operator|.
name|add
argument_list|(
name|f
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|paths
operator|.
name|add
argument_list|(
name|indexFilePath
argument_list|)
expr_stmt|;
block|}
block|}
name|long
name|maxEntriesToLoad
init|=
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_INDEX_COMPACT_QUERY_MAX_ENTRIES
argument_list|)
decl_stmt|;
if|if
condition|(
name|maxEntriesToLoad
operator|<
literal|0
condition|)
block|{
name|maxEntriesToLoad
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
name|long
name|lineCounter
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Path
name|indexFinalPath
range|:
name|paths
control|)
block|{
name|FileSystem
name|fs
init|=
name|indexFinalPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FSDataInputStream
name|ifile
init|=
name|fs
operator|.
name|open
argument_list|(
name|indexFinalPath
argument_list|)
decl_stmt|;
name|LineReader
name|lr
init|=
operator|new
name|LineReader
argument_list|(
name|ifile
argument_list|,
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|Text
name|line
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
while|while
condition|(
name|lr
operator|.
name|readLine
argument_list|(
name|line
argument_list|)
operator|>
literal|0
condition|)
block|{
if|if
condition|(
operator|++
name|lineCounter
operator|>
name|maxEntriesToLoad
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Number of compact index entries loaded during the query exceeded the maximum of "
operator|+
name|maxEntriesToLoad
operator|+
literal|" set in "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_INDEX_COMPACT_QUERY_MAX_ENTRIES
operator|.
name|varname
argument_list|)
throw|;
block|}
name|add
argument_list|(
name|line
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
comment|// this will close the input stream
name|lr
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|IBucket
argument_list|>
name|buckets
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|IBucket
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|void
name|add
parameter_list|(
name|Text
name|line
parameter_list|)
throws|throws
name|HiveException
block|{
name|String
name|l
init|=
name|line
operator|.
name|toString
argument_list|()
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|l
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|int
name|firstEnd
init|=
literal|0
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|bytes
operator|.
name|length
condition|;
name|index
operator|++
control|)
block|{
if|if
condition|(
name|bytes
index|[
name|index
index|]
operator|==
name|LazySerDeParameters
operator|.
name|DefaultSeparators
index|[
literal|0
index|]
condition|)
block|{
name|i
operator|++
expr_stmt|;
name|firstEnd
operator|=
name|index
expr_stmt|;
block|}
block|}
if|if
condition|(
name|i
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Bad index file row (index file should only contain two columns: bucket_file_name and offset lists.) ."
operator|+
name|line
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
name|String
name|bucketFileName
init|=
operator|new
name|String
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|firstEnd
argument_list|)
decl_stmt|;
if|if
condition|(
name|ignoreHdfsLoc
condition|)
block|{
name|Path
name|tmpPath
init|=
operator|new
name|Path
argument_list|(
name|bucketFileName
argument_list|)
decl_stmt|;
name|bucketFileName
operator|=
name|tmpPath
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
expr_stmt|;
block|}
name|IBucket
name|bucket
init|=
name|buckets
operator|.
name|get
argument_list|(
name|bucketFileName
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucket
operator|==
literal|null
condition|)
block|{
name|bucket
operator|=
operator|new
name|IBucket
argument_list|(
name|bucketFileName
argument_list|)
expr_stmt|;
name|buckets
operator|.
name|put
argument_list|(
name|bucketFileName
argument_list|,
name|bucket
argument_list|)
expr_stmt|;
block|}
name|int
name|currentStart
init|=
name|firstEnd
operator|+
literal|1
decl_stmt|;
name|int
name|currentEnd
init|=
name|firstEnd
operator|+
literal|1
decl_stmt|;
for|for
control|(
init|;
name|currentEnd
operator|<
name|bytes
operator|.
name|length
condition|;
name|currentEnd
operator|++
control|)
block|{
if|if
condition|(
name|bytes
index|[
name|currentEnd
index|]
operator|==
name|LazySerDeParameters
operator|.
name|DefaultSeparators
index|[
literal|1
index|]
condition|)
block|{
name|String
name|one_offset
init|=
operator|new
name|String
argument_list|(
name|bytes
argument_list|,
name|currentStart
argument_list|,
name|currentEnd
operator|-
name|currentStart
argument_list|)
decl_stmt|;
name|Long
name|offset
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|one_offset
argument_list|)
decl_stmt|;
name|bucket
operator|.
name|getOffsets
argument_list|()
operator|.
name|add
argument_list|(
name|offset
argument_list|)
expr_stmt|;
name|currentStart
operator|=
name|currentEnd
operator|+
literal|1
expr_stmt|;
block|}
block|}
name|String
name|one_offset
init|=
operator|new
name|String
argument_list|(
name|bytes
argument_list|,
name|currentStart
argument_list|,
name|currentEnd
operator|-
name|currentStart
argument_list|)
decl_stmt|;
name|bucket
operator|.
name|getOffsets
argument_list|()
operator|.
name|add
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|one_offset
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|contains
parameter_list|(
name|FileSplit
name|split
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|buckets
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|String
name|bucketName
init|=
name|split
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|IBucket
name|bucket
init|=
name|buckets
operator|.
name|get
argument_list|(
name|bucketName
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucket
operator|==
literal|null
condition|)
block|{
name|bucketName
operator|=
name|split
operator|.
name|getPath
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
expr_stmt|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
name|bucketName
argument_list|)
expr_stmt|;
if|if
condition|(
name|bucket
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
for|for
control|(
name|Long
name|offset
range|:
name|bucket
operator|.
name|getOffsets
argument_list|()
control|)
block|{
if|if
condition|(
operator|(
name|offset
operator|>=
name|split
operator|.
name|getStart
argument_list|()
operator|)
operator|&&
operator|(
name|offset
operator|<=
name|split
operator|.
name|getStart
argument_list|()
operator|+
name|split
operator|.
name|getLength
argument_list|()
operator|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

