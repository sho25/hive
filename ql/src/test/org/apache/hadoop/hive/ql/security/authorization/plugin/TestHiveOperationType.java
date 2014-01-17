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
name|security
operator|.
name|authorization
operator|.
name|plugin
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
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
name|plan
operator|.
name|HiveOperation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * Test HiveOperationType  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveOperationType
block|{
comment|/**    * test that all enums in {@link HiveOperation} match one in @{link HiveOperationType}    */
annotation|@
name|Test
specifier|public
name|void
name|checkHiveOperationTypeMatch
parameter_list|()
block|{
for|for
control|(
name|HiveOperation
name|op
range|:
name|HiveOperation
operator|.
name|values
argument_list|()
control|)
block|{
try|try
block|{
name|HiveOperationType
operator|.
name|valueOf
argument_list|(
name|op
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// if value is null or not found, exception would get thrown
name|fail
argument_list|(
literal|"Unable to find corresponding type in HiveOperationType for "
operator|+
name|op
operator|+
literal|" : "
operator|+
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|"Check if HiveOperation, HiveOperationType have same number of instances"
argument_list|,
name|HiveOperation
operator|.
name|values
argument_list|()
operator|.
name|length
argument_list|,
name|HiveOperationType
operator|.
name|values
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

