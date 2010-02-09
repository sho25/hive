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
name|serde2
operator|.
name|binarysortable
package|;
end_package

begin_comment
comment|/**  * MyTestInnerStruct.  *  */
end_comment

begin_class
specifier|public
class|class
name|MyTestInnerStruct
block|{
name|Integer
name|int1
decl_stmt|;
name|Integer
name|int2
decl_stmt|;
specifier|public
name|MyTestInnerStruct
parameter_list|()
block|{   }
specifier|public
name|MyTestInnerStruct
parameter_list|(
name|Integer
name|int1
parameter_list|,
name|Integer
name|int2
parameter_list|)
block|{
name|this
operator|.
name|int1
operator|=
name|int1
expr_stmt|;
name|this
operator|.
name|int2
operator|=
name|int2
expr_stmt|;
block|}
block|}
end_class

end_unit

