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
name|parse
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
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
name|parse
operator|.
name|OperatorInfo
import|;
end_import

begin_comment
comment|/**  * Implementation of the OperatorInfoList  *  **/
end_comment

begin_class
specifier|public
class|class
name|OperatorInfoList
extends|extends
name|Vector
argument_list|<
name|OperatorInfo
argument_list|>
block|{
comment|/**    *    */
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
name|Object
name|clone
parameter_list|()
block|{
name|OperatorInfoList
name|newL
init|=
operator|new
name|OperatorInfoList
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|newL
operator|.
name|add
argument_list|(
name|i
argument_list|,
operator|(
name|OperatorInfo
operator|)
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|newL
return|;
block|}
block|}
end_class

end_unit

