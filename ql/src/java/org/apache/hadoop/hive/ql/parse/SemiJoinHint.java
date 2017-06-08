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

begin_class
specifier|public
class|class
name|SemiJoinHint
block|{
specifier|private
name|String
name|colName
decl_stmt|;
specifier|private
name|String
name|target
decl_stmt|;
specifier|private
name|Integer
name|numEntries
decl_stmt|;
specifier|public
name|SemiJoinHint
parameter_list|(
name|String
name|colName
parameter_list|,
name|String
name|target
parameter_list|,
name|Integer
name|numEntries
parameter_list|)
block|{
name|this
operator|.
name|colName
operator|=
name|colName
expr_stmt|;
name|this
operator|.
name|target
operator|=
name|target
expr_stmt|;
name|this
operator|.
name|numEntries
operator|=
name|numEntries
expr_stmt|;
block|}
specifier|public
name|String
name|getColName
parameter_list|()
block|{
return|return
name|colName
return|;
block|}
specifier|public
name|String
name|getTarget
parameter_list|()
block|{
return|return
name|target
return|;
block|}
specifier|public
name|Integer
name|getNumEntries
parameter_list|()
block|{
return|return
name|numEntries
operator|!=
literal|null
condition|?
name|numEntries
else|:
operator|-
literal|1
return|;
block|}
block|}
end_class

end_unit

