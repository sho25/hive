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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CharacterCodingException
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
name|FloatWritable
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
name|Text
import|;
end_import

begin_comment
comment|/**  * LazyObject for storing a value of Double.  *   */
end_comment

begin_class
specifier|public
class|class
name|LazyFloat
extends|extends
name|LazyPrimitive
argument_list|<
name|FloatWritable
argument_list|>
block|{
specifier|public
name|LazyFloat
parameter_list|()
block|{
name|data
operator|=
operator|new
name|FloatWritable
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|ByteArrayRef
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
try|try
block|{
name|data
operator|.
name|set
argument_list|(
name|Float
operator|.
name|parseFloat
argument_list|(
name|Text
operator|.
name|decode
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|isNull
operator|=
literal|false
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
name|isNull
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CharacterCodingException
name|e
parameter_list|)
block|{
name|isNull
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

