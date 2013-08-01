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
name|Calendar
import|;
end_import

begin_comment
comment|/**  * Returns hour of day.  * Extends {@link VectorUDFTimestampFieldLong}  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|VectorUDFHourLong
extends|extends
name|VectorUDFTimestampFieldLong
block|{
specifier|public
name|VectorUDFHourLong
parameter_list|(
name|int
name|colNum
parameter_list|,
name|int
name|outputColumn
parameter_list|)
block|{
name|super
argument_list|(
name|Calendar
operator|.
name|HOUR_OF_DAY
argument_list|,
name|colNum
argument_list|,
name|outputColumn
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

