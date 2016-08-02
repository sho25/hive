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
name|thrift
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|AbstractList
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
name|BitSet
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
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TBinaryColumn
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TBoolColumn
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TByteColumn
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TColumn
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TDoubleColumn
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TI16Column
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TI32Column
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TI64Column
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TStringColumn
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
name|primitives
operator|.
name|Booleans
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
name|primitives
operator|.
name|Bytes
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
name|primitives
operator|.
name|Doubles
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
name|primitives
operator|.
name|Ints
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
name|primitives
operator|.
name|Longs
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
name|primitives
operator|.
name|Shorts
import|;
end_import

begin_comment
comment|/**  * ColumnBuffer  */
end_comment

begin_class
specifier|public
class|class
name|ColumnBuffer
extends|extends
name|AbstractList
block|{
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_SIZE
init|=
literal|100
decl_stmt|;
specifier|private
specifier|final
name|Type
name|type
decl_stmt|;
specifier|private
name|BitSet
name|nulls
decl_stmt|;
specifier|private
name|int
name|size
decl_stmt|;
specifier|private
name|boolean
index|[]
name|boolVars
decl_stmt|;
specifier|private
name|byte
index|[]
name|byteVars
decl_stmt|;
specifier|private
name|short
index|[]
name|shortVars
decl_stmt|;
specifier|private
name|int
index|[]
name|intVars
decl_stmt|;
specifier|private
name|long
index|[]
name|longVars
decl_stmt|;
specifier|private
name|double
index|[]
name|doubleVars
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|stringVars
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ByteBuffer
argument_list|>
name|binaryVars
decl_stmt|;
specifier|public
name|ColumnBuffer
parameter_list|(
name|Type
name|type
parameter_list|,
name|BitSet
name|nulls
parameter_list|,
name|Object
name|values
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|nulls
operator|=
name|nulls
expr_stmt|;
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|BOOLEAN_TYPE
condition|)
block|{
name|boolVars
operator|=
operator|(
name|boolean
index|[]
operator|)
name|values
expr_stmt|;
name|size
operator|=
name|boolVars
operator|.
name|length
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|TINYINT_TYPE
condition|)
block|{
name|byteVars
operator|=
operator|(
name|byte
index|[]
operator|)
name|values
expr_stmt|;
name|size
operator|=
name|byteVars
operator|.
name|length
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|SMALLINT_TYPE
condition|)
block|{
name|shortVars
operator|=
operator|(
name|short
index|[]
operator|)
name|values
expr_stmt|;
name|size
operator|=
name|shortVars
operator|.
name|length
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|INT_TYPE
condition|)
block|{
name|intVars
operator|=
operator|(
name|int
index|[]
operator|)
name|values
expr_stmt|;
name|size
operator|=
name|intVars
operator|.
name|length
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|BIGINT_TYPE
condition|)
block|{
name|longVars
operator|=
operator|(
name|long
index|[]
operator|)
name|values
expr_stmt|;
name|size
operator|=
name|longVars
operator|.
name|length
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|DOUBLE_TYPE
operator|||
name|type
operator|==
name|Type
operator|.
name|FLOAT_TYPE
condition|)
block|{
name|doubleVars
operator|=
operator|(
name|double
index|[]
operator|)
name|values
expr_stmt|;
name|size
operator|=
name|doubleVars
operator|.
name|length
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|BINARY_TYPE
condition|)
block|{
name|binaryVars
operator|=
operator|(
name|List
argument_list|<
name|ByteBuffer
argument_list|>
operator|)
name|values
expr_stmt|;
name|size
operator|=
name|binaryVars
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|STRING_TYPE
condition|)
block|{
name|stringVars
operator|=
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|values
expr_stmt|;
name|size
operator|=
name|stringVars
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"invalid union object"
argument_list|)
throw|;
block|}
block|}
specifier|public
name|ColumnBuffer
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
name|nulls
operator|=
operator|new
name|BitSet
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|BOOLEAN_TYPE
case|:
name|boolVars
operator|=
operator|new
name|boolean
index|[
name|DEFAULT_SIZE
index|]
expr_stmt|;
break|break;
case|case
name|TINYINT_TYPE
case|:
name|byteVars
operator|=
operator|new
name|byte
index|[
name|DEFAULT_SIZE
index|]
expr_stmt|;
break|break;
case|case
name|SMALLINT_TYPE
case|:
name|shortVars
operator|=
operator|new
name|short
index|[
name|DEFAULT_SIZE
index|]
expr_stmt|;
break|break;
case|case
name|INT_TYPE
case|:
name|intVars
operator|=
operator|new
name|int
index|[
name|DEFAULT_SIZE
index|]
expr_stmt|;
break|break;
case|case
name|BIGINT_TYPE
case|:
name|longVars
operator|=
operator|new
name|long
index|[
name|DEFAULT_SIZE
index|]
expr_stmt|;
break|break;
case|case
name|FLOAT_TYPE
case|:
name|type
operator|=
name|Type
operator|.
name|FLOAT_TYPE
expr_stmt|;
name|doubleVars
operator|=
operator|new
name|double
index|[
name|DEFAULT_SIZE
index|]
expr_stmt|;
break|break;
case|case
name|DOUBLE_TYPE
case|:
name|type
operator|=
name|Type
operator|.
name|DOUBLE_TYPE
expr_stmt|;
name|doubleVars
operator|=
operator|new
name|double
index|[
name|DEFAULT_SIZE
index|]
expr_stmt|;
break|break;
case|case
name|BINARY_TYPE
case|:
name|binaryVars
operator|=
operator|new
name|ArrayList
argument_list|<
name|ByteBuffer
argument_list|>
argument_list|()
expr_stmt|;
break|break;
default|default:
name|type
operator|=
name|Type
operator|.
name|STRING_TYPE
expr_stmt|;
name|stringVars
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
specifier|public
name|ColumnBuffer
parameter_list|(
name|TColumn
name|colValues
parameter_list|)
block|{
if|if
condition|(
name|colValues
operator|.
name|isSetBoolVal
argument_list|()
condition|)
block|{
name|type
operator|=
name|Type
operator|.
name|BOOLEAN_TYPE
expr_stmt|;
name|nulls
operator|=
name|toBitset
argument_list|(
name|colValues
operator|.
name|getBoolVal
argument_list|()
operator|.
name|getNulls
argument_list|()
argument_list|)
expr_stmt|;
name|boolVars
operator|=
name|Booleans
operator|.
name|toArray
argument_list|(
name|colValues
operator|.
name|getBoolVal
argument_list|()
operator|.
name|getValues
argument_list|()
argument_list|)
expr_stmt|;
name|size
operator|=
name|boolVars
operator|.
name|length
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|colValues
operator|.
name|isSetByteVal
argument_list|()
condition|)
block|{
name|type
operator|=
name|Type
operator|.
name|TINYINT_TYPE
expr_stmt|;
name|nulls
operator|=
name|toBitset
argument_list|(
name|colValues
operator|.
name|getByteVal
argument_list|()
operator|.
name|getNulls
argument_list|()
argument_list|)
expr_stmt|;
name|byteVars
operator|=
name|Bytes
operator|.
name|toArray
argument_list|(
name|colValues
operator|.
name|getByteVal
argument_list|()
operator|.
name|getValues
argument_list|()
argument_list|)
expr_stmt|;
name|size
operator|=
name|byteVars
operator|.
name|length
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|colValues
operator|.
name|isSetI16Val
argument_list|()
condition|)
block|{
name|type
operator|=
name|Type
operator|.
name|SMALLINT_TYPE
expr_stmt|;
name|nulls
operator|=
name|toBitset
argument_list|(
name|colValues
operator|.
name|getI16Val
argument_list|()
operator|.
name|getNulls
argument_list|()
argument_list|)
expr_stmt|;
name|shortVars
operator|=
name|Shorts
operator|.
name|toArray
argument_list|(
name|colValues
operator|.
name|getI16Val
argument_list|()
operator|.
name|getValues
argument_list|()
argument_list|)
expr_stmt|;
name|size
operator|=
name|shortVars
operator|.
name|length
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|colValues
operator|.
name|isSetI32Val
argument_list|()
condition|)
block|{
name|type
operator|=
name|Type
operator|.
name|INT_TYPE
expr_stmt|;
name|nulls
operator|=
name|toBitset
argument_list|(
name|colValues
operator|.
name|getI32Val
argument_list|()
operator|.
name|getNulls
argument_list|()
argument_list|)
expr_stmt|;
name|intVars
operator|=
name|Ints
operator|.
name|toArray
argument_list|(
name|colValues
operator|.
name|getI32Val
argument_list|()
operator|.
name|getValues
argument_list|()
argument_list|)
expr_stmt|;
name|size
operator|=
name|intVars
operator|.
name|length
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|colValues
operator|.
name|isSetI64Val
argument_list|()
condition|)
block|{
name|type
operator|=
name|Type
operator|.
name|BIGINT_TYPE
expr_stmt|;
name|nulls
operator|=
name|toBitset
argument_list|(
name|colValues
operator|.
name|getI64Val
argument_list|()
operator|.
name|getNulls
argument_list|()
argument_list|)
expr_stmt|;
name|longVars
operator|=
name|Longs
operator|.
name|toArray
argument_list|(
name|colValues
operator|.
name|getI64Val
argument_list|()
operator|.
name|getValues
argument_list|()
argument_list|)
expr_stmt|;
name|size
operator|=
name|longVars
operator|.
name|length
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|colValues
operator|.
name|isSetDoubleVal
argument_list|()
condition|)
block|{
name|type
operator|=
name|Type
operator|.
name|DOUBLE_TYPE
expr_stmt|;
name|nulls
operator|=
name|toBitset
argument_list|(
name|colValues
operator|.
name|getDoubleVal
argument_list|()
operator|.
name|getNulls
argument_list|()
argument_list|)
expr_stmt|;
name|doubleVars
operator|=
name|Doubles
operator|.
name|toArray
argument_list|(
name|colValues
operator|.
name|getDoubleVal
argument_list|()
operator|.
name|getValues
argument_list|()
argument_list|)
expr_stmt|;
name|size
operator|=
name|doubleVars
operator|.
name|length
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|colValues
operator|.
name|isSetBinaryVal
argument_list|()
condition|)
block|{
name|type
operator|=
name|Type
operator|.
name|BINARY_TYPE
expr_stmt|;
name|nulls
operator|=
name|toBitset
argument_list|(
name|colValues
operator|.
name|getBinaryVal
argument_list|()
operator|.
name|getNulls
argument_list|()
argument_list|)
expr_stmt|;
name|binaryVars
operator|=
name|colValues
operator|.
name|getBinaryVal
argument_list|()
operator|.
name|getValues
argument_list|()
expr_stmt|;
name|size
operator|=
name|binaryVars
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|colValues
operator|.
name|isSetStringVal
argument_list|()
condition|)
block|{
name|type
operator|=
name|Type
operator|.
name|STRING_TYPE
expr_stmt|;
name|nulls
operator|=
name|toBitset
argument_list|(
name|colValues
operator|.
name|getStringVal
argument_list|()
operator|.
name|getNulls
argument_list|()
argument_list|)
expr_stmt|;
name|stringVars
operator|=
name|colValues
operator|.
name|getStringVal
argument_list|()
operator|.
name|getValues
argument_list|()
expr_stmt|;
name|size
operator|=
name|stringVars
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"invalid union object"
argument_list|)
throw|;
block|}
block|}
specifier|public
name|ColumnBuffer
name|extractSubset
parameter_list|(
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|)
block|{
name|BitSet
name|subNulls
init|=
name|nulls
operator|.
name|get
argument_list|(
name|start
argument_list|,
name|end
argument_list|)
decl_stmt|;
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|BOOLEAN_TYPE
condition|)
block|{
name|ColumnBuffer
name|subset
init|=
operator|new
name|ColumnBuffer
argument_list|(
name|type
argument_list|,
name|subNulls
argument_list|,
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|boolVars
argument_list|,
name|start
argument_list|,
name|end
argument_list|)
argument_list|)
decl_stmt|;
name|boolVars
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|boolVars
argument_list|,
name|end
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|nulls
operator|=
name|nulls
operator|.
name|get
argument_list|(
name|start
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|size
operator|=
name|boolVars
operator|.
name|length
expr_stmt|;
return|return
name|subset
return|;
block|}
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|TINYINT_TYPE
condition|)
block|{
name|ColumnBuffer
name|subset
init|=
operator|new
name|ColumnBuffer
argument_list|(
name|type
argument_list|,
name|subNulls
argument_list|,
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|byteVars
argument_list|,
name|start
argument_list|,
name|end
argument_list|)
argument_list|)
decl_stmt|;
name|byteVars
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|byteVars
argument_list|,
name|end
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|nulls
operator|=
name|nulls
operator|.
name|get
argument_list|(
name|start
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|size
operator|=
name|byteVars
operator|.
name|length
expr_stmt|;
return|return
name|subset
return|;
block|}
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|SMALLINT_TYPE
condition|)
block|{
name|ColumnBuffer
name|subset
init|=
operator|new
name|ColumnBuffer
argument_list|(
name|type
argument_list|,
name|subNulls
argument_list|,
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|shortVars
argument_list|,
name|start
argument_list|,
name|end
argument_list|)
argument_list|)
decl_stmt|;
name|shortVars
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|shortVars
argument_list|,
name|end
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|nulls
operator|=
name|nulls
operator|.
name|get
argument_list|(
name|start
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|size
operator|=
name|shortVars
operator|.
name|length
expr_stmt|;
return|return
name|subset
return|;
block|}
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|INT_TYPE
condition|)
block|{
name|ColumnBuffer
name|subset
init|=
operator|new
name|ColumnBuffer
argument_list|(
name|type
argument_list|,
name|subNulls
argument_list|,
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|intVars
argument_list|,
name|start
argument_list|,
name|end
argument_list|)
argument_list|)
decl_stmt|;
name|intVars
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|intVars
argument_list|,
name|end
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|nulls
operator|=
name|nulls
operator|.
name|get
argument_list|(
name|start
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|size
operator|=
name|intVars
operator|.
name|length
expr_stmt|;
return|return
name|subset
return|;
block|}
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|BIGINT_TYPE
condition|)
block|{
name|ColumnBuffer
name|subset
init|=
operator|new
name|ColumnBuffer
argument_list|(
name|type
argument_list|,
name|subNulls
argument_list|,
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|longVars
argument_list|,
name|start
argument_list|,
name|end
argument_list|)
argument_list|)
decl_stmt|;
name|longVars
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|longVars
argument_list|,
name|end
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|nulls
operator|=
name|nulls
operator|.
name|get
argument_list|(
name|start
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|size
operator|=
name|longVars
operator|.
name|length
expr_stmt|;
return|return
name|subset
return|;
block|}
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|DOUBLE_TYPE
operator|||
name|type
operator|==
name|Type
operator|.
name|FLOAT_TYPE
condition|)
block|{
name|ColumnBuffer
name|subset
init|=
operator|new
name|ColumnBuffer
argument_list|(
name|type
argument_list|,
name|subNulls
argument_list|,
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|doubleVars
argument_list|,
name|start
argument_list|,
name|end
argument_list|)
argument_list|)
decl_stmt|;
name|doubleVars
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|doubleVars
argument_list|,
name|end
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|nulls
operator|=
name|nulls
operator|.
name|get
argument_list|(
name|start
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|size
operator|=
name|doubleVars
operator|.
name|length
expr_stmt|;
return|return
name|subset
return|;
block|}
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|BINARY_TYPE
condition|)
block|{
name|ColumnBuffer
name|subset
init|=
operator|new
name|ColumnBuffer
argument_list|(
name|type
argument_list|,
name|subNulls
argument_list|,
name|binaryVars
operator|.
name|subList
argument_list|(
name|start
argument_list|,
name|end
argument_list|)
argument_list|)
decl_stmt|;
name|binaryVars
operator|=
name|binaryVars
operator|.
name|subList
argument_list|(
name|end
argument_list|,
name|binaryVars
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|nulls
operator|=
name|nulls
operator|.
name|get
argument_list|(
name|start
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|size
operator|=
name|binaryVars
operator|.
name|size
argument_list|()
expr_stmt|;
return|return
name|subset
return|;
block|}
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|STRING_TYPE
condition|)
block|{
name|ColumnBuffer
name|subset
init|=
operator|new
name|ColumnBuffer
argument_list|(
name|type
argument_list|,
name|subNulls
argument_list|,
name|stringVars
operator|.
name|subList
argument_list|(
name|start
argument_list|,
name|end
argument_list|)
argument_list|)
decl_stmt|;
name|stringVars
operator|=
name|stringVars
operator|.
name|subList
argument_list|(
name|end
argument_list|,
name|stringVars
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|nulls
operator|=
name|nulls
operator|.
name|get
argument_list|(
name|start
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|size
operator|=
name|stringVars
operator|.
name|size
argument_list|()
expr_stmt|;
return|return
name|subset
return|;
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"invalid union object"
argument_list|)
throw|;
block|}
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|MASKS
init|=
operator|new
name|byte
index|[]
block|{
literal|0x01
block|,
literal|0x02
block|,
literal|0x04
block|,
literal|0x08
block|,
literal|0x10
block|,
literal|0x20
block|,
literal|0x40
block|,
operator|(
name|byte
operator|)
literal|0x80
block|}
decl_stmt|;
specifier|private
specifier|static
name|BitSet
name|toBitset
parameter_list|(
name|byte
index|[]
name|nulls
parameter_list|)
block|{
name|BitSet
name|bitset
init|=
operator|new
name|BitSet
argument_list|()
decl_stmt|;
name|int
name|bits
init|=
name|nulls
operator|.
name|length
operator|*
literal|8
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
name|bits
condition|;
name|i
operator|++
control|)
block|{
name|bitset
operator|.
name|set
argument_list|(
name|i
argument_list|,
operator|(
name|nulls
index|[
name|i
operator|/
literal|8
index|]
operator|&
name|MASKS
index|[
name|i
operator|%
literal|8
index|]
operator|)
operator|!=
literal|0
argument_list|)
expr_stmt|;
block|}
return|return
name|bitset
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
name|toBinary
parameter_list|(
name|BitSet
name|bitset
parameter_list|)
block|{
name|byte
index|[]
name|nulls
init|=
operator|new
name|byte
index|[
literal|1
operator|+
operator|(
name|bitset
operator|.
name|length
argument_list|()
operator|/
literal|8
operator|)
index|]
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
name|bitset
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|nulls
index|[
name|i
operator|/
literal|8
index|]
operator||=
name|bitset
operator|.
name|get
argument_list|(
name|i
argument_list|)
condition|?
name|MASKS
index|[
name|i
operator|%
literal|8
index|]
else|:
literal|0
expr_stmt|;
block|}
return|return
name|nulls
return|;
block|}
specifier|public
name|Type
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
if|if
condition|(
name|nulls
operator|.
name|get
argument_list|(
name|index
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|BOOLEAN_TYPE
case|:
return|return
name|boolVars
index|[
name|index
index|]
return|;
case|case
name|TINYINT_TYPE
case|:
return|return
name|byteVars
index|[
name|index
index|]
return|;
case|case
name|SMALLINT_TYPE
case|:
return|return
name|shortVars
index|[
name|index
index|]
return|;
case|case
name|INT_TYPE
case|:
return|return
name|intVars
index|[
name|index
index|]
return|;
case|case
name|BIGINT_TYPE
case|:
return|return
name|longVars
index|[
name|index
index|]
return|;
case|case
name|FLOAT_TYPE
case|:
case|case
name|DOUBLE_TYPE
case|:
return|return
name|doubleVars
index|[
name|index
index|]
return|;
case|case
name|STRING_TYPE
case|:
return|return
name|stringVars
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
case|case
name|BINARY_TYPE
case|:
return|return
name|binaryVars
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|array
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|size
return|;
block|}
specifier|public
name|TColumn
name|toTColumn
parameter_list|()
block|{
name|TColumn
name|value
init|=
operator|new
name|TColumn
argument_list|()
decl_stmt|;
name|ByteBuffer
name|nullMasks
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|toBinary
argument_list|(
name|nulls
argument_list|)
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|BOOLEAN_TYPE
case|:
name|value
operator|.
name|setBoolVal
argument_list|(
operator|new
name|TBoolColumn
argument_list|(
name|Booleans
operator|.
name|asList
argument_list|(
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|boolVars
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
argument_list|)
argument_list|,
name|nullMasks
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|TINYINT_TYPE
case|:
name|value
operator|.
name|setByteVal
argument_list|(
operator|new
name|TByteColumn
argument_list|(
name|Bytes
operator|.
name|asList
argument_list|(
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|byteVars
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
argument_list|)
argument_list|,
name|nullMasks
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|SMALLINT_TYPE
case|:
name|value
operator|.
name|setI16Val
argument_list|(
operator|new
name|TI16Column
argument_list|(
name|Shorts
operator|.
name|asList
argument_list|(
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|shortVars
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
argument_list|)
argument_list|,
name|nullMasks
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT_TYPE
case|:
name|value
operator|.
name|setI32Val
argument_list|(
operator|new
name|TI32Column
argument_list|(
name|Ints
operator|.
name|asList
argument_list|(
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|intVars
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
argument_list|)
argument_list|,
name|nullMasks
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|BIGINT_TYPE
case|:
name|value
operator|.
name|setI64Val
argument_list|(
operator|new
name|TI64Column
argument_list|(
name|Longs
operator|.
name|asList
argument_list|(
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|longVars
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
argument_list|)
argument_list|,
name|nullMasks
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT_TYPE
case|:
case|case
name|DOUBLE_TYPE
case|:
name|value
operator|.
name|setDoubleVal
argument_list|(
operator|new
name|TDoubleColumn
argument_list|(
name|Doubles
operator|.
name|asList
argument_list|(
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|doubleVars
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
argument_list|)
argument_list|,
name|nullMasks
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING_TYPE
case|:
name|value
operator|.
name|setStringVal
argument_list|(
operator|new
name|TStringColumn
argument_list|(
name|stringVars
argument_list|,
name|nullMasks
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|BINARY_TYPE
case|:
name|value
operator|.
name|setBinaryVal
argument_list|(
operator|new
name|TBinaryColumn
argument_list|(
name|binaryVars
argument_list|,
name|nullMasks
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
return|return
name|value
return|;
block|}
specifier|private
specifier|static
specifier|final
name|ByteBuffer
name|EMPTY_BINARY
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|EMPTY_STRING
init|=
literal|""
decl_stmt|;
specifier|public
name|void
name|addValue
parameter_list|(
name|Object
name|field
parameter_list|)
block|{
name|addValue
argument_list|(
name|this
operator|.
name|type
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addValue
parameter_list|(
name|Type
name|type
parameter_list|,
name|Object
name|field
parameter_list|)
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|BOOLEAN_TYPE
case|:
name|nulls
operator|.
name|set
argument_list|(
name|size
argument_list|,
name|field
operator|==
literal|null
argument_list|)
expr_stmt|;
name|boolVars
argument_list|()
index|[
name|size
index|]
operator|=
name|field
operator|==
literal|null
condition|?
literal|true
else|:
operator|(
name|Boolean
operator|)
name|field
expr_stmt|;
break|break;
case|case
name|TINYINT_TYPE
case|:
name|nulls
operator|.
name|set
argument_list|(
name|size
argument_list|,
name|field
operator|==
literal|null
argument_list|)
expr_stmt|;
name|byteVars
argument_list|()
index|[
name|size
index|]
operator|=
name|field
operator|==
literal|null
condition|?
literal|0
else|:
operator|(
name|Byte
operator|)
name|field
expr_stmt|;
break|break;
case|case
name|SMALLINT_TYPE
case|:
name|nulls
operator|.
name|set
argument_list|(
name|size
argument_list|,
name|field
operator|==
literal|null
argument_list|)
expr_stmt|;
name|shortVars
argument_list|()
index|[
name|size
index|]
operator|=
name|field
operator|==
literal|null
condition|?
literal|0
else|:
operator|(
name|Short
operator|)
name|field
expr_stmt|;
break|break;
case|case
name|INT_TYPE
case|:
name|nulls
operator|.
name|set
argument_list|(
name|size
argument_list|,
name|field
operator|==
literal|null
argument_list|)
expr_stmt|;
name|intVars
argument_list|()
index|[
name|size
index|]
operator|=
name|field
operator|==
literal|null
condition|?
literal|0
else|:
operator|(
name|Integer
operator|)
name|field
expr_stmt|;
break|break;
case|case
name|BIGINT_TYPE
case|:
name|nulls
operator|.
name|set
argument_list|(
name|size
argument_list|,
name|field
operator|==
literal|null
argument_list|)
expr_stmt|;
name|longVars
argument_list|()
index|[
name|size
index|]
operator|=
name|field
operator|==
literal|null
condition|?
literal|0
else|:
operator|(
name|Long
operator|)
name|field
expr_stmt|;
break|break;
case|case
name|FLOAT_TYPE
case|:
name|nulls
operator|.
name|set
argument_list|(
name|size
argument_list|,
name|field
operator|==
literal|null
argument_list|)
expr_stmt|;
name|doubleVars
argument_list|()
index|[
name|size
index|]
operator|=
name|field
operator|==
literal|null
condition|?
literal|0
else|:
operator|new
name|Double
argument_list|(
name|field
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE_TYPE
case|:
name|nulls
operator|.
name|set
argument_list|(
name|size
argument_list|,
name|field
operator|==
literal|null
argument_list|)
expr_stmt|;
name|doubleVars
argument_list|()
index|[
name|size
index|]
operator|=
name|field
operator|==
literal|null
condition|?
literal|0
else|:
operator|(
name|Double
operator|)
name|field
expr_stmt|;
break|break;
case|case
name|BINARY_TYPE
case|:
name|nulls
operator|.
name|set
argument_list|(
name|binaryVars
operator|.
name|size
argument_list|()
argument_list|,
name|field
operator|==
literal|null
argument_list|)
expr_stmt|;
name|binaryVars
operator|.
name|add
argument_list|(
name|field
operator|==
literal|null
condition|?
name|EMPTY_BINARY
else|:
name|ByteBuffer
operator|.
name|wrap
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|field
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
name|nulls
operator|.
name|set
argument_list|(
name|stringVars
operator|.
name|size
argument_list|()
argument_list|,
name|field
operator|==
literal|null
argument_list|)
expr_stmt|;
name|stringVars
operator|.
name|add
argument_list|(
name|field
operator|==
literal|null
condition|?
name|EMPTY_STRING
else|:
name|String
operator|.
name|valueOf
argument_list|(
name|field
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
name|size
operator|++
expr_stmt|;
block|}
specifier|private
name|boolean
index|[]
name|boolVars
parameter_list|()
block|{
if|if
condition|(
name|boolVars
operator|.
name|length
operator|==
name|size
condition|)
block|{
name|boolean
index|[]
name|newVars
init|=
operator|new
name|boolean
index|[
name|size
operator|<<
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|boolVars
argument_list|,
literal|0
argument_list|,
name|newVars
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
return|return
name|boolVars
operator|=
name|newVars
return|;
block|}
return|return
name|boolVars
return|;
block|}
specifier|private
name|byte
index|[]
name|byteVars
parameter_list|()
block|{
if|if
condition|(
name|byteVars
operator|.
name|length
operator|==
name|size
condition|)
block|{
name|byte
index|[]
name|newVars
init|=
operator|new
name|byte
index|[
name|size
operator|<<
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|byteVars
argument_list|,
literal|0
argument_list|,
name|newVars
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
return|return
name|byteVars
operator|=
name|newVars
return|;
block|}
return|return
name|byteVars
return|;
block|}
specifier|private
name|short
index|[]
name|shortVars
parameter_list|()
block|{
if|if
condition|(
name|shortVars
operator|.
name|length
operator|==
name|size
condition|)
block|{
name|short
index|[]
name|newVars
init|=
operator|new
name|short
index|[
name|size
operator|<<
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|shortVars
argument_list|,
literal|0
argument_list|,
name|newVars
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
return|return
name|shortVars
operator|=
name|newVars
return|;
block|}
return|return
name|shortVars
return|;
block|}
specifier|private
name|int
index|[]
name|intVars
parameter_list|()
block|{
if|if
condition|(
name|intVars
operator|.
name|length
operator|==
name|size
condition|)
block|{
name|int
index|[]
name|newVars
init|=
operator|new
name|int
index|[
name|size
operator|<<
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|intVars
argument_list|,
literal|0
argument_list|,
name|newVars
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
return|return
name|intVars
operator|=
name|newVars
return|;
block|}
return|return
name|intVars
return|;
block|}
specifier|private
name|long
index|[]
name|longVars
parameter_list|()
block|{
if|if
condition|(
name|longVars
operator|.
name|length
operator|==
name|size
condition|)
block|{
name|long
index|[]
name|newVars
init|=
operator|new
name|long
index|[
name|size
operator|<<
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|longVars
argument_list|,
literal|0
argument_list|,
name|newVars
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
return|return
name|longVars
operator|=
name|newVars
return|;
block|}
return|return
name|longVars
return|;
block|}
specifier|private
name|double
index|[]
name|doubleVars
parameter_list|()
block|{
if|if
condition|(
name|doubleVars
operator|.
name|length
operator|==
name|size
condition|)
block|{
name|double
index|[]
name|newVars
init|=
operator|new
name|double
index|[
name|size
operator|<<
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|doubleVars
argument_list|,
literal|0
argument_list|,
name|newVars
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
return|return
name|doubleVars
operator|=
name|newVars
return|;
block|}
return|return
name|doubleVars
return|;
block|}
block|}
end_class

end_unit

