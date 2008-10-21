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
name|protocol
operator|.
name|TType
import|;
end_import

begin_class
specifier|public
class|class
name|DynamicSerDeStruct
extends|extends
name|DynamicSerDeStructBase
block|{
comment|// production is: struct this.name { FieldList() }
specifier|final
specifier|private
specifier|static
name|int
name|FD_FIELD_LIST
init|=
literal|0
decl_stmt|;
specifier|public
name|DynamicSerDeStruct
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
name|DynamicSerDeStruct
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
name|String
name|result
init|=
literal|"struct "
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
name|getFieldList
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
name|DynamicSerDeFieldList
name|getFieldList
parameter_list|()
block|{
return|return
operator|(
name|DynamicSerDeFieldList
operator|)
name|this
operator|.
name|jjtGetChild
argument_list|(
name|FD_FIELD_LIST
argument_list|)
return|;
block|}
specifier|public
name|byte
name|getType
parameter_list|()
block|{
return|return
name|TType
operator|.
name|STRUCT
return|;
block|}
block|}
end_class

end_unit

