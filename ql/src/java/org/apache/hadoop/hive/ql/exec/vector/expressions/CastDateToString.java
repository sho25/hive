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
name|serde2
operator|.
name|io
operator|.
name|DateWritable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_class
specifier|public
class|class
name|CastDateToString
extends|extends
name|LongToStringUnaryUDF
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|protected
specifier|transient
name|Date
name|dt
init|=
operator|new
name|Date
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|public
name|CastDateToString
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|CastDateToString
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
literal|0
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
name|long
index|[]
name|vector
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|dt
operator|.
name|setTime
argument_list|(
name|DateWritable
operator|.
name|daysToMillis
argument_list|(
operator|(
name|int
operator|)
name|vector
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|temp
init|=
name|dt
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|assign
argument_list|(
name|outV
argument_list|,
name|i
argument_list|,
name|temp
argument_list|,
name|temp
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

