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
name|udf
operator|.
name|generic
package|;
end_package

begin_comment
comment|/**  * Base class for binary operators, overrides getDisplayString()  *  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|GenericUDFBaseBinary
extends|extends
name|GenericUDF
block|{
specifier|protected
name|String
name|opName
init|=
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
specifier|protected
name|String
name|opDisplayName
decl_stmt|;
comment|// should be set by child class
annotation|@
name|Override
specifier|public
name|String
name|getDisplayString
parameter_list|(
name|String
index|[]
name|children
parameter_list|)
block|{
assert|assert
operator|(
name|children
operator|.
name|length
operator|==
literal|2
operator|)
assert|;
return|return
literal|"("
operator|+
name|children
index|[
literal|0
index|]
operator|+
literal|" "
operator|+
name|opDisplayName
operator|+
literal|" "
operator|+
name|children
index|[
literal|1
index|]
operator|+
literal|")"
return|;
block|}
block|}
end_class

end_unit

