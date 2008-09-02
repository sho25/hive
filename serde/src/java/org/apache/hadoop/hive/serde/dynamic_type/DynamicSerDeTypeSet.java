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
name|TApplicationException
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
name|TBinaryProtocol
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
name|protocol
operator|.
name|TProtocol
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
name|server
operator|.
name|TServer
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
name|server
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
name|transport
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
name|transport
operator|.
name|TServerTransport
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
name|serde
operator|.
name|*
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
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TType
import|;
end_import

begin_class
specifier|public
class|class
name|DynamicSerDeTypeSet
extends|extends
name|DynamicSerDeTypeBase
block|{
comment|// production is: set<FieldType()>
specifier|static
specifier|final
specifier|private
name|int
name|FD_TYPE
init|=
literal|0
decl_stmt|;
specifier|public
name|DynamicSerDeTypeSet
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
name|DynamicSerDeTypeSet
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
comment|// returns Set<?>
specifier|public
name|Class
name|getRealType
parameter_list|()
block|{
try|try
block|{
name|Class
name|c
init|=
name|this
operator|.
name|getElementType
argument_list|()
operator|.
name|getRealType
argument_list|()
decl_stmt|;
name|Object
name|o
init|=
name|c
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|?
argument_list|>
name|l
init|=
name|Collections
operator|.
name|singleton
argument_list|(
name|o
argument_list|)
decl_stmt|;
return|return
name|l
operator|.
name|getClass
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|DynamicSerDeTypeBase
name|getElementType
parameter_list|()
block|{
return|return
call|(
name|DynamicSerDeTypeBase
call|)
argument_list|(
operator|(
name|DynamicSerDeFieldType
operator|)
name|this
operator|.
name|jjtGetChild
argument_list|(
name|FD_TYPE
argument_list|)
argument_list|)
operator|.
name|getMyType
argument_list|()
return|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"set<"
operator|+
name|this
operator|.
name|getElementType
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|">"
return|;
block|}
specifier|public
name|Set
argument_list|<
name|Object
argument_list|>
name|deserialize
parameter_list|(
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
name|TSet
name|theset
init|=
name|iprot
operator|.
name|readSetBegin
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Object
argument_list|>
name|result
init|=
operator|new
name|HashSet
argument_list|<
name|Object
argument_list|>
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
name|theset
operator|.
name|size
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|elem
init|=
name|this
operator|.
name|getElementType
argument_list|()
operator|.
name|deserialize
argument_list|(
name|iprot
argument_list|)
decl_stmt|;
name|result
operator|.
name|add
argument_list|(
name|elem
argument_list|)
expr_stmt|;
block|}
comment|// in theory, the below call isn't needed in non thrift_mode, but let's not get too crazy
name|iprot
operator|.
name|readSetEnd
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
name|void
name|serialize
parameter_list|(
name|Object
name|obj
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
name|Set
argument_list|<
name|Object
argument_list|>
name|set
init|=
operator|(
name|Set
argument_list|<
name|Object
argument_list|>
operator|)
name|obj
decl_stmt|;
name|DynamicSerDeTypeBase
name|mt
init|=
name|this
operator|.
name|getElementType
argument_list|()
decl_stmt|;
name|oprot
operator|.
name|writeSetBegin
argument_list|(
operator|new
name|TSet
argument_list|(
name|mt
operator|.
name|getType
argument_list|()
argument_list|,
name|set
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Object
name|o
range|:
name|set
control|)
block|{
name|mt
operator|.
name|serialize
argument_list|(
name|o
argument_list|,
name|oprot
argument_list|)
expr_stmt|;
block|}
comment|// in theory, the below call isn't needed in non thrift_mode, but let's not get too crazy
name|oprot
operator|.
name|writeSetEnd
argument_list|()
expr_stmt|;
block|}
specifier|public
name|byte
name|getType
parameter_list|()
block|{
return|return
name|TType
operator|.
name|SET
return|;
block|}
block|}
end_class

end_unit

