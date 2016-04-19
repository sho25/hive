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
name|typeinfo
operator|.
name|BaseCharUtils
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

begin_class
specifier|public
class|class
name|JavaHiveVarcharObjectInspector
extends|extends
name|AbstractPrimitiveJavaObjectInspector
implements|implements
name|SettableHiveVarcharObjectInspector
block|{
comment|// no-arg ctor required for Kyro serialization
specifier|public
name|JavaHiveVarcharObjectInspector
parameter_list|()
block|{   }
specifier|public
name|JavaHiveVarcharObjectInspector
parameter_list|(
name|VarcharTypeInfo
name|typeInfo
parameter_list|)
block|{
name|super
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
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
name|value
decl_stmt|;
if|if
condition|(
name|o
operator|instanceof
name|String
condition|)
block|{
name|value
operator|=
operator|new
name|HiveVarchar
argument_list|(
operator|(
name|String
operator|)
name|o
argument_list|,
name|getMaxLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|value
operator|=
operator|(
name|HiveVarchar
operator|)
name|o
expr_stmt|;
block|}
if|if
condition|(
name|BaseCharUtils
operator|.
name|doesPrimitiveMatchTypeParams
argument_list|(
name|value
argument_list|,
operator|(
name|VarcharTypeInfo
operator|)
name|typeInfo
argument_list|)
condition|)
block|{
return|return
name|value
return|;
block|}
comment|// value needs to be converted to match the type params (length, etc).
return|return
name|getPrimitiveWithParams
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|HiveVarcharWritable
name|getPrimitiveWritableObject
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
name|var
decl_stmt|;
if|if
condition|(
name|o
operator|instanceof
name|String
condition|)
block|{
name|var
operator|=
operator|new
name|HiveVarchar
argument_list|(
operator|(
name|String
operator|)
name|o
argument_list|,
name|getMaxLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|var
operator|=
operator|(
name|HiveVarchar
operator|)
name|o
expr_stmt|;
block|}
return|return
name|getWritableWithParams
argument_list|(
name|var
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|set
parameter_list|(
name|Object
name|o
parameter_list|,
name|HiveVarchar
name|value
parameter_list|)
block|{
if|if
condition|(
name|BaseCharUtils
operator|.
name|doesPrimitiveMatchTypeParams
argument_list|(
name|value
argument_list|,
operator|(
name|VarcharTypeInfo
operator|)
name|typeInfo
argument_list|)
condition|)
block|{
return|return
name|value
return|;
block|}
else|else
block|{
comment|// Otherwise value may be too long, convert to appropriate value based on params
return|return
operator|new
name|HiveVarchar
argument_list|(
name|value
argument_list|,
name|getMaxLength
argument_list|()
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|set
parameter_list|(
name|Object
name|o
parameter_list|,
name|String
name|value
parameter_list|)
block|{
return|return
operator|new
name|HiveVarchar
argument_list|(
name|value
argument_list|,
name|getMaxLength
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|create
parameter_list|(
name|HiveVarchar
name|value
parameter_list|)
block|{
return|return
operator|new
name|HiveVarchar
argument_list|(
name|value
argument_list|,
name|getMaxLength
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|int
name|getMaxLength
parameter_list|()
block|{
name|VarcharTypeInfo
name|ti
init|=
operator|(
name|VarcharTypeInfo
operator|)
name|typeInfo
decl_stmt|;
return|return
name|ti
operator|.
name|getLength
argument_list|()
return|;
block|}
specifier|private
name|HiveVarchar
name|getPrimitiveWithParams
parameter_list|(
name|HiveVarchar
name|val
parameter_list|)
block|{
return|return
operator|new
name|HiveVarchar
argument_list|(
name|val
argument_list|,
name|getMaxLength
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|HiveVarcharWritable
name|getWritableWithParams
parameter_list|(
name|HiveVarchar
name|val
parameter_list|)
block|{
name|HiveVarcharWritable
name|newValue
init|=
operator|new
name|HiveVarcharWritable
argument_list|()
decl_stmt|;
name|newValue
operator|.
name|set
argument_list|(
name|val
argument_list|,
name|getMaxLength
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|newValue
return|;
block|}
block|}
end_class

end_unit

