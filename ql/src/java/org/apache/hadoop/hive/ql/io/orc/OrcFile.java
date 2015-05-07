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
operator|.
name|orc
package|;
end_package

begin_import
import|import static
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
operator|.
name|ConfVars
operator|.
name|HIVE_ORC_DEFAULT_BLOCK_PADDING
import|;
end_import

begin_import
import|import static
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
operator|.
name|ConfVars
operator|.
name|HIVE_ORC_DEFAULT_BLOCK_SIZE
import|;
end_import

begin_import
import|import static
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
operator|.
name|ConfVars
operator|.
name|HIVE_ORC_DEFAULT_BUFFER_SIZE
import|;
end_import

begin_import
import|import static
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
operator|.
name|ConfVars
operator|.
name|HIVE_ORC_DEFAULT_COMPRESS
import|;
end_import

begin_import
import|import static
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
operator|.
name|ConfVars
operator|.
name|HIVE_ORC_DEFAULT_ROW_INDEX_STRIDE
import|;
end_import

begin_import
import|import static
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
operator|.
name|ConfVars
operator|.
name|HIVE_ORC_DEFAULT_STRIPE_SIZE
import|;
end_import

begin_import
import|import static
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
operator|.
name|ConfVars
operator|.
name|HIVE_ORC_WRITE_FORMAT
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
name|filters
operator|.
name|BloomFilterIO
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
name|orc
operator|.
name|OrcFile
operator|.
name|WriterVersion
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

begin_comment
comment|/**  * Contains factory methods to read or write ORC files.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|OrcFile
block|{
specifier|public
specifier|static
specifier|final
name|String
name|MAGIC
init|=
literal|"ORC"
decl_stmt|;
comment|/**    * Create a version number for the ORC file format, so that we can add    * non-forward compatible changes in the future. To make it easier for users    * to understand the version numbers, we use the Hive release number that    * first wrote that version of ORC files.    *    * Thus, if you add new encodings or other non-forward compatible changes    * to ORC files, which prevent the old reader from reading the new format,    * you should change these variable to reflect the next Hive release number.    * Non-forward compatible changes should never be added in patch releases.    *    * Do not make any changes that break backwards compatibility, which would    * prevent the new reader from reading ORC files generated by any released    * version of Hive.    */
specifier|public
specifier|static
enum|enum
name|Version
block|{
name|V_0_11
argument_list|(
literal|"0.11"
argument_list|,
literal|0
argument_list|,
literal|11
argument_list|)
block|,
name|V_0_12
argument_list|(
literal|"0.12"
argument_list|,
literal|0
argument_list|,
literal|12
argument_list|)
block|;
specifier|public
specifier|static
specifier|final
name|Version
name|CURRENT
init|=
name|V_0_12
decl_stmt|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|int
name|major
decl_stmt|;
specifier|private
specifier|final
name|int
name|minor
decl_stmt|;
specifier|private
name|Version
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|major
parameter_list|,
name|int
name|minor
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|major
operator|=
name|major
expr_stmt|;
name|this
operator|.
name|minor
operator|=
name|minor
expr_stmt|;
block|}
specifier|public
specifier|static
name|Version
name|byName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
for|for
control|(
name|Version
name|version
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|version
operator|.
name|name
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|version
return|;
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown ORC version "
operator|+
name|name
argument_list|)
throw|;
block|}
comment|/**      * Get the human readable name for the version.      */
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**      * Get the major version number.      */
specifier|public
name|int
name|getMajor
parameter_list|()
block|{
return|return
name|major
return|;
block|}
comment|/**      * Get the minor version number.      */
specifier|public
name|int
name|getMinor
parameter_list|()
block|{
return|return
name|minor
return|;
block|}
block|}
comment|/**    * Records the version of the writer in terms of which bugs have been fixed.    * For bugs in the writer, but the old readers already read the new data    * correctly, bump this version instead of the Version.    */
specifier|public
specifier|static
enum|enum
name|WriterVersion
block|{
name|ORIGINAL
argument_list|(
literal|0
argument_list|)
block|,
name|HIVE_8732
argument_list|(
literal|1
argument_list|)
block|;
comment|// corrupted stripe/file maximum column statistics
specifier|private
specifier|final
name|int
name|id
decl_stmt|;
specifier|public
name|int
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
specifier|private
name|WriterVersion
parameter_list|(
name|int
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|WriterVersion
index|[]
name|values
decl_stmt|;
static|static
block|{
comment|// Assumes few non-negative values close to zero.
name|int
name|max
init|=
name|Integer
operator|.
name|MIN_VALUE
decl_stmt|;
for|for
control|(
name|WriterVersion
name|v
range|:
name|WriterVersion
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|v
operator|.
name|id
operator|<
literal|0
condition|)
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
if|if
condition|(
name|v
operator|.
name|id
operator|>
name|max
condition|)
block|{
name|max
operator|=
name|v
operator|.
name|id
expr_stmt|;
block|}
block|}
name|values
operator|=
operator|new
name|WriterVersion
index|[
name|max
operator|+
literal|1
index|]
expr_stmt|;
for|for
control|(
name|WriterVersion
name|v
range|:
name|WriterVersion
operator|.
name|values
argument_list|()
control|)
block|{
name|values
index|[
name|v
operator|.
name|id
index|]
operator|=
name|v
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|WriterVersion
name|from
parameter_list|(
name|int
name|val
parameter_list|)
block|{
return|return
name|values
index|[
name|val
index|]
return|;
block|}
block|}
specifier|public
specifier|static
enum|enum
name|EncodingStrategy
block|{
name|SPEED
block|,
name|COMPRESSION
block|;   }
specifier|public
specifier|static
enum|enum
name|CompressionStrategy
block|{
name|SPEED
block|,
name|COMPRESSION
block|;   }
comment|// Note : these string definitions for table properties are deprecated,
comment|// and retained only for backward compatibility, please do not add to
comment|// them, add to OrcTableProperties below instead
annotation|@
name|Deprecated
specifier|public
specifier|static
specifier|final
name|String
name|COMPRESSION
init|=
literal|"orc.compress"
decl_stmt|;
annotation|@
name|Deprecated
specifier|public
specifier|static
specifier|final
name|String
name|COMPRESSION_BLOCK_SIZE
init|=
literal|"orc.compress.size"
decl_stmt|;
annotation|@
name|Deprecated
specifier|public
specifier|static
specifier|final
name|String
name|STRIPE_SIZE
init|=
literal|"orc.stripe.size"
decl_stmt|;
annotation|@
name|Deprecated
specifier|public
specifier|static
specifier|final
name|String
name|ROW_INDEX_STRIDE
init|=
literal|"orc.row.index.stride"
decl_stmt|;
annotation|@
name|Deprecated
specifier|public
specifier|static
specifier|final
name|String
name|ENABLE_INDEXES
init|=
literal|"orc.create.index"
decl_stmt|;
annotation|@
name|Deprecated
specifier|public
specifier|static
specifier|final
name|String
name|BLOCK_PADDING
init|=
literal|"orc.block.padding"
decl_stmt|;
comment|/**    * Enum container for all orc table properties.    * If introducing a new orc-specific table property,    * add it here.    */
specifier|public
specifier|static
enum|enum
name|OrcTableProperties
block|{
name|COMPRESSION
argument_list|(
literal|"orc.compress"
argument_list|)
block|,
name|COMPRESSION_BLOCK_SIZE
argument_list|(
literal|"orc.compress.size"
argument_list|)
block|,
name|STRIPE_SIZE
argument_list|(
literal|"orc.stripe.size"
argument_list|)
block|,
name|BLOCK_SIZE
argument_list|(
literal|"orc.block.size"
argument_list|)
block|,
name|ROW_INDEX_STRIDE
argument_list|(
literal|"orc.row.index.stride"
argument_list|)
block|,
name|ENABLE_INDEXES
argument_list|(
literal|"orc.create.index"
argument_list|)
block|,
name|BLOCK_PADDING
argument_list|(
literal|"orc.block.padding"
argument_list|)
block|,
name|ENCODING_STRATEGY
argument_list|(
literal|"orc.encoding.strategy"
argument_list|)
block|,
name|BLOOM_FILTER_COLUMNS
argument_list|(
literal|"orc.bloom.filter.columns"
argument_list|)
block|,
name|BLOOM_FILTER_FPP
argument_list|(
literal|"orc.bloom.filter.fpp"
argument_list|)
block|;
specifier|private
specifier|final
name|String
name|propName
decl_stmt|;
name|OrcTableProperties
parameter_list|(
name|String
name|propName
parameter_list|)
block|{
name|this
operator|.
name|propName
operator|=
name|propName
expr_stmt|;
block|}
specifier|public
name|String
name|getPropName
parameter_list|()
block|{
return|return
name|this
operator|.
name|propName
return|;
block|}
block|}
comment|// unused
specifier|private
name|OrcFile
parameter_list|()
block|{}
comment|/**    * Create an ORC file reader.    * @param fs file system    * @param path file name to read from    * @return a new ORC file reader.    * @throws IOException    */
specifier|public
specifier|static
name|Reader
name|createReader
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|ReaderOptions
name|opts
init|=
operator|new
name|ReaderOptions
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|opts
operator|.
name|filesystem
argument_list|(
name|fs
argument_list|)
expr_stmt|;
return|return
operator|new
name|ReaderImpl
argument_list|(
name|path
argument_list|,
name|opts
argument_list|)
return|;
block|}
specifier|public
specifier|static
class|class
name|ReaderOptions
block|{
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|FileSystem
name|filesystem
decl_stmt|;
specifier|private
name|FileMetaInfo
name|fileMetaInfo
decl_stmt|;
comment|// TODO: this comes from some place.
specifier|private
name|long
name|maxLength
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|private
name|FileMetadata
name|fullFileMetadata
decl_stmt|;
comment|// Propagate from LLAP cache.
specifier|public
name|ReaderOptions
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
name|ReaderOptions
name|fileMetaInfo
parameter_list|(
name|FileMetaInfo
name|info
parameter_list|)
block|{
name|fileMetaInfo
operator|=
name|info
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ReaderOptions
name|filesystem
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
block|{
name|this
operator|.
name|filesystem
operator|=
name|fs
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ReaderOptions
name|maxLength
parameter_list|(
name|long
name|val
parameter_list|)
block|{
name|maxLength
operator|=
name|val
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ReaderOptions
name|fileMetadata
parameter_list|(
name|FileMetadata
name|metadata
parameter_list|)
block|{
name|this
operator|.
name|fullFileMetadata
operator|=
name|metadata
expr_stmt|;
return|return
name|this
return|;
block|}
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
name|FileSystem
name|getFilesystem
parameter_list|()
block|{
return|return
name|filesystem
return|;
block|}
name|FileMetaInfo
name|getFileMetaInfo
parameter_list|()
block|{
return|return
name|fileMetaInfo
return|;
block|}
name|long
name|getMaxLength
parameter_list|()
block|{
return|return
name|maxLength
return|;
block|}
name|FileMetadata
name|getFileMetadata
parameter_list|()
block|{
return|return
name|fullFileMetadata
return|;
block|}
block|}
specifier|public
specifier|static
name|ReaderOptions
name|readerOptions
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|new
name|ReaderOptions
argument_list|(
name|conf
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Reader
name|createReader
parameter_list|(
name|Path
name|path
parameter_list|,
name|ReaderOptions
name|options
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|ReaderImpl
argument_list|(
name|path
argument_list|,
name|options
argument_list|)
return|;
block|}
specifier|public
specifier|static
interface|interface
name|WriterContext
block|{
name|Writer
name|getWriter
parameter_list|()
function_decl|;
block|}
specifier|public
specifier|static
interface|interface
name|WriterCallback
block|{
specifier|public
name|void
name|preStripeWrite
parameter_list|(
name|WriterContext
name|context
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|preFooterWrite
parameter_list|(
name|WriterContext
name|context
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**    * Options for creating ORC file writers.    */
specifier|public
specifier|static
class|class
name|WriterOptions
block|{
specifier|private
specifier|final
name|Configuration
name|configuration
decl_stmt|;
specifier|private
name|FileSystem
name|fileSystemValue
init|=
literal|null
decl_stmt|;
specifier|private
name|ObjectInspector
name|inspectorValue
init|=
literal|null
decl_stmt|;
specifier|private
name|long
name|stripeSizeValue
decl_stmt|;
specifier|private
name|long
name|blockSizeValue
decl_stmt|;
specifier|private
name|int
name|rowIndexStrideValue
decl_stmt|;
specifier|private
name|int
name|bufferSizeValue
decl_stmt|;
specifier|private
name|boolean
name|blockPaddingValue
decl_stmt|;
specifier|private
name|CompressionKind
name|compressValue
decl_stmt|;
specifier|private
name|MemoryManager
name|memoryManagerValue
decl_stmt|;
specifier|private
name|Version
name|versionValue
decl_stmt|;
specifier|private
name|WriterCallback
name|callback
decl_stmt|;
specifier|private
name|EncodingStrategy
name|encodingStrategy
decl_stmt|;
specifier|private
name|CompressionStrategy
name|compressionStrategy
decl_stmt|;
specifier|private
name|float
name|paddingTolerance
decl_stmt|;
specifier|private
name|String
name|bloomFilterColumns
decl_stmt|;
specifier|private
name|double
name|bloomFilterFpp
decl_stmt|;
name|WriterOptions
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|configuration
operator|=
name|conf
expr_stmt|;
name|memoryManagerValue
operator|=
name|getMemoryManager
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|stripeSizeValue
operator|=
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|conf
argument_list|,
name|HIVE_ORC_DEFAULT_STRIPE_SIZE
argument_list|)
expr_stmt|;
name|blockSizeValue
operator|=
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|conf
argument_list|,
name|HIVE_ORC_DEFAULT_BLOCK_SIZE
argument_list|)
expr_stmt|;
name|rowIndexStrideValue
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HIVE_ORC_DEFAULT_ROW_INDEX_STRIDE
argument_list|)
expr_stmt|;
name|bufferSizeValue
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HIVE_ORC_DEFAULT_BUFFER_SIZE
argument_list|)
expr_stmt|;
name|blockPaddingValue
operator|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HIVE_ORC_DEFAULT_BLOCK_PADDING
argument_list|)
expr_stmt|;
name|compressValue
operator|=
name|CompressionKind
operator|.
name|valueOf
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HIVE_ORC_DEFAULT_COMPRESS
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|versionName
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HIVE_ORC_WRITE_FORMAT
argument_list|)
decl_stmt|;
if|if
condition|(
name|versionName
operator|==
literal|null
condition|)
block|{
name|versionValue
operator|=
name|Version
operator|.
name|CURRENT
expr_stmt|;
block|}
else|else
block|{
name|versionValue
operator|=
name|Version
operator|.
name|byName
argument_list|(
name|versionName
argument_list|)
expr_stmt|;
block|}
name|String
name|enString
init|=
name|conf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ORC_ENCODING_STRATEGY
operator|.
name|varname
argument_list|)
decl_stmt|;
if|if
condition|(
name|enString
operator|==
literal|null
condition|)
block|{
name|encodingStrategy
operator|=
name|EncodingStrategy
operator|.
name|SPEED
expr_stmt|;
block|}
else|else
block|{
name|encodingStrategy
operator|=
name|EncodingStrategy
operator|.
name|valueOf
argument_list|(
name|enString
argument_list|)
expr_stmt|;
block|}
name|String
name|compString
init|=
name|conf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ORC_COMPRESSION_STRATEGY
operator|.
name|varname
argument_list|)
decl_stmt|;
if|if
condition|(
name|compString
operator|==
literal|null
condition|)
block|{
name|compressionStrategy
operator|=
name|CompressionStrategy
operator|.
name|SPEED
expr_stmt|;
block|}
else|else
block|{
name|compressionStrategy
operator|=
name|CompressionStrategy
operator|.
name|valueOf
argument_list|(
name|compString
argument_list|)
expr_stmt|;
block|}
name|paddingTolerance
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ORC_BLOCK_PADDING_TOLERANCE
operator|.
name|varname
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ORC_BLOCK_PADDING_TOLERANCE
operator|.
name|defaultFloatVal
argument_list|)
expr_stmt|;
name|bloomFilterFpp
operator|=
name|BloomFilterIO
operator|.
name|DEFAULT_FPP
expr_stmt|;
block|}
comment|/**      * Provide the filesystem for the path, if the client has it available.      * If it is not provided, it will be found from the path.      */
specifier|public
name|WriterOptions
name|fileSystem
parameter_list|(
name|FileSystem
name|value
parameter_list|)
block|{
name|fileSystemValue
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set the stripe size for the file. The writer stores the contents of the      * stripe in memory until this memory limit is reached and the stripe      * is flushed to the HDFS file and the next stripe started.      */
specifier|public
name|WriterOptions
name|stripeSize
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|stripeSizeValue
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set the file system block size for the file. For optimal performance,      * set the block size to be multiple factors of stripe size.      */
specifier|public
name|WriterOptions
name|blockSize
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|blockSizeValue
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set the distance between entries in the row index. The minimum value is      * 1000 to prevent the index from overwhelming the data. If the stride is      * set to 0, no indexes will be included in the file.      */
specifier|public
name|WriterOptions
name|rowIndexStride
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|rowIndexStrideValue
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The size of the memory buffers used for compressing and storing the      * stripe in memory.      */
specifier|public
name|WriterOptions
name|bufferSize
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|bufferSizeValue
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets whether the HDFS blocks are padded to prevent stripes from      * straddling blocks. Padding improves locality and thus the speed of      * reading, but costs space.      */
specifier|public
name|WriterOptions
name|blockPadding
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|blockPaddingValue
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the encoding strategy that is used to encode the data.      */
specifier|public
name|WriterOptions
name|encodingStrategy
parameter_list|(
name|EncodingStrategy
name|strategy
parameter_list|)
block|{
name|encodingStrategy
operator|=
name|strategy
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the tolerance for block padding as a percentage of stripe size.      */
specifier|public
name|WriterOptions
name|paddingTolerance
parameter_list|(
name|float
name|value
parameter_list|)
block|{
name|paddingTolerance
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Comma separated values of column names for which bloom filter is to be created.      */
specifier|public
name|WriterOptions
name|bloomFilterColumns
parameter_list|(
name|String
name|columns
parameter_list|)
block|{
name|bloomFilterColumns
operator|=
name|columns
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Specify the false positive probability for bloom filter.      * @param fpp - false positive probability      * @return      */
specifier|public
name|WriterOptions
name|bloomFilterFpp
parameter_list|(
name|double
name|fpp
parameter_list|)
block|{
name|bloomFilterFpp
operator|=
name|fpp
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the generic compression that is used to compress the data.      */
specifier|public
name|WriterOptions
name|compress
parameter_list|(
name|CompressionKind
name|value
parameter_list|)
block|{
name|compressValue
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * A required option that sets the object inspector for the rows. Used      * to determine the schema for the file.      */
specifier|public
name|WriterOptions
name|inspector
parameter_list|(
name|ObjectInspector
name|value
parameter_list|)
block|{
name|inspectorValue
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the version of the file that will be written.      */
specifier|public
name|WriterOptions
name|version
parameter_list|(
name|Version
name|value
parameter_list|)
block|{
name|versionValue
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Add a listener for when the stripe and file are about to be closed.      * @param callback the object to be called when the stripe is closed      * @return      */
specifier|public
name|WriterOptions
name|callback
parameter_list|(
name|WriterCallback
name|callback
parameter_list|)
block|{
name|this
operator|.
name|callback
operator|=
name|callback
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * A package local option to set the memory manager.      */
name|WriterOptions
name|memory
parameter_list|(
name|MemoryManager
name|value
parameter_list|)
block|{
name|memoryManagerValue
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
comment|/**    * Create a default set of write options that can be modified.    */
specifier|public
specifier|static
name|WriterOptions
name|writerOptions
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|new
name|WriterOptions
argument_list|(
name|conf
argument_list|)
return|;
block|}
comment|/**    * Create an ORC file writer. This is the public interface for creating    * writers going forward and new options will only be added to this method.    * @param path filename to write to    * @param opts the options    * @return a new ORC file writer    * @throws IOException    */
specifier|public
specifier|static
name|Writer
name|createWriter
parameter_list|(
name|Path
name|path
parameter_list|,
name|WriterOptions
name|opts
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|opts
operator|.
name|fileSystemValue
operator|==
literal|null
condition|?
name|path
operator|.
name|getFileSystem
argument_list|(
name|opts
operator|.
name|configuration
argument_list|)
else|:
name|opts
operator|.
name|fileSystemValue
decl_stmt|;
return|return
operator|new
name|WriterImpl
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|opts
operator|.
name|configuration
argument_list|,
name|opts
operator|.
name|inspectorValue
argument_list|,
name|opts
operator|.
name|stripeSizeValue
argument_list|,
name|opts
operator|.
name|compressValue
argument_list|,
name|opts
operator|.
name|bufferSizeValue
argument_list|,
name|opts
operator|.
name|rowIndexStrideValue
argument_list|,
name|opts
operator|.
name|memoryManagerValue
argument_list|,
name|opts
operator|.
name|blockPaddingValue
argument_list|,
name|opts
operator|.
name|versionValue
argument_list|,
name|opts
operator|.
name|callback
argument_list|,
name|opts
operator|.
name|encodingStrategy
argument_list|,
name|opts
operator|.
name|compressionStrategy
argument_list|,
name|opts
operator|.
name|paddingTolerance
argument_list|,
name|opts
operator|.
name|blockSizeValue
argument_list|,
name|opts
operator|.
name|bloomFilterColumns
argument_list|,
name|opts
operator|.
name|bloomFilterFpp
argument_list|)
return|;
block|}
comment|/**    * Create an ORC file writer. This method is provided for API backward    * compatability with Hive 0.11.    * @param fs file system    * @param path filename to write to    * @param inspector the ObjectInspector that inspects the rows    * @param stripeSize the number of bytes in a stripe    * @param compress how to compress the file    * @param bufferSize the number of bytes to compress at once    * @param rowIndexStride the number of rows between row index entries or    *                       0 to suppress all indexes    * @return a new ORC file writer    * @throws IOException    */
specifier|public
specifier|static
name|Writer
name|createWriter
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|ObjectInspector
name|inspector
parameter_list|,
name|long
name|stripeSize
parameter_list|,
name|CompressionKind
name|compress
parameter_list|,
name|int
name|bufferSize
parameter_list|,
name|int
name|rowIndexStride
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createWriter
argument_list|(
name|path
argument_list|,
name|writerOptions
argument_list|(
name|conf
argument_list|)
operator|.
name|fileSystem
argument_list|(
name|fs
argument_list|)
operator|.
name|inspector
argument_list|(
name|inspector
argument_list|)
operator|.
name|stripeSize
argument_list|(
name|stripeSize
argument_list|)
operator|.
name|compress
argument_list|(
name|compress
argument_list|)
operator|.
name|bufferSize
argument_list|(
name|bufferSize
argument_list|)
operator|.
name|rowIndexStride
argument_list|(
name|rowIndexStride
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|MemoryManager
name|memoryManager
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|synchronized
name|MemoryManager
name|getMemoryManager
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|memoryManager
operator|==
literal|null
condition|)
block|{
name|memoryManager
operator|=
operator|new
name|MemoryManager
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
return|return
name|memoryManager
return|;
block|}
block|}
end_class

end_unit

