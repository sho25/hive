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
operator|.
name|generic
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
comment|/**  * GenericUDF Class for SQL construct "least(v1, v2, .. vn)".  *  * NOTES: 1. v1, v2 and vn should have the same TypeInfo, or an exception will  * be thrown.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"least"
argument_list|,
name|value
operator|=
literal|"_FUNC_(v1, v2, ...) - Returns the least value in a list of values"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_(2, 3, 1) FROM src LIMIT 1;\n"
operator|+
literal|"  1"
argument_list|)
specifier|public
class|class
name|GenericUDFLeast
extends|extends
name|GenericUDFGreatest
block|{
annotation|@
name|Override
specifier|protected
name|String
name|getFuncName
parameter_list|()
block|{
return|return
literal|"least"
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|isGreatest
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

