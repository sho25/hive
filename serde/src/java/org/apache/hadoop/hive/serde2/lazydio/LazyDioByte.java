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
name|lazydio
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
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
name|ByteStream
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
name|LazyByte
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
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyByteObjectInspector
import|;
end_import

begin_comment
comment|/**  * LazyByteBinary for storing a byte value as a ByteWritable. This class complements class  * LazyByte. It's primary difference is the {@link #init(ByteArrayRef, int, int)} method, which  * reads the raw byte value stored.  */
end_comment

begin_class
specifier|public
class|class
name|LazyDioByte
extends|extends
name|LazyByte
block|{
specifier|private
name|ByteStream
operator|.
name|Input
name|in
decl_stmt|;
specifier|private
name|DataInputStream
name|din
decl_stmt|;
specifier|public
name|LazyDioByte
parameter_list|(
name|LazyByteObjectInspector
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
block|}
specifier|public
name|LazyDioByte
parameter_list|(
name|LazyDioByte
name|copy
parameter_list|)
block|{
name|super
argument_list|(
name|copy
argument_list|)
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
name|byte
name|value
init|=
literal|0
decl_stmt|;
try|try
block|{
name|in
operator|=
operator|new
name|ByteStream
operator|.
name|Input
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
expr_stmt|;
name|din
operator|=
operator|new
name|DataInputStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|value
operator|=
name|din
operator|.
name|readByte
argument_list|()
expr_stmt|;
name|data
operator|.
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|isNull
operator|=
literal|false
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|isNull
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
try|try
block|{
name|din
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// swallow exception
block|}
try|try
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// swallow exception
block|}
block|}
block|}
block|}
end_class

end_unit

