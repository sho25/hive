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
name|dynamic_type
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TApplicationException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
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
name|thrift
operator|.
name|server
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
name|thrift
operator|.
name|transport
operator|.
name|*
import|;
end_import

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
name|java
operator|.
name|io
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
name|serde2
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
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
name|thrift
operator|.
name|protocol
operator|.
name|TType
operator|.
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|DynamicSerDeTypedef
extends|extends
name|DynamicSerDeTypeBase
block|{
comment|// production is: typedef DefinitionType() this.name
specifier|private
specifier|final
specifier|static
name|int
name|FD_DEFINITION_TYPE
init|=
literal|0
decl_stmt|;
specifier|public
name|DynamicSerDeTypedef
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
name|DynamicSerDeTypedef
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
specifier|private
name|DynamicSerDeSimpleNode
name|getDefinitionType
parameter_list|()
block|{
return|return
operator|(
name|DynamicSerDeSimpleNode
operator|)
name|this
operator|.
name|jjtGetChild
argument_list|(
name|FD_DEFINITION_TYPE
argument_list|)
return|;
block|}
specifier|public
name|DynamicSerDeTypeBase
name|getMyType
parameter_list|()
block|{
name|DynamicSerDeSimpleNode
name|child
init|=
name|this
operator|.
name|getDefinitionType
argument_list|()
decl_stmt|;
name|DynamicSerDeTypeBase
name|ret
init|=
operator|(
name|DynamicSerDeTypeBase
operator|)
name|child
operator|.
name|jjtGetChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
return|return
name|ret
return|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|result
init|=
literal|"typedef "
operator|+
name|this
operator|.
name|name
operator|+
literal|"("
decl_stmt|;
name|result
operator|+=
name|this
operator|.
name|getDefinitionType
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
name|result
operator|+=
literal|")"
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
name|byte
name|getType
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"not implemented"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Object
name|reuse
parameter_list|,
name|TProtocol
name|iprot
parameter_list|)
throws|throws
name|SerDeException
throws|,
name|TException
throws|,
name|IllegalAccessException
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"not implemented"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serialize
parameter_list|(
name|Object
name|o
parameter_list|,
name|ObjectInspector
name|oi
parameter_list|,
name|TProtocol
name|oprot
parameter_list|)
throws|throws
name|TException
throws|,
name|SerDeException
throws|,
name|NoSuchFieldException
throws|,
name|IllegalAccessException
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"not implemented"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

