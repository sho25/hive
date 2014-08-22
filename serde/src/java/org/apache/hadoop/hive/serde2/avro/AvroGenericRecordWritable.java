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
name|serde2
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
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|InputStream
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
name|avro
operator|.
name|io
operator|.
name|BinaryDecoder
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
name|io
operator|.
name|BinaryEncoder
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
name|io
operator|.
name|DecoderFactory
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
name|io
operator|.
name|EncoderFactory
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

begin_comment
comment|/**  * Wrapper around an Avro GenericRecord.  Necessary because Hive's deserializer  * will happily deserialize any object - as long as it's a writable.  */
end_comment

begin_class
specifier|public
class|class
name|AvroGenericRecordWritable
implements|implements
name|Writable
block|{
name|GenericRecord
name|record
decl_stmt|;
specifier|private
name|BinaryDecoder
name|binaryDecoder
decl_stmt|;
comment|// Schema that exists in the Avro data file.
specifier|private
name|Schema
name|fileSchema
decl_stmt|;
comment|/**    * Unique Id determine which record reader created this record    */
specifier|private
name|UID
name|recordReaderID
decl_stmt|;
comment|// There are two areas of exploration for optimization here.
comment|// 1.  We're serializing the schema with every object.  If we assume the schema
comment|//     provided by the table is always correct, we don't need to do this and
comment|//     and can just send the serialized bytes.
comment|// 2.  We serialize/deserialize to/from bytes immediately.  We may save some
comment|//     time but doing this lazily, but until there's evidence this is useful,
comment|//     it's not worth adding the extra state.
specifier|public
name|GenericRecord
name|getRecord
parameter_list|()
block|{
return|return
name|record
return|;
block|}
specifier|public
name|void
name|setRecord
parameter_list|(
name|GenericRecord
name|record
parameter_list|)
block|{
name|this
operator|.
name|record
operator|=
name|record
expr_stmt|;
block|}
specifier|public
name|AvroGenericRecordWritable
parameter_list|()
block|{}
specifier|public
name|AvroGenericRecordWritable
parameter_list|(
name|GenericRecord
name|record
parameter_list|)
block|{
name|this
operator|.
name|record
operator|=
name|record
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Write schema since we need it to pull the data out. (see point #1 above)
name|String
name|schemaString
init|=
name|record
operator|.
name|getSchema
argument_list|()
operator|.
name|toString
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|schemaString
argument_list|)
expr_stmt|;
name|schemaString
operator|=
name|fileSchema
operator|.
name|toString
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|schemaString
argument_list|)
expr_stmt|;
name|recordReaderID
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
comment|// Write record to byte buffer
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
argument_list|()
decl_stmt|;
name|BinaryEncoder
name|be
init|=
name|EncoderFactory
operator|.
name|get
argument_list|()
operator|.
name|directBinaryEncoder
argument_list|(
operator|(
name|DataOutputStream
operator|)
name|out
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|gdw
operator|.
name|setSchema
argument_list|(
name|record
operator|.
name|getSchema
argument_list|()
argument_list|)
expr_stmt|;
name|gdw
operator|.
name|write
argument_list|(
name|record
argument_list|,
name|be
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Schema
name|schema
init|=
name|AvroSerdeUtils
operator|.
name|getSchemaFor
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
decl_stmt|;
name|fileSchema
operator|=
name|AvroSerdeUtils
operator|.
name|getSchemaFor
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
expr_stmt|;
name|recordReaderID
operator|=
name|UID
operator|.
name|read
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|record
operator|=
operator|new
name|GenericData
operator|.
name|Record
argument_list|(
name|schema
argument_list|)
expr_stmt|;
name|binaryDecoder
operator|=
name|DecoderFactory
operator|.
name|defaultFactory
argument_list|()
operator|.
name|createBinaryDecoder
argument_list|(
operator|(
name|InputStream
operator|)
name|in
argument_list|,
name|binaryDecoder
argument_list|)
expr_stmt|;
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
argument_list|(
name|schema
argument_list|)
decl_stmt|;
name|record
operator|=
name|gdr
operator|.
name|read
argument_list|(
name|record
argument_list|,
name|binaryDecoder
argument_list|)
expr_stmt|;
block|}
specifier|public
name|UID
name|getRecordReaderID
parameter_list|()
block|{
return|return
name|recordReaderID
return|;
block|}
specifier|public
name|void
name|setRecordReaderID
parameter_list|(
name|UID
name|recordReaderID
parameter_list|)
block|{
name|this
operator|.
name|recordReaderID
operator|=
name|recordReaderID
expr_stmt|;
block|}
specifier|public
name|Schema
name|getFileSchema
parameter_list|()
block|{
return|return
name|fileSchema
return|;
block|}
specifier|public
name|void
name|setFileSchema
parameter_list|(
name|Schema
name|originalSchema
parameter_list|)
block|{
name|this
operator|.
name|fileSchema
operator|=
name|originalSchema
expr_stmt|;
block|}
block|}
end_class

end_unit

