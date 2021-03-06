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
name|util
operator|.
name|Arrays
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
name|LazyUtils
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
name|BytesWritable
import|;
end_import

begin_class
specifier|public
class|class
name|JavaBinaryObjectInspector
extends|extends
name|AbstractPrimitiveJavaObjectInspector
implements|implements
name|SettableBinaryObjectInspector
block|{
name|JavaBinaryObjectInspector
parameter_list|()
block|{
name|super
argument_list|(
name|TypeInfoFactory
operator|.
name|binaryTypeInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|BytesWritable
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
name|BytesWritable
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|o
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getPrimitiveJavaObject
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
operator|(
name|byte
index|[]
operator|)
name|o
return|;
block|}
comment|/*    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|set
parameter_list|(
name|Object
name|o
parameter_list|,
name|byte
index|[]
name|bb
parameter_list|)
block|{
return|return
name|bb
operator|==
literal|null
condition|?
literal|null
else|:
name|Arrays
operator|.
name|copyOf
argument_list|(
name|bb
argument_list|,
name|bb
operator|.
name|length
argument_list|)
return|;
block|}
comment|/*    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|set
parameter_list|(
name|Object
name|o
parameter_list|,
name|BytesWritable
name|bw
parameter_list|)
block|{
return|return
name|bw
operator|==
literal|null
condition|?
literal|null
else|:
name|LazyUtils
operator|.
name|createByteArray
argument_list|(
name|bw
argument_list|)
return|;
block|}
comment|/*    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|create
parameter_list|(
name|byte
index|[]
name|bb
parameter_list|)
block|{
return|return
name|bb
operator|==
literal|null
condition|?
literal|null
else|:
name|Arrays
operator|.
name|copyOf
argument_list|(
name|bb
argument_list|,
name|bb
operator|.
name|length
argument_list|)
return|;
block|}
comment|/*    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|create
parameter_list|(
name|BytesWritable
name|bw
parameter_list|)
block|{
return|return
name|bw
operator|==
literal|null
condition|?
literal|null
else|:
name|LazyUtils
operator|.
name|createByteArray
argument_list|(
name|bw
argument_list|)
return|;
block|}
block|}
end_class

end_unit

