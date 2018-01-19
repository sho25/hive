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
name|io
operator|.
name|HiveDecimalWritable
import|;
end_import

begin_comment
comment|/**  * To support vectorized cast of decimal to string.  */
end_comment

begin_class
specifier|public
class|class
name|CastDecimalToString
extends|extends
name|DecimalToStringUnaryUDF
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|// Transient members initialized by transientInit method.
comment|// We use a scratch buffer with the HiveDecimalWritable toBytes method so
comment|// we don't incur poor performance creating a String result.
specifier|private
specifier|transient
name|byte
index|[]
name|scratchBuffer
decl_stmt|;
specifier|public
name|CastDecimalToString
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|CastDecimalToString
parameter_list|(
name|int
name|inputColumn
parameter_list|,
name|int
name|outputColumnNum
parameter_list|)
block|{
name|super
argument_list|(
name|inputColumn
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|transientInit
parameter_list|()
throws|throws
name|HiveException
block|{
name|super
operator|.
name|transientInit
argument_list|()
expr_stmt|;
name|scratchBuffer
operator|=
operator|new
name|byte
index|[
name|HiveDecimal
operator|.
name|SCRATCH_BUFFER_LEN_TO_BYTES
index|]
expr_stmt|;
block|}
comment|// The assign method will be overridden for CHAR and VARCHAR.
specifier|protected
name|void
name|assign
parameter_list|(
name|BytesColumnVector
name|outV
parameter_list|,
name|int
name|i
parameter_list|,
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|outV
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
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
name|DecimalColumnVector
name|inV
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|HiveDecimalWritable
name|decWritable
init|=
name|inV
operator|.
name|vector
index|[
name|i
index|]
decl_stmt|;
specifier|final
name|int
name|byteIndex
init|=
name|decWritable
operator|.
name|toBytes
argument_list|(
name|scratchBuffer
argument_list|)
decl_stmt|;
name|assign
argument_list|(
name|outV
argument_list|,
name|i
argument_list|,
name|scratchBuffer
argument_list|,
name|byteIndex
argument_list|,
name|HiveDecimal
operator|.
name|SCRATCH_BUFFER_LEN_TO_BYTES
operator|-
name|byteIndex
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

