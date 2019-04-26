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
import|import static
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|file
operator|.
name|DataFileConstants
operator|.
name|DEFLATE_CODEC
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|mapred
operator|.
name|AvroJob
operator|.
name|OUTPUT_CODEC
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|file
operator|.
name|CodecFactory
operator|.
name|DEFAULT_DEFLATE_LEVEL
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|mapred
operator|.
name|AvroOutputFormat
operator|.
name|DEFLATE_LEVEL_KEY
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
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TimeZone
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
name|CodecFactory
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
name|DataFileWriter
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
name|GenericDatumWriter
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
name|FileSinkOperator
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
name|io
operator|.
name|Writable
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
name|mapred
operator|.
name|Reporter
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
name|Progressable
import|;
end_import

begin_comment
comment|/**  * Write to an Avro file from a Hive process.  */
end_comment

begin_class
specifier|public
class|class
name|AvroContainerOutputFormat
implements|implements
name|HiveOutputFormat
argument_list|<
name|WritableComparable
argument_list|,
name|AvroGenericRecordWritable
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|AvroContainerOutputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
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
name|getHiveRecordWriter
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|Path
name|path
parameter_list|,
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
name|properties
parameter_list|,
name|Progressable
name|progressable
parameter_list|)
throws|throws
name|IOException
block|{
name|Schema
name|schema
decl_stmt|;
try|try
block|{
name|schema
operator|=
name|AvroSerdeUtils
operator|.
name|determineSchemaOrThrowException
argument_list|(
name|jobConf
argument_list|,
name|properties
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
name|GenericDatumWriter
argument_list|<
name|GenericRecord
argument_list|>
name|gdw
init|=
operator|new
name|GenericDatumWriter
argument_list|<
name|GenericRecord
argument_list|>
argument_list|(
name|schema
argument_list|)
decl_stmt|;
name|DataFileWriter
argument_list|<
name|GenericRecord
argument_list|>
name|dfw
init|=
operator|new
name|DataFileWriter
argument_list|<
name|GenericRecord
argument_list|>
argument_list|(
name|gdw
argument_list|)
decl_stmt|;
if|if
condition|(
name|isCompressed
condition|)
block|{
name|int
name|level
init|=
name|jobConf
operator|.
name|getInt
argument_list|(
name|DEFLATE_LEVEL_KEY
argument_list|,
name|DEFAULT_DEFLATE_LEVEL
argument_list|)
decl_stmt|;
name|String
name|codecName
init|=
name|jobConf
operator|.
name|get
argument_list|(
name|OUTPUT_CODEC
argument_list|,
name|DEFLATE_CODEC
argument_list|)
decl_stmt|;
name|CodecFactory
name|factory
init|=
name|codecName
operator|.
name|equals
argument_list|(
name|DEFLATE_CODEC
argument_list|)
condition|?
name|CodecFactory
operator|.
name|deflateCodec
argument_list|(
name|level
argument_list|)
else|:
name|CodecFactory
operator|.
name|fromString
argument_list|(
name|codecName
argument_list|)
decl_stmt|;
name|dfw
operator|.
name|setCodec
argument_list|(
name|factory
argument_list|)
expr_stmt|;
block|}
comment|// add writer.time.zone property to file metadata
name|dfw
operator|.
name|setMeta
argument_list|(
name|AvroSerDe
operator|.
name|WRITER_TIME_ZONE
argument_list|,
name|TimeZone
operator|.
name|getDefault
argument_list|()
operator|.
name|toZoneId
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|dfw
operator|.
name|create
argument_list|(
name|schema
argument_list|,
name|path
operator|.
name|getFileSystem
argument_list|(
name|jobConf
argument_list|)
operator|.
name|create
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|AvroGenericRecordWriter
argument_list|(
name|dfw
argument_list|)
return|;
block|}
class|class
name|WrapperRecordWriter
parameter_list|<
name|K
extends|extends
name|Writable
parameter_list|,
name|V
extends|extends
name|Writable
parameter_list|>
implements|implements
name|RecordWriter
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
name|FileSinkOperator
operator|.
name|RecordWriter
name|hiveWriter
init|=
literal|null
decl_stmt|;
name|JobConf
name|jobConf
decl_stmt|;
name|Progressable
name|progressable
decl_stmt|;
name|String
name|fileName
decl_stmt|;
specifier|public
name|WrapperRecordWriter
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|Progressable
name|progressable
parameter_list|,
name|String
name|fileName
parameter_list|)
block|{
name|this
operator|.
name|progressable
operator|=
name|progressable
expr_stmt|;
name|this
operator|.
name|jobConf
operator|=
name|jobConf
expr_stmt|;
name|this
operator|.
name|fileName
operator|=
name|fileName
expr_stmt|;
block|}
specifier|private
name|FileSinkOperator
operator|.
name|RecordWriter
name|getHiveWriter
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|hiveWriter
operator|==
literal|null
condition|)
block|{
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
for|for
control|(
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
name|tableProperty
range|:
name|AvroSerdeUtils
operator|.
name|AvroTableProperties
operator|.
name|values
argument_list|()
control|)
block|{
name|String
name|propVal
decl_stmt|;
if|if
condition|(
operator|(
name|propVal
operator|=
name|jobConf
operator|.
name|get
argument_list|(
name|tableProperty
operator|.
name|getPropName
argument_list|()
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
name|properties
operator|.
name|put
argument_list|(
name|tableProperty
operator|.
name|getPropName
argument_list|()
argument_list|,
name|propVal
argument_list|)
expr_stmt|;
block|}
block|}
name|Boolean
name|isCompressed
init|=
name|jobConf
operator|.
name|getBoolean
argument_list|(
literal|"mapreduce.output.fileoutputformat.compress"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|this
operator|.
name|fileName
argument_list|)
decl_stmt|;
if|if
condition|(
name|path
operator|.
name|getFileSystem
argument_list|(
name|jobConf
argument_list|)
operator|.
name|isDirectory
argument_list|(
name|path
argument_list|)
condition|)
block|{
comment|// This path is only potentially encountered during setup
comment|// Otherwise, a specific part_xxxx file name is generated and passed in.
name|path
operator|=
operator|new
name|Path
argument_list|(
name|path
argument_list|,
literal|"_dummy"
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|hiveWriter
operator|=
name|getHiveRecordWriter
argument_list|(
name|jobConf
argument_list|,
name|path
argument_list|,
literal|null
argument_list|,
name|isCompressed
argument_list|,
name|properties
argument_list|,
name|progressable
argument_list|)
expr_stmt|;
block|}
return|return
name|this
operator|.
name|hiveWriter
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|getHiveWriter
argument_list|()
operator|.
name|write
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Normally, I'd worry about the blanket false being passed in here, and that
comment|// it'd need to be integrated into an abort call for an OutputCommitter, but the
comment|// underlying recordwriter ignores it and throws it away, so it's irrelevant.
name|getHiveWriter
argument_list|()
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
comment|//no records will be emitted from Hive
annotation|@
name|Override
specifier|public
name|RecordWriter
argument_list|<
name|WritableComparable
argument_list|,
name|AvroGenericRecordWritable
argument_list|>
name|getRecordWriter
parameter_list|(
name|FileSystem
name|ignored
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|String
name|fileName
parameter_list|,
name|Progressable
name|progress
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|WrapperRecordWriter
argument_list|<
name|WritableComparable
argument_list|,
name|AvroGenericRecordWritable
argument_list|>
argument_list|(
name|job
argument_list|,
name|progress
argument_list|,
name|fileName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkOutputSpecs
parameter_list|(
name|FileSystem
name|ignored
parameter_list|,
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
return|return;
comment|// Not doing any check
block|}
block|}
end_class

end_unit

