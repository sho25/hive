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
name|hive
operator|.
name|benchmark
operator|.
name|storage
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
name|orc
operator|.
name|OrcInputFormat
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
name|OrcOutputFormat
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
name|MapredParquetInputFormat
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
name|MapredParquetOutputFormat
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
name|serde2
operator|.
name|SerDe
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
name|PrimitiveTypeInfo
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
name|ListTypeInfo
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
name|MapTypeInfo
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
name|InputFormat
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
name|RecordReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|openjdk
operator|.
name|jmh
operator|.
name|annotations
operator|.
name|Param
import|;
end_import

begin_import
import|import
name|org
operator|.
name|openjdk
operator|.
name|jmh
operator|.
name|annotations
operator|.
name|Setup
import|;
end_import

begin_import
import|import
name|org
operator|.
name|openjdk
operator|.
name|jmh
operator|.
name|annotations
operator|.
name|TearDown
import|;
end_import

begin_import
import|import
name|org
operator|.
name|openjdk
operator|.
name|jmh
operator|.
name|annotations
operator|.
name|State
import|;
end_import

begin_import
import|import
name|org
operator|.
name|openjdk
operator|.
name|jmh
operator|.
name|annotations
operator|.
name|Benchmark
import|;
end_import

begin_import
import|import
name|org
operator|.
name|openjdk
operator|.
name|jmh
operator|.
name|annotations
operator|.
name|Level
import|;
end_import

begin_import
import|import
name|org
operator|.
name|openjdk
operator|.
name|jmh
operator|.
name|annotations
operator|.
name|Scope
import|;
end_import

begin_import
import|import
name|org
operator|.
name|openjdk
operator|.
name|jmh
operator|.
name|runner
operator|.
name|Runner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|openjdk
operator|.
name|jmh
operator|.
name|runner
operator|.
name|options
operator|.
name|Options
import|;
end_import

begin_import
import|import
name|org
operator|.
name|openjdk
operator|.
name|jmh
operator|.
name|runner
operator|.
name|options
operator|.
name|OptionsBuilder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|net
operator|.
name|URI
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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

begin_class
annotation|@
name|State
argument_list|(
name|Scope
operator|.
name|Benchmark
argument_list|)
specifier|public
class|class
name|ColumnarStorageBench
block|{
comment|/**   * This test measures the performance between different columnar storage formats used   * by Hive. If you need to add more formats, see the 'format' gobal variable to add   * a new one on the list, and create a class that implements StorageFormatTest interface.   *   * This test uses JMH framework for benchmarking.   * You may execute this benchmark tool using JMH command line in different ways:   *   * To use the settings shown in the main() function, use:   * $ java -cp target/benchmarks.jar org.apache.hive.benchmark.storage.ColumnarStorageBench   *   * To use the default settings used by JMH, use:   * $ java -jar target/benchmarks.jar org.apache.hive.benchmark.storage ColumnStorageBench   *   * To specify different parameters, use:   * - This command will use 10 warm-up iterations, 5 test iterations, and 2 forks. And it will   *   display the Average Time (avgt) in Microseconds (us)   * - Benchmark mode. Available modes are:   *   [Throughput/thrpt, AverageTime/avgt, SampleTime/sample, SingleShotTime/ss, All/all]   * - Output time unit. Available time units are: [m, s, ms, us, ns].   *   * $ java -jar target/benchmarks.jar org.apache.hive.benchmark.storage ColumnStorageBench -wi 10 -i 5 -f 2 -bm avgt -tu us   */
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_TEMP_LOCATION
init|=
literal|"/tmp"
decl_stmt|;
specifier|private
name|File
name|writeFile
decl_stmt|,
name|readFile
decl_stmt|,
name|recordWriterFile
decl_stmt|;
specifier|private
name|Path
name|writePath
decl_stmt|,
name|readPath
decl_stmt|,
name|recordWriterPath
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
comment|/**    * Contains implementation for the storage format to test    */
specifier|private
name|StorageFormatTest
name|storageFormatTest
decl_stmt|;
specifier|private
name|RecordWriter
name|recordWriter
decl_stmt|;
specifier|private
name|RecordReader
name|recordReader
decl_stmt|;
comment|/**    * These objects contains the record to be tested.    */
specifier|private
name|Writable
name|recordWritable
index|[]
decl_stmt|;
specifier|private
name|Object
name|rows
index|[]
decl_stmt|;
specifier|private
name|StructObjectInspector
name|oi
decl_stmt|;
comment|/**    * These column types are used for the record that will be tested.    */
specifier|private
name|Properties
name|recordProperties
decl_stmt|;
specifier|private
name|String
name|DEFAULT_COLUMN_TYPES
init|=
literal|"int,double,boolean,string,array<int>,map<string,string>,struct<a:int,b:int>"
decl_stmt|;
specifier|public
name|ColumnarStorageBench
parameter_list|()
block|{
name|recordProperties
operator|=
operator|new
name|Properties
argument_list|()
expr_stmt|;
name|recordProperties
operator|.
name|setProperty
argument_list|(
literal|"columns"
argument_list|,
name|getColumnNames
argument_list|(
name|DEFAULT_COLUMN_TYPES
argument_list|)
argument_list|)
expr_stmt|;
name|recordProperties
operator|.
name|setProperty
argument_list|(
literal|"columns.types"
argument_list|,
name|DEFAULT_COLUMN_TYPES
argument_list|)
expr_stmt|;
name|oi
operator|=
name|getObjectInspector
argument_list|(
name|DEFAULT_COLUMN_TYPES
argument_list|)
expr_stmt|;
specifier|final
name|int
name|NUMBER_OF_ROWS_TO_TEST
init|=
literal|100
decl_stmt|;
name|rows
operator|=
operator|new
name|Object
index|[
name|NUMBER_OF_ROWS_TO_TEST
index|]
expr_stmt|;
name|recordWritable
operator|=
operator|new
name|Writable
index|[
name|NUMBER_OF_ROWS_TO_TEST
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUMBER_OF_ROWS_TO_TEST
condition|;
name|i
operator|++
control|)
block|{
name|rows
index|[
name|i
index|]
operator|=
name|createRandomRow
argument_list|(
name|DEFAULT_COLUMN_TYPES
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|getColumnNames
parameter_list|(
specifier|final
name|String
name|columnTypes
parameter_list|)
block|{
name|StringBuilder
name|columnNames
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
comment|/* Construct a string of column names based on the number of column types */
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypesList
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
name|columnTypes
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
name|columnTypesList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|columnNames
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|columnNames
operator|.
name|append
argument_list|(
literal|"c"
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
return|return
name|columnNames
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|long
name|fileLength
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
operator|.
name|getLen
argument_list|()
return|;
block|}
specifier|private
name|ArrayWritable
name|record
parameter_list|(
name|Writable
modifier|...
name|fields
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
name|fields
argument_list|)
return|;
block|}
specifier|private
name|Writable
name|getPrimitiveWritable
parameter_list|(
specifier|final
name|PrimitiveTypeInfo
name|typeInfo
parameter_list|)
block|{
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|typeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|INT
case|:
return|return
operator|new
name|IntWritable
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|()
argument_list|)
return|;
case|case
name|DOUBLE
case|:
return|return
operator|new
name|DoubleWritable
argument_list|(
name|rand
operator|.
name|nextDouble
argument_list|()
argument_list|)
return|;
case|case
name|BOOLEAN
case|:
return|return
operator|new
name|BooleanWritable
argument_list|(
name|rand
operator|.
name|nextBoolean
argument_list|()
argument_list|)
return|;
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
case|case
name|STRING
case|:
name|byte
name|b
index|[]
init|=
operator|new
name|byte
index|[
literal|30
index|]
decl_stmt|;
name|rand
operator|.
name|nextBytes
argument_list|(
name|b
argument_list|)
expr_stmt|;
return|return
operator|new
name|BytesWritable
argument_list|(
name|b
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid primitive type: "
operator|+
name|typeInfo
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
name|ArrayWritable
name|createRecord
parameter_list|(
specifier|final
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
parameter_list|)
block|{
name|Writable
index|[]
name|fields
init|=
operator|new
name|Writable
index|[
name|columnTypes
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
for|for
control|(
name|TypeInfo
name|type
range|:
name|columnTypes
control|)
block|{
switch|switch
condition|(
name|type
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
name|fields
index|[
name|pos
operator|++
index|]
operator|=
name|getPrimitiveWritable
argument_list|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|type
argument_list|)
expr_stmt|;
break|break;
case|case
name|LIST
case|:
block|{
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|elementType
init|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
decl_stmt|;
name|elementType
operator|.
name|add
argument_list|(
operator|(
operator|(
name|ListTypeInfo
operator|)
name|type
operator|)
operator|.
name|getListElementTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|fields
index|[
name|pos
operator|++
index|]
operator|=
name|record
argument_list|(
name|createRecord
argument_list|(
name|elementType
argument_list|)
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|MAP
case|:
block|{
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|keyValueType
init|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
decl_stmt|;
name|keyValueType
operator|.
name|add
argument_list|(
operator|(
operator|(
name|MapTypeInfo
operator|)
name|type
operator|)
operator|.
name|getMapKeyTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|keyValueType
operator|.
name|add
argument_list|(
operator|(
operator|(
name|MapTypeInfo
operator|)
name|type
operator|)
operator|.
name|getMapValueTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|fields
index|[
name|pos
operator|++
index|]
operator|=
name|record
argument_list|(
name|record
argument_list|(
name|createRecord
argument_list|(
name|keyValueType
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|STRUCT
case|:
block|{
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|elementType
init|=
operator|(
operator|(
name|StructTypeInfo
operator|)
name|type
operator|)
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
decl_stmt|;
name|fields
index|[
name|pos
operator|++
index|]
operator|=
name|createRecord
argument_list|(
name|elementType
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Invalid column type: "
operator|+
name|type
argument_list|)
throw|;
block|}
block|}
return|return
name|record
argument_list|(
name|fields
argument_list|)
return|;
block|}
specifier|private
name|StructObjectInspector
name|getObjectInspector
parameter_list|(
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
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
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
name|Arrays
operator|.
name|asList
argument_list|(
name|getColumnNames
argument_list|(
name|columnTypes
argument_list|)
operator|.
name|split
argument_list|(
literal|","
argument_list|)
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
name|Object
name|createRandomRow
parameter_list|(
specifier|final
name|String
name|columnTypes
parameter_list|)
block|{
return|return
name|createRecord
argument_list|(
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
name|columnTypes
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * This class encapsulates all methods that will be called by each of the @Benchmark    * methods.    */
specifier|private
class|class
name|StorageFormatTest
block|{
specifier|private
name|SerDe
name|serDe
decl_stmt|;
specifier|private
name|JobConf
name|jobConf
decl_stmt|;
specifier|private
name|HiveOutputFormat
name|outputFormat
decl_stmt|;
specifier|private
name|InputFormat
name|inputFormat
decl_stmt|;
specifier|public
name|StorageFormatTest
parameter_list|(
name|SerDe
name|serDeImpl
parameter_list|,
name|HiveOutputFormat
name|outputFormatImpl
parameter_list|,
name|InputFormat
name|inputFormatImpl
parameter_list|)
throws|throws
name|SerDeException
block|{
name|jobConf
operator|=
operator|new
name|JobConf
argument_list|()
expr_stmt|;
name|serDe
operator|=
name|serDeImpl
expr_stmt|;
name|outputFormat
operator|=
name|outputFormatImpl
expr_stmt|;
name|inputFormat
operator|=
name|inputFormatImpl
expr_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serDe
argument_list|,
name|conf
argument_list|,
name|recordProperties
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Writable
name|serialize
parameter_list|(
name|Object
name|row
parameter_list|,
name|StructObjectInspector
name|oi
parameter_list|)
throws|throws
name|SerDeException
block|{
return|return
name|serDe
operator|.
name|serialize
argument_list|(
name|row
argument_list|,
name|oi
argument_list|)
return|;
block|}
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|record
parameter_list|)
throws|throws
name|SerDeException
block|{
return|return
name|serDe
operator|.
name|deserialize
argument_list|(
name|record
argument_list|)
return|;
block|}
comment|/* We write many records because sometimes the RecordWriter for the format to test      * behaves different with one record than a bunch of records */
specifier|public
name|void
name|writeRecords
parameter_list|(
name|RecordWriter
name|writer
parameter_list|,
name|Writable
name|records
index|[]
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|records
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|writer
operator|.
name|write
argument_list|(
name|records
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|/* We read many records because sometimes the RecordReader for the format to test      * behaves different with one record than a bunch of records */
specifier|public
name|Object
name|readRecords
parameter_list|(
name|RecordReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
name|Object
name|alwaysNull
init|=
name|reader
operator|.
name|createKey
argument_list|()
decl_stmt|;
name|Object
name|record
init|=
name|reader
operator|.
name|createValue
argument_list|()
decl_stmt|;
comment|// Just loop through all values. We do not need to store anything though.
comment|// This is just for test purposes
while|while
condition|(
name|reader
operator|.
name|next
argument_list|(
name|alwaysNull
argument_list|,
name|record
argument_list|)
condition|)
empty_stmt|;
return|return
name|record
return|;
block|}
specifier|public
name|RecordWriter
name|getRecordWriter
parameter_list|(
name|Path
name|outputPath
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|outputFormat
operator|.
name|getHiveRecordWriter
argument_list|(
name|jobConf
argument_list|,
name|outputPath
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
name|recordProperties
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
name|RecordReader
name|getRecordReader
parameter_list|(
name|Path
name|inputPath
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|inputFormat
operator|.
name|getRecordReader
argument_list|(
operator|new
name|FileSplit
argument_list|(
name|inputPath
argument_list|,
literal|0
argument_list|,
name|fileLength
argument_list|(
name|inputPath
argument_list|)
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
argument_list|,
name|jobConf
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
comment|/**    * This class is called to run I/O parquet tests.    */
specifier|private
class|class
name|ParquetStorageFormatTest
extends|extends
name|StorageFormatTest
block|{
specifier|public
name|ParquetStorageFormatTest
parameter_list|()
throws|throws
name|SerDeException
block|{
name|super
argument_list|(
operator|new
name|ParquetHiveSerDe
argument_list|()
argument_list|,
operator|new
name|MapredParquetOutputFormat
argument_list|()
argument_list|,
operator|new
name|MapredParquetInputFormat
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This class is called to run i/o orc tests.    */
specifier|private
class|class
name|OrcStorageFormatTest
extends|extends
name|StorageFormatTest
block|{
specifier|public
name|OrcStorageFormatTest
parameter_list|()
throws|throws
name|SerDeException
block|{
name|super
argument_list|(
operator|new
name|OrcSerde
argument_list|()
argument_list|,
operator|new
name|OrcOutputFormat
argument_list|()
argument_list|,
operator|new
name|OrcInputFormat
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|File
name|createTempFile
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|URI
operator|.
name|create
argument_list|(
name|DEFAULT_TEMP_LOCATION
argument_list|)
operator|.
name|getScheme
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot create temporary files in a non-local file-system: Operation not permitted."
argument_list|)
throw|;
block|}
name|File
name|temp
init|=
name|File
operator|.
name|createTempFile
argument_list|(
name|this
operator|.
name|toString
argument_list|()
argument_list|,
literal|null
argument_list|,
operator|new
name|File
argument_list|(
name|DEFAULT_TEMP_LOCATION
argument_list|)
argument_list|)
decl_stmt|;
name|temp
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|temp
operator|.
name|delete
argument_list|()
expr_stmt|;
return|return
name|temp
return|;
block|}
comment|// Test different format types
annotation|@
name|Param
argument_list|(
block|{
literal|"orc"
block|,
literal|"parquet"
block|}
argument_list|)
specifier|public
name|String
name|format
decl_stmt|;
comment|/**    * Initializes resources that will be needed for each of the benchmark tests.    *    * @throws SerDeException If it cannot initialize the desired test format.    * @throws IOException If it cannot write data to temporary files.    */
annotation|@
name|Setup
argument_list|(
name|Level
operator|.
name|Trial
argument_list|)
specifier|public
name|void
name|prepareBenchmark
parameter_list|()
throws|throws
name|SerDeException
throws|,
name|IOException
block|{
if|if
condition|(
name|format
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"parquet"
argument_list|)
condition|)
block|{
name|storageFormatTest
operator|=
operator|new
name|ParquetStorageFormatTest
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|format
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"orc"
argument_list|)
condition|)
block|{
name|storageFormatTest
operator|=
operator|new
name|OrcStorageFormatTest
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid file format argument: "
operator|+
name|format
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rows
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|recordWritable
index|[
name|i
index|]
operator|=
name|storageFormatTest
operator|.
name|serialize
argument_list|(
name|rows
index|[
name|i
index|]
argument_list|,
name|oi
argument_list|)
expr_stmt|;
block|}
name|fs
operator|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
expr_stmt|;
name|writeFile
operator|=
name|createTempFile
argument_list|()
expr_stmt|;
name|writePath
operator|=
operator|new
name|Path
argument_list|(
name|writeFile
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|readFile
operator|=
name|createTempFile
argument_list|()
expr_stmt|;
name|readPath
operator|=
operator|new
name|Path
argument_list|(
name|readFile
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
comment|/*      * Write a bunch of random rows that will be used for read benchmark.      */
name|RecordWriter
name|writer
init|=
name|storageFormatTest
operator|.
name|getRecordWriter
argument_list|(
name|readPath
argument_list|)
decl_stmt|;
name|storageFormatTest
operator|.
name|writeRecords
argument_list|(
name|writer
argument_list|,
name|recordWritable
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * It deletes any temporary file created by prepareBenchmark.    */
annotation|@
name|TearDown
argument_list|(
name|Level
operator|.
name|Trial
argument_list|)
specifier|public
name|void
name|cleanUpBenchmark
parameter_list|()
block|{
name|readFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
comment|/**    * This method is invoked before every call to the methods to test. It creates    * resources that are needed for each call (not in a benchmark level).    *    * @throws IOException If it cannot writes temporary files.    */
annotation|@
name|Setup
argument_list|(
name|Level
operator|.
name|Invocation
argument_list|)
specifier|public
name|void
name|prepareInvocation
parameter_list|()
throws|throws
name|IOException
block|{
name|recordWriterFile
operator|=
name|createTempFile
argument_list|()
expr_stmt|;
name|recordWriterPath
operator|=
operator|new
name|Path
argument_list|(
name|recordWriterFile
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|recordWriter
operator|=
name|storageFormatTest
operator|.
name|getRecordWriter
argument_list|(
name|writePath
argument_list|)
expr_stmt|;
name|recordReader
operator|=
name|storageFormatTest
operator|.
name|getRecordReader
argument_list|(
name|readPath
argument_list|)
expr_stmt|;
block|}
comment|/**    * This method is invoked after every call to the methods to test. It closes    * and cleans up all temporary files.    *    * @throws IOException If it cannot close or delete temporary files.    */
annotation|@
name|TearDown
argument_list|(
name|Level
operator|.
name|Invocation
argument_list|)
specifier|public
name|void
name|cleanUpInvocation
parameter_list|()
throws|throws
name|IOException
block|{
name|recordWriter
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|recordReader
operator|.
name|close
argument_list|()
expr_stmt|;
name|recordWriterFile
operator|.
name|delete
argument_list|()
expr_stmt|;
name|writeFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Benchmark
specifier|public
name|void
name|write
parameter_list|()
throws|throws
name|IOException
block|{
name|storageFormatTest
operator|.
name|writeRecords
argument_list|(
name|recordWriter
argument_list|,
name|recordWritable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Benchmark
specifier|public
name|Object
name|read
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|storageFormatTest
operator|.
name|readRecords
argument_list|(
name|recordReader
argument_list|)
return|;
block|}
annotation|@
name|Benchmark
specifier|public
name|Writable
name|serialize
parameter_list|()
throws|throws
name|SerDeException
block|{
return|return
name|storageFormatTest
operator|.
name|serialize
argument_list|(
name|rows
index|[
literal|0
index|]
argument_list|,
name|oi
argument_list|)
return|;
block|}
annotation|@
name|Benchmark
specifier|public
name|Object
name|deserialize
parameter_list|()
throws|throws
name|SerDeException
block|{
return|return
name|storageFormatTest
operator|.
name|deserialize
argument_list|(
name|recordWritable
index|[
literal|0
index|]
argument_list|)
return|;
block|}
annotation|@
name|Benchmark
specifier|public
name|RecordWriter
name|getRecordWriter
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|storageFormatTest
operator|.
name|getRecordWriter
argument_list|(
name|recordWriterPath
argument_list|)
return|;
block|}
annotation|@
name|Benchmark
specifier|public
name|RecordReader
name|getRecordReader
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|storageFormatTest
operator|.
name|getRecordReader
argument_list|(
name|readPath
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
name|args
index|[]
parameter_list|)
throws|throws
name|Exception
block|{
name|Options
name|opt
init|=
operator|new
name|OptionsBuilder
argument_list|()
operator|.
name|include
argument_list|(
name|ColumnarStorageBench
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
operator|.
name|warmupIterations
argument_list|(
literal|1
argument_list|)
operator|.
name|measurementIterations
argument_list|(
literal|1
argument_list|)
operator|.
name|forks
argument_list|(
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
operator|new
name|Runner
argument_list|(
name|opt
argument_list|)
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

