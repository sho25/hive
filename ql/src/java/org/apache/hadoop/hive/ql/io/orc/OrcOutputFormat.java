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
name|io
operator|.
name|AcidOutputFormat
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
name|AcidUtils
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
name|StatsProvidingRecordWriter
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
name|RecordUpdater
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
name|EncodingStrategy
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
name|OrcSerde
operator|.
name|OrcSerdeRow
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
name|SerDeStats
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorFactory
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
name|PrimitiveObjectInspector
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
name|StructField
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
name|StructObjectInspector
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
name|IntWritable
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
name|mapred
operator|.
name|FileOutputFormat
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
name|ArrayList
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

begin_comment
comment|/**  * A Hive OutputFormat for ORC files.  */
end_comment

begin_class
specifier|public
class|class
name|OrcOutputFormat
extends|extends
name|FileOutputFormat
argument_list|<
name|NullWritable
argument_list|,
name|OrcSerdeRow
argument_list|>
implements|implements
name|AcidOutputFormat
argument_list|<
name|NullWritable
argument_list|,
name|OrcSerdeRow
argument_list|>
block|{
specifier|private
specifier|static
class|class
name|OrcRecordWriter
implements|implements
name|RecordWriter
argument_list|<
name|NullWritable
argument_list|,
name|OrcSerdeRow
argument_list|>
implements|,
name|StatsProvidingRecordWriter
block|{
specifier|private
name|Writer
name|writer
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|Path
name|path
decl_stmt|;
specifier|private
specifier|final
name|OrcFile
operator|.
name|WriterOptions
name|options
decl_stmt|;
specifier|private
specifier|final
name|SerDeStats
name|stats
decl_stmt|;
name|OrcRecordWriter
parameter_list|(
name|Path
name|path
parameter_list|,
name|OrcFile
operator|.
name|WriterOptions
name|options
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|options
operator|=
name|options
expr_stmt|;
name|this
operator|.
name|stats
operator|=
operator|new
name|SerDeStats
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|NullWritable
name|nullWritable
parameter_list|,
name|OrcSerdeRow
name|row
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|writer
operator|==
literal|null
condition|)
block|{
name|options
operator|.
name|inspector
argument_list|(
name|row
operator|.
name|getInspector
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|=
name|OrcFile
operator|.
name|createWriter
argument_list|(
name|path
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|addRow
argument_list|(
name|row
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|Writable
name|row
parameter_list|)
throws|throws
name|IOException
block|{
name|OrcSerdeRow
name|serdeRow
init|=
operator|(
name|OrcSerdeRow
operator|)
name|row
decl_stmt|;
if|if
condition|(
name|writer
operator|==
literal|null
condition|)
block|{
name|options
operator|.
name|inspector
argument_list|(
name|serdeRow
operator|.
name|getInspector
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|=
name|OrcFile
operator|.
name|createWriter
argument_list|(
name|path
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|addRow
argument_list|(
name|serdeRow
operator|.
name|getRow
argument_list|()
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
name|close
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|b
parameter_list|)
throws|throws
name|IOException
block|{
comment|// if we haven't written any rows, we need to create a file with a
comment|// generic schema.
if|if
condition|(
name|writer
operator|==
literal|null
condition|)
block|{
comment|// a row with no columns
name|ObjectInspector
name|inspector
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
name|options
operator|.
name|inspector
argument_list|(
name|inspector
argument_list|)
expr_stmt|;
name|writer
operator|=
name|OrcFile
operator|.
name|createWriter
argument_list|(
name|path
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getStats
parameter_list|()
block|{
name|stats
operator|.
name|setRawDataSize
argument_list|(
name|writer
operator|.
name|getRawDataSize
argument_list|()
argument_list|)
expr_stmt|;
name|stats
operator|.
name|setRowCount
argument_list|(
name|writer
operator|.
name|getNumberOfRows
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|stats
return|;
block|}
block|}
comment|/**    * Helper method to get a parameter first from props if present, falling back to JobConf if not.    * Returns null if key is present in neither.    */
specifier|private
name|String
name|getSettingFromPropsFallingBackToConf
parameter_list|(
name|String
name|key
parameter_list|,
name|Properties
name|props
parameter_list|,
name|JobConf
name|conf
parameter_list|)
block|{
if|if
condition|(
operator|(
name|props
operator|!=
literal|null
operator|)
operator|&&
name|props
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
return|return
name|props
operator|.
name|getProperty
argument_list|(
name|key
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|conf
operator|!=
literal|null
condition|)
block|{
comment|// If conf is not null, and the key is not present, Configuration.get() will
comment|// return null for us. So, we don't have to check if it contains it.
return|return
name|conf
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|OrcFile
operator|.
name|WriterOptions
name|getOptions
parameter_list|(
name|JobConf
name|conf
parameter_list|,
name|Properties
name|props
parameter_list|)
block|{
name|OrcFile
operator|.
name|WriterOptions
name|options
init|=
name|OrcFile
operator|.
name|writerOptions
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|String
name|propVal
decl_stmt|;
if|if
condition|(
operator|(
name|propVal
operator|=
name|getSettingFromPropsFallingBackToConf
argument_list|(
name|OrcFile
operator|.
name|OrcTableProperties
operator|.
name|STRIPE_SIZE
operator|.
name|getPropName
argument_list|()
argument_list|,
name|props
argument_list|,
name|conf
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
name|options
operator|.
name|stripeSize
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|propVal
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|propVal
operator|=
name|getSettingFromPropsFallingBackToConf
argument_list|(
name|OrcFile
operator|.
name|OrcTableProperties
operator|.
name|COMPRESSION
operator|.
name|getPropName
argument_list|()
argument_list|,
name|props
argument_list|,
name|conf
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
name|options
operator|.
name|compress
argument_list|(
name|CompressionKind
operator|.
name|valueOf
argument_list|(
name|propVal
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|propVal
operator|=
name|getSettingFromPropsFallingBackToConf
argument_list|(
name|OrcFile
operator|.
name|OrcTableProperties
operator|.
name|COMPRESSION_BLOCK_SIZE
operator|.
name|getPropName
argument_list|()
argument_list|,
name|props
argument_list|,
name|conf
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
name|options
operator|.
name|bufferSize
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|propVal
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|propVal
operator|=
name|getSettingFromPropsFallingBackToConf
argument_list|(
name|OrcFile
operator|.
name|OrcTableProperties
operator|.
name|ROW_INDEX_STRIDE
operator|.
name|getPropName
argument_list|()
argument_list|,
name|props
argument_list|,
name|conf
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
name|options
operator|.
name|rowIndexStride
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|propVal
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|propVal
operator|=
name|getSettingFromPropsFallingBackToConf
argument_list|(
name|OrcFile
operator|.
name|OrcTableProperties
operator|.
name|ENABLE_INDEXES
operator|.
name|getPropName
argument_list|()
argument_list|,
name|props
argument_list|,
name|conf
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
literal|"false"
operator|.
name|equalsIgnoreCase
argument_list|(
name|propVal
argument_list|)
condition|)
block|{
name|options
operator|.
name|rowIndexStride
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|(
name|propVal
operator|=
name|getSettingFromPropsFallingBackToConf
argument_list|(
name|OrcFile
operator|.
name|OrcTableProperties
operator|.
name|BLOCK_PADDING
operator|.
name|getPropName
argument_list|()
argument_list|,
name|props
argument_list|,
name|conf
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
name|options
operator|.
name|blockPadding
argument_list|(
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|propVal
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|propVal
operator|=
name|getSettingFromPropsFallingBackToConf
argument_list|(
name|OrcFile
operator|.
name|OrcTableProperties
operator|.
name|ENCODING_STRATEGY
operator|.
name|getPropName
argument_list|()
argument_list|,
name|props
argument_list|,
name|conf
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
name|options
operator|.
name|encodingStrategy
argument_list|(
name|EncodingStrategy
operator|.
name|valueOf
argument_list|(
name|propVal
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|options
return|;
block|}
annotation|@
name|Override
specifier|public
name|RecordWriter
argument_list|<
name|NullWritable
argument_list|,
name|OrcSerdeRow
argument_list|>
name|getRecordWriter
parameter_list|(
name|FileSystem
name|fileSystem
parameter_list|,
name|JobConf
name|conf
parameter_list|,
name|String
name|name
parameter_list|,
name|Progressable
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|OrcRecordWriter
argument_list|(
operator|new
name|Path
argument_list|(
name|name
argument_list|)
argument_list|,
name|getOptions
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|StatsProvidingRecordWriter
name|getHiveRecordWriter
parameter_list|(
name|JobConf
name|conf
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
name|tableProperties
parameter_list|,
name|Progressable
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|OrcRecordWriter
argument_list|(
name|path
argument_list|,
name|getOptions
argument_list|(
name|conf
argument_list|,
name|tableProperties
argument_list|)
argument_list|)
return|;
block|}
specifier|private
class|class
name|DummyOrcRecordUpdater
implements|implements
name|RecordUpdater
block|{
specifier|private
specifier|final
name|Path
name|path
decl_stmt|;
specifier|private
specifier|final
name|ObjectInspector
name|inspector
decl_stmt|;
specifier|private
specifier|final
name|PrintStream
name|out
decl_stmt|;
specifier|private
name|DummyOrcRecordUpdater
parameter_list|(
name|Path
name|path
parameter_list|,
name|Options
name|options
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|inspector
operator|=
name|options
operator|.
name|getInspector
argument_list|()
expr_stmt|;
name|this
operator|.
name|out
operator|=
name|options
operator|.
name|getDummyStream
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|insert
parameter_list|(
name|long
name|currentTransaction
parameter_list|,
name|Object
name|row
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|println
argument_list|(
literal|"insert "
operator|+
name|path
operator|+
literal|" currTxn: "
operator|+
name|currentTransaction
operator|+
literal|" obj: "
operator|+
name|stringifyObject
argument_list|(
name|row
argument_list|,
name|inspector
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|update
parameter_list|(
name|long
name|currentTransaction
parameter_list|,
name|Object
name|row
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|println
argument_list|(
literal|"update "
operator|+
name|path
operator|+
literal|" currTxn: "
operator|+
name|currentTransaction
operator|+
literal|" obj: "
operator|+
name|stringifyObject
argument_list|(
name|row
argument_list|,
name|inspector
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|delete
parameter_list|(
name|long
name|currentTransaction
parameter_list|,
name|Object
name|row
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|println
argument_list|(
literal|"delete "
operator|+
name|path
operator|+
literal|" currTxn: "
operator|+
name|currentTransaction
operator|+
literal|" obj: "
operator|+
name|row
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
block|{
name|out
operator|.
name|println
argument_list|(
literal|"flush "
operator|+
name|path
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|println
argument_list|(
literal|"close "
operator|+
name|path
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getStats
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|stringifyObject
parameter_list|(
name|StringBuilder
name|buffer
parameter_list|,
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|inspector
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|inspector
operator|instanceof
name|StructObjectInspector
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|"{ "
argument_list|)
expr_stmt|;
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|inspector
decl_stmt|;
name|boolean
name|isFirst
init|=
literal|true
decl_stmt|;
for|for
control|(
name|StructField
name|field
range|:
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
control|)
block|{
if|if
condition|(
name|isFirst
condition|)
block|{
name|isFirst
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
name|field
operator|.
name|getFieldName
argument_list|()
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|": "
argument_list|)
expr_stmt|;
name|stringifyObject
argument_list|(
name|buffer
argument_list|,
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|obj
argument_list|,
name|field
argument_list|)
argument_list|,
name|field
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
literal|" }"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|inspector
operator|instanceof
name|PrimitiveObjectInspector
condition|)
block|{
name|PrimitiveObjectInspector
name|poi
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|inspector
decl_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|poi
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|obj
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|"*unknown*"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|stringifyObject
parameter_list|(
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|inspector
parameter_list|)
throws|throws
name|IOException
block|{
name|StringBuilder
name|buffer
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|stringifyObject
argument_list|(
name|buffer
argument_list|,
name|obj
argument_list|,
name|inspector
argument_list|)
expr_stmt|;
return|return
name|buffer
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
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
block|{
if|if
condition|(
name|options
operator|.
name|getDummyStream
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|DummyOrcRecordUpdater
argument_list|(
name|path
argument_list|,
name|options
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|OrcRecordUpdater
argument_list|(
name|path
argument_list|,
name|options
argument_list|)
return|;
block|}
block|}
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
block|{
specifier|final
name|Path
name|filename
init|=
name|AcidUtils
operator|.
name|createFilename
argument_list|(
name|path
argument_list|,
name|options
argument_list|)
decl_stmt|;
specifier|final
name|OrcFile
operator|.
name|WriterOptions
name|opts
init|=
name|OrcFile
operator|.
name|writerOptions
argument_list|(
name|options
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|options
operator|.
name|isWritingBase
argument_list|()
condition|)
block|{
name|opts
operator|.
name|bufferSize
argument_list|(
name|OrcRecordUpdater
operator|.
name|DELTA_BUFFER_SIZE
argument_list|)
operator|.
name|stripeSize
argument_list|(
name|OrcRecordUpdater
operator|.
name|DELTA_STRIPE_SIZE
argument_list|)
operator|.
name|blockPadding
argument_list|(
literal|false
argument_list|)
operator|.
name|compress
argument_list|(
name|CompressionKind
operator|.
name|NONE
argument_list|)
operator|.
name|rowIndexStride
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|final
name|OrcRecordUpdater
operator|.
name|KeyIndexBuilder
name|watcher
init|=
operator|new
name|OrcRecordUpdater
operator|.
name|KeyIndexBuilder
argument_list|()
decl_stmt|;
name|opts
operator|.
name|inspector
argument_list|(
name|options
operator|.
name|getInspector
argument_list|()
argument_list|)
operator|.
name|callback
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
specifier|final
name|Writer
name|writer
init|=
name|OrcFile
operator|.
name|createWriter
argument_list|(
name|filename
argument_list|,
name|opts
argument_list|)
decl_stmt|;
return|return
operator|new
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
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|Writable
name|w
parameter_list|)
throws|throws
name|IOException
block|{
name|OrcStruct
name|orc
init|=
operator|(
name|OrcStruct
operator|)
name|w
decl_stmt|;
name|watcher
operator|.
name|addKey
argument_list|(
operator|(
operator|(
name|IntWritable
operator|)
name|orc
operator|.
name|getFieldValue
argument_list|(
name|OrcRecordUpdater
operator|.
name|OPERATION
argument_list|)
operator|)
operator|.
name|get
argument_list|()
argument_list|,
operator|(
operator|(
name|LongWritable
operator|)
name|orc
operator|.
name|getFieldValue
argument_list|(
name|OrcRecordUpdater
operator|.
name|ORIGINAL_TRANSACTION
argument_list|)
operator|)
operator|.
name|get
argument_list|()
argument_list|,
operator|(
operator|(
name|IntWritable
operator|)
name|orc
operator|.
name|getFieldValue
argument_list|(
name|OrcRecordUpdater
operator|.
name|BUCKET
argument_list|)
operator|)
operator|.
name|get
argument_list|()
argument_list|,
operator|(
operator|(
name|LongWritable
operator|)
name|orc
operator|.
name|getFieldValue
argument_list|(
name|OrcRecordUpdater
operator|.
name|ROW_ID
argument_list|)
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addRow
argument_list|(
name|w
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

