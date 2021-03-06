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
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|EOFException
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
name|InputStream
import|;
end_import

begin_class
specifier|public
class|class
name|DataInputInputStream
extends|extends
name|InputStream
block|{
specifier|private
specifier|final
name|DataInput
name|dataInput
decl_stmt|;
specifier|public
name|DataInputInputStream
parameter_list|(
name|DataInput
name|dataInput
parameter_list|)
block|{
name|this
operator|.
name|dataInput
operator|=
name|dataInput
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|dataInput
operator|.
name|readUnsignedByte
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|EOFException
name|e
parameter_list|)
block|{
comment|// contract on EOF differs between DataInput and InputStream
return|return
operator|-
literal|1
return|;
block|}
block|}
specifier|public
specifier|static
name|InputStream
name|from
parameter_list|(
name|DataInput
name|dataInput
parameter_list|)
block|{
if|if
condition|(
name|dataInput
operator|instanceof
name|InputStream
condition|)
block|{
return|return
operator|(
name|InputStream
operator|)
name|dataInput
return|;
block|}
return|return
operator|new
name|DataInputInputStream
argument_list|(
name|dataInput
argument_list|)
return|;
block|}
block|}
end_class

end_unit

