begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|hive
operator|.
name|it
operator|.
name|custom
operator|.
name|udfs
operator|.
name|vector
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
name|exec
operator|.
name|vector
operator|.
name|expressions
operator|.
name|StringUnaryUDF
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
name|StringUnaryUDFDirect
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
name|orc
operator|.
name|impl
operator|.
name|TreeReaderFactory
operator|.
name|BytesColumnVectorUtil
import|;
end_import

begin_class
specifier|public
class|class
name|VectorStringRot13
extends|extends
name|StringUnaryUDFDirect
block|{
specifier|public
name|VectorStringRot13
parameter_list|(
name|int
name|inputColumn
parameter_list|,
name|int
name|outputColumn
parameter_list|)
block|{
name|super
argument_list|(
name|inputColumn
argument_list|,
name|outputColumn
argument_list|)
expr_stmt|;
block|}
specifier|public
name|VectorStringRot13
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|func
parameter_list|(
name|BytesColumnVector
name|outV
parameter_list|,
name|byte
index|[]
index|[]
name|vector
parameter_list|,
name|int
index|[]
name|start
parameter_list|,
name|int
index|[]
name|length
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|int
name|off
init|=
name|start
index|[
name|i
index|]
decl_stmt|;
name|int
name|len
init|=
name|length
index|[
name|i
index|]
decl_stmt|;
name|byte
index|[]
name|src
init|=
name|vector
index|[
name|i
index|]
decl_stmt|;
name|byte
index|[]
name|dst
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|len
condition|;
name|j
operator|++
control|)
block|{
name|dst
index|[
name|j
index|]
operator|=
name|rot13
argument_list|(
name|src
index|[
name|off
operator|+
name|j
index|]
argument_list|)
expr_stmt|;
block|}
name|outV
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|dst
argument_list|,
literal|0
argument_list|,
name|length
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
specifier|private
name|byte
name|rot13
parameter_list|(
name|byte
name|b
parameter_list|)
block|{
if|if
condition|(
name|b
operator|>=
literal|'a'
operator|&&
name|b
operator|<=
literal|'m'
operator|||
name|b
operator|>=
literal|'A'
operator|&&
name|b
operator|<=
literal|'M'
condition|)
block|{
return|return
call|(
name|byte
call|)
argument_list|(
name|b
operator|+
literal|13
argument_list|)
return|;
block|}
if|if
condition|(
name|b
operator|>=
literal|'n'
operator|&&
name|b
operator|<=
literal|'z'
operator|||
name|b
operator|>=
literal|'N'
operator|&&
name|b
operator|<=
literal|'Z'
condition|)
block|{
return|return
call|(
name|byte
call|)
argument_list|(
name|b
operator|-
literal|13
argument_list|)
return|;
block|}
return|return
name|b
return|;
block|}
block|}
end_class

end_unit

