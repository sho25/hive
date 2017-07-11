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
name|io
operator|.
name|PrintStream
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
name|exec
operator|.
name|FileSinkOperator
operator|.
name|RecordWriter
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
name|WritableComparable
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
name|Reporter
import|;
end_import

begin_comment
comment|/**  * An extension for OutputFormats that want to implement ACID transactions.  * @param<V> the row type of the file  */
end_comment

begin_interface
specifier|public
interface|interface
name|AcidOutputFormat
parameter_list|<
name|K
extends|extends
name|WritableComparable
parameter_list|,
name|V
parameter_list|>
extends|extends
name|HiveOutputFormat
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
comment|/**    * Options to control how the files are written    */
specifier|public
specifier|static
class|class
name|Options
implements|implements
name|Cloneable
block|{
specifier|private
specifier|final
name|Configuration
name|configuration
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|ObjectInspector
name|inspector
decl_stmt|;
specifier|private
name|boolean
name|writingBase
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|writingDeleteDelta
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|isCompressed
init|=
literal|false
decl_stmt|;
specifier|private
name|Properties
name|properties
decl_stmt|;
specifier|private
name|Reporter
name|reporter
decl_stmt|;
specifier|private
name|long
name|minimumTransactionId
decl_stmt|;
specifier|private
name|long
name|maximumTransactionId
decl_stmt|;
specifier|private
name|int
name|bucket
decl_stmt|;
comment|/**      * Based on {@link org.apache.hadoop.hive.ql.metadata.Hive#mvFile(HiveConf, FileSystem, Path, FileSystem, Path, boolean, boolean)}      * _copy_N starts with 1.      */
specifier|private
name|int
name|copyNumber
init|=
literal|0
decl_stmt|;
specifier|private
name|PrintStream
name|dummyStream
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|oldStyle
init|=
literal|false
decl_stmt|;
specifier|private
name|int
name|recIdCol
init|=
operator|-
literal|1
decl_stmt|;
comment|// Column the record identifier is in, -1 indicates no record id
comment|//unique within a transaction
specifier|private
name|int
name|statementId
init|=
literal|0
decl_stmt|;
specifier|private
name|Path
name|finalDestination
decl_stmt|;
comment|/**      * Create the options object.      * @param conf Use the given configuration      */
specifier|public
name|Options
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|configuration
operator|=
name|conf
expr_stmt|;
block|}
comment|/**      * shallow clone      */
annotation|@
name|Override
specifier|public
name|Options
name|clone
parameter_list|()
block|{
try|try
block|{
return|return
operator|(
name|Options
operator|)
name|super
operator|.
name|clone
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|CloneNotSupportedException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"clone() not properly implemented: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
comment|/**      * Use the given ObjectInspector for each record written.      * @param inspector the inspector to use.      * @return this      */
specifier|public
name|Options
name|inspector
parameter_list|(
name|ObjectInspector
name|inspector
parameter_list|)
block|{
name|this
operator|.
name|inspector
operator|=
name|inspector
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Is this writing a base directory? Should only be used by the compactor,      * or when implementing insert overwrite.      * @param val is this a base file?      * @return this      */
specifier|public
name|Options
name|writingBase
parameter_list|(
name|boolean
name|val
parameter_list|)
block|{
name|this
operator|.
name|writingBase
operator|=
name|val
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Is this writing a delete delta directory?      * @param val is this a delete delta file?      * @return this      */
specifier|public
name|Options
name|writingDeleteDelta
parameter_list|(
name|boolean
name|val
parameter_list|)
block|{
name|this
operator|.
name|writingDeleteDelta
operator|=
name|val
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Provide a file system to the writer. Otherwise, the filesystem for the      * path will be used.      * @param fs the file system that corresponds to the the path      * @return this      */
specifier|public
name|Options
name|filesystem
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
block|{
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the output be compressed?      * @param isCompressed is the output compressed?      * @return this      */
specifier|public
name|Options
name|isCompressed
parameter_list|(
name|boolean
name|isCompressed
parameter_list|)
block|{
name|this
operator|.
name|isCompressed
operator|=
name|isCompressed
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Provide the table properties for the table.      * @param properties the table's properties      * @return this      */
specifier|public
name|Options
name|tableProperties
parameter_list|(
name|Properties
name|properties
parameter_list|)
block|{
name|this
operator|.
name|properties
operator|=
name|properties
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Provide the MapReduce reporter.      * @param reporter the reporter object      * @return this      */
specifier|public
name|Options
name|reporter
parameter_list|(
name|Reporter
name|reporter
parameter_list|)
block|{
name|this
operator|.
name|reporter
operator|=
name|reporter
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The minimum transaction id that is included in this file.      * @param min minimum transaction id      * @return this      */
specifier|public
name|Options
name|minimumTransactionId
parameter_list|(
name|long
name|min
parameter_list|)
block|{
name|this
operator|.
name|minimumTransactionId
operator|=
name|min
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The maximum transaction id that is included in this file.      * @param max maximum transaction id      * @return this      */
specifier|public
name|Options
name|maximumTransactionId
parameter_list|(
name|long
name|max
parameter_list|)
block|{
name|this
operator|.
name|maximumTransactionId
operator|=
name|max
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The bucket that is included in this file.      * @param bucket the bucket number      * @return this      */
specifier|public
name|Options
name|bucket
parameter_list|(
name|int
name|bucket
parameter_list|)
block|{
name|this
operator|.
name|bucket
operator|=
name|bucket
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Multiple inserts into legacy (pre-acid) tables can generate multiple copies of each bucket      * file.      * @see org.apache.hadoop.hive.ql.exec.Utilities#COPY_KEYWORD      * @param copyNumber the number of the copy (> 0)      * @return this      */
specifier|public
name|Options
name|copyNumber
parameter_list|(
name|int
name|copyNumber
parameter_list|)
block|{
name|this
operator|.
name|copyNumber
operator|=
name|copyNumber
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Whether it should use the old style (0000000_0) filenames.      * @param value should use the old style names      * @return this      */
name|Options
name|setOldStyle
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|oldStyle
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Which column the row id field is in.      * @param recIdCol      * @return this      */
specifier|public
name|Options
name|recordIdColumn
parameter_list|(
name|int
name|recIdCol
parameter_list|)
block|{
name|this
operator|.
name|recIdCol
operator|=
name|recIdCol
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Temporary switch while we are in development that replaces the      * implementation with a dummy one that just prints to stream.      * @param stream the stream to print to      * @return this      */
specifier|public
name|Options
name|useDummy
parameter_list|(
name|PrintStream
name|stream
parameter_list|)
block|{
name|this
operator|.
name|dummyStream
operator|=
name|stream
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @since 1.3.0      * This can be set to -1 to make the system generate old style (delta_xxxx_yyyy) file names.      * This is primarily needed for testing to make sure 1.3 code can still read files created      * by older code.  Also used by Comactor.      */
specifier|public
name|Options
name|statementId
parameter_list|(
name|int
name|id
parameter_list|)
block|{
if|if
condition|(
name|id
operator|>=
name|AcidUtils
operator|.
name|MAX_STATEMENTS_PER_TXN
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Too many statements for transactionId: "
operator|+
name|maximumTransactionId
argument_list|)
throw|;
block|}
if|if
condition|(
name|id
operator|<
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Illegal statementId value: "
operator|+
name|id
argument_list|)
throw|;
block|}
name|this
operator|.
name|statementId
operator|=
name|id
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * @param p where the data for this operation will eventually end up;      *          basically table or partition directory in FS      */
specifier|public
name|Options
name|finalDestination
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
name|this
operator|.
name|finalDestination
operator|=
name|p
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|configuration
return|;
block|}
specifier|public
name|FileSystem
name|getFilesystem
parameter_list|()
block|{
return|return
name|fs
return|;
block|}
specifier|public
name|ObjectInspector
name|getInspector
parameter_list|()
block|{
return|return
name|inspector
return|;
block|}
specifier|public
name|boolean
name|isCompressed
parameter_list|()
block|{
return|return
name|isCompressed
return|;
block|}
specifier|public
name|Properties
name|getTableProperties
parameter_list|()
block|{
return|return
name|properties
return|;
block|}
specifier|public
name|Reporter
name|getReporter
parameter_list|()
block|{
return|return
name|reporter
return|;
block|}
specifier|public
name|long
name|getMinimumTransactionId
parameter_list|()
block|{
return|return
name|minimumTransactionId
return|;
block|}
specifier|public
name|long
name|getMaximumTransactionId
parameter_list|()
block|{
return|return
name|maximumTransactionId
return|;
block|}
specifier|public
name|boolean
name|isWritingBase
parameter_list|()
block|{
return|return
name|writingBase
return|;
block|}
specifier|public
name|boolean
name|isWritingDeleteDelta
parameter_list|()
block|{
return|return
name|writingDeleteDelta
return|;
block|}
specifier|public
name|int
name|getBucket
parameter_list|()
block|{
return|return
name|bucket
return|;
block|}
specifier|public
name|int
name|getRecordIdColumn
parameter_list|()
block|{
return|return
name|recIdCol
return|;
block|}
specifier|public
name|PrintStream
name|getDummyStream
parameter_list|()
block|{
return|return
name|dummyStream
return|;
block|}
name|boolean
name|getOldStyle
parameter_list|()
block|{
return|return
name|oldStyle
return|;
block|}
specifier|public
name|int
name|getStatementId
parameter_list|()
block|{
return|return
name|statementId
return|;
block|}
specifier|public
name|int
name|getCopyNumber
parameter_list|()
block|{
return|return
name|copyNumber
return|;
block|}
specifier|public
name|Path
name|getFinalDestination
parameter_list|()
block|{
return|return
name|finalDestination
return|;
block|}
block|}
comment|/**    * Create a RecordUpdater for inserting, updating, or deleting records.    * @param path the partition directory name    * @param options the options for the writer    * @return the RecordUpdater for the output file    */
specifier|public
name|RecordUpdater
name|getRecordUpdater
parameter_list|(
name|Path
name|path
parameter_list|,
name|Options
name|options
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Create a raw writer for ACID events.    * This is only intended for the compactor.    * @param path the root directory    * @param options options for writing the file    * @return a record writer    * @throws IOException    */
specifier|public
name|RecordWriter
name|getRawRecordWriter
parameter_list|(
name|Path
name|path
parameter_list|,
name|Options
name|options
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

