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
name|vector
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
name|objectinspector
operator|.
name|ListObjectInspector
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
name|MapObjectInspector
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
name|fast
operator|.
name|SerializeWrite
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
name|objectinspector
operator|.
name|UnionObjectInspector
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
name|UnionTypeInfo
import|;
end_import

begin_comment
comment|/**  * This class serializes columns from a row in a VectorizedRowBatch into a serialization format.  *  * The caller provides the hive type names and column numbers in the order desired to  * serialize.  *  * This class uses an provided SerializeWrite object to directly serialize by writing  * field-by-field into a serialization format from the primitive values of the VectorizedRowBatch.  *  * Note that when serializing a row, the logical mapping using selected in use has already  * been performed.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|VectorSerializeRow
parameter_list|<
name|T
extends|extends
name|SerializeWrite
parameter_list|>
block|{
specifier|private
name|T
name|serializeWrite
decl_stmt|;
specifier|private
name|TypeInfo
index|[]
name|typeInfos
decl_stmt|;
specifier|private
name|ObjectInspector
index|[]
name|objectInspectors
decl_stmt|;
specifier|private
name|int
index|[]
name|outputColumnNums
decl_stmt|;
specifier|private
name|VectorExtractRow
name|vectorExtractRow
decl_stmt|;
specifier|public
name|VectorSerializeRow
parameter_list|(
name|T
name|serializeWrite
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|serializeWrite
operator|=
name|serializeWrite
expr_stmt|;
name|vectorExtractRow
operator|=
operator|new
name|VectorExtractRow
argument_list|()
expr_stmt|;
block|}
comment|// Not public since we must have the serialize write object.
specifier|private
name|VectorSerializeRow
parameter_list|()
block|{   }
specifier|public
name|void
name|init
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|typeNames
parameter_list|,
name|int
index|[]
name|columnMap
parameter_list|)
throws|throws
name|HiveException
block|{
specifier|final
name|int
name|size
init|=
name|typeNames
operator|.
name|size
argument_list|()
decl_stmt|;
name|typeInfos
operator|=
operator|new
name|TypeInfo
index|[
name|size
index|]
expr_stmt|;
name|outputColumnNums
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|columnMap
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|objectInspectors
operator|=
operator|new
name|ObjectInspector
index|[
name|size
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
name|size
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|TypeInfo
name|typeInfo
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfoFromTypeString
argument_list|(
name|typeNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|typeInfos
index|[
name|i
index|]
operator|=
name|typeInfo
expr_stmt|;
name|objectInspectors
index|[
name|i
index|]
operator|=
name|TypeInfoUtils
operator|.
name|getStandardJavaObjectInspectorFromTypeInfo
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
block|}
name|vectorExtractRow
operator|.
name|init
argument_list|(
name|typeInfos
argument_list|,
name|outputColumnNums
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|typeNames
parameter_list|)
throws|throws
name|HiveException
block|{
specifier|final
name|int
name|size
init|=
name|typeNames
operator|.
name|size
argument_list|()
decl_stmt|;
name|typeInfos
operator|=
operator|new
name|TypeInfo
index|[
name|size
index|]
expr_stmt|;
name|outputColumnNums
operator|=
operator|new
name|int
index|[
name|size
index|]
expr_stmt|;
name|objectInspectors
operator|=
operator|new
name|ObjectInspector
index|[
name|size
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
name|size
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|TypeInfo
name|typeInfo
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfoFromTypeString
argument_list|(
name|typeNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|typeInfos
index|[
name|i
index|]
operator|=
name|typeInfo
expr_stmt|;
name|objectInspectors
index|[
name|i
index|]
operator|=
name|TypeInfoUtils
operator|.
name|getStandardJavaObjectInspectorFromTypeInfo
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
name|outputColumnNums
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
block|}
name|vectorExtractRow
operator|.
name|init
argument_list|(
name|typeInfos
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|(
name|TypeInfo
index|[]
name|typeInfos
parameter_list|)
throws|throws
name|HiveException
block|{
specifier|final
name|int
name|size
init|=
name|typeInfos
operator|.
name|length
decl_stmt|;
name|this
operator|.
name|typeInfos
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|typeInfos
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|outputColumnNums
operator|=
operator|new
name|int
index|[
name|size
index|]
expr_stmt|;
name|objectInspectors
operator|=
operator|new
name|ObjectInspector
index|[
name|size
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|objectInspectors
index|[
name|i
index|]
operator|=
name|TypeInfoUtils
operator|.
name|getStandardJavaObjectInspectorFromTypeInfo
argument_list|(
name|typeInfos
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|outputColumnNums
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
block|}
name|vectorExtractRow
operator|.
name|init
argument_list|(
name|this
operator|.
name|typeInfos
argument_list|,
name|outputColumnNums
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|(
name|TypeInfo
index|[]
name|typeInfos
parameter_list|,
name|int
index|[]
name|columnMap
parameter_list|)
throws|throws
name|HiveException
block|{
specifier|final
name|int
name|size
init|=
name|typeInfos
operator|.
name|length
decl_stmt|;
name|this
operator|.
name|typeInfos
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|typeInfos
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|outputColumnNums
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|columnMap
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|objectInspectors
operator|=
operator|new
name|ObjectInspector
index|[
name|size
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|objectInspectors
index|[
name|i
index|]
operator|=
name|TypeInfoUtils
operator|.
name|getStandardJavaObjectInspectorFromTypeInfo
argument_list|(
name|typeInfos
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|vectorExtractRow
operator|.
name|init
argument_list|(
name|this
operator|.
name|typeInfos
argument_list|,
name|outputColumnNums
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getCount
parameter_list|()
block|{
return|return
name|typeInfos
operator|.
name|length
return|;
block|}
specifier|public
name|void
name|setOutput
parameter_list|(
name|Output
name|output
parameter_list|)
block|{
name|serializeWrite
operator|.
name|set
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setOutputAppend
parameter_list|(
name|Output
name|output
parameter_list|)
block|{
name|serializeWrite
operator|.
name|setAppend
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|hasAnyNulls
decl_stmt|;
specifier|private
name|boolean
name|isAllNulls
decl_stmt|;
comment|/*    * Note that when serializing a row, the logical mapping using selected in use has already    * been performed.  batchIndex is the actual index of the row.    */
specifier|public
name|void
name|serializeWrite
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|,
name|int
name|batchIndex
parameter_list|)
throws|throws
name|IOException
block|{
name|hasAnyNulls
operator|=
literal|false
expr_stmt|;
name|isAllNulls
operator|=
literal|true
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
name|typeInfos
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|ColumnVector
name|colVector
init|=
name|batch
operator|.
name|cols
index|[
name|outputColumnNums
index|[
name|i
index|]
index|]
decl_stmt|;
name|serializeWrite
argument_list|(
name|colVector
argument_list|,
name|typeInfos
index|[
name|i
index|]
argument_list|,
name|objectInspectors
index|[
name|i
index|]
argument_list|,
name|batchIndex
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|serializeWrite
parameter_list|(
name|ColumnVector
name|colVector
parameter_list|,
name|TypeInfo
name|typeInfo
parameter_list|,
name|ObjectInspector
name|objectInspector
parameter_list|,
name|int
name|batchIndex
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|adjustedBatchIndex
decl_stmt|;
if|if
condition|(
name|colVector
operator|.
name|isRepeating
condition|)
block|{
name|adjustedBatchIndex
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
name|adjustedBatchIndex
operator|=
name|batchIndex
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|colVector
operator|.
name|noNulls
operator|&&
name|colVector
operator|.
name|isNull
index|[
name|adjustedBatchIndex
index|]
condition|)
block|{
name|serializeWrite
operator|.
name|writeNull
argument_list|()
expr_stmt|;
name|hasAnyNulls
operator|=
literal|true
expr_stmt|;
return|return;
block|}
name|isAllNulls
operator|=
literal|false
expr_stmt|;
specifier|final
name|Category
name|category
init|=
name|typeInfo
operator|.
name|getCategory
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|category
condition|)
block|{
case|case
name|PRIMITIVE
case|:
name|serializePrimitiveWrite
argument_list|(
name|colVector
argument_list|,
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
argument_list|,
name|adjustedBatchIndex
argument_list|)
expr_stmt|;
break|break;
case|case
name|LIST
case|:
name|serializeListWrite
argument_list|(
operator|(
name|ListColumnVector
operator|)
name|colVector
argument_list|,
operator|(
name|ListTypeInfo
operator|)
name|typeInfo
argument_list|,
operator|(
name|ListObjectInspector
operator|)
name|objectInspector
argument_list|,
name|adjustedBatchIndex
argument_list|)
expr_stmt|;
break|break;
case|case
name|MAP
case|:
name|serializeMapWrite
argument_list|(
operator|(
name|MapColumnVector
operator|)
name|colVector
argument_list|,
operator|(
name|MapTypeInfo
operator|)
name|typeInfo
argument_list|,
operator|(
name|MapObjectInspector
operator|)
name|objectInspector
argument_list|,
name|adjustedBatchIndex
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRUCT
case|:
name|serializeStructWrite
argument_list|(
operator|(
name|StructColumnVector
operator|)
name|colVector
argument_list|,
operator|(
name|StructTypeInfo
operator|)
name|typeInfo
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|objectInspector
argument_list|,
name|adjustedBatchIndex
argument_list|)
expr_stmt|;
break|break;
case|case
name|UNION
case|:
name|serializeUnionWrite
argument_list|(
operator|(
name|UnionColumnVector
operator|)
name|colVector
argument_list|,
operator|(
name|UnionTypeInfo
operator|)
name|typeInfo
argument_list|,
operator|(
name|UnionObjectInspector
operator|)
name|objectInspector
argument_list|,
name|adjustedBatchIndex
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected category "
operator|+
name|category
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|serializeUnionWrite
parameter_list|(
name|UnionColumnVector
name|colVector
parameter_list|,
name|UnionTypeInfo
name|typeInfo
parameter_list|,
name|UnionObjectInspector
name|objectInspector
parameter_list|,
name|int
name|adjustedBatchIndex
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|byte
name|tag
init|=
operator|(
name|byte
operator|)
name|colVector
operator|.
name|tags
index|[
name|adjustedBatchIndex
index|]
decl_stmt|;
specifier|final
name|ColumnVector
name|fieldColumnVector
init|=
name|colVector
operator|.
name|fields
index|[
name|tag
index|]
decl_stmt|;
specifier|final
name|TypeInfo
name|objectTypeInfo
init|=
name|typeInfo
operator|.
name|getAllUnionObjectTypeInfos
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
decl_stmt|;
name|serializeWrite
operator|.
name|beginUnion
argument_list|(
name|tag
argument_list|)
expr_stmt|;
name|serializeWrite
argument_list|(
name|fieldColumnVector
argument_list|,
name|objectTypeInfo
argument_list|,
name|objectInspector
operator|.
name|getObjectInspectors
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
argument_list|,
name|adjustedBatchIndex
argument_list|)
expr_stmt|;
name|serializeWrite
operator|.
name|finishUnion
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|serializeStructWrite
parameter_list|(
name|StructColumnVector
name|colVector
parameter_list|,
name|StructTypeInfo
name|typeInfo
parameter_list|,
name|StructObjectInspector
name|objectInspector
parameter_list|,
name|int
name|adjustedBatchIndex
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|ColumnVector
index|[]
name|fieldColumnVectors
init|=
name|colVector
operator|.
name|fields
decl_stmt|;
specifier|final
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|fieldTypeInfos
init|=
name|typeInfo
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|structFields
init|=
name|objectInspector
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
specifier|final
name|int
name|size
init|=
name|fieldTypeInfos
operator|.
name|size
argument_list|()
decl_stmt|;
specifier|final
name|List
name|list
init|=
operator|(
name|List
operator|)
name|vectorExtractRow
operator|.
name|extractRowColumn
argument_list|(
name|colVector
argument_list|,
name|typeInfo
argument_list|,
name|objectInspector
argument_list|,
name|adjustedBatchIndex
argument_list|)
decl_stmt|;
name|serializeWrite
operator|.
name|beginStruct
argument_list|(
name|list
argument_list|)
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
name|size
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
name|serializeWrite
operator|.
name|separateStruct
argument_list|()
expr_stmt|;
block|}
name|serializeWrite
argument_list|(
name|fieldColumnVectors
index|[
name|i
index|]
argument_list|,
name|fieldTypeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|structFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|,
name|adjustedBatchIndex
argument_list|)
expr_stmt|;
block|}
name|serializeWrite
operator|.
name|finishStruct
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|serializeMapWrite
parameter_list|(
name|MapColumnVector
name|colVector
parameter_list|,
name|MapTypeInfo
name|typeInfo
parameter_list|,
name|MapObjectInspector
name|objectInspector
parameter_list|,
name|int
name|adjustedBatchIndex
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|ColumnVector
name|keyColumnVector
init|=
name|colVector
operator|.
name|keys
decl_stmt|;
specifier|final
name|ColumnVector
name|valueColumnVector
init|=
name|colVector
operator|.
name|values
decl_stmt|;
specifier|final
name|TypeInfo
name|keyTypeInfo
init|=
name|typeInfo
operator|.
name|getMapKeyTypeInfo
argument_list|()
decl_stmt|;
specifier|final
name|TypeInfo
name|valueTypeInfo
init|=
name|typeInfo
operator|.
name|getMapValueTypeInfo
argument_list|()
decl_stmt|;
specifier|final
name|int
name|offset
init|=
operator|(
name|int
operator|)
name|colVector
operator|.
name|offsets
index|[
name|adjustedBatchIndex
index|]
decl_stmt|;
specifier|final
name|int
name|size
init|=
operator|(
name|int
operator|)
name|colVector
operator|.
name|lengths
index|[
name|adjustedBatchIndex
index|]
decl_stmt|;
specifier|final
name|Map
name|map
init|=
operator|(
name|Map
operator|)
name|vectorExtractRow
operator|.
name|extractRowColumn
argument_list|(
name|colVector
argument_list|,
name|typeInfo
argument_list|,
name|objectInspector
argument_list|,
name|adjustedBatchIndex
argument_list|)
decl_stmt|;
name|serializeWrite
operator|.
name|beginMap
argument_list|(
name|map
argument_list|)
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
name|size
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
name|serializeWrite
operator|.
name|separateKeyValuePair
argument_list|()
expr_stmt|;
block|}
name|serializeWrite
argument_list|(
name|keyColumnVector
argument_list|,
name|keyTypeInfo
argument_list|,
name|objectInspector
operator|.
name|getMapKeyObjectInspector
argument_list|()
argument_list|,
name|offset
operator|+
name|i
argument_list|)
expr_stmt|;
name|serializeWrite
operator|.
name|separateKey
argument_list|()
expr_stmt|;
name|serializeWrite
argument_list|(
name|valueColumnVector
argument_list|,
name|valueTypeInfo
argument_list|,
name|objectInspector
operator|.
name|getMapValueObjectInspector
argument_list|()
argument_list|,
name|offset
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|serializeWrite
operator|.
name|finishMap
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|serializeListWrite
parameter_list|(
name|ListColumnVector
name|colVector
parameter_list|,
name|ListTypeInfo
name|typeInfo
parameter_list|,
name|ListObjectInspector
name|objectInspector
parameter_list|,
name|int
name|adjustedBatchIndex
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|ColumnVector
name|childColumnVector
init|=
name|colVector
operator|.
name|child
decl_stmt|;
specifier|final
name|TypeInfo
name|elementTypeInfo
init|=
name|typeInfo
operator|.
name|getListElementTypeInfo
argument_list|()
decl_stmt|;
specifier|final
name|int
name|offset
init|=
operator|(
name|int
operator|)
name|colVector
operator|.
name|offsets
index|[
name|adjustedBatchIndex
index|]
decl_stmt|;
specifier|final
name|int
name|size
init|=
operator|(
name|int
operator|)
name|colVector
operator|.
name|lengths
index|[
name|adjustedBatchIndex
index|]
decl_stmt|;
specifier|final
name|ObjectInspector
name|elementObjectInspector
init|=
name|objectInspector
operator|.
name|getListElementObjectInspector
argument_list|()
decl_stmt|;
specifier|final
name|List
name|list
init|=
operator|(
name|List
operator|)
name|vectorExtractRow
operator|.
name|extractRowColumn
argument_list|(
name|colVector
argument_list|,
name|typeInfo
argument_list|,
name|objectInspector
argument_list|,
name|adjustedBatchIndex
argument_list|)
decl_stmt|;
name|serializeWrite
operator|.
name|beginList
argument_list|(
name|list
argument_list|)
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
name|size
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
name|serializeWrite
operator|.
name|separateList
argument_list|()
expr_stmt|;
block|}
name|serializeWrite
argument_list|(
name|childColumnVector
argument_list|,
name|elementTypeInfo
argument_list|,
name|elementObjectInspector
argument_list|,
name|offset
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|serializeWrite
operator|.
name|finishList
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|serializePrimitiveWrite
parameter_list|(
name|ColumnVector
name|colVector
parameter_list|,
name|PrimitiveTypeInfo
name|typeInfo
parameter_list|,
name|int
name|adjustedBatchIndex
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|PrimitiveCategory
name|primitiveCategory
init|=
name|typeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|primitiveCategory
condition|)
block|{
case|case
name|BOOLEAN
case|:
name|serializeWrite
operator|.
name|writeBoolean
argument_list|(
operator|(
operator|(
name|LongColumnVector
operator|)
name|colVector
operator|)
operator|.
name|vector
index|[
name|adjustedBatchIndex
index|]
operator|!=
literal|0
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTE
case|:
name|serializeWrite
operator|.
name|writeByte
argument_list|(
call|(
name|byte
call|)
argument_list|(
operator|(
name|LongColumnVector
operator|)
name|colVector
argument_list|)
operator|.
name|vector
index|[
name|adjustedBatchIndex
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|serializeWrite
operator|.
name|writeShort
argument_list|(
call|(
name|short
call|)
argument_list|(
operator|(
name|LongColumnVector
operator|)
name|colVector
argument_list|)
operator|.
name|vector
index|[
name|adjustedBatchIndex
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|serializeWrite
operator|.
name|writeInt
argument_list|(
call|(
name|int
call|)
argument_list|(
operator|(
name|LongColumnVector
operator|)
name|colVector
argument_list|)
operator|.
name|vector
index|[
name|adjustedBatchIndex
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|serializeWrite
operator|.
name|writeLong
argument_list|(
operator|(
operator|(
name|LongColumnVector
operator|)
name|colVector
operator|)
operator|.
name|vector
index|[
name|adjustedBatchIndex
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|serializeWrite
operator|.
name|writeDate
argument_list|(
call|(
name|int
call|)
argument_list|(
operator|(
name|LongColumnVector
operator|)
name|colVector
argument_list|)
operator|.
name|vector
index|[
name|adjustedBatchIndex
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|serializeWrite
operator|.
name|writeTimestamp
argument_list|(
operator|(
operator|(
name|TimestampColumnVector
operator|)
name|colVector
operator|)
operator|.
name|asScratchTimestamp
argument_list|(
name|adjustedBatchIndex
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|serializeWrite
operator|.
name|writeFloat
argument_list|(
call|(
name|float
call|)
argument_list|(
operator|(
name|DoubleColumnVector
operator|)
name|colVector
argument_list|)
operator|.
name|vector
index|[
name|adjustedBatchIndex
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|serializeWrite
operator|.
name|writeDouble
argument_list|(
operator|(
operator|(
name|DoubleColumnVector
operator|)
name|colVector
operator|)
operator|.
name|vector
index|[
name|adjustedBatchIndex
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING
case|:
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
block|{
comment|// We store CHAR and VARCHAR without pads, so write with STRING.
specifier|final
name|BytesColumnVector
name|bytesColVector
init|=
operator|(
name|BytesColumnVector
operator|)
name|colVector
decl_stmt|;
name|serializeWrite
operator|.
name|writeString
argument_list|(
name|bytesColVector
operator|.
name|vector
index|[
name|adjustedBatchIndex
index|]
argument_list|,
name|bytesColVector
operator|.
name|start
index|[
name|adjustedBatchIndex
index|]
argument_list|,
name|bytesColVector
operator|.
name|length
index|[
name|adjustedBatchIndex
index|]
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|BINARY
case|:
block|{
specifier|final
name|BytesColumnVector
name|bytesColVector
init|=
operator|(
name|BytesColumnVector
operator|)
name|colVector
decl_stmt|;
name|serializeWrite
operator|.
name|writeBinary
argument_list|(
name|bytesColVector
operator|.
name|vector
index|[
name|adjustedBatchIndex
index|]
argument_list|,
name|bytesColVector
operator|.
name|start
index|[
name|adjustedBatchIndex
index|]
argument_list|,
name|bytesColVector
operator|.
name|length
index|[
name|adjustedBatchIndex
index|]
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|DECIMAL
case|:
block|{
if|if
condition|(
name|colVector
operator|instanceof
name|Decimal64ColumnVector
condition|)
block|{
specifier|final
name|Decimal64ColumnVector
name|decimal64ColVector
init|=
operator|(
name|Decimal64ColumnVector
operator|)
name|colVector
decl_stmt|;
name|serializeWrite
operator|.
name|writeDecimal64
argument_list|(
name|decimal64ColVector
operator|.
name|vector
index|[
name|adjustedBatchIndex
index|]
argument_list|,
name|decimal64ColVector
operator|.
name|scale
argument_list|)
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|DecimalColumnVector
name|decimalColVector
init|=
operator|(
name|DecimalColumnVector
operator|)
name|colVector
decl_stmt|;
name|serializeWrite
operator|.
name|writeHiveDecimal
argument_list|(
name|decimalColVector
operator|.
name|vector
index|[
name|adjustedBatchIndex
index|]
argument_list|,
name|decimalColVector
operator|.
name|scale
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
name|INTERVAL_YEAR_MONTH
case|:
name|serializeWrite
operator|.
name|writeHiveIntervalYearMonth
argument_list|(
call|(
name|int
call|)
argument_list|(
operator|(
name|LongColumnVector
operator|)
name|colVector
argument_list|)
operator|.
name|vector
index|[
name|adjustedBatchIndex
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
name|INTERVAL_DAY_TIME
case|:
name|serializeWrite
operator|.
name|writeHiveIntervalDayTime
argument_list|(
operator|(
operator|(
name|IntervalDayTimeColumnVector
operator|)
name|colVector
operator|)
operator|.
name|asScratchIntervalDayTime
argument_list|(
name|adjustedBatchIndex
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected primitive category "
operator|+
name|primitiveCategory
argument_list|)
throw|;
block|}
block|}
specifier|public
name|boolean
name|getHasAnyNulls
parameter_list|()
block|{
return|return
name|hasAnyNulls
return|;
block|}
specifier|public
name|boolean
name|getIsAllNulls
parameter_list|()
block|{
return|return
name|isAllNulls
return|;
block|}
block|}
end_class

end_unit

