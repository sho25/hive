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
name|serde
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Extensions to bytearrayinput/output streams  *  */
end_comment

begin_class
specifier|public
class|class
name|ByteStream
block|{
specifier|public
specifier|static
class|class
name|Input
extends|extends
name|ByteArrayInputStream
block|{
specifier|public
name|byte
index|[]
name|getData
parameter_list|()
block|{
return|return
name|buf
return|;
block|}
specifier|public
name|int
name|getCount
parameter_list|()
block|{
return|return
name|count
return|;
block|}
specifier|public
name|void
name|reset
parameter_list|(
name|byte
index|[]
name|argBuf
parameter_list|,
name|int
name|argCount
parameter_list|)
block|{
name|buf
operator|=
name|argBuf
expr_stmt|;
name|mark
operator|=
name|pos
operator|=
literal|0
expr_stmt|;
name|count
operator|=
name|argCount
expr_stmt|;
block|}
specifier|public
name|Input
parameter_list|()
block|{
name|super
argument_list|(
operator|new
name|byte
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Input
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|)
block|{
name|super
argument_list|(
name|buf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Input
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|super
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|Output
extends|extends
name|ByteArrayOutputStream
block|{
specifier|public
name|byte
index|[]
name|getData
parameter_list|()
block|{
return|return
name|buf
return|;
block|}
specifier|public
name|int
name|getCount
parameter_list|()
block|{
return|return
name|count
return|;
block|}
specifier|public
name|Output
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Output
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|super
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

