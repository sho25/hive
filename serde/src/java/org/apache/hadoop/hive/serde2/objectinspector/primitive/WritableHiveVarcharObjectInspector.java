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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|WritableHiveVarcharObjectInspector
extends|extends
name|AbstractPrimitiveWritableObjectInspector
implements|implements
name|SettableHiveVarcharObjectInspector
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|WritableHiveVarcharObjectInspector
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// no-arg ctor required for Kyro serialization
specifier|public
name|WritableHiveVarcharObjectInspector
parameter_list|()
block|{   }
specifier|public
name|WritableHiveVarcharObjectInspector
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
comment|// check input object's length, if it doesn't match
comment|// then output a new primitive with the correct params.
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
name|HiveVarcharWritable
name|writable
init|=
operator|(
operator|(
name|HiveVarcharWritable
operator|)
name|o
operator|)
decl_stmt|;
if|if
condition|(
name|doesWritableMatchTypeParams
argument_list|(
name|writable
argument_list|)
condition|)
block|{
return|return
name|writable
operator|.
name|getHiveVarchar
argument_list|()
return|;
block|}
return|return
name|getPrimitiveWithParams
argument_list|(
name|writable
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
comment|// check input object's length, if it doesn't match
comment|// then output new writable with correct params.
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
name|HiveVarcharWritable
name|writable
init|=
operator|(
operator|(
name|HiveVarcharWritable
operator|)
name|o
operator|)
decl_stmt|;
if|if
condition|(
name|doesWritableMatchTypeParams
argument_list|(
operator|(
name|HiveVarcharWritable
operator|)
name|o
argument_list|)
condition|)
block|{
return|return
name|writable
return|;
block|}
return|return
name|getWritableWithParams
argument_list|(
name|writable
argument_list|)
return|;
block|}
specifier|private
name|HiveVarchar
name|getPrimitiveWithParams
parameter_list|(
name|HiveVarcharWritable
name|val
parameter_list|)
block|{
name|HiveVarchar
name|hv
init|=
operator|new
name|HiveVarchar
argument_list|()
decl_stmt|;
name|hv
operator|.
name|setValue
argument_list|(
name|val
operator|.
name|getHiveVarchar
argument_list|()
argument_list|,
name|getMaxLength
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|hv
return|;
block|}
specifier|private
name|HiveVarcharWritable
name|getWritableWithParams
parameter_list|(
name|HiveVarcharWritable
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
specifier|private
name|boolean
name|doesWritableMatchTypeParams
parameter_list|(
name|HiveVarcharWritable
name|writable
parameter_list|)
block|{
return|return
name|BaseCharUtils
operator|.
name|doesWritableMatchTypeParams
argument_list|(
name|writable
argument_list|,
operator|(
name|VarcharTypeInfo
operator|)
name|typeInfo
argument_list|)
return|;
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
name|HiveVarcharWritable
name|writable
init|=
operator|(
name|HiveVarcharWritable
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|doesWritableMatchTypeParams
argument_list|(
operator|(
name|HiveVarcharWritable
operator|)
name|o
argument_list|)
condition|)
block|{
return|return
operator|new
name|HiveVarcharWritable
argument_list|(
name|writable
argument_list|)
return|;
block|}
return|return
name|getWritableWithParams
argument_list|(
name|writable
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
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|HiveVarcharWritable
name|writable
init|=
operator|(
name|HiveVarcharWritable
operator|)
name|o
decl_stmt|;
name|writable
operator|.
name|set
argument_list|(
name|value
argument_list|,
name|getMaxLength
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|o
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
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|HiveVarcharWritable
name|writable
init|=
operator|(
name|HiveVarcharWritable
operator|)
name|o
decl_stmt|;
name|writable
operator|.
name|set
argument_list|(
name|value
argument_list|,
name|getMaxLength
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|o
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
name|HiveVarcharWritable
name|ret
decl_stmt|;
name|ret
operator|=
operator|new
name|HiveVarcharWritable
argument_list|()
expr_stmt|;
name|ret
operator|.
name|set
argument_list|(
name|value
argument_list|,
name|getMaxLength
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
specifier|public
name|int
name|getMaxLength
parameter_list|()
block|{
return|return
operator|(
operator|(
name|VarcharTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getLength
argument_list|()
return|;
block|}
block|}
end_class

end_unit

