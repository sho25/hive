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
name|serde2
operator|.
name|lazy
operator|.
name|ByteArrayRef
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
name|LazyBinary
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
name|BinaryObjectInspector
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
name|PrimitiveObjectInspectorUtils
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
name|LazyBinaryObjectInspector
extends|extends
name|AbstractPrimitiveLazyObjectInspector
argument_list|<
name|BytesWritable
argument_list|>
implements|implements
name|BinaryObjectInspector
block|{
specifier|protected
name|LazyBinaryObjectInspector
parameter_list|()
block|{
name|super
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|binaryTypeEntry
argument_list|)
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
return|return
literal|null
operator|==
name|o
condition|?
literal|null
else|:
operator|new
name|LazyBinary
argument_list|(
operator|(
name|LazyBinary
operator|)
name|o
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteArrayRef
name|getPrimitiveJavaObject
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
literal|null
operator|==
name|o
condition|)
block|{
return|return
literal|null
return|;
block|}
name|ByteArrayRef
name|ba
init|=
operator|new
name|ByteArrayRef
argument_list|()
decl_stmt|;
name|ba
operator|.
name|setData
argument_list|(
operator|(
operator|(
name|LazyBinary
operator|)
name|o
operator|)
operator|.
name|getWritableObject
argument_list|()
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|ba
return|;
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
literal|null
operator|==
name|o
condition|?
literal|null
else|:
operator|(
operator|(
name|LazyBinary
operator|)
name|o
operator|)
operator|.
name|getWritableObject
argument_list|()
return|;
block|}
block|}
end_class

end_unit

