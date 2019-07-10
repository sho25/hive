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
name|common
operator|.
name|type
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
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_comment
comment|/**  * Vectorized UDF for CAST (<STRING> TO TIMESTAMP WITH FORMAT<STRING>).  */
end_comment

begin_class
specifier|public
class|class
name|CastStringToTimestampWithFormat
extends|extends
name|CastStringToTimestamp
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
name|HiveSqlDateTimeFormatter
name|formatter
decl_stmt|;
specifier|public
name|CastStringToTimestampWithFormat
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|CastStringToTimestampWithFormat
parameter_list|(
name|int
name|inputColumn
parameter_list|,
name|byte
index|[]
name|patternBytes
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
if|if
condition|(
name|patternBytes
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Tried to cast (<string> to timestamp with format"
operator|+
literal|"<pattern>), but<pattern> not found"
argument_list|)
throw|;
block|}
name|formatter
operator|=
operator|new
name|HiveSqlDateTimeFormatter
argument_list|(
operator|new
name|String
argument_list|(
name|patternBytes
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|evaluate
parameter_list|(
name|TimestampColumnVector
name|outputColVector
parameter_list|,
name|BytesColumnVector
name|inputColVector
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|String
name|inputString
init|=
operator|new
name|String
argument_list|(
name|inputColVector
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|inputColVector
operator|.
name|start
index|[
name|i
index|]
argument_list|,
name|inputColVector
operator|.
name|length
index|[
name|i
index|]
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
name|Timestamp
name|timestamp
init|=
name|formatter
operator|.
name|parseTimestamp
argument_list|(
name|inputString
operator|.
name|replaceAll
argument_list|(
literal|"\u0000"
argument_list|,
literal|""
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|timestamp
operator|!=
literal|null
condition|)
block|{
name|outputColVector
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|timestamp
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|super
operator|.
name|setNull
argument_list|(
name|outputColVector
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

