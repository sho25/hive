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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
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
name|common
operator|.
name|type
operator|.
name|HiveVarchar
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
name|io
operator|.
name|HiveVarcharWritable
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
name|lazy
operator|.
name|LazyHiveVarchar
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
name|HiveVarcharObjectInspector
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
name|typeinfo
operator|.
name|VarcharTypeInfo
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
name|typeinfo
operator|.
name|BaseCharUtils
import|;
end_import

begin_class
specifier|public
class|class
name|LazyHiveVarcharObjectInspector
extends|extends
name|AbstractPrimitiveLazyObjectInspector
argument_list|<
name|HiveVarcharWritable
argument_list|>
implements|implements
name|HiveVarcharObjectInspector
block|{
specifier|private
name|boolean
name|escaped
decl_stmt|;
specifier|private
name|byte
name|escapeChar
decl_stmt|;
comment|// no-arg ctor required for Kyro
specifier|public
name|LazyHiveVarcharObjectInspector
parameter_list|()
block|{   }
specifier|public
name|LazyHiveVarcharObjectInspector
parameter_list|(
name|VarcharTypeInfo
name|typeInfo
parameter_list|)
block|{
name|this
argument_list|(
name|typeInfo
argument_list|,
literal|false
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
name|LazyHiveVarcharObjectInspector
parameter_list|(
name|VarcharTypeInfo
name|typeInfo
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|)
block|{
name|super
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|escaped
operator|=
name|escaped
expr_stmt|;
name|this
operator|.
name|escapeChar
operator|=
name|escapeChar
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|copyObject
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|LazyHiveVarchar
name|ret
init|=
operator|new
name|LazyHiveVarchar
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setValue
argument_list|(
operator|(
name|LazyHiveVarchar
operator|)
name|o
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|HiveVarchar
name|getPrimitiveJavaObject
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|HiveVarchar
name|ret
init|=
operator|(
operator|(
name|LazyHiveVarchar
operator|)
name|o
operator|)
operator|.
name|getWritableObject
argument_list|()
operator|.
name|getHiveVarchar
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|BaseCharUtils
operator|.
name|doesPrimitiveMatchTypeParams
argument_list|(
name|ret
argument_list|,
operator|(
name|VarcharTypeInfo
operator|)
name|typeInfo
argument_list|)
condition|)
block|{
name|HiveVarchar
name|newValue
init|=
operator|new
name|HiveVarchar
argument_list|(
name|ret
argument_list|,
operator|(
operator|(
name|VarcharTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getLength
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|newValue
return|;
block|}
return|return
name|ret
return|;
block|}
specifier|public
name|boolean
name|isEscaped
parameter_list|()
block|{
return|return
name|escaped
return|;
block|}
specifier|public
name|byte
name|getEscapeChar
parameter_list|()
block|{
return|return
name|escapeChar
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getTypeName
argument_list|()
return|;
block|}
block|}
end_class

end_unit

