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
name|serde2
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_comment
comment|/**  * ColumnSet.  *  */
end_comment

begin_class
specifier|public
class|class
name|ColumnSet
block|{
specifier|public
name|ArrayList
argument_list|<
name|String
argument_list|>
name|col
decl_stmt|;
specifier|public
name|ColumnSet
parameter_list|()
block|{   }
specifier|public
name|ColumnSet
parameter_list|(
name|ArrayList
argument_list|<
name|String
argument_list|>
name|col
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|col
operator|=
name|col
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|col
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

