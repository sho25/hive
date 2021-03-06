begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
package|;
end_package

begin_import
import|import
name|com
operator|.
name|esotericsoftware
operator|.
name|kryo
operator|.
name|Kryo
import|;
end_import

begin_import
import|import
name|com
operator|.
name|esotericsoftware
operator|.
name|kryo
operator|.
name|Serializer
import|;
end_import

begin_import
import|import
name|com
operator|.
name|esotericsoftware
operator|.
name|kryo
operator|.
name|io
operator|.
name|Input
import|;
end_import

begin_import
import|import
name|com
operator|.
name|esotericsoftware
operator|.
name|kryo
operator|.
name|io
operator|.
name|Output
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
name|HiveKey
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|serializer
operator|.
name|KryoRegistrator
import|;
end_import

begin_comment
comment|/**  * Kryo registrator for shuffle data, i.e. HiveKey and BytesWritable.  *  * Active use (e.g. reflection to get a class instance) of this class on hive side can cause  * problems because kryo is relocated in hive-exec.  */
end_comment

begin_class
specifier|public
class|class
name|HiveKryoRegistrator
implements|implements
name|KryoRegistrator
block|{
annotation|@
name|Override
specifier|public
name|void
name|registerClasses
parameter_list|(
name|Kryo
name|kryo
parameter_list|)
block|{
name|kryo
operator|.
name|register
argument_list|(
name|HiveKey
operator|.
name|class
argument_list|,
operator|new
name|HiveKeySerializer
argument_list|()
argument_list|)
expr_stmt|;
name|kryo
operator|.
name|register
argument_list|(
name|BytesWritable
operator|.
name|class
argument_list|,
operator|new
name|BytesWritableSerializer
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|HiveKeySerializer
extends|extends
name|Serializer
argument_list|<
name|HiveKey
argument_list|>
block|{
specifier|public
name|void
name|write
parameter_list|(
name|Kryo
name|kryo
parameter_list|,
name|Output
name|output
parameter_list|,
name|HiveKey
name|object
parameter_list|)
block|{
name|output
operator|.
name|writeVarInt
argument_list|(
name|object
operator|.
name|getLength
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|output
operator|.
name|write
argument_list|(
name|object
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|object
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeVarInt
argument_list|(
name|object
operator|.
name|hashCode
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveKey
name|read
parameter_list|(
name|Kryo
name|kryo
parameter_list|,
name|Input
name|input
parameter_list|,
name|Class
argument_list|<
name|HiveKey
argument_list|>
name|type
parameter_list|)
block|{
name|int
name|len
init|=
name|input
operator|.
name|readVarInt
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
name|input
operator|.
name|readBytes
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
return|return
operator|new
name|HiveKey
argument_list|(
name|bytes
argument_list|,
name|input
operator|.
name|readVarInt
argument_list|(
literal|false
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|static
class|class
name|BytesWritableSerializer
extends|extends
name|Serializer
argument_list|<
name|BytesWritable
argument_list|>
block|{
specifier|public
name|void
name|write
parameter_list|(
name|Kryo
name|kryo
parameter_list|,
name|Output
name|output
parameter_list|,
name|BytesWritable
name|object
parameter_list|)
block|{
name|output
operator|.
name|writeVarInt
argument_list|(
name|object
operator|.
name|getLength
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|output
operator|.
name|write
argument_list|(
name|object
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|object
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|BytesWritable
name|read
parameter_list|(
name|Kryo
name|kryo
parameter_list|,
name|Input
name|input
parameter_list|,
name|Class
argument_list|<
name|BytesWritable
argument_list|>
name|type
parameter_list|)
block|{
name|int
name|len
init|=
name|input
operator|.
name|readVarInt
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
name|input
operator|.
name|readBytes
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
return|return
operator|new
name|BytesWritable
argument_list|(
name|bytes
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

