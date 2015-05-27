begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|parquet
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
name|io
operator|.
name|parquet
operator|.
name|serde
operator|.
name|ArrayWritableObjectInspector
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
name|parquet
operator|.
name|serde
operator|.
name|ParquetHiveSerDe
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
name|parquet
operator|.
name|write
operator|.
name|DataWritableWriter
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
name|io
operator|.
name|ByteWritable
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
name|io
operator|.
name|ShortWritable
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
name|io
operator|.
name|ParquetHiveRecord
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
name|SerDeUtils
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
name|io
operator|.
name|DoubleWritable
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
name|typeinfo
operator|.
name|StructTypeInfo
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
name|typeinfo
operator|.
name|TypeInfo
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
name|typeinfo
operator|.
name|TypeInfoFactory
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
name|typeinfo
operator|.
name|TypeInfoUtils
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
name|ArrayWritable
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
name|BooleanWritable
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
name|BytesWritable
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
name|FloatWritable
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
name|Writable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|InOrder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|MockitoAnnotations
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|Binary
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|RecordConsumer
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|MessageType
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|MessageTypeParser
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|TestDataWritableWriter
block|{
annotation|@
name|Mock
specifier|private
name|RecordConsumer
name|mockRecordConsumer
decl_stmt|;
specifier|private
name|InOrder
name|inOrder
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|initMocks
parameter_list|()
block|{
name|MockitoAnnotations
operator|.
name|initMocks
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|inOrder
operator|=
name|inOrder
argument_list|(
name|mockRecordConsumer
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|startMessage
parameter_list|()
block|{
name|inOrder
operator|.
name|verify
argument_list|(
name|mockRecordConsumer
argument_list|)
operator|.
name|startMessage
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|endMessage
parameter_list|()
block|{
name|inOrder
operator|.
name|verify
argument_list|(
name|mockRecordConsumer
argument_list|)
operator|.
name|endMessage
argument_list|()
expr_stmt|;
name|verifyNoMoreInteractions
argument_list|(
name|mockRecordConsumer
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|startField
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|index
parameter_list|)
block|{
name|inOrder
operator|.
name|verify
argument_list|(
name|mockRecordConsumer
argument_list|)
operator|.
name|startField
argument_list|(
name|name
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|endField
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|index
parameter_list|)
block|{
name|inOrder
operator|.
name|verify
argument_list|(
name|mockRecordConsumer
argument_list|)
operator|.
name|endField
argument_list|(
name|name
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addInteger
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|inOrder
operator|.
name|verify
argument_list|(
name|mockRecordConsumer
argument_list|)
operator|.
name|addInteger
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addLong
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|inOrder
operator|.
name|verify
argument_list|(
name|mockRecordConsumer
argument_list|)
operator|.
name|addLong
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addFloat
parameter_list|(
name|float
name|value
parameter_list|)
block|{
name|inOrder
operator|.
name|verify
argument_list|(
name|mockRecordConsumer
argument_list|)
operator|.
name|addFloat
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addDouble
parameter_list|(
name|double
name|value
parameter_list|)
block|{
name|inOrder
operator|.
name|verify
argument_list|(
name|mockRecordConsumer
argument_list|)
operator|.
name|addDouble
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addBoolean
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|inOrder
operator|.
name|verify
argument_list|(
name|mockRecordConsumer
argument_list|)
operator|.
name|addBoolean
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addString
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|inOrder
operator|.
name|verify
argument_list|(
name|mockRecordConsumer
argument_list|)
operator|.
name|addBinary
argument_list|(
name|Binary
operator|.
name|fromString
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|startGroup
parameter_list|()
block|{
name|inOrder
operator|.
name|verify
argument_list|(
name|mockRecordConsumer
argument_list|)
operator|.
name|startGroup
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|endGroup
parameter_list|()
block|{
name|inOrder
operator|.
name|verify
argument_list|(
name|mockRecordConsumer
argument_list|)
operator|.
name|endGroup
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Writable
name|createNull
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
specifier|private
name|ByteWritable
name|createTinyInt
parameter_list|(
name|byte
name|value
parameter_list|)
block|{
return|return
operator|new
name|ByteWritable
argument_list|(
name|value
argument_list|)
return|;
block|}
specifier|private
name|ShortWritable
name|createSmallInt
parameter_list|(
name|short
name|value
parameter_list|)
block|{
return|return
operator|new
name|ShortWritable
argument_list|(
name|value
argument_list|)
return|;
block|}
specifier|private
name|LongWritable
name|createBigInt
parameter_list|(
name|long
name|value
parameter_list|)
block|{
return|return
operator|new
name|LongWritable
argument_list|(
name|value
argument_list|)
return|;
block|}
specifier|private
name|IntWritable
name|createInt
parameter_list|(
name|int
name|value
parameter_list|)
block|{
return|return
operator|new
name|IntWritable
argument_list|(
name|value
argument_list|)
return|;
block|}
specifier|private
name|FloatWritable
name|createFloat
parameter_list|(
name|float
name|value
parameter_list|)
block|{
return|return
operator|new
name|FloatWritable
argument_list|(
name|value
argument_list|)
return|;
block|}
specifier|private
name|DoubleWritable
name|createDouble
parameter_list|(
name|double
name|value
parameter_list|)
block|{
return|return
operator|new
name|DoubleWritable
argument_list|(
name|value
argument_list|)
return|;
block|}
specifier|private
name|BooleanWritable
name|createBoolean
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
return|return
operator|new
name|BooleanWritable
argument_list|(
name|value
argument_list|)
return|;
block|}
specifier|private
name|BytesWritable
name|createString
parameter_list|(
name|String
name|value
parameter_list|)
throws|throws
name|UnsupportedEncodingException
block|{
return|return
operator|new
name|BytesWritable
argument_list|(
name|value
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|ArrayWritable
name|createGroup
parameter_list|(
name|Writable
modifier|...
name|values
parameter_list|)
block|{
return|return
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|values
argument_list|)
return|;
block|}
specifier|private
name|ArrayWritable
name|createArray
parameter_list|(
name|Writable
modifier|...
name|values
parameter_list|)
block|{
return|return
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|createGroup
argument_list|(
name|values
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|createHiveColumnsFrom
parameter_list|(
specifier|final
name|String
name|columnNamesStr
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
decl_stmt|;
if|if
condition|(
name|columnNamesStr
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|columnNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|columnNames
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|columnNamesStr
operator|.
name|split
argument_list|(
literal|","
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|columnNames
return|;
block|}
specifier|private
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|createHiveTypeInfoFrom
parameter_list|(
specifier|final
name|String
name|columnsTypeStr
parameter_list|)
block|{
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
decl_stmt|;
if|if
condition|(
name|columnsTypeStr
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|columnTypes
operator|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|columnTypes
operator|=
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
name|columnsTypeStr
argument_list|)
expr_stmt|;
block|}
return|return
name|columnTypes
return|;
block|}
specifier|private
name|ArrayWritableObjectInspector
name|getObjectInspector
parameter_list|(
specifier|final
name|String
name|columnNames
parameter_list|,
specifier|final
name|String
name|columnTypes
parameter_list|)
block|{
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypeList
init|=
name|createHiveTypeInfoFrom
argument_list|(
name|columnTypes
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columnNameList
init|=
name|createHiveColumnsFrom
argument_list|(
name|columnNames
argument_list|)
decl_stmt|;
name|StructTypeInfo
name|rowTypeInfo
init|=
operator|(
name|StructTypeInfo
operator|)
name|TypeInfoFactory
operator|.
name|getStructTypeInfo
argument_list|(
name|columnNameList
argument_list|,
name|columnTypeList
argument_list|)
decl_stmt|;
return|return
operator|new
name|ArrayWritableObjectInspector
argument_list|(
name|rowTypeInfo
argument_list|)
return|;
block|}
specifier|private
name|ParquetHiveRecord
name|getParquetWritable
parameter_list|(
name|String
name|columnNames
parameter_list|,
name|String
name|columnTypes
parameter_list|,
name|ArrayWritable
name|record
parameter_list|)
throws|throws
name|SerDeException
block|{
name|Properties
name|recordProperties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|recordProperties
operator|.
name|setProperty
argument_list|(
literal|"columns"
argument_list|,
name|columnNames
argument_list|)
expr_stmt|;
name|recordProperties
operator|.
name|setProperty
argument_list|(
literal|"columns.types"
argument_list|,
name|columnTypes
argument_list|)
expr_stmt|;
name|ParquetHiveSerDe
name|serDe
init|=
operator|new
name|ParquetHiveSerDe
argument_list|()
decl_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serDe
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|,
name|recordProperties
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
operator|new
name|ParquetHiveRecord
argument_list|(
name|serDe
operator|.
name|deserialize
argument_list|(
name|record
argument_list|)
argument_list|,
name|getObjectInspector
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|void
name|writeParquetRecord
parameter_list|(
name|String
name|schema
parameter_list|,
name|ParquetHiveRecord
name|record
parameter_list|)
throws|throws
name|SerDeException
block|{
name|MessageType
name|fileSchema
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
name|schema
argument_list|)
decl_stmt|;
name|DataWritableWriter
name|hiveParquetWriter
init|=
operator|new
name|DataWritableWriter
argument_list|(
name|mockRecordConsumer
argument_list|,
name|fileSchema
argument_list|)
decl_stmt|;
name|hiveParquetWriter
operator|.
name|write
argument_list|(
name|record
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSimpleType
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|columnNames
init|=
literal|"int,double,boolean,float,string,tinyint,smallint,bigint"
decl_stmt|;
name|String
name|columnTypes
init|=
literal|"int,double,boolean,float,string,tinyint,smallint,bigint"
decl_stmt|;
name|String
name|fileSchema
init|=
literal|"message hive_schema {\n"
operator|+
literal|"  optional int32 int;\n"
operator|+
literal|"  optional double double;\n"
operator|+
literal|"  optional boolean boolean;\n"
operator|+
literal|"  optional float float;\n"
operator|+
literal|"  optional binary string (UTF8);\n"
operator|+
literal|"  optional int32 tinyint;\n"
operator|+
literal|"  optional int32 smallint;\n"
operator|+
literal|"  optional int64 bigint;\n"
operator|+
literal|"}\n"
decl_stmt|;
name|ArrayWritable
name|hiveRecord
init|=
name|createGroup
argument_list|(
name|createInt
argument_list|(
literal|1
argument_list|)
argument_list|,
name|createDouble
argument_list|(
literal|1.0
argument_list|)
argument_list|,
name|createBoolean
argument_list|(
literal|true
argument_list|)
argument_list|,
name|createFloat
argument_list|(
literal|1.0f
argument_list|)
argument_list|,
name|createString
argument_list|(
literal|"one"
argument_list|)
argument_list|,
name|createTinyInt
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
argument_list|,
name|createSmallInt
argument_list|(
operator|(
name|short
operator|)
literal|1
argument_list|)
argument_list|,
name|createBigInt
argument_list|(
operator|(
name|long
operator|)
literal|1
argument_list|)
argument_list|)
decl_stmt|;
comment|// Write record to Parquet format
name|writeParquetRecord
argument_list|(
name|fileSchema
argument_list|,
name|getParquetWritable
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|,
name|hiveRecord
argument_list|)
argument_list|)
expr_stmt|;
comment|// Verify record was written correctly to Parquet
name|startMessage
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"int"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|addInteger
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"int"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|startField
argument_list|(
literal|"double"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|addDouble
argument_list|(
literal|1.0
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"double"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|startField
argument_list|(
literal|"boolean"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|addBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"boolean"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|startField
argument_list|(
literal|"float"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|addFloat
argument_list|(
literal|1.0f
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"float"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|startField
argument_list|(
literal|"string"
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|addString
argument_list|(
literal|"one"
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"string"
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|startField
argument_list|(
literal|"tinyint"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|addInteger
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"tinyint"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|startField
argument_list|(
literal|"smallint"
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|addInteger
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"smallint"
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|startField
argument_list|(
literal|"bigint"
argument_list|,
literal|7
argument_list|)
expr_stmt|;
name|addLong
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"bigint"
argument_list|,
literal|7
argument_list|)
expr_stmt|;
name|endMessage
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStructType
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|columnNames
init|=
literal|"structCol"
decl_stmt|;
name|String
name|columnTypes
init|=
literal|"struct<a:int,b:double,c:boolean>"
decl_stmt|;
name|String
name|fileSchema
init|=
literal|"message hive_schema {\n"
operator|+
literal|"  optional group structCol {\n"
operator|+
literal|"    optional int32 a;\n"
operator|+
literal|"    optional double b;\n"
operator|+
literal|"    optional boolean c;\n"
operator|+
literal|"  }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|ArrayWritable
name|hiveRecord
init|=
name|createGroup
argument_list|(
name|createGroup
argument_list|(
name|createInt
argument_list|(
literal|1
argument_list|)
argument_list|,
name|createDouble
argument_list|(
literal|1.0
argument_list|)
argument_list|,
name|createBoolean
argument_list|(
literal|true
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
comment|// Write record to Parquet format
name|writeParquetRecord
argument_list|(
name|fileSchema
argument_list|,
name|getParquetWritable
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|,
name|hiveRecord
argument_list|)
argument_list|)
expr_stmt|;
comment|// Verify record was written correctly to Parquet
name|startMessage
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"structCol"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|startGroup
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"a"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|addInteger
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"a"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|startField
argument_list|(
literal|"b"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|addDouble
argument_list|(
literal|1.0
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"b"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|startField
argument_list|(
literal|"c"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|addBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"c"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|endGroup
argument_list|()
expr_stmt|;
name|endField
argument_list|(
literal|"structCol"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|endMessage
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testArrayType
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|columnNames
init|=
literal|"arrayCol"
decl_stmt|;
name|String
name|columnTypes
init|=
literal|"array<int>"
decl_stmt|;
name|String
name|fileSchema
init|=
literal|"message hive_schema {\n"
operator|+
literal|"  optional group arrayCol (LIST) {\n"
operator|+
literal|"    repeated group array {\n"
operator|+
literal|"      optional int32 array_element;\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|ArrayWritable
name|hiveRecord
init|=
name|createGroup
argument_list|(
name|createGroup
argument_list|(
name|createArray
argument_list|(
name|createInt
argument_list|(
literal|1
argument_list|)
argument_list|,
name|createNull
argument_list|()
argument_list|,
name|createInt
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
comment|// Write record to Parquet format
name|writeParquetRecord
argument_list|(
name|fileSchema
argument_list|,
name|getParquetWritable
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|,
name|hiveRecord
argument_list|)
argument_list|)
expr_stmt|;
comment|// Verify record was written correctly to Parquet
name|startMessage
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"arrayCol"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|startGroup
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"array"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|startGroup
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"array_element"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|addInteger
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"array_element"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|endGroup
argument_list|()
expr_stmt|;
name|startGroup
argument_list|()
expr_stmt|;
name|endGroup
argument_list|()
expr_stmt|;
name|startGroup
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"array_element"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|addInteger
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"array_element"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|endGroup
argument_list|()
expr_stmt|;
name|endField
argument_list|(
literal|"array"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|endGroup
argument_list|()
expr_stmt|;
name|endField
argument_list|(
literal|"arrayCol"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|endMessage
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMapType
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|columnNames
init|=
literal|"mapCol"
decl_stmt|;
name|String
name|columnTypes
init|=
literal|"map<string,int>"
decl_stmt|;
name|String
name|fileSchema
init|=
literal|"message hive_schema {\n"
operator|+
literal|"  optional group mapCol (MAP) {\n"
operator|+
literal|"    repeated group map (MAP_KEY_VALUE) {\n"
operator|+
literal|"      required binary key;\n"
operator|+
literal|"      optional int32 value;\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|ArrayWritable
name|hiveRecord
init|=
name|createGroup
argument_list|(
name|createGroup
argument_list|(
name|createArray
argument_list|(
name|createArray
argument_list|(
name|createString
argument_list|(
literal|"key1"
argument_list|)
argument_list|,
name|createInt
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|,
name|createArray
argument_list|(
name|createString
argument_list|(
literal|"key2"
argument_list|)
argument_list|,
name|createInt
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|,
name|createArray
argument_list|(
name|createString
argument_list|(
literal|"key3"
argument_list|)
argument_list|,
name|createNull
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
comment|// Write record to Parquet format
name|writeParquetRecord
argument_list|(
name|fileSchema
argument_list|,
name|getParquetWritable
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|,
name|hiveRecord
argument_list|)
argument_list|)
expr_stmt|;
comment|// Verify record was written correctly to Parquet
name|startMessage
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"mapCol"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|startGroup
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"map"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|startGroup
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"key"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|addString
argument_list|(
literal|"key1"
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"key"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|startField
argument_list|(
literal|"value"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|addInteger
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"value"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|endGroup
argument_list|()
expr_stmt|;
name|startGroup
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"key"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|addString
argument_list|(
literal|"key2"
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"key"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|startField
argument_list|(
literal|"value"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|addInteger
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"value"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|endGroup
argument_list|()
expr_stmt|;
name|startGroup
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"key"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|addString
argument_list|(
literal|"key3"
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"key"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|endGroup
argument_list|()
expr_stmt|;
name|endField
argument_list|(
literal|"map"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|endGroup
argument_list|()
expr_stmt|;
name|endField
argument_list|(
literal|"mapCol"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|endMessage
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testArrayOfArrays
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|columnNames
init|=
literal|"array_of_arrays"
decl_stmt|;
name|String
name|columnTypes
init|=
literal|"array<array<int>>"
decl_stmt|;
name|String
name|fileSchema
init|=
literal|"message hive_schema {\n"
operator|+
literal|"  optional group array_of_arrays (LIST) {\n"
operator|+
literal|"    repeated group array {\n"
operator|+
literal|"      optional group array_element (LIST) {\n"
operator|+
literal|"        repeated group array {\n"
operator|+
literal|"          optional int32 array_element;\n"
operator|+
literal|"        }\n"
operator|+
literal|"      }\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|ArrayWritable
name|hiveRecord
init|=
name|createGroup
argument_list|(
name|createGroup
argument_list|(
name|createArray
argument_list|(
name|createGroup
argument_list|(
name|createArray
argument_list|(
name|createInt
argument_list|(
literal|1
argument_list|)
argument_list|,
name|createInt
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
comment|// Write record to Parquet format
name|writeParquetRecord
argument_list|(
name|fileSchema
argument_list|,
name|getParquetWritable
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|,
name|hiveRecord
argument_list|)
argument_list|)
expr_stmt|;
comment|// Verify record was written correctly to Parquet
name|startMessage
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"array_of_arrays"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|startGroup
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"array"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|startGroup
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"array_element"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|startGroup
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"array"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|startGroup
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"array_element"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|addInteger
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"array_element"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|endGroup
argument_list|()
expr_stmt|;
name|startGroup
argument_list|()
expr_stmt|;
name|startField
argument_list|(
literal|"array_element"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|addInteger
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|endField
argument_list|(
literal|"array_element"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|endGroup
argument_list|()
expr_stmt|;
name|endField
argument_list|(
literal|"array"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|endGroup
argument_list|()
expr_stmt|;
name|endField
argument_list|(
literal|"array_element"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|endGroup
argument_list|()
expr_stmt|;
name|endField
argument_list|(
literal|"array"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|endGroup
argument_list|()
expr_stmt|;
name|endField
argument_list|(
literal|"array_of_arrays"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|endMessage
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExpectedStructTypeOnRecord
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|columnNames
init|=
literal|"structCol"
decl_stmt|;
name|String
name|columnTypes
init|=
literal|"int"
decl_stmt|;
name|ArrayWritable
name|hiveRecord
init|=
name|createGroup
argument_list|(
name|createInt
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|fileSchema
init|=
literal|"message hive_schema {\n"
operator|+
literal|"  optional group structCol {\n"
operator|+
literal|"      optional int32 int;\n"
operator|+
literal|"    }\n"
operator|+
literal|"}\n"
decl_stmt|;
try|try
block|{
name|writeParquetRecord
argument_list|(
name|fileSchema
argument_list|,
name|getParquetWritable
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|,
name|hiveRecord
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Parquet record is malformed: Invalid data type: expected STRUCT type, but found: PRIMITIVE"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExpectedArrayTypeOnRecord
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|columnNames
init|=
literal|"arrayCol"
decl_stmt|;
name|String
name|columnTypes
init|=
literal|"int"
decl_stmt|;
name|ArrayWritable
name|hiveRecord
init|=
name|createGroup
argument_list|(
name|createInt
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|fileSchema
init|=
literal|"message hive_schema {\n"
operator|+
literal|"  optional group arrayCol (LIST) {\n"
operator|+
literal|"    repeated group bag {\n"
operator|+
literal|"      optional int32 array_element;\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}\n"
decl_stmt|;
try|try
block|{
name|writeParquetRecord
argument_list|(
name|fileSchema
argument_list|,
name|getParquetWritable
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|,
name|hiveRecord
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Parquet record is malformed: Invalid data type: expected LIST type, but found: PRIMITIVE"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExpectedMapTypeOnRecord
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|columnNames
init|=
literal|"mapCol"
decl_stmt|;
name|String
name|columnTypes
init|=
literal|"int"
decl_stmt|;
name|ArrayWritable
name|hiveRecord
init|=
name|createGroup
argument_list|(
name|createInt
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|fileSchema
init|=
literal|"message hive_schema {\n"
operator|+
literal|"  optional group mapCol (MAP) {\n"
operator|+
literal|"    repeated group map (MAP_KEY_VALUE) {\n"
operator|+
literal|"      required binary key;\n"
operator|+
literal|"      optional int32 value;\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}\n"
decl_stmt|;
try|try
block|{
name|writeParquetRecord
argument_list|(
name|fileSchema
argument_list|,
name|getParquetWritable
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|,
name|hiveRecord
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Parquet record is malformed: Invalid data type: expected MAP type, but found: PRIMITIVE"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

