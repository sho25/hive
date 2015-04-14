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
name|typeinfo
operator|.
name|DecimalTypeInfo
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
name|HiveDecimalUtils
import|;
end_import

begin_class
specifier|public
class|class
name|WritableHiveDecimalObjectInspector
extends|extends
name|AbstractPrimitiveWritableObjectInspector
implements|implements
name|SettableHiveDecimalObjectInspector
block|{
specifier|public
name|WritableHiveDecimalObjectInspector
parameter_list|()
block|{   }
specifier|public
name|WritableHiveDecimalObjectInspector
parameter_list|(
name|DecimalTypeInfo
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
name|HiveDecimalWritable
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
return|return
name|enforcePrecisionScale
argument_list|(
operator|(
operator|(
name|HiveDecimalWritable
operator|)
name|o
operator|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|HiveDecimal
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
return|return
name|enforcePrecisionScale
argument_list|(
operator|(
operator|(
name|HiveDecimalWritable
operator|)
name|o
operator|)
operator|.
name|getHiveDecimal
argument_list|()
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
return|return
name|o
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|HiveDecimalWritable
argument_list|(
operator|(
name|HiveDecimalWritable
operator|)
name|o
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
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
name|HiveDecimalWritable
name|writable
init|=
operator|(
name|HiveDecimalWritable
operator|)
name|create
argument_list|(
name|bytes
argument_list|,
name|scale
argument_list|)
decl_stmt|;
if|if
condition|(
name|writable
operator|!=
literal|null
condition|)
block|{
operator|(
operator|(
name|HiveDecimalWritable
operator|)
name|o
operator|)
operator|.
name|set
argument_list|(
name|writable
argument_list|)
expr_stmt|;
return|return
name|o
return|;
block|}
else|else
block|{
return|return
literal|null
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
name|HiveDecimal
name|t
parameter_list|)
block|{
name|HiveDecimal
name|dec
init|=
name|enforcePrecisionScale
argument_list|(
name|t
argument_list|)
decl_stmt|;
if|if
condition|(
name|dec
operator|!=
literal|null
condition|)
block|{
operator|(
operator|(
name|HiveDecimalWritable
operator|)
name|o
operator|)
operator|.
name|set
argument_list|(
name|dec
argument_list|)
expr_stmt|;
return|return
name|o
return|;
block|}
else|else
block|{
return|return
literal|null
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
name|HiveDecimalWritable
name|t
parameter_list|)
block|{
name|HiveDecimalWritable
name|writable
init|=
name|enforcePrecisionScale
argument_list|(
name|t
argument_list|)
decl_stmt|;
if|if
condition|(
name|writable
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
operator|(
operator|(
name|HiveDecimalWritable
operator|)
name|o
operator|)
operator|.
name|set
argument_list|(
name|writable
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
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
return|return
operator|new
name|HiveDecimalWritable
argument_list|(
name|bytes
argument_list|,
name|scale
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|create
parameter_list|(
name|HiveDecimal
name|t
parameter_list|)
block|{
return|return
name|t
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|HiveDecimalWritable
argument_list|(
name|t
argument_list|)
return|;
block|}
specifier|private
name|HiveDecimal
name|enforcePrecisionScale
parameter_list|(
name|HiveDecimal
name|dec
parameter_list|)
block|{
return|return
name|HiveDecimalUtils
operator|.
name|enforcePrecisionScale
argument_list|(
name|dec
argument_list|,
operator|(
name|DecimalTypeInfo
operator|)
name|typeInfo
argument_list|)
return|;
block|}
specifier|private
name|HiveDecimalWritable
name|enforcePrecisionScale
parameter_list|(
name|HiveDecimalWritable
name|writable
parameter_list|)
block|{
return|return
name|HiveDecimalUtils
operator|.
name|enforcePrecisionScale
argument_list|(
name|writable
argument_list|,
operator|(
name|DecimalTypeInfo
operator|)
name|typeInfo
argument_list|)
return|;
block|}
block|}
end_class

end_unit

