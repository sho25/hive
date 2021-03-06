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
name|serde2
operator|.
name|SerDeException
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
name|primitive
operator|.
name|ShortObjectInspector
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
name|protocol
operator|.
name|TProtocol
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
import|;
end_import

begin_comment
comment|/**  * DynamicSerDeTypei16.  *  */
end_comment

begin_class
specifier|public
class|class
name|DynamicSerDeTypei16
extends|extends
name|DynamicSerDeTypeBase
block|{
annotation|@
name|Override
specifier|public
name|Class
name|getRealType
parameter_list|()
block|{
return|return
name|Integer
operator|.
name|valueOf
argument_list|(
literal|2
argument_list|)
operator|.
name|getClass
argument_list|()
return|;
block|}
comment|// production is: i16
specifier|public
name|DynamicSerDeTypei16
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
name|DynamicSerDeTypei16
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
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"i16"
return|;
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
name|int
name|val
init|=
name|iprot
operator|.
name|readI16
argument_list|()
decl_stmt|;
if|if
condition|(
name|val
operator|==
literal|0
operator|&&
name|iprot
operator|instanceof
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
name|thrift
operator|.
name|WriteNullsProtocol
operator|&&
operator|(
operator|(
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
name|thrift
operator|.
name|WriteNullsProtocol
operator|)
name|iprot
operator|)
operator|.
name|lastPrimitiveWasNull
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|Integer
operator|.
name|valueOf
argument_list|(
name|val
argument_list|)
return|;
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
name|ShortObjectInspector
name|poi
init|=
operator|(
name|ShortObjectInspector
operator|)
name|oi
decl_stmt|;
name|oprot
operator|.
name|writeI16
argument_list|(
name|poi
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|getType
parameter_list|()
block|{
return|return
name|TType
operator|.
name|I16
return|;
block|}
block|}
end_class

end_unit

