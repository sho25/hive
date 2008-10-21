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
name|PrimitiveObjectInspector
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
name|DynamicSerDeTypeByte
extends|extends
name|DynamicSerDeTypeBase
block|{
comment|// production is: byte
specifier|public
name|DynamicSerDeTypeByte
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
name|DynamicSerDeTypeByte
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
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"byte"
return|;
block|}
specifier|public
name|Byte
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
return|return
name|Byte
operator|.
name|valueOf
argument_list|(
name|iprot
operator|.
name|readByte
argument_list|()
argument_list|)
return|;
block|}
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
return|return
name|Byte
operator|.
name|valueOf
argument_list|(
name|iprot
operator|.
name|readByte
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|void
name|serialize
parameter_list|(
name|Object
name|s
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
comment|// bugbug need to use object of byte type!!!
name|oprot
operator|.
name|writeByte
argument_list|(
operator|(
name|Byte
operator|)
name|s
argument_list|)
expr_stmt|;
block|}
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
assert|assert
operator|(
name|oi
operator|.
name|getCategory
argument_list|()
operator|==
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
operator|)
assert|;
assert|assert
operator|(
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|oi
operator|)
operator|.
name|getPrimitiveClass
argument_list|()
operator|.
name|equals
argument_list|(
name|Byte
operator|.
name|class
argument_list|)
operator|)
assert|;
name|oprot
operator|.
name|writeByte
argument_list|(
operator|(
name|Byte
operator|)
name|o
argument_list|)
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
name|BYTE
return|;
block|}
block|}
end_class

end_unit

