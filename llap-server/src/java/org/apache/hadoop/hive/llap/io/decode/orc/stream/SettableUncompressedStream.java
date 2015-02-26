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
name|llap
operator|.
name|io
operator|.
name|decode
operator|.
name|orc
operator|.
name|stream
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|common
operator|.
name|DiskRange
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
name|ql
operator|.
name|io
operator|.
name|orc
operator|.
name|InStream
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
specifier|public
class|class
name|SettableUncompressedStream
extends|extends
name|InStream
operator|.
name|UncompressedStream
block|{
specifier|public
name|SettableUncompressedStream
parameter_list|(
name|String
name|fileName
parameter_list|,
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|DiskRange
argument_list|>
name|input
parameter_list|,
name|long
name|length
parameter_list|)
block|{
name|super
argument_list|(
name|fileName
argument_list|,
name|name
argument_list|,
name|input
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setBuffers
parameter_list|(
name|List
argument_list|<
name|DiskRange
argument_list|>
name|input
parameter_list|,
name|long
name|length
parameter_list|)
block|{
name|this
operator|.
name|bytes
operator|=
name|input
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
block|}
block|}
end_class

end_unit

