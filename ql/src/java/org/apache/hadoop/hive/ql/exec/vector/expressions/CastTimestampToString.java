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
name|format
operator|.
name|datetime
operator|.
name|HiveSqlDateTimeFormatter
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
name|TimestampColumnVector
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
name|java
operator|.
name|time
operator|.
name|Instant
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|LocalDateTime
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|ZoneOffset
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|format
operator|.
name|DateTimeFormatter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|format
operator|.
name|DateTimeFormatterBuilder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|temporal
operator|.
name|ChronoField
import|;
end_import

begin_class
specifier|public
class|class
name|CastTimestampToString
extends|extends
name|TimestampToStringUnaryUDF
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
specifier|static
specifier|final
name|DateTimeFormatter
name|PRINT_FORMATTER
decl_stmt|;
static|static
block|{
name|DateTimeFormatterBuilder
name|builder
init|=
operator|new
name|DateTimeFormatterBuilder
argument_list|()
decl_stmt|;
comment|// Date and time parts
name|builder
operator|.
name|append
argument_list|(
name|DateTimeFormatter
operator|.
name|ofPattern
argument_list|(
literal|"yyyy-MM-dd HH:mm:ss"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Fractional part
name|builder
operator|.
name|optionalStart
argument_list|()
operator|.
name|appendFraction
argument_list|(
name|ChronoField
operator|.
name|NANO_OF_SECOND
argument_list|,
literal|0
argument_list|,
literal|9
argument_list|,
literal|true
argument_list|)
operator|.
name|optionalEnd
argument_list|()
expr_stmt|;
name|PRINT_FORMATTER
operator|=
name|builder
operator|.
name|toFormatter
argument_list|()
expr_stmt|;
block|}
specifier|public
name|CastTimestampToString
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|CastTimestampToString
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
name|TimestampColumnVector
name|inV
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|byte
index|[]
name|temp
init|=
name|LocalDateTime
operator|.
name|ofInstant
argument_list|(
name|Instant
operator|.
name|ofEpochMilli
argument_list|(
name|inV
operator|.
name|time
index|[
name|i
index|]
argument_list|)
argument_list|,
name|ZoneOffset
operator|.
name|UTC
argument_list|)
operator|.
name|withNano
argument_list|(
name|inV
operator|.
name|nanos
index|[
name|i
index|]
argument_list|)
operator|.
name|format
argument_list|(
name|PRINT_FORMATTER
argument_list|)
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
comment|/**    * CastTimestampToString, CastTimestampToChar, CastTimestampToVarchar use this.    */
name|void
name|sqlFormat
parameter_list|(
name|BytesColumnVector
name|outV
parameter_list|,
name|TimestampColumnVector
name|inV
parameter_list|,
name|int
name|i
parameter_list|,
name|HiveSqlDateTimeFormatter
name|sqlFormatter
parameter_list|)
block|{
name|String
name|formattedString
init|=
name|sqlFormatter
operator|.
name|format
argument_list|(
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
name|Timestamp
operator|.
name|ofEpochMilli
argument_list|(
name|inV
operator|.
name|time
index|[
name|i
index|]
argument_list|,
name|inV
operator|.
name|nanos
index|[
name|i
index|]
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|formattedString
operator|==
literal|null
condition|)
block|{
name|outV
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|outV
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
return|return;
block|}
name|byte
index|[]
name|temp
init|=
name|formattedString
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
specifier|public
specifier|static
name|String
name|getTimestampString
parameter_list|(
name|Timestamp
name|ts
parameter_list|)
block|{
return|return
name|LocalDateTime
operator|.
name|ofInstant
argument_list|(
name|Instant
operator|.
name|ofEpochMilli
argument_list|(
name|ts
operator|.
name|getTime
argument_list|()
argument_list|)
argument_list|,
name|ZoneOffset
operator|.
name|UTC
argument_list|)
operator|.
name|withNano
argument_list|(
name|ts
operator|.
name|getNanos
argument_list|()
argument_list|)
operator|.
name|format
argument_list|(
name|PRINT_FORMATTER
argument_list|)
return|;
block|}
block|}
end_class

end_unit

