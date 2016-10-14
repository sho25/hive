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
operator|.
name|expressions
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
name|BytesColumnVector
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
name|DecimalColumnVector
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
name|DoubleColumnVector
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
name|LongColumnVector
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
name|VectorExpressionDescriptor
operator|.
name|Descriptor
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
name|ColumnVector
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
name|VectorizationContext
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
name|VectorizedRowBatch
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
name|ql
operator|.
name|plan
operator|.
name|ExprNodeDesc
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
name|binarysortable
operator|.
name|fast
operator|.
name|BinarySortableSerializeWrite
import|;
end_import

begin_comment
comment|/**  * Evaluate an IN filter on a batch for a vector of structs.  * This is optimized so that no objects have to be created in  * the inner loop, and there is a hash table implemented  * with Cuckoo hashing that has fast lookup to do the IN test.  */
end_comment

begin_class
specifier|public
class|class
name|FilterStructColumnInList
extends|extends
name|FilterStringColumnInList
implements|implements
name|IStructInExpr
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|VectorExpression
index|[]
name|structExpressions
decl_stmt|;
specifier|private
name|ColumnVector
operator|.
name|Type
index|[]
name|fieldVectorColumnTypes
decl_stmt|;
specifier|private
name|int
index|[]
name|structColumnMap
decl_stmt|;
specifier|private
name|int
name|scratchBytesColumn
decl_stmt|;
specifier|private
specifier|transient
name|Output
name|buffer
decl_stmt|;
specifier|private
specifier|transient
name|BinarySortableSerializeWrite
name|binarySortableSerializeWrite
decl_stmt|;
comment|/**    * After construction you must call setInListValues() to add the values to the IN set    * (on the IStringInExpr interface).    *    * And, call a and b on the IStructInExpr interface.    */
specifier|public
name|FilterStructColumnInList
parameter_list|()
block|{
name|super
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|evaluate
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
block|{
specifier|final
name|int
name|logicalSize
init|=
name|batch
operator|.
name|size
decl_stmt|;
if|if
condition|(
name|logicalSize
operator|==
literal|0
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|buffer
operator|==
literal|null
condition|)
block|{
name|buffer
operator|=
operator|new
name|Output
argument_list|()
expr_stmt|;
name|binarySortableSerializeWrite
operator|=
operator|new
name|BinarySortableSerializeWrite
argument_list|(
name|structColumnMap
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|VectorExpression
name|ve
range|:
name|structExpressions
control|)
block|{
name|ve
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
name|BytesColumnVector
name|scratchBytesColumnVector
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|scratchBytesColumn
index|]
decl_stmt|;
try|try
block|{
name|boolean
name|selectedInUse
init|=
name|batch
operator|.
name|selectedInUse
decl_stmt|;
name|int
index|[]
name|selected
init|=
name|batch
operator|.
name|selected
decl_stmt|;
for|for
control|(
name|int
name|logical
init|=
literal|0
init|;
name|logical
operator|<
name|logicalSize
condition|;
name|logical
operator|++
control|)
block|{
name|int
name|batchIndex
init|=
operator|(
name|selectedInUse
condition|?
name|selected
index|[
name|logical
index|]
else|:
name|logical
operator|)
decl_stmt|;
name|binarySortableSerializeWrite
operator|.
name|set
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|f
init|=
literal|0
init|;
name|f
operator|<
name|structColumnMap
operator|.
name|length
condition|;
name|f
operator|++
control|)
block|{
name|int
name|fieldColumn
init|=
name|structColumnMap
index|[
name|f
index|]
decl_stmt|;
name|ColumnVector
name|colVec
init|=
name|batch
operator|.
name|cols
index|[
name|fieldColumn
index|]
decl_stmt|;
name|int
name|adjustedIndex
init|=
operator|(
name|colVec
operator|.
name|isRepeating
condition|?
literal|0
else|:
name|batchIndex
operator|)
decl_stmt|;
if|if
condition|(
name|colVec
operator|.
name|noNulls
operator|||
operator|!
name|colVec
operator|.
name|isNull
index|[
name|adjustedIndex
index|]
condition|)
block|{
switch|switch
condition|(
name|fieldVectorColumnTypes
index|[
name|f
index|]
condition|)
block|{
case|case
name|BYTES
case|:
block|{
name|BytesColumnVector
name|bytesColVec
init|=
operator|(
name|BytesColumnVector
operator|)
name|colVec
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|bytesColVec
operator|.
name|vector
index|[
name|adjustedIndex
index|]
decl_stmt|;
name|int
name|start
init|=
name|bytesColVec
operator|.
name|start
index|[
name|adjustedIndex
index|]
decl_stmt|;
name|int
name|length
init|=
name|bytesColVec
operator|.
name|length
index|[
name|adjustedIndex
index|]
decl_stmt|;
name|binarySortableSerializeWrite
operator|.
name|writeString
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|LONG
case|:
name|binarySortableSerializeWrite
operator|.
name|writeLong
argument_list|(
operator|(
operator|(
name|LongColumnVector
operator|)
name|colVec
operator|)
operator|.
name|vector
index|[
name|adjustedIndex
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|binarySortableSerializeWrite
operator|.
name|writeDouble
argument_list|(
operator|(
operator|(
name|DoubleColumnVector
operator|)
name|colVec
operator|)
operator|.
name|vector
index|[
name|adjustedIndex
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|DecimalColumnVector
name|decColVector
init|=
operator|(
operator|(
name|DecimalColumnVector
operator|)
name|colVec
operator|)
decl_stmt|;
name|binarySortableSerializeWrite
operator|.
name|writeHiveDecimal
argument_list|(
name|decColVector
operator|.
name|vector
index|[
name|adjustedIndex
index|]
operator|.
name|getHiveDecimal
argument_list|()
argument_list|,
name|decColVector
operator|.
name|scale
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected vector column type "
operator|+
name|fieldVectorColumnTypes
index|[
name|f
index|]
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|binarySortableSerializeWrite
operator|.
name|writeNull
argument_list|()
expr_stmt|;
block|}
block|}
name|scratchBytesColumnVector
operator|.
name|setVal
argument_list|(
name|batchIndex
argument_list|,
name|buffer
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Now, take the serialized keys we just wrote into our scratch column and look them
comment|// up in the IN list.
name|super
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getOutputType
parameter_list|()
block|{
return|return
literal|"boolean"
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getOutputColumn
parameter_list|()
block|{
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|Descriptor
name|getDescriptor
parameter_list|()
block|{
comment|// This VectorExpression (IN) is a special case, so don't return a descriptor.
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setScratchBytesColumn
parameter_list|(
name|int
name|scratchBytesColumn
parameter_list|)
block|{
comment|// Tell our super class FilterStringColumnInList it will be evaluating our scratch
comment|// BytesColumnVector.
name|super
operator|.
name|setInputColumn
argument_list|(
name|scratchBytesColumn
argument_list|)
expr_stmt|;
name|this
operator|.
name|scratchBytesColumn
operator|=
name|scratchBytesColumn
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setStructColumnExprs
parameter_list|(
name|VectorizationContext
name|vContext
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|structColumnExprs
parameter_list|,
name|ColumnVector
operator|.
name|Type
index|[]
name|fieldVectorColumnTypes
parameter_list|)
throws|throws
name|HiveException
block|{
name|structExpressions
operator|=
name|vContext
operator|.
name|getVectorExpressions
argument_list|(
name|structColumnExprs
argument_list|)
expr_stmt|;
name|structColumnMap
operator|=
operator|new
name|int
index|[
name|structExpressions
operator|.
name|length
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
name|structColumnMap
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|VectorExpression
name|ve
init|=
name|structExpressions
index|[
name|i
index|]
decl_stmt|;
name|structColumnMap
index|[
name|i
index|]
operator|=
name|ve
operator|.
name|getOutputColumn
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|fieldVectorColumnTypes
operator|=
name|fieldVectorColumnTypes
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|vectorExpressionParameters
parameter_list|()
block|{
return|return
literal|"structExpressions "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|structExpressions
argument_list|)
operator|+
literal|", fieldVectorColumnTypes "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|fieldVectorColumnTypes
argument_list|)
operator|+
literal|", structColumnMap "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|structColumnMap
argument_list|)
return|;
block|}
block|}
end_class

end_unit

