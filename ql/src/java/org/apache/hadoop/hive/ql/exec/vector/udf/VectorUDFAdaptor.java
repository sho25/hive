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
name|udf
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|MapredContext
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
name|UDFArgumentException
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
name|*
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
name|StringExpr
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
name|VectorExpression
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
name|exec
operator|.
name|vector
operator|.
name|expressions
operator|.
name|VectorExpressionWriterFactory
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
name|ExprNodeGenericFuncDesc
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDF
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
name|HiveCharWritable
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
name|primitive
operator|.
name|*
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
name|CharTypeInfo
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
name|Text
import|;
end_import

begin_comment
comment|/**  * A VectorUDFAdaptor is a vectorized expression for invoking a custom  * UDF on zero or more input vectors or constants which are the function arguments.  */
end_comment

begin_class
specifier|public
class|class
name|VectorUDFAdaptor
extends|extends
name|VectorExpression
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
name|int
name|outputColumn
decl_stmt|;
specifier|private
name|String
name|resultType
decl_stmt|;
specifier|private
name|VectorUDFArgDesc
index|[]
name|argDescs
decl_stmt|;
specifier|private
name|ExprNodeGenericFuncDesc
name|expr
decl_stmt|;
specifier|private
specifier|transient
name|GenericUDF
name|genericUDF
decl_stmt|;
specifier|private
specifier|transient
name|GenericUDF
operator|.
name|DeferredObject
index|[]
name|deferredChildren
decl_stmt|;
specifier|private
specifier|transient
name|ObjectInspector
name|outputOI
decl_stmt|;
specifier|private
specifier|transient
name|ObjectInspector
index|[]
name|childrenOIs
decl_stmt|;
specifier|private
specifier|transient
name|VectorExpressionWriter
index|[]
name|writers
decl_stmt|;
specifier|public
name|VectorUDFAdaptor
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorUDFAdaptor
parameter_list|(
name|ExprNodeGenericFuncDesc
name|expr
parameter_list|,
name|int
name|outputColumn
parameter_list|,
name|String
name|resultType
parameter_list|,
name|VectorUDFArgDesc
index|[]
name|argDescs
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|expr
operator|=
name|expr
expr_stmt|;
name|this
operator|.
name|outputColumn
operator|=
name|outputColumn
expr_stmt|;
name|this
operator|.
name|resultType
operator|=
name|resultType
expr_stmt|;
name|this
operator|.
name|argDescs
operator|=
name|argDescs
expr_stmt|;
block|}
comment|// Initialize transient fields. To be called after deserialization of other fields.
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|HiveException
throws|,
name|UDFArgumentException
block|{
name|genericUDF
operator|=
name|expr
operator|.
name|getGenericUDF
argument_list|()
expr_stmt|;
name|deferredChildren
operator|=
operator|new
name|GenericUDF
operator|.
name|DeferredObject
index|[
name|expr
operator|.
name|getChildren
argument_list|()
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|childrenOIs
operator|=
operator|new
name|ObjectInspector
index|[
name|expr
operator|.
name|getChildren
argument_list|()
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|writers
operator|=
name|VectorExpressionWriterFactory
operator|.
name|getExpressionWriters
argument_list|(
name|expr
operator|.
name|getChildren
argument_list|()
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
name|childrenOIs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|childrenOIs
index|[
name|i
index|]
operator|=
name|writers
index|[
name|i
index|]
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
block|}
name|MapredContext
name|context
init|=
name|MapredContext
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|context
operator|!=
literal|null
condition|)
block|{
name|context
operator|.
name|setup
argument_list|(
name|genericUDF
argument_list|)
expr_stmt|;
block|}
name|outputOI
operator|=
name|VectorExpressionWriterFactory
operator|.
name|genVectorExpressionWritable
argument_list|(
name|expr
argument_list|)
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
name|genericUDF
operator|.
name|initialize
argument_list|(
name|childrenOIs
argument_list|)
expr_stmt|;
comment|// Initialize constant arguments
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|argDescs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|argDescs
index|[
name|i
index|]
operator|.
name|isConstant
argument_list|()
condition|)
block|{
name|argDescs
index|[
name|i
index|]
operator|.
name|prepareConstant
argument_list|()
expr_stmt|;
block|}
block|}
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
if|if
condition|(
name|genericUDF
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|init
argument_list|()
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
if|if
condition|(
name|childExpressions
operator|!=
literal|null
condition|)
block|{
name|super
operator|.
name|evaluateChildren
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
name|int
index|[]
name|sel
init|=
name|batch
operator|.
name|selected
decl_stmt|;
name|int
name|n
init|=
name|batch
operator|.
name|size
decl_stmt|;
name|ColumnVector
name|outV
init|=
name|batch
operator|.
name|cols
index|[
name|outputColumn
index|]
decl_stmt|;
comment|// If the output column is of type string, initialize the buffer to receive data.
if|if
condition|(
name|outV
operator|instanceof
name|BytesColumnVector
condition|)
block|{
operator|(
operator|(
name|BytesColumnVector
operator|)
name|outV
operator|)
operator|.
name|initBuffer
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|n
operator|==
literal|0
condition|)
block|{
comment|//Nothing to do
return|return;
block|}
name|batch
operator|.
name|cols
index|[
name|outputColumn
index|]
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
comment|/* If all input columns are repeating, just evaluate function      * for row 0 in the batch and set output repeating.      */
if|if
condition|(
name|allInputColsRepeating
argument_list|(
name|batch
argument_list|)
condition|)
block|{
name|setResult
argument_list|(
literal|0
argument_list|,
name|batch
argument_list|)
expr_stmt|;
name|batch
operator|.
name|cols
index|[
name|outputColumn
index|]
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
return|return;
block|}
else|else
block|{
name|batch
operator|.
name|cols
index|[
name|outputColumn
index|]
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|!=
name|n
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
name|setResult
argument_list|(
name|i
argument_list|,
name|batch
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
name|setResult
argument_list|(
name|i
argument_list|,
name|batch
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/* Return false if any input column is non-repeating, otherwise true.    * This returns false if all the arguments are constant or there    * are zero arguments.    *    * A possible future optimization is to set the output to isRepeating    * for cases of all-constant arguments for deterministic functions.    */
specifier|private
name|boolean
name|allInputColsRepeating
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
block|{
name|int
name|varArgCount
init|=
literal|0
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
name|argDescs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|argDescs
index|[
name|i
index|]
operator|.
name|isVariable
argument_list|()
operator|&&
operator|!
name|batch
operator|.
name|cols
index|[
name|argDescs
index|[
name|i
index|]
operator|.
name|getColumnNum
argument_list|()
index|]
operator|.
name|isRepeating
condition|)
block|{
return|return
literal|false
return|;
block|}
name|varArgCount
operator|+=
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|varArgCount
operator|>
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
comment|/* Calculate the function result for row i of the batch and    * set the output column vector entry i to the result.    */
specifier|private
name|void
name|setResult
parameter_list|(
name|int
name|i
parameter_list|,
name|VectorizedRowBatch
name|b
parameter_list|)
block|{
comment|// get arguments
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|argDescs
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|deferredChildren
index|[
name|j
index|]
operator|=
name|argDescs
index|[
name|j
index|]
operator|.
name|getDeferredJavaObject
argument_list|(
name|i
argument_list|,
name|b
argument_list|,
name|j
argument_list|,
name|writers
argument_list|)
expr_stmt|;
block|}
comment|// call function
name|Object
name|result
decl_stmt|;
try|try
block|{
name|result
operator|=
name|genericUDF
operator|.
name|evaluate
argument_list|(
name|deferredChildren
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
comment|/* For UDFs that expect primitive types (like int instead of Integer or IntWritable),        * this will catch the the exception that happens if they are passed a NULL value.        * Then the default NULL handling logic will apply, and the result will be NULL.        */
name|result
operator|=
literal|null
expr_stmt|;
block|}
comment|// set output column vector entry
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|b
operator|.
name|cols
index|[
name|outputColumn
index|]
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|b
operator|.
name|cols
index|[
name|outputColumn
index|]
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|b
operator|.
name|cols
index|[
name|outputColumn
index|]
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|setOutputCol
argument_list|(
name|b
operator|.
name|cols
index|[
name|outputColumn
index|]
argument_list|,
name|i
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|setOutputCol
parameter_list|(
name|ColumnVector
name|colVec
parameter_list|,
name|int
name|i
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
comment|/* Depending on the output type, get the value, cast the result to the      * correct type if needed, and assign the result into the output vector.      */
if|if
condition|(
name|outputOI
operator|instanceof
name|WritableStringObjectInspector
condition|)
block|{
name|BytesColumnVector
name|bv
init|=
operator|(
name|BytesColumnVector
operator|)
name|colVec
decl_stmt|;
name|Text
name|t
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|String
condition|)
block|{
name|t
operator|=
operator|new
name|Text
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|t
operator|=
operator|(
operator|(
name|WritableStringObjectInspector
operator|)
name|outputOI
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|bv
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|t
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|t
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|outputOI
operator|instanceof
name|WritableHiveCharObjectInspector
condition|)
block|{
name|WritableHiveCharObjectInspector
name|writableHiveCharObjectOI
init|=
operator|(
name|WritableHiveCharObjectInspector
operator|)
name|outputOI
decl_stmt|;
name|int
name|maxLength
init|=
operator|(
operator|(
name|CharTypeInfo
operator|)
name|writableHiveCharObjectOI
operator|.
name|getTypeInfo
argument_list|()
operator|)
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|BytesColumnVector
name|bv
init|=
operator|(
name|BytesColumnVector
operator|)
name|colVec
decl_stmt|;
name|HiveCharWritable
name|hiveCharWritable
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|HiveCharWritable
condition|)
block|{
name|hiveCharWritable
operator|=
operator|(
operator|(
name|HiveCharWritable
operator|)
name|value
operator|)
expr_stmt|;
block|}
else|else
block|{
name|hiveCharWritable
operator|=
name|writableHiveCharObjectOI
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|Text
name|t
init|=
name|hiveCharWritable
operator|.
name|getTextValue
argument_list|()
decl_stmt|;
comment|// In vector mode, we stored CHAR as unpadded.
name|StringExpr
operator|.
name|rightTrimAndTruncate
argument_list|(
name|bv
argument_list|,
name|i
argument_list|,
name|t
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|t
operator|.
name|getLength
argument_list|()
argument_list|,
name|maxLength
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|outputOI
operator|instanceof
name|WritableHiveVarcharObjectInspector
condition|)
block|{
name|WritableHiveVarcharObjectInspector
name|writableHiveVarcharObjectOI
init|=
operator|(
name|WritableHiveVarcharObjectInspector
operator|)
name|outputOI
decl_stmt|;
name|int
name|maxLength
init|=
operator|(
operator|(
name|VarcharTypeInfo
operator|)
name|writableHiveVarcharObjectOI
operator|.
name|getTypeInfo
argument_list|()
operator|)
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|BytesColumnVector
name|bv
init|=
operator|(
name|BytesColumnVector
operator|)
name|colVec
decl_stmt|;
name|HiveVarcharWritable
name|hiveVarcharWritable
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|HiveVarcharWritable
condition|)
block|{
name|hiveVarcharWritable
operator|=
operator|(
operator|(
name|HiveVarcharWritable
operator|)
name|value
operator|)
expr_stmt|;
block|}
else|else
block|{
name|hiveVarcharWritable
operator|=
name|writableHiveVarcharObjectOI
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|Text
name|t
init|=
name|hiveVarcharWritable
operator|.
name|getTextValue
argument_list|()
decl_stmt|;
name|StringExpr
operator|.
name|truncate
argument_list|(
name|bv
argument_list|,
name|i
argument_list|,
name|t
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|t
operator|.
name|getLength
argument_list|()
argument_list|,
name|maxLength
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|outputOI
operator|instanceof
name|WritableIntObjectInspector
condition|)
block|{
name|LongColumnVector
name|lv
init|=
operator|(
name|LongColumnVector
operator|)
name|colVec
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|Integer
condition|)
block|{
name|lv
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
name|Integer
operator|)
name|value
expr_stmt|;
block|}
else|else
block|{
name|lv
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|WritableIntObjectInspector
operator|)
name|outputOI
operator|)
operator|.
name|get
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|outputOI
operator|instanceof
name|WritableLongObjectInspector
condition|)
block|{
name|LongColumnVector
name|lv
init|=
operator|(
name|LongColumnVector
operator|)
name|colVec
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|Long
condition|)
block|{
name|lv
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
name|Long
operator|)
name|value
expr_stmt|;
block|}
else|else
block|{
name|lv
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|WritableLongObjectInspector
operator|)
name|outputOI
operator|)
operator|.
name|get
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|outputOI
operator|instanceof
name|WritableDoubleObjectInspector
condition|)
block|{
name|DoubleColumnVector
name|dv
init|=
operator|(
name|DoubleColumnVector
operator|)
name|colVec
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|Double
condition|)
block|{
name|dv
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
name|Double
operator|)
name|value
expr_stmt|;
block|}
else|else
block|{
name|dv
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|WritableDoubleObjectInspector
operator|)
name|outputOI
operator|)
operator|.
name|get
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|outputOI
operator|instanceof
name|WritableFloatObjectInspector
condition|)
block|{
name|DoubleColumnVector
name|dv
init|=
operator|(
name|DoubleColumnVector
operator|)
name|colVec
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|Float
condition|)
block|{
name|dv
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
name|Float
operator|)
name|value
expr_stmt|;
block|}
else|else
block|{
name|dv
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|WritableFloatObjectInspector
operator|)
name|outputOI
operator|)
operator|.
name|get
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|outputOI
operator|instanceof
name|WritableShortObjectInspector
condition|)
block|{
name|LongColumnVector
name|lv
init|=
operator|(
name|LongColumnVector
operator|)
name|colVec
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|Short
condition|)
block|{
name|lv
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
name|Short
operator|)
name|value
expr_stmt|;
block|}
else|else
block|{
name|lv
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|WritableShortObjectInspector
operator|)
name|outputOI
operator|)
operator|.
name|get
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|outputOI
operator|instanceof
name|WritableByteObjectInspector
condition|)
block|{
name|LongColumnVector
name|lv
init|=
operator|(
name|LongColumnVector
operator|)
name|colVec
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|Byte
condition|)
block|{
name|lv
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
name|Byte
operator|)
name|value
expr_stmt|;
block|}
else|else
block|{
name|lv
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|WritableByteObjectInspector
operator|)
name|outputOI
operator|)
operator|.
name|get
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|outputOI
operator|instanceof
name|WritableTimestampObjectInspector
condition|)
block|{
name|TimestampColumnVector
name|tv
init|=
operator|(
name|TimestampColumnVector
operator|)
name|colVec
decl_stmt|;
name|Timestamp
name|ts
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|Timestamp
condition|)
block|{
name|ts
operator|=
operator|(
name|Timestamp
operator|)
name|value
expr_stmt|;
block|}
else|else
block|{
name|ts
operator|=
operator|(
operator|(
name|WritableTimestampObjectInspector
operator|)
name|outputOI
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|tv
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|outputOI
operator|instanceof
name|WritableDateObjectInspector
condition|)
block|{
name|LongColumnVector
name|lv
init|=
operator|(
name|LongColumnVector
operator|)
name|colVec
decl_stmt|;
name|Date
name|ts
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|Date
condition|)
block|{
name|ts
operator|=
operator|(
name|Date
operator|)
name|value
expr_stmt|;
block|}
else|else
block|{
name|ts
operator|=
operator|(
operator|(
name|WritableDateObjectInspector
operator|)
name|outputOI
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|long
name|l
init|=
name|DateWritable
operator|.
name|dateToDays
argument_list|(
name|ts
argument_list|)
decl_stmt|;
name|lv
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|l
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|outputOI
operator|instanceof
name|WritableBooleanObjectInspector
condition|)
block|{
name|LongColumnVector
name|lv
init|=
operator|(
name|LongColumnVector
operator|)
name|colVec
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|Boolean
condition|)
block|{
name|lv
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
name|Boolean
operator|)
name|value
condition|?
literal|1
else|:
literal|0
expr_stmt|;
block|}
else|else
block|{
name|lv
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|WritableBooleanObjectInspector
operator|)
name|outputOI
operator|)
operator|.
name|get
argument_list|(
name|value
argument_list|)
condition|?
literal|1
else|:
literal|0
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|outputOI
operator|instanceof
name|WritableHiveDecimalObjectInspector
condition|)
block|{
name|DecimalColumnVector
name|dcv
init|=
operator|(
name|DecimalColumnVector
operator|)
name|colVec
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|HiveDecimal
condition|)
block|{
name|dcv
operator|.
name|set
argument_list|(
name|i
argument_list|,
operator|(
name|HiveDecimal
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|HiveDecimal
name|hd
init|=
operator|(
operator|(
name|WritableHiveDecimalObjectInspector
operator|)
name|outputOI
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|dcv
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|hd
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unhandled object type "
operator|+
name|outputOI
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getOutputColumn
parameter_list|()
block|{
return|return
name|outputColumn
return|;
block|}
specifier|public
name|void
name|setOutputColumn
parameter_list|(
name|int
name|outputColumn
parameter_list|)
block|{
name|this
operator|.
name|outputColumn
operator|=
name|outputColumn
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getOutputType
parameter_list|()
block|{
return|return
name|resultType
return|;
block|}
specifier|public
name|String
name|getResultType
parameter_list|()
block|{
return|return
name|resultType
return|;
block|}
specifier|public
name|void
name|setResultType
parameter_list|(
name|String
name|resultType
parameter_list|)
block|{
name|this
operator|.
name|resultType
operator|=
name|resultType
expr_stmt|;
block|}
specifier|public
name|VectorUDFArgDesc
index|[]
name|getArgDescs
parameter_list|()
block|{
return|return
name|argDescs
return|;
block|}
specifier|public
name|void
name|setArgDescs
parameter_list|(
name|VectorUDFArgDesc
index|[]
name|argDescs
parameter_list|)
block|{
name|this
operator|.
name|argDescs
operator|=
name|argDescs
expr_stmt|;
block|}
specifier|public
name|ExprNodeGenericFuncDesc
name|getExpr
parameter_list|()
block|{
return|return
name|expr
return|;
block|}
specifier|public
name|void
name|setExpr
parameter_list|(
name|ExprNodeGenericFuncDesc
name|expr
parameter_list|)
block|{
name|this
operator|.
name|expr
operator|=
name|expr
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
name|expr
operator|.
name|getExprString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|VectorExpressionDescriptor
operator|.
name|Descriptor
name|getDescriptor
parameter_list|()
block|{
return|return
operator|(
operator|new
name|VectorExpressionDescriptor
operator|.
name|Builder
argument_list|()
operator|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

