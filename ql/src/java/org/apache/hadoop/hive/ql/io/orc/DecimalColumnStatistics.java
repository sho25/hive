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
name|io
operator|.
name|orc
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

begin_comment
comment|/**  * Statistics for decimal columns.  */
end_comment

begin_interface
specifier|public
interface|interface
name|DecimalColumnStatistics
extends|extends
name|ColumnStatistics
block|{
comment|/**    * Get the minimum value for the column.    * @return the minimum value    */
name|HiveDecimal
name|getMinimum
parameter_list|()
function_decl|;
comment|/**    * Get the maximum value for the column.    * @return the maximum value    */
name|HiveDecimal
name|getMaximum
parameter_list|()
function_decl|;
comment|/**    * Get the sum of the values of the column.    * @return the sum    */
name|HiveDecimal
name|getSum
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

