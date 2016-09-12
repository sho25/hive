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
name|udf
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
name|Description
import|;
end_import

begin_comment
comment|/**  * UDFDateFloorWeek.  *  * Converts a timestamp to a timestamp with week granularity.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"floor_week"
argument_list|,
name|value
operator|=
literal|"_FUNC_(param) - Returns the timestamp at a week granularity"
argument_list|,
name|extended
operator|=
literal|"param needs to be a timestamp value\n"
operator|+
literal|"Example:\n "
operator|+
literal|"> SELECT _FUNC_(CAST('yyyy-MM-dd HH:mm:ss' AS TIMESTAMP)) FROM src;\n"
operator|+
literal|"  yyyy-MM-xx 00:00:00"
argument_list|)
specifier|public
class|class
name|UDFDateFloorWeek
extends|extends
name|UDFDateFloor
block|{
specifier|public
name|UDFDateFloorWeek
parameter_list|()
block|{
name|super
argument_list|(
literal|"WEEK"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

