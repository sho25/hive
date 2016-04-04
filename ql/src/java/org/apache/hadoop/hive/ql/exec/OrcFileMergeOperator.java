begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|IOException
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
name|lang
operator|.
name|exception
operator|.
name|ExceptionUtils
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
name|CompilationOpContext
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
name|Writer
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
name|Path
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|CompressionKind
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
name|OrcFileKeyWrapper
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
name|OrcFileValueWrapper
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
name|OrcFileMergeDesc
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
name|shims
operator|.
name|CombineHiveKey
import|;
end_import

begin_comment
comment|/**  * Fast file merge operator for ORC files.  */
end_comment

begin_class
specifier|public
class|class
name|OrcFileMergeOperator
extends|extends
name|AbstractFileMergeOperator
argument_list|<
name|OrcFileMergeDesc
argument_list|>
block|{
specifier|public
specifier|final
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"OrcFileMergeOperator"
argument_list|)
decl_stmt|;
comment|// These parameters must match for all orc files involved in merging. If it
comment|// does not merge, the file will be put into incompatible file set and will
comment|// not be merged.
name|CompressionKind
name|compression
init|=
literal|null
decl_stmt|;
name|int
name|compressBuffSize
init|=
literal|0
decl_stmt|;
name|OrcFile
operator|.
name|Version
name|version
decl_stmt|;
name|int
name|columnCount
init|=
literal|0
decl_stmt|;
name|int
name|rowIndexStride
init|=
literal|0
decl_stmt|;
name|Writer
name|outWriter
decl_stmt|;
name|Path
name|prevPath
decl_stmt|;
specifier|private
name|Reader
name|reader
decl_stmt|;
specifier|private
name|FSDataInputStream
name|fdis
decl_stmt|;
comment|/** Kryo ctor. */
specifier|protected
name|OrcFileMergeOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|OrcFileMergeOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
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
name|Object
index|[]
name|keyValue
init|=
operator|(
name|Object
index|[]
operator|)
name|row
decl_stmt|;
name|processKeyValuePairs
argument_list|(
name|keyValue
index|[
literal|0
index|]
argument_list|,
name|keyValue
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|processKeyValuePairs
parameter_list|(
name|Object
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
throws|throws
name|HiveException
block|{
name|String
name|filePath
init|=
literal|""
decl_stmt|;
try|try
block|{
name|OrcFileValueWrapper
name|v
decl_stmt|;
name|OrcFileKeyWrapper
name|k
decl_stmt|;
if|if
condition|(
name|key
operator|instanceof
name|CombineHiveKey
condition|)
block|{
name|k
operator|=
call|(
name|OrcFileKeyWrapper
call|)
argument_list|(
operator|(
name|CombineHiveKey
operator|)
name|key
argument_list|)
operator|.
name|getKey
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|k
operator|=
operator|(
name|OrcFileKeyWrapper
operator|)
name|key
expr_stmt|;
block|}
comment|// skip incompatible file, files that are missing stripe statistics are set to incompatible
if|if
condition|(
name|k
operator|.
name|isIncompatFile
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Incompatible ORC file merge! Stripe statistics is missing. "
operator|+
name|k
operator|.
name|getInputPath
argument_list|()
argument_list|)
expr_stmt|;
name|incompatFileSet
operator|.
name|add
argument_list|(
name|k
operator|.
name|getInputPath
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|filePath
operator|=
name|k
operator|.
name|getInputPath
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
expr_stmt|;
name|fixTmpPath
argument_list|(
name|k
operator|.
name|getInputPath
argument_list|()
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
name|v
operator|=
operator|(
name|OrcFileValueWrapper
operator|)
name|value
expr_stmt|;
if|if
condition|(
name|prevPath
operator|==
literal|null
condition|)
block|{
name|prevPath
operator|=
name|k
operator|.
name|getInputPath
argument_list|()
expr_stmt|;
name|reader
operator|=
name|OrcFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|k
operator|.
name|getInputPath
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"ORC merge file input path: "
operator|+
name|k
operator|.
name|getInputPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// store the orc configuration from the first file. All other files should
comment|// match this configuration before merging else will not be merged
if|if
condition|(
name|outWriter
operator|==
literal|null
condition|)
block|{
name|compression
operator|=
name|k
operator|.
name|getCompression
argument_list|()
expr_stmt|;
name|compressBuffSize
operator|=
name|k
operator|.
name|getCompressBufferSize
argument_list|()
expr_stmt|;
name|version
operator|=
name|k
operator|.
name|getVersion
argument_list|()
expr_stmt|;
name|columnCount
operator|=
name|k
operator|.
name|getTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getSubtypesCount
argument_list|()
expr_stmt|;
name|rowIndexStride
operator|=
name|k
operator|.
name|getRowIndexStride
argument_list|()
expr_stmt|;
name|OrcFile
operator|.
name|WriterOptions
name|options
init|=
name|OrcFile
operator|.
name|writerOptions
argument_list|(
name|jc
argument_list|)
operator|.
name|compress
argument_list|(
name|compression
argument_list|)
operator|.
name|version
argument_list|(
name|version
argument_list|)
operator|.
name|rowIndexStride
argument_list|(
name|rowIndexStride
argument_list|)
operator|.
name|inspector
argument_list|(
name|reader
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
decl_stmt|;
comment|// compression buffer size should only be set if compression is enabled
if|if
condition|(
name|compression
operator|!=
name|CompressionKind
operator|.
name|NONE
condition|)
block|{
comment|// enforce is required to retain the buffer sizes of old files instead of orc writer
comment|// inferring the optimal buffer size
name|options
operator|.
name|bufferSize
argument_list|(
name|compressBuffSize
argument_list|)
operator|.
name|enforceBufferSize
argument_list|()
expr_stmt|;
block|}
name|outWriter
operator|=
name|OrcFile
operator|.
name|createWriter
argument_list|(
name|outPath
argument_list|,
name|options
argument_list|)
expr_stmt|;
if|if
condition|(
name|isLogDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"ORC merge file output path: "
operator|+
name|outPath
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|checkCompatibility
argument_list|(
name|k
argument_list|)
condition|)
block|{
name|incompatFileSet
operator|.
name|add
argument_list|(
name|k
operator|.
name|getInputPath
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// next file in the path
if|if
condition|(
operator|!
name|k
operator|.
name|getInputPath
argument_list|()
operator|.
name|equals
argument_list|(
name|prevPath
argument_list|)
condition|)
block|{
name|reader
operator|=
name|OrcFile
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|k
operator|.
name|getInputPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// initialize buffer to read the entire stripe
name|byte
index|[]
name|buffer
init|=
operator|new
name|byte
index|[
operator|(
name|int
operator|)
name|v
operator|.
name|getStripeInformation
argument_list|()
operator|.
name|getLength
argument_list|()
index|]
decl_stmt|;
name|fdis
operator|=
name|fs
operator|.
name|open
argument_list|(
name|k
operator|.
name|getInputPath
argument_list|()
argument_list|)
expr_stmt|;
name|fdis
operator|.
name|readFully
argument_list|(
name|v
operator|.
name|getStripeInformation
argument_list|()
operator|.
name|getOffset
argument_list|()
argument_list|,
name|buffer
argument_list|,
literal|0
argument_list|,
operator|(
name|int
operator|)
name|v
operator|.
name|getStripeInformation
argument_list|()
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
comment|// append the stripe buffer to the new ORC file
name|outWriter
operator|.
name|appendStripe
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|,
name|v
operator|.
name|getStripeInformation
argument_list|()
argument_list|,
name|v
operator|.
name|getStripeStatistics
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Merged stripe from file "
operator|+
name|k
operator|.
name|getInputPath
argument_list|()
operator|+
literal|" [ offset : "
operator|+
name|v
operator|.
name|getStripeInformation
argument_list|()
operator|.
name|getOffset
argument_list|()
operator|+
literal|" length: "
operator|+
name|v
operator|.
name|getStripeInformation
argument_list|()
operator|.
name|getLength
argument_list|()
operator|+
literal|" row: "
operator|+
name|v
operator|.
name|getStripeStatistics
argument_list|()
operator|.
name|getColStats
argument_list|(
literal|0
argument_list|)
operator|.
name|getNumberOfValues
argument_list|()
operator|+
literal|" ]"
argument_list|)
expr_stmt|;
block|}
comment|// add user metadata to footer in case of any
if|if
condition|(
name|v
operator|.
name|isLastStripeInFile
argument_list|()
condition|)
block|{
name|outWriter
operator|.
name|appendUserMetadata
argument_list|(
name|v
operator|.
name|getUserMetadata
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|this
operator|.
name|exception
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Closing operator..Exception: "
operator|+
name|ExceptionUtils
operator|.
name|getStackTrace
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|exception
condition|)
block|{
name|closeOp
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fdis
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|fdis
operator|.
name|close
argument_list|()
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
name|String
operator|.
name|format
argument_list|(
literal|"Unable to close file %s"
argument_list|,
name|filePath
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|fdis
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|boolean
name|checkCompatibility
parameter_list|(
name|OrcFileKeyWrapper
name|k
parameter_list|)
block|{
comment|// check compatibility with subsequent files
if|if
condition|(
operator|(
name|k
operator|.
name|getTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getSubtypesCount
argument_list|()
operator|!=
name|columnCount
operator|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Incompatible ORC file merge! Column counts mismatch for "
operator|+
name|k
operator|.
name|getInputPath
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|k
operator|.
name|getCompression
argument_list|()
operator|.
name|equals
argument_list|(
name|compression
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Incompatible ORC file merge! Compression codec mismatch for "
operator|+
name|k
operator|.
name|getInputPath
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|k
operator|.
name|getCompressBufferSize
argument_list|()
operator|!=
name|compressBuffSize
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Incompatible ORC file merge! Compression buffer size mismatch for "
operator|+
name|k
operator|.
name|getInputPath
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|k
operator|.
name|getVersion
argument_list|()
operator|.
name|equals
argument_list|(
name|version
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Incompatible ORC file merge! Version mismatch for "
operator|+
name|k
operator|.
name|getInputPath
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|k
operator|.
name|getRowIndexStride
argument_list|()
operator|!=
name|rowIndexStride
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Incompatible ORC file merge! Row index stride mismatch for "
operator|+
name|k
operator|.
name|getInputPath
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
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
name|ORCFILEMERGE
return|;
block|}
comment|/**    * @return the name of the operator    */
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|getOperatorName
argument_list|()
return|;
block|}
specifier|static
specifier|public
name|String
name|getOperatorName
parameter_list|()
block|{
return|return
literal|"OFM"
return|;
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
if|if
condition|(
name|fdis
operator|!=
literal|null
condition|)
block|{
name|fdis
operator|.
name|close
argument_list|()
expr_stmt|;
name|fdis
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|outWriter
operator|!=
literal|null
condition|)
block|{
name|outWriter
operator|.
name|close
argument_list|()
expr_stmt|;
name|outWriter
operator|=
literal|null
expr_stmt|;
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
literal|"Unable to close OrcFileMergeOperator"
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// When there are no exceptions, this has to be called always to make sure incompatible files
comment|// are moved properly to the destination path
name|super
operator|.
name|closeOp
argument_list|(
name|abort
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

