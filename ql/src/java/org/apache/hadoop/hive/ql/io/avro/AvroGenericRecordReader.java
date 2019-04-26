begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|avro
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
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_import
import|import
name|java
operator|.
name|rmi
operator|.
name|server
operator|.
name|UID
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|DateTimeException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|ZoneId
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
name|Properties
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Schema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|file
operator|.
name|DataFileReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|generic
operator|.
name|GenericData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|generic
operator|.
name|GenericDatumReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|generic
operator|.
name|GenericRecord
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|mapred
operator|.
name|FsInput
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
name|lang3
operator|.
name|StringUtils
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
name|avro
operator|.
name|AvroSerDe
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
name|plan
operator|.
name|MapWork
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
name|PartitionDesc
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
name|avro
operator|.
name|AvroGenericRecordWritable
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
name|avro
operator|.
name|AvroSerdeException
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
name|avro
operator|.
name|AvroSerdeUtils
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
name|avro
operator|.
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
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
name|NullWritable
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
name|JobConfigurable
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
name|mapred
operator|.
name|Reporter
import|;
end_import

begin_comment
comment|/**  * RecordReader optimized against Avro GenericRecords that returns to record  * as the value of the k-v pair, as Hive requires.  */
end_comment

begin_class
specifier|public
class|class
name|AvroGenericRecordReader
implements|implements
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|AvroGenericRecordWritable
argument_list|>
implements|,
name|JobConfigurable
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|AvroGenericRecordReader
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
specifier|private
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|file
operator|.
name|FileReader
argument_list|<
name|GenericRecord
argument_list|>
name|reader
decl_stmt|;
specifier|final
specifier|private
name|long
name|start
decl_stmt|;
specifier|final
specifier|private
name|long
name|stop
decl_stmt|;
specifier|private
name|ZoneId
name|writerTimezone
decl_stmt|;
specifier|protected
name|JobConf
name|jobConf
decl_stmt|;
specifier|final
specifier|private
name|boolean
name|isEmptyInput
decl_stmt|;
comment|/**    * A unique ID for each record reader.    */
specifier|final
specifier|private
name|UID
name|recordReaderID
decl_stmt|;
specifier|public
name|AvroGenericRecordReader
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|FileSplit
name|split
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|jobConf
operator|=
name|job
expr_stmt|;
name|Schema
name|latest
decl_stmt|;
try|try
block|{
name|latest
operator|=
name|getSchema
argument_list|(
name|job
argument_list|,
name|split
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AvroSerdeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|GenericDatumReader
argument_list|<
name|GenericRecord
argument_list|>
name|gdr
init|=
operator|new
name|GenericDatumReader
argument_list|<
name|GenericRecord
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|latest
operator|!=
literal|null
condition|)
block|{
name|gdr
operator|.
name|setExpected
argument_list|(
name|latest
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|split
operator|.
name|getLength
argument_list|()
operator|==
literal|0
condition|)
block|{
name|this
operator|.
name|isEmptyInput
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|start
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|reader
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|isEmptyInput
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|reader
operator|=
operator|new
name|DataFileReader
argument_list|<
name|GenericRecord
argument_list|>
argument_list|(
operator|new
name|FsInput
argument_list|(
name|split
operator|.
name|getPath
argument_list|()
argument_list|,
name|job
argument_list|)
argument_list|,
name|gdr
argument_list|)
expr_stmt|;
name|this
operator|.
name|reader
operator|.
name|sync
argument_list|(
name|split
operator|.
name|getStart
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|start
operator|=
name|reader
operator|.
name|tell
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|stop
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
name|recordReaderID
operator|=
operator|new
name|UID
argument_list|()
expr_stmt|;
name|this
operator|.
name|writerTimezone
operator|=
name|extractWriterTimezoneFromMetadata
argument_list|(
name|job
argument_list|,
name|split
argument_list|,
name|gdr
argument_list|)
expr_stmt|;
block|}
comment|/**    * Attempt to retrieve the reader schema.  We have a couple opportunities    * to provide this, depending on whether or not we're just selecting data    * or running with a MR job.    * @return  Reader schema for the Avro object, or null if it has not been provided.    * @throws AvroSerdeException    */
specifier|private
name|Schema
name|getSchema
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|FileSplit
name|split
parameter_list|)
throws|throws
name|AvroSerdeException
throws|,
name|IOException
block|{
comment|// Inside of a MR job, we can pull out the actual properties
if|if
condition|(
name|AvroSerdeUtils
operator|.
name|insideMRJob
argument_list|(
name|job
argument_list|)
condition|)
block|{
name|MapWork
name|mapWork
init|=
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|job
argument_list|)
decl_stmt|;
comment|// Iterate over the Path -> Partition descriptions to find the partition
comment|// that matches our input split.
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
name|pathsAndParts
range|:
name|mapWork
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Path
name|partitionPath
init|=
name|pathsAndParts
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|pathIsInPartition
argument_list|(
name|split
operator|.
name|getPath
argument_list|()
argument_list|,
name|partitionPath
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Matching partition {} with input split {}"
argument_list|,
name|partitionPath
argument_list|,
name|split
argument_list|)
expr_stmt|;
name|Properties
name|props
init|=
name|pathsAndParts
operator|.
name|getValue
argument_list|()
operator|.
name|getProperties
argument_list|()
decl_stmt|;
if|if
condition|(
name|props
operator|.
name|containsKey
argument_list|(
name|AvroTableProperties
operator|.
name|SCHEMA_LITERAL
operator|.
name|getPropName
argument_list|()
argument_list|)
operator|||
name|props
operator|.
name|containsKey
argument_list|(
name|AvroTableProperties
operator|.
name|SCHEMA_URL
operator|.
name|getPropName
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|AvroSerdeUtils
operator|.
name|determineSchemaOrThrowException
argument_list|(
name|job
argument_list|,
name|props
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
comment|// If it's not in this property, it won't be in any others
block|}
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Unable to match filesplit {} with a partition."
argument_list|,
name|split
argument_list|)
expr_stmt|;
block|}
comment|// In "select * from table" situations (non-MR), we can add things to the job
comment|// It's safe to add this to the job since it's not *actually* a mapred job.
comment|// Here the global state is confined to just this process.
name|String
name|s
init|=
name|job
operator|.
name|get
argument_list|(
name|AvroTableProperties
operator|.
name|AVRO_SERDE_SCHEMA
operator|.
name|getPropName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Found the avro schema in the job"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Avro schema: {}"
argument_list|,
name|s
argument_list|)
expr_stmt|;
return|return
name|AvroSerdeUtils
operator|.
name|getSchemaFor
argument_list|(
name|s
argument_list|)
return|;
block|}
comment|// No more places to get the schema from. Give up.  May have to re-encode later.
return|return
literal|null
return|;
block|}
specifier|private
name|ZoneId
name|extractWriterTimezoneFromMetadata
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|FileSplit
name|split
parameter_list|,
name|GenericDatumReader
argument_list|<
name|GenericRecord
argument_list|>
name|gdr
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|job
operator|==
literal|null
operator|||
name|gdr
operator|==
literal|null
operator|||
name|split
operator|==
literal|null
operator|||
name|split
operator|.
name|getPath
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
name|DataFileReader
argument_list|<
name|GenericRecord
argument_list|>
name|dataFileReader
init|=
operator|new
name|DataFileReader
argument_list|<
name|GenericRecord
argument_list|>
argument_list|(
operator|new
name|FsInput
argument_list|(
name|split
operator|.
name|getPath
argument_list|()
argument_list|,
name|job
argument_list|)
argument_list|,
name|gdr
argument_list|)
decl_stmt|;
if|if
condition|(
name|dataFileReader
operator|.
name|getMeta
argument_list|(
name|AvroSerDe
operator|.
name|WRITER_TIME_ZONE
argument_list|)
operator|!=
literal|null
condition|)
block|{
try|try
block|{
return|return
name|ZoneId
operator|.
name|of
argument_list|(
operator|new
name|String
argument_list|(
name|dataFileReader
operator|.
name|getMeta
argument_list|(
name|AvroSerDe
operator|.
name|WRITER_TIME_ZONE
argument_list|)
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|DateTimeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Can't parse writer time zone stored in file metadata"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Can't access metadata, carry on.
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|boolean
name|pathIsInPartition
parameter_list|(
name|Path
name|split
parameter_list|,
name|Path
name|partitionPath
parameter_list|)
block|{
name|boolean
name|schemeless
init|=
name|split
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
operator|==
literal|null
decl_stmt|;
if|if
condition|(
name|schemeless
condition|)
block|{
name|Path
name|pathNoSchema
init|=
name|Path
operator|.
name|getPathWithoutSchemeAndAuthority
argument_list|(
name|partitionPath
argument_list|)
decl_stmt|;
return|return
name|FileUtils
operator|.
name|isPathWithinSubtree
argument_list|(
name|split
argument_list|,
name|pathNoSchema
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|FileUtils
operator|.
name|isPathWithinSubtree
argument_list|(
name|split
argument_list|,
name|partitionPath
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|NullWritable
name|nullWritable
parameter_list|,
name|AvroGenericRecordWritable
name|record
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|isEmptyInput
operator|||
operator|!
name|reader
operator|.
name|hasNext
argument_list|()
operator|||
name|reader
operator|.
name|pastSync
argument_list|(
name|stop
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|GenericData
operator|.
name|Record
name|r
init|=
operator|(
name|GenericData
operator|.
name|Record
operator|)
name|reader
operator|.
name|next
argument_list|()
decl_stmt|;
name|record
operator|.
name|setRecord
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|record
operator|.
name|setRecordReaderID
argument_list|(
name|recordReaderID
argument_list|)
expr_stmt|;
name|record
operator|.
name|setFileSchema
argument_list|(
name|reader
operator|.
name|getSchema
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|NullWritable
name|createKey
parameter_list|()
block|{
return|return
name|NullWritable
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|AvroGenericRecordWritable
name|createValue
parameter_list|()
block|{
return|return
operator|new
name|AvroGenericRecordWritable
argument_list|(
name|writerTimezone
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getPos
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|isEmptyInput
condition|?
literal|0
else|:
name|reader
operator|.
name|tell
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|isEmptyInput
operator|==
literal|false
condition|)
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|stop
operator|==
name|start
condition|?
literal|0.0f
else|:
name|Math
operator|.
name|min
argument_list|(
literal|1.0f
argument_list|,
operator|(
name|getPos
argument_list|()
operator|-
name|start
operator|)
operator|/
call|(
name|float
call|)
argument_list|(
name|stop
operator|-
name|start
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|configure
parameter_list|(
name|JobConf
name|jobConf
parameter_list|)
block|{
name|this
operator|.
name|jobConf
operator|=
name|jobConf
expr_stmt|;
block|}
block|}
end_class

end_unit

