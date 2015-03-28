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
name|HiveIntervalYearMonth
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
name|HiveIntervalYearMonthWritable
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
name|TypeInfoFactory
import|;
end_import

begin_class
specifier|public
class|class
name|JavaHiveIntervalYearMonthObjectInspector
extends|extends
name|AbstractPrimitiveJavaObjectInspector
implements|implements
name|SettableHiveIntervalYearMonthObjectInspector
block|{
specifier|public
name|JavaHiveIntervalYearMonthObjectInspector
parameter_list|()
block|{
name|super
argument_list|(
name|TypeInfoFactory
operator|.
name|intervalYearMonthTypeInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HiveIntervalYearMonth
name|getPrimitiveJavaObject
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
operator|(
name|HiveIntervalYearMonth
operator|)
name|o
return|;
block|}
annotation|@
name|Override
specifier|public
name|HiveIntervalYearMonthWritable
name|getPrimitiveWritableObject
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
name|HiveIntervalYearMonthWritable
argument_list|(
operator|(
name|HiveIntervalYearMonth
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
name|HiveIntervalYearMonth
name|i
parameter_list|)
block|{
return|return
name|i
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|HiveIntervalYearMonth
argument_list|(
name|i
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
name|HiveIntervalYearMonthWritable
name|i
parameter_list|)
block|{
return|return
name|i
operator|==
literal|null
condition|?
literal|null
else|:
name|i
operator|.
name|getHiveIntervalYearMonth
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|create
parameter_list|(
name|HiveIntervalYearMonth
name|i
parameter_list|)
block|{
return|return
name|i
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|HiveIntervalYearMonth
argument_list|(
name|i
argument_list|)
return|;
block|}
block|}
end_class

end_unit

