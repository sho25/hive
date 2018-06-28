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
name|serde2
operator|.
name|io
operator|.
name|DateWritableV2
import|;
end_import

begin_comment
comment|/**  * Return Unix Timestamp.  * Extends {@link VectorUDFTimestampFieldDate}  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|VectorUDFUnixTimeStampDate
extends|extends
name|VectorUDFTimestampFieldDate
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
name|DateWritableV2
name|dateWritable
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|long
name|getDateField
parameter_list|(
name|long
name|days
parameter_list|)
block|{
name|dateWritable
operator|.
name|set
argument_list|(
operator|(
name|int
operator|)
name|days
argument_list|)
expr_stmt|;
return|return
name|dateWritable
operator|.
name|getTimeInSeconds
argument_list|()
return|;
block|}
specifier|public
name|VectorUDFUnixTimeStampDate
parameter_list|(
name|int
name|colNum
parameter_list|,
name|int
name|outputColumnNum
parameter_list|)
block|{
comment|/* not a real field */
name|super
argument_list|(
operator|-
literal|1
argument_list|,
name|colNum
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
name|dateWritable
operator|=
operator|new
name|DateWritableV2
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorUDFUnixTimeStampDate
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

