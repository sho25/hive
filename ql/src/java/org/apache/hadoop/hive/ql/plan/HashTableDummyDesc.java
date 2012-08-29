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
name|plan
package|;
end_package

begin_comment
comment|/**  * HashTable Dummy Descriptor implementation.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"HashTable Dummy Operator"
argument_list|)
specifier|public
class|class
name|HashTableDummyDesc
extends|extends
name|AbstractOperatorDesc
block|{
specifier|private
name|TableDesc
name|tbl
decl_stmt|;
specifier|public
name|TableDesc
name|getTbl
parameter_list|()
block|{
return|return
name|tbl
return|;
block|}
specifier|public
name|void
name|setTbl
parameter_list|(
name|TableDesc
name|tbl
parameter_list|)
block|{
name|this
operator|.
name|tbl
operator|=
name|tbl
expr_stmt|;
block|}
block|}
end_class

end_unit

