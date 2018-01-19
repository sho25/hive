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
name|objectinspector
operator|.
name|primitive
package|;
end_package

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
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
name|io
operator|.
name|LongWritable
import|;
end_import

begin_comment
comment|/**  * A WritableConstantLongObjectInspector is a WritableLongObjectInspector  * that implements ConstantObjectInspector.  */
end_comment

begin_class
specifier|public
class|class
name|WritableConstantLongObjectInspector
extends|extends
name|WritableLongObjectInspector
implements|implements
name|ConstantObjectInspector
block|{
specifier|private
name|LongWritable
name|value
decl_stmt|;
specifier|protected
name|WritableConstantLongObjectInspector
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
name|WritableConstantLongObjectInspector
parameter_list|(
name|LongWritable
name|value
parameter_list|)
block|{
name|super
argument_list|()
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
name|LongWritable
name|getWritableConstantValue
parameter_list|()
block|{
return|return
name|value
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
name|BigDecimal
operator|.
name|valueOf
argument_list|(
name|value
operator|.
name|get
argument_list|()
argument_list|)
operator|.
name|precision
argument_list|()
return|;
block|}
block|}
end_class

end_unit

