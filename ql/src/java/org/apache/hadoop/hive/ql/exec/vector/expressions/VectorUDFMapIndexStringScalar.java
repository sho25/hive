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
name|exec
operator|.
name|vector
operator|.
name|expressions
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|ArrayUtils
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
name|VectorExpressionDescriptor
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

begin_comment
comment|/**  * Returns value of Map.  * Extends {@link VectorUDFMapIndexBaseScalar}  */
end_comment

begin_class
specifier|public
class|class
name|VectorUDFMapIndexStringScalar
extends|extends
name|VectorUDFMapIndexBaseScalar
block|{
specifier|private
name|byte
index|[]
name|key
decl_stmt|;
specifier|public
name|VectorUDFMapIndexStringScalar
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorUDFMapIndexStringScalar
parameter_list|(
name|int
name|mapColumnNum
parameter_list|,
name|byte
index|[]
name|key
parameter_list|,
name|int
name|outputColumnNum
parameter_list|)
block|{
name|super
argument_list|(
name|mapColumnNum
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
name|this
operator|.
name|key
operator|=
name|key
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
name|getColumnParamString
argument_list|(
literal|0
argument_list|,
name|getMapColumnNum
argument_list|()
argument_list|)
operator|+
literal|", key: "
operator|+
operator|new
name|String
argument_list|(
name|key
argument_list|)
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
name|setMode
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|Mode
operator|.
name|PROJECTION
argument_list|)
operator|.
name|setNumArguments
argument_list|(
literal|2
argument_list|)
operator|.
name|setArgumentTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|MAP
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|STRING_FAMILY
argument_list|)
operator|.
name|setInputExpressionTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|COLUMN
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|SCALAR
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Object
name|getKeyByIndex
parameter_list|(
name|ColumnVector
name|cv
parameter_list|,
name|int
name|index
parameter_list|)
block|{
name|BytesColumnVector
name|bytesCV
init|=
operator|(
name|BytesColumnVector
operator|)
name|cv
decl_stmt|;
return|return
name|ArrayUtils
operator|.
name|subarray
argument_list|(
name|bytesCV
operator|.
name|vector
index|[
name|index
index|]
argument_list|,
name|bytesCV
operator|.
name|start
index|[
name|index
index|]
argument_list|,
name|bytesCV
operator|.
name|start
index|[
name|index
index|]
operator|+
name|bytesCV
operator|.
name|length
index|[
name|index
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getCurrentKey
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|key
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|compareKeyInternal
parameter_list|(
name|Object
name|columnKey
parameter_list|,
name|Object
name|otherKey
parameter_list|)
block|{
return|return
name|Arrays
operator|.
name|equals
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|columnKey
argument_list|,
operator|(
name|byte
index|[]
operator|)
name|otherKey
argument_list|)
return|;
block|}
block|}
end_class

end_unit

