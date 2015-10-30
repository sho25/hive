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
operator|.
name|persistence
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
name|ObjectOutputStream
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
name|HashSet
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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|VectorHashKeyWrapper
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
name|vector
operator|.
name|VectorHashKeyWrapperBatch
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
name|vector
operator|.
name|expressions
operator|.
name|VectorExpressionWriter
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
name|serde2
operator|.
name|ByteStream
operator|.
name|Output
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
name|binarysortable
operator|.
name|BinarySortableSerDe
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
name|lazybinary
operator|.
name|LazyBinarySerDe
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
name|ObjectInspector
operator|.
name|Category
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
name|Writable
import|;
end_import

begin_comment
comment|/**  * The base class for MapJoinKey.  * Ideally, this should now be removed, some table wrappers have no key object.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|MapJoinKey
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|EMPTY_BYTE_ARRAY
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
specifier|public
specifier|abstract
name|void
name|write
parameter_list|(
name|MapJoinObjectSerDeContext
name|context
parameter_list|,
name|ObjectOutputStream
name|out
parameter_list|)
throws|throws
name|IOException
throws|,
name|SerDeException
function_decl|;
specifier|public
specifier|abstract
name|boolean
name|hasAnyNulls
parameter_list|(
name|int
name|fieldCount
parameter_list|,
name|boolean
index|[]
name|nullsafes
parameter_list|)
function_decl|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
specifier|static
name|MapJoinKey
name|read
parameter_list|(
name|Output
name|output
parameter_list|,
name|MapJoinObjectSerDeContext
name|context
parameter_list|,
name|Writable
name|writable
parameter_list|)
throws|throws
name|SerDeException
throws|,
name|HiveException
block|{
name|SerDe
name|serde
init|=
name|context
operator|.
name|getSerDe
argument_list|()
decl_stmt|;
name|Object
name|obj
init|=
name|serde
operator|.
name|deserialize
argument_list|(
name|writable
argument_list|)
decl_stmt|;
name|MapJoinKeyObject
name|result
init|=
operator|new
name|MapJoinKeyObject
argument_list|()
decl_stmt|;
name|result
operator|.
name|read
argument_list|(
name|serde
operator|.
name|getObjectInspector
argument_list|()
argument_list|,
name|obj
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|private
specifier|static
specifier|final
name|HashSet
argument_list|<
name|PrimitiveCategory
argument_list|>
name|SUPPORTED_PRIMITIVES
init|=
operator|new
name|HashSet
argument_list|<
name|PrimitiveCategory
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
comment|// All but decimal.
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|BOOLEAN
argument_list|)
expr_stmt|;
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|VOID
argument_list|)
expr_stmt|;
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|BOOLEAN
argument_list|)
expr_stmt|;
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|BYTE
argument_list|)
expr_stmt|;
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|SHORT
argument_list|)
expr_stmt|;
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|INT
argument_list|)
expr_stmt|;
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|LONG
argument_list|)
expr_stmt|;
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|FLOAT
argument_list|)
expr_stmt|;
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|DOUBLE
argument_list|)
expr_stmt|;
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|STRING
argument_list|)
expr_stmt|;
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|DATE
argument_list|)
expr_stmt|;
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|TIMESTAMP
argument_list|)
expr_stmt|;
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|INTERVAL_YEAR_MONTH
argument_list|)
expr_stmt|;
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|INTERVAL_DAY_TIME
argument_list|)
expr_stmt|;
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|BINARY
argument_list|)
expr_stmt|;
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|VARCHAR
argument_list|)
expr_stmt|;
name|SUPPORTED_PRIMITIVES
operator|.
name|add
argument_list|(
name|PrimitiveCategory
operator|.
name|CHAR
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|boolean
name|isSupportedField
parameter_list|(
name|ObjectInspector
name|foi
parameter_list|)
block|{
if|if
condition|(
name|foi
operator|.
name|getCategory
argument_list|()
operator|!=
name|Category
operator|.
name|PRIMITIVE
condition|)
return|return
literal|false
return|;
comment|// not supported
name|PrimitiveCategory
name|pc
init|=
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|foi
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|SUPPORTED_PRIMITIVES
operator|.
name|contains
argument_list|(
name|pc
argument_list|)
condition|)
return|return
literal|false
return|;
comment|// not supported
return|return
literal|true
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isSupportedField
parameter_list|(
name|String
name|typeName
parameter_list|)
block|{
name|TypeInfo
name|typeInfo
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfoFromTypeString
argument_list|(
name|typeName
argument_list|)
decl_stmt|;
if|if
condition|(
name|typeInfo
operator|.
name|getCategory
argument_list|()
operator|!=
name|Category
operator|.
name|PRIMITIVE
condition|)
return|return
literal|false
return|;
comment|// not supported
name|PrimitiveTypeInfo
name|primitiveTypeInfo
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|PrimitiveCategory
name|pc
init|=
name|primitiveTypeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|SUPPORTED_PRIMITIVES
operator|.
name|contains
argument_list|(
name|pc
argument_list|)
condition|)
return|return
literal|false
return|;
comment|// not supported
return|return
literal|true
return|;
block|}
specifier|public
specifier|static
name|MapJoinKey
name|readFromVector
parameter_list|(
name|Output
name|output
parameter_list|,
name|MapJoinKey
name|key
parameter_list|,
name|Object
index|[]
name|keyObject
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|keyOIs
parameter_list|,
name|boolean
name|mayReuseKey
parameter_list|)
throws|throws
name|HiveException
block|{
name|MapJoinKeyObject
name|result
init|=
name|mayReuseKey
condition|?
operator|(
name|MapJoinKeyObject
operator|)
name|key
else|:
operator|new
name|MapJoinKeyObject
argument_list|()
decl_stmt|;
name|result
operator|.
name|setKeyObjects
argument_list|(
name|keyObject
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Serializes row to output for vectorized path.    * @param byteStream Output to reuse. Can be null, in that case a new one would be created.    */
specifier|public
specifier|static
name|Output
name|serializeVector
parameter_list|(
name|Output
name|byteStream
parameter_list|,
name|VectorHashKeyWrapper
name|kw
parameter_list|,
name|VectorExpressionWriter
index|[]
name|keyOutputWriters
parameter_list|,
name|VectorHashKeyWrapperBatch
name|keyWrapperBatch
parameter_list|,
name|boolean
index|[]
name|nulls
parameter_list|,
name|boolean
index|[]
name|sortableSortOrders
parameter_list|)
throws|throws
name|HiveException
throws|,
name|SerDeException
block|{
name|Object
index|[]
name|fieldData
init|=
operator|new
name|Object
index|[
name|keyOutputWriters
operator|.
name|length
index|]
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldOis
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
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
name|keyOutputWriters
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|VectorExpressionWriter
name|writer
init|=
name|keyOutputWriters
index|[
name|i
index|]
decl_stmt|;
name|fieldOis
operator|.
name|add
argument_list|(
name|writer
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
comment|// This is rather convoluted... to simplify for perf, we could call getRawKeyValue
comment|// instead of writable, and serialize based on Java type as opposed to OI.
name|fieldData
index|[
name|i
index|]
operator|=
name|keyWrapperBatch
operator|.
name|getWritableKeyValue
argument_list|(
name|kw
argument_list|,
name|i
argument_list|,
name|writer
argument_list|)
expr_stmt|;
if|if
condition|(
name|nulls
operator|!=
literal|null
condition|)
block|{
name|nulls
index|[
name|i
index|]
operator|=
operator|(
name|fieldData
index|[
name|i
index|]
operator|==
literal|null
operator|)
expr_stmt|;
block|}
block|}
return|return
name|serializeRow
argument_list|(
name|byteStream
argument_list|,
name|fieldData
argument_list|,
name|fieldOis
argument_list|,
name|sortableSortOrders
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|MapJoinKey
name|readFromRow
parameter_list|(
name|Output
name|output
parameter_list|,
name|MapJoinKey
name|key
parameter_list|,
name|Object
index|[]
name|keyObject
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|keyFieldsOI
parameter_list|,
name|boolean
name|mayReuseKey
parameter_list|)
throws|throws
name|HiveException
block|{
name|MapJoinKeyObject
name|result
init|=
name|mayReuseKey
condition|?
operator|(
name|MapJoinKeyObject
operator|)
name|key
else|:
operator|new
name|MapJoinKeyObject
argument_list|()
decl_stmt|;
name|result
operator|.
name|readFromRow
argument_list|(
name|keyObject
argument_list|,
name|keyFieldsOI
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Serializes row to output.    * @param byteStream Output to reuse. Can be null, in that case a new one would be created.    */
specifier|public
specifier|static
name|Output
name|serializeRow
parameter_list|(
name|Output
name|byteStream
parameter_list|,
name|Object
index|[]
name|fieldData
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldOis
parameter_list|,
name|boolean
index|[]
name|sortableSortOrders
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|byteStream
operator|==
literal|null
condition|)
block|{
name|byteStream
operator|=
operator|new
name|Output
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|byteStream
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|fieldData
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|byteStream
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sortableSortOrders
operator|==
literal|null
condition|)
block|{
name|LazyBinarySerDe
operator|.
name|serializeStruct
argument_list|(
name|byteStream
argument_list|,
name|fieldData
argument_list|,
name|fieldOis
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|BinarySortableSerDe
operator|.
name|serializeStruct
argument_list|(
name|byteStream
argument_list|,
name|fieldData
argument_list|,
name|fieldOis
argument_list|,
name|sortableSortOrders
argument_list|)
expr_stmt|;
block|}
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
literal|"Serialization error"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|byteStream
return|;
block|}
block|}
end_class

end_unit

