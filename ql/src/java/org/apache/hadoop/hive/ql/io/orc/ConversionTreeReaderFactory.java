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
name|ql
operator|.
name|io
operator|.
name|orc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * Factory for creating ORC tree readers. These tree readers can handle type promotions and type  * conversions.  */
end_comment

begin_class
specifier|public
class|class
name|ConversionTreeReaderFactory
extends|extends
name|TreeReaderFactory
block|{
comment|// TODO: This is currently only a place holder for type conversions.
specifier|public
specifier|static
name|TreeReader
name|createTreeReader
parameter_list|(
name|int
name|columnId
parameter_list|,
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|types
parameter_list|,
name|boolean
index|[]
name|included
parameter_list|,
name|boolean
name|skipCorrupt
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|TreeReaderFactory
operator|.
name|createTreeReader
argument_list|(
name|columnId
argument_list|,
name|types
argument_list|,
name|included
argument_list|,
name|skipCorrupt
argument_list|)
return|;
block|}
block|}
end_class

end_unit

