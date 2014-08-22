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
name|HiveDecimal
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
name|HiveDecimalWritable
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
name|ConstantObjectInspector
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
name|DecimalTypeInfo
import|;
end_import

begin_comment
comment|/**  * A WritableConstantHiveDecimalObjectInspector is a WritableHiveDecimalObjectInspector  * that implements ConstantObjectInspector.  */
end_comment

begin_class
specifier|public
class|class
name|WritableConstantHiveDecimalObjectInspector
extends|extends
name|WritableHiveDecimalObjectInspector
implements|implements
name|ConstantObjectInspector
block|{
specifier|private
name|HiveDecimalWritable
name|value
decl_stmt|;
specifier|protected
name|WritableConstantHiveDecimalObjectInspector
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
name|WritableConstantHiveDecimalObjectInspector
parameter_list|(
name|DecimalTypeInfo
name|typeInfo
parameter_list|,
name|HiveDecimalWritable
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HiveDecimalWritable
name|getWritableConstantValue
parameter_list|()
block|{
comment|// We need to enforce precision/scale here.
comment|// A little inefficiency here as we need to create a HiveDecimal instance from the writable and
comment|// recreate a HiveDecimalWritable instance on the HiveDecimal instance. However, we don't know
comment|// the precision/scale of the original writable until we get a HiveDecimal instance from it.
name|DecimalTypeInfo
name|decTypeInfo
init|=
operator|(
name|DecimalTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|HiveDecimal
name|dec
init|=
name|value
operator|==
literal|null
condition|?
literal|null
else|:
name|value
operator|.
name|getHiveDecimal
argument_list|(
name|decTypeInfo
operator|.
name|precision
argument_list|()
argument_list|,
name|decTypeInfo
operator|.
name|scale
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|dec
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|HiveDecimalWritable
argument_list|(
name|dec
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|precision
parameter_list|()
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|precision
argument_list|()
return|;
block|}
return|return
name|value
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|precision
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|scale
parameter_list|()
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|scale
argument_list|()
return|;
block|}
return|return
name|value
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|scale
argument_list|()
return|;
block|}
block|}
end_class

end_unit

