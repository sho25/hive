begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|plan
package|;
end_package

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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Strings
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
name|VectorPartitionDesc
operator|.
name|VectorMapOperatorReadType
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

begin_comment
comment|/**  * VectorMapDesc.  *  * Extra vector information just for the PartitionDesc.  *  */
end_comment

begin_class
specifier|public
class|class
name|VectorPartitionDesc
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|// Data Type Conversion Needed?
comment|//
comment|// VECTORIZED_INPUT_FILE_FORMAT:
comment|//    No data type conversion check?  Assume ALTER TABLE prevented conversions that
comment|//    VectorizedInputFileFormat cannot handle...
comment|//
comment|// VECTOR_DESERIALIZE:
comment|//    LAZY_SIMPLE:
comment|//        Capable of converting on its own.
comment|//    LAZY_BINARY
comment|//        Partition schema assumed to match file contents.
comment|//        Conversion necessary from partition field values to vector columns.
comment|// ROW_DESERIALIZE
comment|//    Partition schema assumed to match file contents.
comment|//    Conversion necessary from partition field values to vector columns.
comment|//
specifier|public
specifier|static
enum|enum
name|VectorMapOperatorReadType
block|{
name|NONE
block|,
name|VECTORIZED_INPUT_FILE_FORMAT
block|,
name|VECTOR_DESERIALIZE
block|,
name|ROW_DESERIALIZE
block|}
specifier|public
specifier|static
enum|enum
name|VectorDeserializeType
block|{
name|NONE
block|,
name|LAZY_SIMPLE
block|,
name|LAZY_BINARY
block|}
specifier|private
name|VectorMapOperatorReadType
name|vectorMapOperatorReadType
decl_stmt|;
specifier|private
specifier|final
name|VectorDeserializeType
name|vectorDeserializeType
decl_stmt|;
specifier|private
specifier|final
name|String
name|rowDeserializerClassName
decl_stmt|;
specifier|private
specifier|final
name|String
name|inputFileFormatClassName
decl_stmt|;
name|boolean
name|isInputFileFormatSelfDescribing
decl_stmt|;
specifier|private
name|TypeInfo
index|[]
name|dataTypeInfos
decl_stmt|;
specifier|private
name|VectorPartitionDesc
parameter_list|(
name|String
name|inputFileFormatClassName
parameter_list|,
name|boolean
name|isInputFileFormatSelfDescribing
parameter_list|,
name|VectorMapOperatorReadType
name|vectorMapOperatorReadType
parameter_list|)
block|{
name|this
operator|.
name|vectorMapOperatorReadType
operator|=
name|vectorMapOperatorReadType
expr_stmt|;
name|this
operator|.
name|vectorDeserializeType
operator|=
name|VectorDeserializeType
operator|.
name|NONE
expr_stmt|;
name|this
operator|.
name|inputFileFormatClassName
operator|=
name|inputFileFormatClassName
expr_stmt|;
name|rowDeserializerClassName
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|isInputFileFormatSelfDescribing
operator|=
name|isInputFileFormatSelfDescribing
expr_stmt|;
name|dataTypeInfos
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Create a VECTOR_DESERIALIZE flavor object.    * @param vectorMapOperatorReadType    * @param vectorDeserializeType    * @param needsDataTypeConversionCheck    */
specifier|private
name|VectorPartitionDesc
parameter_list|(
name|String
name|inputFileFormatClassName
parameter_list|,
name|VectorDeserializeType
name|vectorDeserializeType
parameter_list|)
block|{
name|this
operator|.
name|vectorMapOperatorReadType
operator|=
name|VectorMapOperatorReadType
operator|.
name|VECTOR_DESERIALIZE
expr_stmt|;
name|this
operator|.
name|vectorDeserializeType
operator|=
name|vectorDeserializeType
expr_stmt|;
name|this
operator|.
name|inputFileFormatClassName
operator|=
name|inputFileFormatClassName
expr_stmt|;
name|rowDeserializerClassName
operator|=
literal|null
expr_stmt|;
name|isInputFileFormatSelfDescribing
operator|=
literal|false
expr_stmt|;
name|dataTypeInfos
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Create a ROW_DESERIALIZE flavor object.    * @param rowDeserializerClassName    * @param inputFileFormatClassName    */
specifier|private
name|VectorPartitionDesc
parameter_list|(
name|String
name|inputFileFormatClassName
parameter_list|,
name|boolean
name|isInputFileFormatSelfDescribing
parameter_list|,
name|String
name|rowDeserializerClassName
parameter_list|)
block|{
name|this
operator|.
name|vectorMapOperatorReadType
operator|=
name|VectorMapOperatorReadType
operator|.
name|ROW_DESERIALIZE
expr_stmt|;
name|this
operator|.
name|vectorDeserializeType
operator|=
name|VectorDeserializeType
operator|.
name|NONE
expr_stmt|;
name|this
operator|.
name|inputFileFormatClassName
operator|=
name|inputFileFormatClassName
expr_stmt|;
name|this
operator|.
name|rowDeserializerClassName
operator|=
name|rowDeserializerClassName
expr_stmt|;
name|this
operator|.
name|isInputFileFormatSelfDescribing
operator|=
name|isInputFileFormatSelfDescribing
expr_stmt|;
name|dataTypeInfos
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
specifier|static
name|VectorPartitionDesc
name|createVectorizedInputFileFormat
parameter_list|(
name|String
name|inputFileFormatClassName
parameter_list|,
name|boolean
name|isInputFileFormatSelfDescribing
parameter_list|)
block|{
return|return
operator|new
name|VectorPartitionDesc
argument_list|(
name|inputFileFormatClassName
argument_list|,
name|isInputFileFormatSelfDescribing
argument_list|,
name|VectorMapOperatorReadType
operator|.
name|VECTORIZED_INPUT_FILE_FORMAT
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|VectorPartitionDesc
name|createVectorDeserialize
parameter_list|(
name|String
name|inputFileFormatClassName
parameter_list|,
name|VectorDeserializeType
name|vectorDeserializeType
parameter_list|)
block|{
return|return
operator|new
name|VectorPartitionDesc
argument_list|(
name|inputFileFormatClassName
argument_list|,
name|vectorDeserializeType
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|VectorPartitionDesc
name|createRowDeserialize
parameter_list|(
name|String
name|inputFileFormatClassName
parameter_list|,
name|boolean
name|isInputFileFormatSelfDescribing
parameter_list|,
name|String
name|rowDeserializerClassName
parameter_list|)
block|{
return|return
operator|new
name|VectorPartitionDesc
argument_list|(
name|rowDeserializerClassName
argument_list|,
name|isInputFileFormatSelfDescribing
argument_list|,
name|inputFileFormatClassName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|VectorPartitionDesc
name|clone
parameter_list|()
block|{
name|VectorPartitionDesc
name|result
decl_stmt|;
switch|switch
condition|(
name|vectorMapOperatorReadType
condition|)
block|{
case|case
name|VECTORIZED_INPUT_FILE_FORMAT
case|:
name|result
operator|=
operator|new
name|VectorPartitionDesc
argument_list|(
name|inputFileFormatClassName
argument_list|,
name|isInputFileFormatSelfDescribing
argument_list|,
name|vectorMapOperatorReadType
argument_list|)
expr_stmt|;
break|break;
case|case
name|VECTOR_DESERIALIZE
case|:
name|result
operator|=
operator|new
name|VectorPartitionDesc
argument_list|(
name|inputFileFormatClassName
argument_list|,
name|vectorDeserializeType
argument_list|)
expr_stmt|;
break|break;
case|case
name|ROW_DESERIALIZE
case|:
name|result
operator|=
operator|new
name|VectorPartitionDesc
argument_list|(
name|inputFileFormatClassName
argument_list|,
name|isInputFileFormatSelfDescribing
argument_list|,
name|rowDeserializerClassName
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected vector map operator read type "
operator|+
name|vectorMapOperatorReadType
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
name|result
operator|.
name|dataTypeInfos
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|dataTypeInfos
argument_list|,
name|dataTypeInfos
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|instanceof
name|VectorPartitionDesc
condition|)
block|{
name|VectorPartitionDesc
name|other
init|=
operator|(
name|VectorPartitionDesc
operator|)
name|o
decl_stmt|;
return|return
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|getInputFileFormatClassName
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|other
operator|.
name|getInputFileFormatClassName
argument_list|()
argument_list|)
argument_list|)
operator|&&
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|getRowDeserializerClassName
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|other
operator|.
name|getRowDeserializerClassName
argument_list|()
argument_list|)
argument_list|)
operator|&&
name|getVectorDeserializeType
argument_list|()
operator|==
name|other
operator|.
name|getVectorDeserializeType
argument_list|()
operator|&&
name|getVectorMapOperatorReadType
argument_list|()
operator|==
name|other
operator|.
name|getVectorMapOperatorReadType
argument_list|()
operator|&&
name|getIsInputFileFormatSelfDescribing
argument_list|()
operator|==
name|other
operator|.
name|getIsInputFileFormatSelfDescribing
argument_list|()
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|getDataTypeInfos
argument_list|()
argument_list|,
name|other
operator|.
name|getDataTypeInfos
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
specifier|final
name|int
name|prime
init|=
literal|31
decl_stmt|;
name|int
name|result
init|=
literal|1
decl_stmt|;
name|result
operator|=
name|result
operator|*
name|prime
operator|+
operator|(
name|getInputFileFormatClassName
argument_list|()
operator|==
literal|null
condition|?
literal|0
else|:
name|getInputFileFormatClassName
argument_list|()
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|result
operator|*
name|prime
operator|+
operator|(
name|getRowDeserializerClassName
argument_list|()
operator|==
literal|null
condition|?
literal|0
else|:
name|getRowDeserializerClassName
argument_list|()
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|result
operator|*
name|prime
operator|+
operator|(
name|getVectorDeserializeType
argument_list|()
operator|==
literal|null
condition|?
literal|0
else|:
name|getVectorDeserializeType
argument_list|()
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|result
operator|*
name|prime
operator|+
operator|(
name|getVectorMapOperatorReadType
argument_list|()
operator|==
literal|null
condition|?
literal|0
else|:
name|getVectorMapOperatorReadType
argument_list|()
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
name|result
operator|=
name|result
operator|*
name|prime
operator|+
name|Boolean
operator|.
name|valueOf
argument_list|(
name|getIsInputFileFormatSelfDescribing
argument_list|()
argument_list|)
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|=
name|result
operator|*
name|prime
operator|+
name|Arrays
operator|.
name|hashCode
argument_list|(
name|getDataTypeInfos
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
name|VectorMapOperatorReadType
name|getVectorMapOperatorReadType
parameter_list|()
block|{
return|return
name|vectorMapOperatorReadType
return|;
block|}
specifier|public
name|String
name|getInputFileFormatClassName
parameter_list|()
block|{
return|return
name|inputFileFormatClassName
return|;
block|}
specifier|public
name|VectorDeserializeType
name|getVectorDeserializeType
parameter_list|()
block|{
return|return
name|vectorDeserializeType
return|;
block|}
specifier|public
name|String
name|getRowDeserializerClassName
parameter_list|()
block|{
return|return
name|rowDeserializerClassName
return|;
block|}
specifier|public
name|boolean
name|getIsInputFileFormatSelfDescribing
parameter_list|()
block|{
return|return
name|isInputFileFormatSelfDescribing
return|;
block|}
specifier|public
name|TypeInfo
index|[]
name|getDataTypeInfos
parameter_list|()
block|{
return|return
name|dataTypeInfos
return|;
block|}
specifier|public
name|void
name|setDataTypeInfos
parameter_list|(
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|dataTypeInfoList
parameter_list|)
block|{
name|dataTypeInfos
operator|=
name|dataTypeInfoList
operator|.
name|toArray
argument_list|(
operator|new
name|TypeInfo
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getDataColumnCount
parameter_list|()
block|{
return|return
name|dataTypeInfos
operator|.
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"vector map operator read type "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|vectorMapOperatorReadType
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", input file format class name "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|inputFileFormatClassName
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|vectorMapOperatorReadType
condition|)
block|{
case|case
name|VECTORIZED_INPUT_FILE_FORMAT
case|:
break|break;
case|case
name|VECTOR_DESERIALIZE
case|:
name|sb
operator|.
name|append
argument_list|(
literal|", deserialize type "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|vectorDeserializeType
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|ROW_DESERIALIZE
case|:
name|sb
operator|.
name|append
argument_list|(
literal|", deserializer class name "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|rowDeserializerClassName
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected vector map operator read type "
operator|+
name|vectorMapOperatorReadType
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|void
name|setVectorMapOperatorReadType
parameter_list|(
name|VectorMapOperatorReadType
name|val
parameter_list|)
block|{
name|this
operator|.
name|vectorMapOperatorReadType
operator|=
name|val
expr_stmt|;
block|}
block|}
end_class

end_unit

