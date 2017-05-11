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
name|common
operator|.
name|jsonexplain
package|;
end_package

begin_class
specifier|public
specifier|final
class|class
name|Connection
implements|implements
name|Comparable
argument_list|<
name|Connection
argument_list|>
block|{
specifier|public
specifier|final
name|String
name|type
decl_stmt|;
specifier|public
specifier|final
name|Vertex
name|from
decl_stmt|;
specifier|public
name|Connection
parameter_list|(
name|String
name|type
parameter_list|,
name|Vertex
name|from
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|from
operator|=
name|from
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Connection
name|o
parameter_list|)
block|{
return|return
name|from
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|from
argument_list|)
return|;
block|}
block|}
end_class

end_unit
