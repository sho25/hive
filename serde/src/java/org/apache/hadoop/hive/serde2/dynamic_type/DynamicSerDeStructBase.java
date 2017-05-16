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
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|TStruct
import|;
end_import

begin_comment
comment|/**  * DynamicSerDeStructBase.  *  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|DynamicSerDeStructBase
extends|extends
name|DynamicSerDeTypeBase
implements|implements
name|Serializable
block|{
name|DynamicSerDeFieldList
name|fieldList
decl_stmt|;
specifier|public
name|DynamicSerDeStructBase
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
name|DynamicSerDeStructBase
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
specifier|abstract
name|DynamicSerDeFieldList
name|getFieldList
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|()
block|{
name|fieldList
operator|=
name|getFieldList
argument_list|()
expr_stmt|;
name|fieldList
operator|.
name|initialize
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isPrimitive
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
name|getRealType
parameter_list|()
block|{
return|return
name|List
operator|.
name|class
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
if|if
condition|(
name|thrift_mode
condition|)
block|{
name|iprot
operator|.
name|readStructBegin
argument_list|()
expr_stmt|;
block|}
name|Object
name|o
init|=
name|fieldList
operator|.
name|deserialize
argument_list|(
name|reuse
argument_list|,
name|iprot
argument_list|)
decl_stmt|;
if|if
condition|(
name|thrift_mode
condition|)
block|{
name|iprot
operator|.
name|readStructEnd
argument_list|()
expr_stmt|;
block|}
return|return
name|o
return|;
block|}
comment|/**    * serialize    *     * The way to serialize a Thrift "table" which in thrift land is really a    * function and thus this class's name.    *     * @param o    *          - this list should be in the order of the function's params for    *          now. If we wanted to remove this requirement, we'd need to make it    *          a List&lt;Pair&lt;String, Object&gt;&gt; with the String being the field name.    *     */
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
if|if
condition|(
name|thrift_mode
condition|)
block|{
name|oprot
operator|.
name|writeStructBegin
argument_list|(
operator|new
name|TStruct
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|fieldList
operator|.
name|serialize
argument_list|(
name|o
argument_list|,
name|oi
argument_list|,
name|oprot
argument_list|)
expr_stmt|;
if|if
condition|(
name|thrift_mode
condition|)
block|{
name|oprot
operator|.
name|writeStructEnd
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

