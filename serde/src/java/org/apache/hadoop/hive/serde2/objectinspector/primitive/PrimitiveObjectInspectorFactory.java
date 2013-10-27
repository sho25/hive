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
name|objectinspector
operator|.
name|primitive
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|serdeConstants
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
name|DateWritable
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
name|HiveDecimalWritable
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
name|HiveVarcharWritable
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
name|TimestampWritable
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
name|ConstantObjectInspector
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
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
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
name|primitive
operator|.
name|PrimitiveObjectInspectorUtils
operator|.
name|PrimitiveTypeEntry
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
name|DecimalTypeInfo
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
name|VarcharTypeInfo
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
name|Text
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
comment|/**  * PrimitiveObjectInspectorFactory is the primary way to create new  * PrimitiveObjectInspector instances.  *  * The reason of having caches here is that ObjectInspector is because  * ObjectInspectors do not have an internal state - so ObjectInspectors with the  * same construction parameters should result in exactly the same  * ObjectInspector.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|PrimitiveObjectInspectorFactory
block|{
specifier|public
specifier|static
specifier|final
name|WritableBooleanObjectInspector
name|writableBooleanObjectInspector
init|=
operator|new
name|WritableBooleanObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|WritableByteObjectInspector
name|writableByteObjectInspector
init|=
operator|new
name|WritableByteObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|WritableShortObjectInspector
name|writableShortObjectInspector
init|=
operator|new
name|WritableShortObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|WritableIntObjectInspector
name|writableIntObjectInspector
init|=
operator|new
name|WritableIntObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|WritableLongObjectInspector
name|writableLongObjectInspector
init|=
operator|new
name|WritableLongObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|WritableFloatObjectInspector
name|writableFloatObjectInspector
init|=
operator|new
name|WritableFloatObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|WritableDoubleObjectInspector
name|writableDoubleObjectInspector
init|=
operator|new
name|WritableDoubleObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|WritableStringObjectInspector
name|writableStringObjectInspector
init|=
operator|new
name|WritableStringObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|WritableVoidObjectInspector
name|writableVoidObjectInspector
init|=
operator|new
name|WritableVoidObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|WritableDateObjectInspector
name|writableDateObjectInspector
init|=
operator|new
name|WritableDateObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|WritableTimestampObjectInspector
name|writableTimestampObjectInspector
init|=
operator|new
name|WritableTimestampObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|WritableBinaryObjectInspector
name|writableBinaryObjectInspector
init|=
operator|new
name|WritableBinaryObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|WritableHiveDecimalObjectInspector
name|writableHiveDecimalObjectInspector
init|=
operator|new
name|WritableHiveDecimalObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|decimalTypeInfo
argument_list|)
decl_stmt|;
comment|// Map from PrimitiveTypeInfo to AbstractPrimitiveWritableObjectInspector.
specifier|private
specifier|static
name|HashMap
argument_list|<
name|PrimitiveTypeInfo
argument_list|,
name|AbstractPrimitiveWritableObjectInspector
argument_list|>
name|cachedPrimitiveWritableInspectorCache
init|=
operator|new
name|HashMap
argument_list|<
name|PrimitiveTypeInfo
argument_list|,
name|AbstractPrimitiveWritableObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|cachedPrimitiveWritableInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|BOOLEAN_TYPE_NAME
argument_list|)
argument_list|,
name|writableBooleanObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveWritableInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|TINYINT_TYPE_NAME
argument_list|)
argument_list|,
name|writableByteObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveWritableInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|SMALLINT_TYPE_NAME
argument_list|)
argument_list|,
name|writableShortObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveWritableInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|INT_TYPE_NAME
argument_list|)
argument_list|,
name|writableIntObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveWritableInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|BIGINT_TYPE_NAME
argument_list|)
argument_list|,
name|writableLongObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveWritableInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|FLOAT_TYPE_NAME
argument_list|)
argument_list|,
name|writableFloatObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveWritableInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|DOUBLE_TYPE_NAME
argument_list|)
argument_list|,
name|writableDoubleObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveWritableInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|)
argument_list|,
name|writableStringObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveWritableInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|VOID_TYPE_NAME
argument_list|)
argument_list|,
name|writableVoidObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveWritableInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|DATE_TYPE_NAME
argument_list|)
argument_list|,
name|writableDateObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveWritableInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|TIMESTAMP_TYPE_NAME
argument_list|)
argument_list|,
name|writableTimestampObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveWritableInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|BINARY_TYPE_NAME
argument_list|)
argument_list|,
name|writableBinaryObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveWritableInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|decimalTypeInfo
argument_list|,
name|writableHiveDecimalObjectInspector
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|Map
argument_list|<
name|PrimitiveCategory
argument_list|,
name|AbstractPrimitiveWritableObjectInspector
argument_list|>
name|primitiveCategoryToWritableOI
init|=
operator|new
name|EnumMap
argument_list|<
name|PrimitiveCategory
argument_list|,
name|AbstractPrimitiveWritableObjectInspector
argument_list|>
argument_list|(
name|PrimitiveCategory
operator|.
name|class
argument_list|)
decl_stmt|;
static|static
block|{
name|primitiveCategoryToWritableOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|BOOLEAN
argument_list|,
name|writableBooleanObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToWritableOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|BYTE
argument_list|,
name|writableByteObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToWritableOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|SHORT
argument_list|,
name|writableShortObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToWritableOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|INT
argument_list|,
name|writableIntObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToWritableOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|LONG
argument_list|,
name|writableLongObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToWritableOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|FLOAT
argument_list|,
name|writableFloatObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToWritableOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|DOUBLE
argument_list|,
name|writableDoubleObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToWritableOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|STRING
argument_list|,
name|writableStringObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToWritableOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|VOID
argument_list|,
name|writableVoidObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToWritableOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|DATE
argument_list|,
name|writableDateObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToWritableOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|TIMESTAMP
argument_list|,
name|writableTimestampObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToWritableOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|BINARY
argument_list|,
name|writableBinaryObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToWritableOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|DECIMAL
argument_list|,
name|writableHiveDecimalObjectInspector
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
name|JavaBooleanObjectInspector
name|javaBooleanObjectInspector
init|=
operator|new
name|JavaBooleanObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|JavaByteObjectInspector
name|javaByteObjectInspector
init|=
operator|new
name|JavaByteObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|JavaShortObjectInspector
name|javaShortObjectInspector
init|=
operator|new
name|JavaShortObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|JavaIntObjectInspector
name|javaIntObjectInspector
init|=
operator|new
name|JavaIntObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|JavaLongObjectInspector
name|javaLongObjectInspector
init|=
operator|new
name|JavaLongObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|JavaFloatObjectInspector
name|javaFloatObjectInspector
init|=
operator|new
name|JavaFloatObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|JavaDoubleObjectInspector
name|javaDoubleObjectInspector
init|=
operator|new
name|JavaDoubleObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|JavaStringObjectInspector
name|javaStringObjectInspector
init|=
operator|new
name|JavaStringObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|JavaVoidObjectInspector
name|javaVoidObjectInspector
init|=
operator|new
name|JavaVoidObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|JavaDateObjectInspector
name|javaDateObjectInspector
init|=
operator|new
name|JavaDateObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|JavaTimestampObjectInspector
name|javaTimestampObjectInspector
init|=
operator|new
name|JavaTimestampObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|JavaBinaryObjectInspector
name|javaByteArrayObjectInspector
init|=
operator|new
name|JavaBinaryObjectInspector
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|JavaHiveDecimalObjectInspector
name|javaHiveDecimalObjectInspector
init|=
operator|new
name|JavaHiveDecimalObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|decimalTypeInfo
argument_list|)
decl_stmt|;
comment|// Map from PrimitiveTypeInfo to AbstractPrimitiveJavaObjectInspector.
specifier|private
specifier|static
name|HashMap
argument_list|<
name|PrimitiveTypeInfo
argument_list|,
name|AbstractPrimitiveJavaObjectInspector
argument_list|>
name|cachedPrimitiveJavaInspectorCache
init|=
operator|new
name|HashMap
argument_list|<
name|PrimitiveTypeInfo
argument_list|,
name|AbstractPrimitiveJavaObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|cachedPrimitiveJavaInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|BOOLEAN_TYPE_NAME
argument_list|)
argument_list|,
name|javaBooleanObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveJavaInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|TINYINT_TYPE_NAME
argument_list|)
argument_list|,
name|javaByteObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveJavaInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|SMALLINT_TYPE_NAME
argument_list|)
argument_list|,
name|javaShortObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveJavaInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|INT_TYPE_NAME
argument_list|)
argument_list|,
name|javaIntObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveJavaInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|BIGINT_TYPE_NAME
argument_list|)
argument_list|,
name|javaLongObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveJavaInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|FLOAT_TYPE_NAME
argument_list|)
argument_list|,
name|javaFloatObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveJavaInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|DOUBLE_TYPE_NAME
argument_list|)
argument_list|,
name|javaDoubleObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveJavaInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|)
argument_list|,
name|javaStringObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveJavaInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|VOID_TYPE_NAME
argument_list|)
argument_list|,
name|javaVoidObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveJavaInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|DATE_TYPE_NAME
argument_list|)
argument_list|,
name|javaDateObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveJavaInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|TIMESTAMP_TYPE_NAME
argument_list|)
argument_list|,
name|javaTimestampObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveJavaInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|serdeConstants
operator|.
name|BINARY_TYPE_NAME
argument_list|)
argument_list|,
name|javaByteArrayObjectInspector
argument_list|)
expr_stmt|;
name|cachedPrimitiveJavaInspectorCache
operator|.
name|put
argument_list|(
name|TypeInfoFactory
operator|.
name|decimalTypeInfo
argument_list|,
name|javaHiveDecimalObjectInspector
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|Map
argument_list|<
name|PrimitiveCategory
argument_list|,
name|AbstractPrimitiveJavaObjectInspector
argument_list|>
name|primitiveCategoryToJavaOI
init|=
operator|new
name|EnumMap
argument_list|<
name|PrimitiveCategory
argument_list|,
name|AbstractPrimitiveJavaObjectInspector
argument_list|>
argument_list|(
name|PrimitiveCategory
operator|.
name|class
argument_list|)
decl_stmt|;
static|static
block|{
name|primitiveCategoryToJavaOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|BOOLEAN
argument_list|,
name|javaBooleanObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToJavaOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|BYTE
argument_list|,
name|javaByteObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToJavaOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|SHORT
argument_list|,
name|javaShortObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToJavaOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|INT
argument_list|,
name|javaIntObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToJavaOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|LONG
argument_list|,
name|javaLongObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToJavaOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|FLOAT
argument_list|,
name|javaFloatObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToJavaOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|DOUBLE
argument_list|,
name|javaDoubleObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToJavaOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|STRING
argument_list|,
name|javaStringObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToJavaOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|VOID
argument_list|,
name|javaVoidObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToJavaOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|DATE
argument_list|,
name|javaDateObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToJavaOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|TIMESTAMP
argument_list|,
name|javaTimestampObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToJavaOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|BINARY
argument_list|,
name|javaByteArrayObjectInspector
argument_list|)
expr_stmt|;
name|primitiveCategoryToJavaOI
operator|.
name|put
argument_list|(
name|PrimitiveCategory
operator|.
name|DECIMAL
argument_list|,
name|javaHiveDecimalObjectInspector
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns the PrimitiveWritableObjectInspector for the PrimitiveCategory.    *    * @param primitiveCategory    */
specifier|public
specifier|static
name|AbstractPrimitiveWritableObjectInspector
name|getPrimitiveWritableObjectInspector
parameter_list|(
name|PrimitiveCategory
name|primitiveCategory
parameter_list|)
block|{
name|AbstractPrimitiveWritableObjectInspector
name|result
init|=
name|primitiveCategoryToWritableOI
operator|.
name|get
argument_list|(
name|primitiveCategory
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Internal error: Cannot find ObjectInspector "
operator|+
literal|" for "
operator|+
name|primitiveCategory
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Returns the PrimitiveWritableObjectInspector for the given type info    * @param PrimitiveTypeInfo    PrimitiveTypeInfo instance    * @return AbstractPrimitiveWritableObjectInspector instance    */
specifier|public
specifier|static
name|AbstractPrimitiveWritableObjectInspector
name|getPrimitiveWritableObjectInspector
parameter_list|(
name|PrimitiveTypeInfo
name|typeInfo
parameter_list|)
block|{
name|AbstractPrimitiveWritableObjectInspector
name|result
init|=
name|cachedPrimitiveWritableInspectorCache
operator|.
name|get
argument_list|(
name|typeInfo
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
return|return
name|result
return|;
block|}
switch|switch
condition|(
name|typeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|VARCHAR
case|:
name|result
operator|=
operator|new
name|WritableHiveVarcharObjectInspector
argument_list|(
operator|(
name|VarcharTypeInfo
operator|)
name|typeInfo
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|result
operator|=
operator|new
name|WritableHiveDecimalObjectInspector
argument_list|(
operator|(
name|DecimalTypeInfo
operator|)
name|typeInfo
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to create object inspector for "
operator|+
name|typeInfo
argument_list|)
throw|;
block|}
name|cachedPrimitiveWritableInspectorCache
operator|.
name|put
argument_list|(
name|typeInfo
argument_list|,
name|result
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Returns a PrimitiveWritableObjectInspector which implements ConstantObjectInspector    * for the PrimitiveCategory.    *    * @param primitiveCategory    * @param value    */
specifier|public
specifier|static
name|ConstantObjectInspector
name|getPrimitiveWritableConstantObjectInspector
parameter_list|(
name|PrimitiveTypeInfo
name|typeInfo
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
switch|switch
condition|(
name|typeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
return|return
operator|new
name|WritableConstantBooleanObjectInspector
argument_list|(
operator|(
name|BooleanWritable
operator|)
name|value
argument_list|)
return|;
case|case
name|BYTE
case|:
return|return
operator|new
name|WritableConstantByteObjectInspector
argument_list|(
operator|(
name|ByteWritable
operator|)
name|value
argument_list|)
return|;
case|case
name|SHORT
case|:
return|return
operator|new
name|WritableConstantShortObjectInspector
argument_list|(
operator|(
name|ShortWritable
operator|)
name|value
argument_list|)
return|;
case|case
name|INT
case|:
return|return
operator|new
name|WritableConstantIntObjectInspector
argument_list|(
operator|(
name|IntWritable
operator|)
name|value
argument_list|)
return|;
case|case
name|LONG
case|:
return|return
operator|new
name|WritableConstantLongObjectInspector
argument_list|(
operator|(
name|LongWritable
operator|)
name|value
argument_list|)
return|;
case|case
name|FLOAT
case|:
return|return
operator|new
name|WritableConstantFloatObjectInspector
argument_list|(
operator|(
name|FloatWritable
operator|)
name|value
argument_list|)
return|;
case|case
name|DOUBLE
case|:
return|return
operator|new
name|WritableConstantDoubleObjectInspector
argument_list|(
operator|(
name|DoubleWritable
operator|)
name|value
argument_list|)
return|;
case|case
name|STRING
case|:
return|return
operator|new
name|WritableConstantStringObjectInspector
argument_list|(
operator|(
name|Text
operator|)
name|value
argument_list|)
return|;
case|case
name|VARCHAR
case|:
return|return
operator|new
name|WritableConstantHiveVarcharObjectInspector
argument_list|(
operator|(
name|VarcharTypeInfo
operator|)
name|typeInfo
argument_list|,
operator|(
name|HiveVarcharWritable
operator|)
name|value
argument_list|)
return|;
case|case
name|DATE
case|:
return|return
operator|new
name|WritableConstantDateObjectInspector
argument_list|(
operator|(
name|DateWritable
operator|)
name|value
argument_list|)
return|;
case|case
name|TIMESTAMP
case|:
return|return
operator|new
name|WritableConstantTimestampObjectInspector
argument_list|(
operator|(
name|TimestampWritable
operator|)
name|value
argument_list|)
return|;
case|case
name|DECIMAL
case|:
return|return
operator|new
name|WritableConstantHiveDecimalObjectInspector
argument_list|(
operator|(
name|DecimalTypeInfo
operator|)
name|typeInfo
argument_list|,
operator|(
name|HiveDecimalWritable
operator|)
name|value
argument_list|)
return|;
case|case
name|BINARY
case|:
return|return
operator|new
name|WritableConstantBinaryObjectInspector
argument_list|(
operator|(
name|BytesWritable
operator|)
name|value
argument_list|)
return|;
case|case
name|VOID
case|:
return|return
operator|new
name|WritableVoidObjectInspector
argument_list|()
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Internal error: Cannot find "
operator|+
literal|"ConstantObjectInspector for "
operator|+
name|typeInfo
argument_list|)
throw|;
block|}
block|}
comment|/**    * Returns the PrimitiveJavaObjectInspector for the PrimitiveCategory.    *    * @param primitiveCategory    */
specifier|public
specifier|static
name|AbstractPrimitiveJavaObjectInspector
name|getPrimitiveJavaObjectInspector
parameter_list|(
name|PrimitiveCategory
name|primitiveCategory
parameter_list|)
block|{
name|AbstractPrimitiveJavaObjectInspector
name|result
init|=
name|primitiveCategoryToJavaOI
operator|.
name|get
argument_list|(
name|primitiveCategory
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Internal error: Cannot find ObjectInspector "
operator|+
literal|" for "
operator|+
name|primitiveCategory
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Returns the PrimitiveJavaObjectInspector for the given PrimitiveTypeInfo instance,    * @param PrimitiveTypeInfo    PrimitiveTypeInfo instance    * @return AbstractPrimitiveJavaObjectInspector instance    */
specifier|public
specifier|static
name|AbstractPrimitiveJavaObjectInspector
name|getPrimitiveJavaObjectInspector
parameter_list|(
name|PrimitiveTypeInfo
name|typeInfo
parameter_list|)
block|{
name|AbstractPrimitiveJavaObjectInspector
name|result
init|=
name|cachedPrimitiveJavaInspectorCache
operator|.
name|get
argument_list|(
name|typeInfo
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
return|return
name|result
return|;
block|}
switch|switch
condition|(
name|typeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|VARCHAR
case|:
name|result
operator|=
operator|new
name|JavaHiveVarcharObjectInspector
argument_list|(
operator|(
name|VarcharTypeInfo
operator|)
name|typeInfo
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|result
operator|=
operator|new
name|JavaHiveDecimalObjectInspector
argument_list|(
operator|(
name|DecimalTypeInfo
operator|)
name|typeInfo
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to create JavaHiveVarcharObjectInspector for "
operator|+
name|typeInfo
argument_list|)
throw|;
block|}
name|cachedPrimitiveJavaInspectorCache
operator|.
name|put
argument_list|(
name|typeInfo
argument_list|,
name|result
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Returns an ObjectInspector for a primitive Class. The Class can be a Hive    * Writable class, or a Java Primitive Class.    *    * A runtimeException will be thrown if the class is not recognized as a    * primitive type by Hive.    */
specifier|public
specifier|static
name|PrimitiveObjectInspector
name|getPrimitiveObjectInspectorFromClass
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
if|if
condition|(
name|Writable
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|c
argument_list|)
condition|)
block|{
comment|// It is a writable class
name|PrimitiveTypeEntry
name|te
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getTypeEntryFromPrimitiveWritableClass
argument_list|(
name|c
argument_list|)
decl_stmt|;
if|if
condition|(
name|te
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Internal error: Cannot recognize "
operator|+
name|c
argument_list|)
throw|;
block|}
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|te
operator|.
name|primitiveCategory
argument_list|)
return|;
block|}
else|else
block|{
comment|// It is a Java class
name|PrimitiveTypeEntry
name|te
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getTypeEntryFromPrimitiveJavaClass
argument_list|(
name|c
argument_list|)
decl_stmt|;
if|if
condition|(
name|te
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Internal error: Cannot recognize "
operator|+
name|c
argument_list|)
throw|;
block|}
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|te
operator|.
name|primitiveCategory
argument_list|)
return|;
block|}
block|}
specifier|private
name|PrimitiveObjectInspectorFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

