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
name|persistence
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
name|io
operator|.
name|InputStream
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
name|io
operator|.
name|ObjectOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ConcurrentModificationException
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
name|common
operator|.
name|JavaUtils
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
name|exec
operator|.
name|vector
operator|.
name|mapjoin
operator|.
name|fast
operator|.
name|VectorMapJoinFastTableContainer
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
name|serde2
operator|.
name|AbstractSerDe
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
name|SerDeException
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
name|io
operator|.
name|Writable
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
class|class
name|MapJoinTableContainerSerDe
block|{
specifier|private
specifier|final
name|MapJoinObjectSerDeContext
name|keyContext
decl_stmt|;
specifier|private
specifier|final
name|MapJoinObjectSerDeContext
name|valueContext
decl_stmt|;
specifier|public
name|MapJoinTableContainerSerDe
parameter_list|(
name|MapJoinObjectSerDeContext
name|keyContext
parameter_list|,
name|MapJoinObjectSerDeContext
name|valueContext
parameter_list|)
block|{
name|this
operator|.
name|keyContext
operator|=
name|keyContext
expr_stmt|;
name|this
operator|.
name|valueContext
operator|=
name|valueContext
expr_stmt|;
block|}
specifier|public
name|MapJoinObjectSerDeContext
name|getKeyContext
parameter_list|()
block|{
return|return
name|keyContext
return|;
block|}
specifier|public
name|MapJoinObjectSerDeContext
name|getValueContext
parameter_list|()
block|{
return|return
name|valueContext
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
comment|/**    * Loads the table container. Only used on MR path.    * @param in Input stream.    * @return Loaded table.    */
specifier|public
name|MapJoinPersistableTableContainer
name|load
parameter_list|(
name|ObjectInputStream
name|in
parameter_list|)
throws|throws
name|HiveException
block|{
name|AbstractSerDe
name|keySerDe
init|=
name|keyContext
operator|.
name|getSerDe
argument_list|()
decl_stmt|;
name|AbstractSerDe
name|valueSerDe
init|=
name|valueContext
operator|.
name|getSerDe
argument_list|()
decl_stmt|;
name|MapJoinPersistableTableContainer
name|tableContainer
decl_stmt|;
try|try
block|{
name|String
name|name
init|=
name|in
operator|.
name|readUTF
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|metaData
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
name|in
operator|.
name|readObject
argument_list|()
decl_stmt|;
name|tableContainer
operator|=
name|create
argument_list|(
name|name
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"IO error while trying to create table container"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Class Initialization error while trying to create table container"
argument_list|,
name|e
argument_list|)
throw|;
block|}
try|try
block|{
name|Writable
name|keyContainer
init|=
name|keySerDe
operator|.
name|getSerializedClass
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|Writable
name|valueContainer
init|=
name|valueSerDe
operator|.
name|getSerializedClass
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|int
name|numKeys
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|keyIndex
init|=
literal|0
init|;
name|keyIndex
operator|<
name|numKeys
condition|;
name|keyIndex
operator|++
control|)
block|{
name|MapJoinKeyObject
name|key
init|=
operator|new
name|MapJoinKeyObject
argument_list|()
decl_stmt|;
name|key
operator|.
name|read
argument_list|(
name|keyContext
argument_list|,
name|in
argument_list|,
name|keyContainer
argument_list|)
expr_stmt|;
name|MapJoinEagerRowContainer
name|values
init|=
operator|new
name|MapJoinEagerRowContainer
argument_list|()
decl_stmt|;
name|values
operator|.
name|read
argument_list|(
name|valueContext
argument_list|,
name|in
argument_list|,
name|valueContainer
argument_list|)
expr_stmt|;
name|tableContainer
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
return|return
name|tableContainer
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"IO error while trying to create table container"
argument_list|,
name|e
argument_list|)
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
literal|"Error while trying to create table container"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Loads the table container from a folder. Only used on Spark path.    * @param fs FileSystem of the folder.    * @param folder The folder to load table container.    * @param hconf The hive configuration    * @return Loaded table.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|MapJoinTableContainer
name|load
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|folder
parameter_list|,
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|folder
argument_list|)
condition|)
block|{
return|return
name|getDefaultEmptyContainer
argument_list|(
name|hconf
argument_list|,
name|keyContext
argument_list|,
name|valueContext
argument_list|)
return|;
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|isDirectory
argument_list|(
name|folder
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Error, not a directory: "
operator|+
name|folder
argument_list|)
throw|;
block|}
name|FileStatus
index|[]
name|fileStatuses
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|folder
argument_list|)
decl_stmt|;
if|if
condition|(
name|fileStatuses
operator|==
literal|null
operator|||
name|fileStatuses
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|getDefaultEmptyContainer
argument_list|(
name|hconf
argument_list|,
name|keyContext
argument_list|,
name|valueContext
argument_list|)
return|;
block|}
name|AbstractSerDe
name|keySerDe
init|=
name|keyContext
operator|.
name|getSerDe
argument_list|()
decl_stmt|;
name|AbstractSerDe
name|valueSerDe
init|=
name|valueContext
operator|.
name|getSerDe
argument_list|()
decl_stmt|;
name|Writable
name|keyContainer
init|=
name|keySerDe
operator|.
name|getSerializedClass
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|Writable
name|valueContainer
init|=
name|valueSerDe
operator|.
name|getSerializedClass
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|MapJoinTableContainer
name|tableContainer
init|=
literal|null
decl_stmt|;
name|boolean
name|useOptimizedContainer
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMAPJOINUSEOPTIMIZEDTABLE
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|fileStatus
range|:
name|fileStatuses
control|)
block|{
name|Path
name|filePath
init|=
name|fileStatus
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|isDirectory
argument_list|(
name|fileStatus
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Error, not a file: "
operator|+
name|filePath
argument_list|)
throw|;
block|}
name|InputStream
name|is
init|=
literal|null
decl_stmt|;
name|ObjectInputStream
name|in
init|=
literal|null
decl_stmt|;
try|try
block|{
name|is
operator|=
name|fs
operator|.
name|open
argument_list|(
name|filePath
argument_list|)
expr_stmt|;
name|in
operator|=
operator|new
name|ObjectInputStream
argument_list|(
name|is
argument_list|)
expr_stmt|;
name|String
name|name
init|=
name|in
operator|.
name|readUTF
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|metaData
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
name|in
operator|.
name|readObject
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableContainer
operator|==
literal|null
condition|)
block|{
name|tableContainer
operator|=
name|useOptimizedContainer
condition|?
operator|new
name|MapJoinBytesTableContainer
argument_list|(
name|hconf
argument_list|,
name|valueContext
argument_list|,
operator|-
literal|1
argument_list|,
literal|0
argument_list|)
else|:
name|create
argument_list|(
name|name
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
block|}
name|tableContainer
operator|.
name|setSerde
argument_list|(
name|keyContext
argument_list|,
name|valueContext
argument_list|)
expr_stmt|;
if|if
condition|(
name|useOptimizedContainer
condition|)
block|{
name|loadOptimized
argument_list|(
operator|(
name|MapJoinBytesTableContainer
operator|)
name|tableContainer
argument_list|,
name|in
argument_list|,
name|keyContainer
argument_list|,
name|valueContainer
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|loadNormal
argument_list|(
operator|(
name|MapJoinPersistableTableContainer
operator|)
name|tableContainer
argument_list|,
name|in
argument_list|,
name|keyContainer
argument_list|,
name|valueContainer
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|in
operator|!=
literal|null
condition|)
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|is
operator|!=
literal|null
condition|)
block|{
name|is
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|tableContainer
operator|!=
literal|null
condition|)
block|{
name|tableContainer
operator|.
name|setKey
argument_list|(
name|folder
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|tableContainer
operator|.
name|seal
argument_list|()
expr_stmt|;
block|}
return|return
name|tableContainer
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"IO error while trying to create table container"
argument_list|,
name|e
argument_list|)
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
literal|"Error while trying to create table container"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|loadNormal
parameter_list|(
name|MapJoinPersistableTableContainer
name|container
parameter_list|,
name|ObjectInputStream
name|in
parameter_list|,
name|Writable
name|keyContainer
parameter_list|,
name|Writable
name|valueContainer
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|numKeys
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|keyIndex
init|=
literal|0
init|;
name|keyIndex
operator|<
name|numKeys
condition|;
name|keyIndex
operator|++
control|)
block|{
name|MapJoinKeyObject
name|key
init|=
operator|new
name|MapJoinKeyObject
argument_list|()
decl_stmt|;
name|key
operator|.
name|read
argument_list|(
name|keyContext
argument_list|,
name|in
argument_list|,
name|keyContainer
argument_list|)
expr_stmt|;
if|if
condition|(
name|container
operator|.
name|get
argument_list|(
name|key
argument_list|)
operator|==
literal|null
condition|)
block|{
name|container
operator|.
name|put
argument_list|(
name|key
argument_list|,
operator|new
name|MapJoinEagerRowContainer
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|MapJoinEagerRowContainer
name|values
init|=
operator|(
name|MapJoinEagerRowContainer
operator|)
name|container
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|values
operator|.
name|read
argument_list|(
name|valueContext
argument_list|,
name|in
argument_list|,
name|valueContainer
argument_list|)
expr_stmt|;
name|container
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|loadOptimized
parameter_list|(
name|MapJoinBytesTableContainer
name|container
parameter_list|,
name|ObjectInputStream
name|in
parameter_list|,
name|Writable
name|key
parameter_list|,
name|Writable
name|value
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|numKeys
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|keyIndex
init|=
literal|0
init|;
name|keyIndex
operator|<
name|numKeys
condition|;
name|keyIndex
operator|++
control|)
block|{
name|key
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|long
name|numRows
init|=
name|in
operator|.
name|readLong
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|rowIndex
init|=
literal|0L
init|;
name|rowIndex
operator|<
name|numRows
condition|;
name|rowIndex
operator|++
control|)
block|{
name|value
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|container
operator|.
name|putRow
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Loads the small table into a VectorMapJoinFastTableContainer. Only used on Spark path.    * @param mapJoinDesc The descriptor for the map join    * @param fs FileSystem of the folder.    * @param folder The folder to load table container.    * @param hconf The hive configuration    * @return Loaded table.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|MapJoinTableContainer
name|loadFastContainer
parameter_list|(
name|MapJoinDesc
name|mapJoinDesc
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|folder
parameter_list|,
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|VectorMapJoinFastTableContainer
name|tableContainer
init|=
operator|new
name|VectorMapJoinFastTableContainer
argument_list|(
name|mapJoinDesc
argument_list|,
name|hconf
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
name|tableContainer
operator|.
name|setSerde
argument_list|(
name|keyContext
argument_list|,
name|valueContext
argument_list|)
expr_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|folder
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|isDirectory
argument_list|(
name|folder
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Error, not a directory: "
operator|+
name|folder
argument_list|)
throw|;
block|}
name|FileStatus
index|[]
name|fileStatuses
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|folder
argument_list|)
decl_stmt|;
if|if
condition|(
name|fileStatuses
operator|!=
literal|null
operator|&&
name|fileStatuses
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|AbstractSerDe
name|keySerDe
init|=
name|keyContext
operator|.
name|getSerDe
argument_list|()
decl_stmt|;
name|AbstractSerDe
name|valueSerDe
init|=
name|valueContext
operator|.
name|getSerDe
argument_list|()
decl_stmt|;
name|Writable
name|key
init|=
name|keySerDe
operator|.
name|getSerializedClass
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|Writable
name|value
init|=
name|valueSerDe
operator|.
name|getSerializedClass
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|fileStatus
range|:
name|fileStatuses
control|)
block|{
name|Path
name|filePath
init|=
name|fileStatus
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|isDirectory
argument_list|(
name|fileStatus
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Error, not a file: "
operator|+
name|filePath
argument_list|)
throw|;
block|}
name|InputStream
name|is
init|=
literal|null
decl_stmt|;
name|ObjectInputStream
name|in
init|=
literal|null
decl_stmt|;
try|try
block|{
name|is
operator|=
name|fs
operator|.
name|open
argument_list|(
name|filePath
argument_list|)
expr_stmt|;
name|in
operator|=
operator|new
name|ObjectInputStream
argument_list|(
name|is
argument_list|)
expr_stmt|;
comment|// skip the name and metadata
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|in
operator|.
name|readObject
argument_list|()
expr_stmt|;
name|int
name|numKeys
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|keyIndex
init|=
literal|0
init|;
name|keyIndex
operator|<
name|numKeys
condition|;
name|keyIndex
operator|++
control|)
block|{
name|key
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|long
name|numRows
init|=
name|in
operator|.
name|readLong
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|rowIndex
init|=
literal|0L
init|;
name|rowIndex
operator|<
name|numRows
condition|;
name|rowIndex
operator|++
control|)
block|{
name|value
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|tableContainer
operator|.
name|putRow
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|in
operator|!=
literal|null
condition|)
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|is
operator|!=
literal|null
condition|)
block|{
name|is
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
name|tableContainer
operator|.
name|setKey
argument_list|(
name|folder
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|tableContainer
operator|.
name|seal
argument_list|()
expr_stmt|;
return|return
name|tableContainer
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"IO error while trying to create table container"
argument_list|,
name|e
argument_list|)
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
literal|"Error while trying to create table container"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|persist
parameter_list|(
name|ObjectOutputStream
name|out
parameter_list|,
name|MapJoinPersistableTableContainer
name|tableContainer
parameter_list|)
throws|throws
name|HiveException
block|{
name|int
name|numKeys
init|=
name|tableContainer
operator|.
name|size
argument_list|()
decl_stmt|;
try|try
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|tableContainer
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeObject
argument_list|(
name|tableContainer
operator|.
name|getMetaData
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|numKeys
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|MapJoinKey
argument_list|,
name|MapJoinRowContainer
argument_list|>
name|entry
range|:
name|tableContainer
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|write
argument_list|(
name|keyContext
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|write
argument_list|(
name|valueContext
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"SerDe error while attempting to persist table container"
decl_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"IO error while attempting to persist table container"
decl_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|numKeys
operator|!=
name|tableContainer
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ConcurrentModificationException
argument_list|(
literal|"TableContainer was modified while persisting: "
operator|+
name|tableContainer
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|void
name|persistDummyTable
parameter_list|(
name|ObjectOutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|MapJoinPersistableTableContainer
name|tableContainer
init|=
operator|new
name|HashMapWrapper
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|tableContainer
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeObject
argument_list|(
name|tableContainer
operator|.
name|getMetaData
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|tableContainer
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|MapJoinPersistableTableContainer
name|create
parameter_list|(
name|String
name|name
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|metaData
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Class
argument_list|<
name|?
extends|extends
name|MapJoinPersistableTableContainer
argument_list|>
name|clazz
init|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|MapJoinPersistableTableContainer
argument_list|>
operator|)
name|JavaUtils
operator|.
name|loadClass
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|Constructor
argument_list|<
name|?
extends|extends
name|MapJoinPersistableTableContainer
argument_list|>
name|constructor
init|=
name|clazz
operator|.
name|getDeclaredConstructor
argument_list|(
name|Map
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|constructor
operator|.
name|newInstance
argument_list|(
name|metaData
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Error while attempting to create table container"
operator|+
literal|" of type: "
operator|+
name|name
operator|+
literal|", with metaData: "
operator|+
name|metaData
decl_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|// Get an empty container when the small table is empty.
specifier|private
specifier|static
name|MapJoinTableContainer
name|getDefaultEmptyContainer
parameter_list|(
name|Configuration
name|hconf
parameter_list|,
name|MapJoinObjectSerDeContext
name|keyCtx
parameter_list|,
name|MapJoinObjectSerDeContext
name|valCtx
parameter_list|)
throws|throws
name|SerDeException
block|{
name|boolean
name|useOptimizedContainer
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMAPJOINUSEOPTIMIZEDTABLE
argument_list|)
decl_stmt|;
if|if
condition|(
name|useOptimizedContainer
condition|)
block|{
return|return
operator|new
name|MapJoinBytesTableContainer
argument_list|(
name|hconf
argument_list|,
name|valCtx
argument_list|,
operator|-
literal|1
argument_list|,
literal|0
argument_list|)
return|;
block|}
name|MapJoinTableContainer
name|container
init|=
operator|new
name|HashMapWrapper
argument_list|()
decl_stmt|;
name|container
operator|.
name|setSerde
argument_list|(
name|keyCtx
argument_list|,
name|valCtx
argument_list|)
expr_stmt|;
name|container
operator|.
name|seal
argument_list|()
expr_stmt|;
return|return
name|container
return|;
block|}
block|}
end_class

end_unit

