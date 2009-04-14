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

begin_comment
comment|/**  * A WritableVoidObjectInspector inspects a NullWritable Object.  */
end_comment

begin_class
specifier|public
class|class
name|WritableVoidObjectInspector
extends|extends
name|AbstractPrimitiveWritableObjectInspector
implements|implements
name|VoidObjectInspector
block|{
name|WritableVoidObjectInspector
parameter_list|()
block|{
name|super
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|voidTypeEntry
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
name|o
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getPrimitiveJavaObject
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Internal error: cannot create Void object."
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

