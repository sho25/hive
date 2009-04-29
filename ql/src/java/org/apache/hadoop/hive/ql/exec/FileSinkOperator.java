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
name|*
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
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|*
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
name|*
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
name|*
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
name|fileSinkDesc
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
name|tableDesc
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
name|FilterOperator
operator|.
name|Counter
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
name|HiveOutputFormat
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
name|HiveFileFormatUtils
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
name|IgnoreKeyTextOutputFormat
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
name|serde
operator|.
name|Constants
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
name|serde2
operator|.
name|Serializer
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
comment|/**  * File Sink operator implementation  **/
end_comment

begin_class
specifier|public
class|class
name|FileSinkOperator
extends|extends
name|TerminalOperator
argument_list|<
name|fileSinkDesc
argument_list|>
implements|implements
name|Serializable
block|{
specifier|public
specifier|static
interface|interface
name|RecordWriter
block|{
specifier|public
name|void
name|write
parameter_list|(
name|Writable
name|w
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|transient
specifier|protected
name|RecordWriter
name|outWriter
decl_stmt|;
specifier|transient
specifier|protected
name|FileSystem
name|fs
decl_stmt|;
specifier|transient
specifier|protected
name|Path
name|outPath
decl_stmt|;
specifier|transient
specifier|protected
name|Path
name|finalPath
decl_stmt|;
specifier|transient
specifier|protected
name|Serializer
name|serializer
decl_stmt|;
specifier|transient
specifier|protected
name|BytesWritable
name|commonKey
init|=
operator|new
name|BytesWritable
argument_list|()
decl_stmt|;
specifier|transient
specifier|protected
name|TableIdEnum
name|tabIdEnum
init|=
literal|null
decl_stmt|;
specifier|transient
specifier|private
name|LongWritable
name|row_count
decl_stmt|;
specifier|public
specifier|static
enum|enum
name|TableIdEnum
block|{
name|TABLE_ID_1_ROWCOUNT
block|,
name|TABLE_ID_2_ROWCOUNT
block|,
name|TABLE_ID_3_ROWCOUNT
block|,
name|TABLE_ID_4_ROWCOUNT
block|,
name|TABLE_ID_5_ROWCOUNT
block|,
name|TABLE_ID_6_ROWCOUNT
block|,
name|TABLE_ID_7_ROWCOUNT
block|,
name|TABLE_ID_8_ROWCOUNT
block|,
name|TABLE_ID_9_ROWCOUNT
block|,
name|TABLE_ID_10_ROWCOUNT
block|,
name|TABLE_ID_11_ROWCOUNT
block|,
name|TABLE_ID_12_ROWCOUNT
block|,
name|TABLE_ID_13_ROWCOUNT
block|,
name|TABLE_ID_14_ROWCOUNT
block|,
name|TABLE_ID_15_ROWCOUNT
block|;    }
specifier|transient
specifier|protected
name|boolean
name|autoDelete
init|=
literal|false
decl_stmt|;
specifier|private
name|void
name|commit
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|outPath
argument_list|,
name|finalPath
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to rename output to: "
operator|+
name|finalPath
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Committed to output file: "
operator|+
name|finalPath
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|!
name|abort
condition|)
block|{
if|if
condition|(
name|outWriter
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|outWriter
operator|.
name|close
argument_list|(
name|abort
argument_list|)
expr_stmt|;
name|commit
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Don't throw an exception, just ignore and return
return|return;
block|}
block|}
block|}
else|else
block|{
try|try
block|{
name|outWriter
operator|.
name|close
argument_list|(
name|abort
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|autoDelete
condition|)
name|fs
operator|.
name|delete
argument_list|(
name|outPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|hconf
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
try|try
block|{
name|serializer
operator|=
operator|(
name|Serializer
operator|)
name|conf
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getDeserializerClass
argument_list|()
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|serializer
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
name|conf
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
name|JobConf
name|jc
decl_stmt|;
if|if
condition|(
name|hconf
operator|instanceof
name|JobConf
condition|)
block|{
name|jc
operator|=
operator|(
name|JobConf
operator|)
name|hconf
expr_stmt|;
block|}
else|else
block|{
comment|// test code path
name|jc
operator|=
operator|new
name|JobConf
argument_list|(
name|hconf
argument_list|,
name|ExecDriver
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
name|int
name|id
init|=
name|conf
operator|.
name|getDestTableId
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|id
operator|!=
literal|0
operator|)
operator|&&
operator|(
name|id
operator|<=
name|TableIdEnum
operator|.
name|values
argument_list|()
operator|.
name|length
operator|)
condition|)
block|{
name|String
name|enumName
init|=
literal|"TABLE_ID_"
operator|+
name|String
operator|.
name|valueOf
argument_list|(
name|id
argument_list|)
operator|+
literal|"_ROWCOUNT"
decl_stmt|;
name|tabIdEnum
operator|=
name|TableIdEnum
operator|.
name|valueOf
argument_list|(
name|enumName
argument_list|)
expr_stmt|;
name|row_count
operator|=
operator|new
name|LongWritable
argument_list|()
expr_stmt|;
name|statsMap
operator|.
name|put
argument_list|(
name|tabIdEnum
argument_list|,
name|row_count
argument_list|)
expr_stmt|;
block|}
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|finalPath
operator|=
operator|new
name|Path
argument_list|(
name|Utilities
operator|.
name|toTempPath
argument_list|(
name|conf
operator|.
name|getDirName
argument_list|()
argument_list|)
argument_list|,
name|Utilities
operator|.
name|getTaskId
argument_list|(
name|hconf
argument_list|)
argument_list|)
expr_stmt|;
name|outPath
operator|=
operator|new
name|Path
argument_list|(
name|Utilities
operator|.
name|toTempPath
argument_list|(
name|conf
operator|.
name|getDirName
argument_list|()
argument_list|)
argument_list|,
name|Utilities
operator|.
name|toTempPath
argument_list|(
name|Utilities
operator|.
name|getTaskId
argument_list|(
name|hconf
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Writing to temp file: "
operator|+
name|outPath
argument_list|)
expr_stmt|;
name|HiveOutputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|hiveOutputFormat
init|=
name|conf
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getOutputFileFormatClass
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|outputClass
init|=
name|serializer
operator|.
name|getSerializedClass
argument_list|()
decl_stmt|;
name|boolean
name|isCompressed
init|=
name|conf
operator|.
name|getCompressed
argument_list|()
decl_stmt|;
comment|// The reason to keep these instead of using
comment|// OutputFormat.getRecordWriter() is that
comment|// getRecordWriter does not give us enough control over the file name that
comment|// we create.
name|Path
name|parent
init|=
name|Utilities
operator|.
name|toTempPath
argument_list|(
name|conf
operator|.
name|getDirName
argument_list|()
argument_list|)
decl_stmt|;
name|finalPath
operator|=
name|HiveFileFormatUtils
operator|.
name|getOutputFormatFinalPath
argument_list|(
name|parent
argument_list|,
name|jc
argument_list|,
name|hiveOutputFormat
argument_list|,
name|isCompressed
argument_list|,
name|finalPath
argument_list|)
expr_stmt|;
name|tableDesc
name|tableInfo
init|=
name|conf
operator|.
name|getTableInfo
argument_list|()
decl_stmt|;
name|this
operator|.
name|outWriter
operator|=
name|getRecordWriter
argument_list|(
name|jc
argument_list|,
name|hiveOutputFormat
argument_list|,
name|outputClass
argument_list|,
name|isCompressed
argument_list|,
name|tableInfo
operator|.
name|getProperties
argument_list|()
argument_list|,
name|outPath
argument_list|)
expr_stmt|;
comment|// in recent hadoop versions, use deleteOnExit to clean tmp files.
try|try
block|{
name|Method
name|deleteOnExit
init|=
name|FileSystem
operator|.
name|class
operator|.
name|getDeclaredMethod
argument_list|(
literal|"deleteOnExit"
argument_list|,
operator|new
name|Class
index|[]
block|{
name|Path
operator|.
name|class
block|}
argument_list|)
decl_stmt|;
name|deleteOnExit
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|deleteOnExit
operator|.
name|invoke
argument_list|(
name|fs
argument_list|,
name|outPath
argument_list|)
expr_stmt|;
name|autoDelete
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{}
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
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|RecordWriter
name|getRecordWriter
parameter_list|(
name|JobConf
name|jc
parameter_list|,
name|HiveOutputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|hiveOutputFormat
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|valueClass
parameter_list|,
name|boolean
name|isCompressed
parameter_list|,
name|Properties
name|tableProp
parameter_list|,
name|Path
name|outPath
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveException
block|{
if|if
condition|(
name|hiveOutputFormat
operator|!=
literal|null
condition|)
block|{
return|return
name|hiveOutputFormat
operator|.
name|getHiveRecordWriter
argument_list|(
name|jc
argument_list|,
name|outPath
argument_list|,
name|valueClass
argument_list|,
name|isCompressed
argument_list|,
name|tableProp
argument_list|,
literal|null
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
name|Writable
name|recordValue
decl_stmt|;
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
if|if
condition|(
name|reporter
operator|!=
literal|null
condition|)
name|reporter
operator|.
name|progress
argument_list|()
expr_stmt|;
comment|// user SerDe to serialize r, and write it out
name|recordValue
operator|=
name|serializer
operator|.
name|serialize
argument_list|(
name|row
argument_list|,
name|rowInspector
argument_list|)
expr_stmt|;
if|if
condition|(
name|row_count
operator|!=
literal|null
condition|)
block|{
name|row_count
operator|.
name|set
argument_list|(
name|row_count
operator|.
name|get
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|outWriter
operator|.
name|write
argument_list|(
name|recordValue
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
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|SerDeException
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
comment|/**    * @return the name of the operator    */
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
operator|new
name|String
argument_list|(
literal|"FS"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|jobClose
parameter_list|(
name|Configuration
name|hconf
parameter_list|,
name|boolean
name|success
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
if|if
condition|(
name|conf
operator|!=
literal|null
condition|)
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|Path
name|tmpPath
init|=
name|Utilities
operator|.
name|toTempPath
argument_list|(
name|conf
operator|.
name|getDirName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|finalPath
init|=
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|getDirName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|success
condition|)
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|tmpPath
argument_list|)
condition|)
block|{
comment|// Step1: rename tmp output folder to final path. After this point,
comment|// updates from speculative tasks still writing to tmpPath will not
comment|// appear in finalPath
name|LOG
operator|.
name|info
argument_list|(
literal|"Moving tmp dir: "
operator|+
name|tmpPath
operator|+
literal|" to: "
operator|+
name|finalPath
argument_list|)
expr_stmt|;
name|renameOrMoveFiles
argument_list|(
name|fs
argument_list|,
name|tmpPath
argument_list|,
name|finalPath
argument_list|)
expr_stmt|;
comment|// Step2: Clean any temp files from finalPath
name|Utilities
operator|.
name|removeTempFiles
argument_list|(
name|fs
argument_list|,
name|finalPath
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|fs
operator|.
name|delete
argument_list|(
name|tmpPath
argument_list|)
expr_stmt|;
block|}
block|}
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
name|e
argument_list|)
throw|;
block|}
name|super
operator|.
name|jobClose
argument_list|(
name|hconf
argument_list|,
name|success
argument_list|)
expr_stmt|;
block|}
comment|/**    * Rename src to dst, or in the case dst already exists, move files in src     * to dst.  If there is an existing file with the same name, the new file's     * name will be appended with "_1", "_2", etc.    * @param fs the FileSystem where src and dst are on.      * @param src the src directory    * @param dst the target directory    * @throws IOException     */
specifier|static
specifier|public
name|void
name|renameOrMoveFiles
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|src
parameter_list|,
name|Path
name|dst
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|dst
argument_list|)
condition|)
block|{
name|fs
operator|.
name|rename
argument_list|(
name|src
argument_list|,
name|dst
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// move file by file
name|FileStatus
index|[]
name|files
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|src
argument_list|)
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
name|files
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Path
name|srcFilePath
init|=
name|files
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|String
name|fileName
init|=
name|srcFilePath
operator|.
name|getName
argument_list|()
decl_stmt|;
name|Path
name|dstFilePath
init|=
operator|new
name|Path
argument_list|(
name|dst
argument_list|,
name|fileName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|dstFilePath
argument_list|)
condition|)
block|{
name|int
name|suffix
init|=
literal|0
decl_stmt|;
do|do
block|{
name|suffix
operator|++
expr_stmt|;
name|dstFilePath
operator|=
operator|new
name|Path
argument_list|(
name|dst
argument_list|,
name|fileName
operator|+
literal|"_"
operator|+
name|suffix
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|dstFilePath
argument_list|)
condition|)
do|;
block|}
name|fs
operator|.
name|rename
argument_list|(
name|srcFilePath
argument_list|,
name|dstFilePath
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

