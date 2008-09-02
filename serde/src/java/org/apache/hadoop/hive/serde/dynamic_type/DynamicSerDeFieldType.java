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
name|serde
operator|.
name|dynamic_type
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
name|serde
operator|.
name|*
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
import|;
end_import

begin_comment
comment|// basically just a container for the real type so more like a proxy
end_comment

begin_class
specifier|public
class|class
name|DynamicSerDeFieldType
extends|extends
name|DynamicSerDeSimpleNode
block|{
comment|// production: this.name | BaseType() | MapType() | SetType() | ListType()
specifier|private
specifier|final
name|int
name|FD_FIELD_TYPE
init|=
literal|0
decl_stmt|;
specifier|public
name|DynamicSerDeFieldType
parameter_list|(
name|int
name|i
parameter_list|)
block|{
name|super
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
specifier|public
name|DynamicSerDeFieldType
parameter_list|(
name|thrift_grammar
name|p
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|super
argument_list|(
name|p
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|DynamicSerDeTypeBase
name|getMyType
parameter_list|()
block|{
comment|// bugbug, need to deal with a named type here - i.e., look it up and proxy to it
comment|// should raise an exception if this is a typedef since won't be any children
comment|// and thus we can quickly find this comment and limitation.
return|return
operator|(
name|DynamicSerDeTypeBase
operator|)
name|this
operator|.
name|jjtGetChild
argument_list|(
name|FD_FIELD_TYPE
argument_list|)
return|;
block|}
block|}
end_class

end_unit

