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
name|hbase
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

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
name|io
operator|.
name|OutputStream
import|;
end_import

begin_class
specifier|public
class|class
name|DataOutputOutputStream
extends|extends
name|OutputStream
block|{
specifier|private
specifier|final
name|DataOutput
name|dataOutput
decl_stmt|;
specifier|public
name|DataOutputOutputStream
parameter_list|(
name|DataOutput
name|dataOutput
parameter_list|)
block|{
name|this
operator|.
name|dataOutput
operator|=
name|dataOutput
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|int
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|dataOutput
operator|.
name|write
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|OutputStream
name|from
parameter_list|(
name|DataOutput
name|dataOutput
parameter_list|)
block|{
if|if
condition|(
name|dataOutput
operator|instanceof
name|OutputStream
condition|)
block|{
return|return
operator|(
name|OutputStream
operator|)
name|dataOutput
return|;
block|}
return|return
operator|new
name|DataOutputOutputStream
argument_list|(
name|dataOutput
argument_list|)
return|;
block|}
block|}
end_class

end_unit

