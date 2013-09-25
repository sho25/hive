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
name|io
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
name|Collections
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
name|WeakHashMap
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
name|io
operator|.
name|RCFile
operator|.
name|KeyBuffer
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
name|io
operator|.
name|RCFile
operator|.
name|Reader
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
name|BytesRefArrayWritable
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
name|LongWritable
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
name|RecordReader
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * RCFileRecordReader.  *  * @param<K>  * @param<V>  */
end_comment

begin_class
specifier|public
class|class
name|RCFileRecordReader
parameter_list|<
name|K
extends|extends
name|LongWritable
parameter_list|,
name|V
extends|extends
name|BytesRefArrayWritable
parameter_list|>
implements|implements
name|RecordReader
argument_list|<
name|LongWritable
argument_list|,
name|BytesRefArrayWritable
argument_list|>
block|{
specifier|private
specifier|final
name|Reader
name|in
decl_stmt|;
specifier|private
specifier|final
name|long
name|start
decl_stmt|;
specifier|private
specifier|final
name|long
name|end
decl_stmt|;
specifier|private
name|boolean
name|more
init|=
literal|true
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|FileSplit
name|split
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|useCache
decl_stmt|;
specifier|private
specifier|static
name|RCFileSyncCache
name|syncCache
init|=
operator|new
name|RCFileSyncCache
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
class|class
name|RCFileSyncEntry
block|{
name|long
name|end
decl_stmt|;
name|long
name|endSync
decl_stmt|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|RCFileSyncCache
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|RCFileSyncEntry
argument_list|>
name|cache
decl_stmt|;
specifier|public
name|RCFileSyncCache
parameter_list|()
block|{
name|cache
operator|=
name|Collections
operator|.
name|synchronizedMap
argument_list|(
operator|new
name|WeakHashMap
argument_list|<
name|String
argument_list|,
name|RCFileSyncEntry
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|put
parameter_list|(
name|FileSplit
name|split
parameter_list|,
name|long
name|endSync
parameter_list|)
block|{
name|Path
name|path
init|=
name|split
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|long
name|end
init|=
name|split
operator|.
name|getStart
argument_list|()
operator|+
name|split
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|String
name|key
init|=
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|"+"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%d"
argument_list|,
name|end
argument_list|)
decl_stmt|;
name|RCFileSyncEntry
name|entry
init|=
operator|new
name|RCFileSyncEntry
argument_list|()
decl_stmt|;
name|entry
operator|.
name|end
operator|=
name|end
expr_stmt|;
name|entry
operator|.
name|endSync
operator|=
name|endSync
expr_stmt|;
if|if
condition|(
name|entry
operator|.
name|endSync
operator|>=
name|entry
operator|.
name|end
condition|)
block|{
name|cache
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|entry
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|long
name|get
parameter_list|(
name|FileSplit
name|split
parameter_list|)
block|{
name|Path
name|path
init|=
name|split
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|long
name|start
init|=
name|split
operator|.
name|getStart
argument_list|()
decl_stmt|;
name|String
name|key
init|=
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|"+"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%d"
argument_list|,
name|start
argument_list|)
decl_stmt|;
name|RCFileSyncEntry
name|entry
init|=
name|cache
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|!=
literal|null
condition|)
block|{
return|return
name|entry
operator|.
name|endSync
return|;
block|}
return|return
operator|-
literal|1
return|;
block|}
block|}
specifier|public
name|RCFileRecordReader
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSplit
name|split
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|path
init|=
name|split
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|this
operator|.
name|in
operator|=
operator|new
name|RCFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|end
operator|=
name|split
operator|.
name|getStart
argument_list|()
operator|+
name|split
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|split
operator|=
name|split
expr_stmt|;
name|useCache
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
name|HIVEUSERCFILESYNCCACHE
argument_list|)
expr_stmt|;
if|if
condition|(
name|split
operator|.
name|getStart
argument_list|()
operator|>
name|in
operator|.
name|getPosition
argument_list|()
condition|)
block|{
name|long
name|oldSync
init|=
name|useCache
condition|?
name|syncCache
operator|.
name|get
argument_list|(
name|split
argument_list|)
else|:
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|oldSync
operator|==
operator|-
literal|1
condition|)
block|{
name|in
operator|.
name|sync
argument_list|(
name|split
operator|.
name|getStart
argument_list|()
argument_list|)
expr_stmt|;
comment|// sync to start
block|}
else|else
block|{
name|in
operator|.
name|seek
argument_list|(
name|oldSync
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|start
operator|=
name|in
operator|.
name|getPosition
argument_list|()
expr_stmt|;
name|more
operator|=
name|start
operator|<
name|end
expr_stmt|;
block|}
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getKeyClass
parameter_list|()
block|{
return|return
name|LongWritable
operator|.
name|class
return|;
block|}
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getValueClass
parameter_list|()
block|{
return|return
name|BytesRefArrayWritable
operator|.
name|class
return|;
block|}
specifier|public
name|LongWritable
name|createKey
parameter_list|()
block|{
return|return
operator|(
name|LongWritable
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|getKeyClass
argument_list|()
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|public
name|BytesRefArrayWritable
name|createValue
parameter_list|()
block|{
return|return
operator|(
name|BytesRefArrayWritable
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|getValueClass
argument_list|()
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|nextBlock
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|in
operator|.
name|nextBlock
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|LongWritable
name|key
parameter_list|,
name|BytesRefArrayWritable
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|more
operator|=
name|next
argument_list|(
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
name|more
condition|)
block|{
name|in
operator|.
name|getCurrentRow
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|more
return|;
block|}
specifier|protected
name|boolean
name|next
parameter_list|(
name|LongWritable
name|key
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|more
condition|)
block|{
return|return
literal|false
return|;
block|}
name|more
operator|=
name|in
operator|.
name|next
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|long
name|lastSeenSyncPos
init|=
name|in
operator|.
name|lastSeenSyncPos
argument_list|()
decl_stmt|;
if|if
condition|(
name|lastSeenSyncPos
operator|>=
name|end
condition|)
block|{
if|if
condition|(
name|useCache
condition|)
block|{
name|syncCache
operator|.
name|put
argument_list|(
name|split
argument_list|,
name|lastSeenSyncPos
argument_list|)
expr_stmt|;
block|}
name|more
operator|=
literal|false
expr_stmt|;
return|return
name|more
return|;
block|}
return|return
name|more
return|;
block|}
comment|/**    * Return the progress within the input split.    *    * @return 0.0 to 1.0 of the input byte range    */
specifier|public
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|end
operator|==
name|start
condition|)
block|{
return|return
literal|0.0f
return|;
block|}
else|else
block|{
return|return
name|Math
operator|.
name|min
argument_list|(
literal|1.0f
argument_list|,
operator|(
name|in
operator|.
name|getPosition
argument_list|()
operator|-
name|start
operator|)
operator|/
call|(
name|float
call|)
argument_list|(
name|end
operator|-
name|start
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|public
name|long
name|getPos
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|in
operator|.
name|getPosition
argument_list|()
return|;
block|}
specifier|public
name|KeyBuffer
name|getKeyBuffer
parameter_list|()
block|{
return|return
name|in
operator|.
name|getCurrentKeyBufferObj
argument_list|()
return|;
block|}
specifier|protected
name|void
name|seek
parameter_list|(
name|long
name|pos
parameter_list|)
throws|throws
name|IOException
block|{
name|in
operator|.
name|seek
argument_list|(
name|pos
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|sync
parameter_list|(
name|long
name|pos
parameter_list|)
throws|throws
name|IOException
block|{
name|in
operator|.
name|sync
argument_list|(
name|pos
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|resetBuffer
parameter_list|()
block|{
name|in
operator|.
name|resetBuffer
argument_list|()
expr_stmt|;
block|}
specifier|public
name|long
name|getStart
parameter_list|()
block|{
return|return
name|start
return|;
block|}
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

