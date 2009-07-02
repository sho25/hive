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
name|serde2
operator|.
name|lazy
package|;
end_package

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
name|Text
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_class
specifier|public
class|class
name|TestLazySimpleSerDe
extends|extends
name|TestCase
block|{
comment|/**    * Test the LazySimpleSerDe class.    */
specifier|public
name|void
name|testLazySimpleSerDe
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
comment|// Create the SerDe
name|LazySimpleSerDe
name|serDe
init|=
operator|new
name|LazySimpleSerDe
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Properties
name|tbl
init|=
name|createProperties
argument_list|()
decl_stmt|;
name|serDe
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
comment|// Data
name|Text
name|t
init|=
operator|new
name|Text
argument_list|(
literal|"123\t456\t789\t1000\t5.3\thive and hadoop\t1.\tNULL"
argument_list|)
decl_stmt|;
name|String
name|s
init|=
literal|"123\t456\t789\t1000\t5.3\thive and hadoop\tNULL\tNULL"
decl_stmt|;
name|Object
index|[]
name|expectedFieldsData
init|=
block|{
operator|new
name|ByteWritable
argument_list|(
operator|(
name|byte
operator|)
literal|123
argument_list|)
block|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|456
argument_list|)
block|,
operator|new
name|IntWritable
argument_list|(
literal|789
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|1000
argument_list|)
block|,
operator|new
name|DoubleWritable
argument_list|(
literal|5.3
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"hive and hadoop"
argument_list|)
block|,
literal|null
block|,
literal|null
block|}
decl_stmt|;
comment|// Test
name|deserializeAndSerialize
argument_list|(
name|serDe
argument_list|,
name|t
argument_list|,
name|s
argument_list|,
name|expectedFieldsData
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|private
name|void
name|deserializeAndSerialize
parameter_list|(
name|LazySimpleSerDe
name|serDe
parameter_list|,
name|Text
name|t
parameter_list|,
name|String
name|s
parameter_list|,
name|Object
index|[]
name|expectedFieldsData
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Get the row structure
name|StructObjectInspector
name|oi
init|=
operator|(
name|StructObjectInspector
operator|)
name|serDe
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fieldRefs
init|=
name|oi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|8
argument_list|,
name|fieldRefs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Deserialize
name|Object
name|row
init|=
name|serDe
operator|.
name|deserialize
argument_list|(
name|t
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
name|fieldRefs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|fieldData
init|=
name|oi
operator|.
name|getStructFieldData
argument_list|(
name|row
argument_list|,
name|fieldRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldData
operator|!=
literal|null
condition|)
block|{
name|fieldData
operator|=
operator|(
operator|(
name|LazyPrimitive
operator|)
name|fieldData
operator|)
operator|.
name|getWritableObject
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Field "
operator|+
name|i
argument_list|,
name|expectedFieldsData
index|[
name|i
index|]
argument_list|,
name|fieldData
argument_list|)
expr_stmt|;
block|}
comment|// Serialize
name|assertEquals
argument_list|(
name|Text
operator|.
name|class
argument_list|,
name|serDe
operator|.
name|getSerializedClass
argument_list|()
argument_list|)
expr_stmt|;
name|Text
name|serializedText
init|=
operator|(
name|Text
operator|)
name|serDe
operator|.
name|serialize
argument_list|(
name|row
argument_list|,
name|oi
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Serialized data"
argument_list|,
name|s
argument_list|,
name|serializedText
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Properties
name|createProperties
parameter_list|()
block|{
name|Properties
name|tbl
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
comment|// Set the configuration parameters
name|tbl
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
literal|"9"
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setProperty
argument_list|(
literal|"columns"
argument_list|,
literal|"abyte,ashort,aint,along,adouble,astring,anullint,anullstring"
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setProperty
argument_list|(
literal|"columns.types"
argument_list|,
literal|"tinyint:smallint:int:bigint:double:string:int:string"
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_NULL_FORMAT
argument_list|,
literal|"NULL"
argument_list|)
expr_stmt|;
return|return
name|tbl
return|;
block|}
comment|/**    * Test the LazySimpleSerDe class with LastColumnTakesRest option.    */
specifier|public
name|void
name|testLazySimpleSerDeLastColumnTakesRest
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
comment|// Create the SerDe
name|LazySimpleSerDe
name|serDe
init|=
operator|new
name|LazySimpleSerDe
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Properties
name|tbl
init|=
name|createProperties
argument_list|()
decl_stmt|;
name|tbl
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_LAST_COLUMN_TAKES_REST
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|serDe
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
comment|// Data
name|Text
name|t
init|=
operator|new
name|Text
argument_list|(
literal|"123\t456\t789\t1000\t5.3\thive and hadoop\t1.\ta\tb\t"
argument_list|)
decl_stmt|;
name|String
name|s
init|=
literal|"123\t456\t789\t1000\t5.3\thive and hadoop\tNULL\ta\tb\t"
decl_stmt|;
name|Object
index|[]
name|expectedFieldsData
init|=
block|{
operator|new
name|ByteWritable
argument_list|(
operator|(
name|byte
operator|)
literal|123
argument_list|)
block|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|456
argument_list|)
block|,
operator|new
name|IntWritable
argument_list|(
literal|789
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|1000
argument_list|)
block|,
operator|new
name|DoubleWritable
argument_list|(
literal|5.3
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"hive and hadoop"
argument_list|)
block|,
literal|null
block|,
operator|new
name|Text
argument_list|(
literal|"a\tb\t"
argument_list|)
block|}
decl_stmt|;
comment|// Test
name|deserializeAndSerialize
argument_list|(
name|serDe
argument_list|,
name|t
argument_list|,
name|s
argument_list|,
name|expectedFieldsData
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
comment|/**    * Test the LazySimpleSerDe class with extra columns.    */
specifier|public
name|void
name|testLazySimpleSerDeExtraColumns
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
comment|// Create the SerDe
name|LazySimpleSerDe
name|serDe
init|=
operator|new
name|LazySimpleSerDe
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Properties
name|tbl
init|=
name|createProperties
argument_list|()
decl_stmt|;
name|serDe
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
comment|// Data
name|Text
name|t
init|=
operator|new
name|Text
argument_list|(
literal|"123\t456\t789\t1000\t5.3\thive and hadoop\t1.\ta\tb\t"
argument_list|)
decl_stmt|;
name|String
name|s
init|=
literal|"123\t456\t789\t1000\t5.3\thive and hadoop\tNULL\ta"
decl_stmt|;
name|Object
index|[]
name|expectedFieldsData
init|=
block|{
operator|new
name|ByteWritable
argument_list|(
operator|(
name|byte
operator|)
literal|123
argument_list|)
block|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|456
argument_list|)
block|,
operator|new
name|IntWritable
argument_list|(
literal|789
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|1000
argument_list|)
block|,
operator|new
name|DoubleWritable
argument_list|(
literal|5.3
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"hive and hadoop"
argument_list|)
block|,
literal|null
block|,
operator|new
name|Text
argument_list|(
literal|"a"
argument_list|)
block|}
decl_stmt|;
comment|// Test
name|deserializeAndSerialize
argument_list|(
name|serDe
argument_list|,
name|t
argument_list|,
name|s
argument_list|,
name|expectedFieldsData
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
comment|/**    * Test the LazySimpleSerDe class with missing columns.    */
specifier|public
name|void
name|testLazySimpleSerDeMissingColumns
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
comment|// Create the SerDe
name|LazySimpleSerDe
name|serDe
init|=
operator|new
name|LazySimpleSerDe
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Properties
name|tbl
init|=
name|createProperties
argument_list|()
decl_stmt|;
name|serDe
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
comment|// Data
name|Text
name|t
init|=
operator|new
name|Text
argument_list|(
literal|"123\t456\t789\t1000\t5.3\t"
argument_list|)
decl_stmt|;
name|String
name|s
init|=
literal|"123\t456\t789\t1000\t5.3\t\tNULL\tNULL"
decl_stmt|;
name|Object
index|[]
name|expectedFieldsData
init|=
block|{
operator|new
name|ByteWritable
argument_list|(
operator|(
name|byte
operator|)
literal|123
argument_list|)
block|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|456
argument_list|)
block|,
operator|new
name|IntWritable
argument_list|(
literal|789
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|1000
argument_list|)
block|,
operator|new
name|DoubleWritable
argument_list|(
literal|5.3
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|""
argument_list|)
block|,
literal|null
block|,
literal|null
block|}
decl_stmt|;
comment|// Test
name|deserializeAndSerialize
argument_list|(
name|serDe
argument_list|,
name|t
argument_list|,
name|s
argument_list|,
name|expectedFieldsData
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
block|}
end_class

end_unit

