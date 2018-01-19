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
name|columnar
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
name|AbstractSerDe
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
name|SerDeException
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
name|SerDeStats
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
name|ObjectInspector
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
name|Writable
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|ColumnarSerDeBase
extends|extends
name|AbstractSerDe
block|{
comment|// The object for storing row data
name|ColumnarStructBase
name|cachedLazyStruct
decl_stmt|;
comment|// We need some initial values in case user don't call initialize()
specifier|protected
name|ObjectInspector
name|cachedObjectInspector
decl_stmt|;
specifier|protected
name|long
name|serializedSize
decl_stmt|;
specifier|protected
name|SerDeStats
name|stats
decl_stmt|;
specifier|protected
name|boolean
name|lastOperationSerialize
decl_stmt|;
specifier|protected
name|boolean
name|lastOperationDeserialize
decl_stmt|;
name|BytesRefArrayWritable
name|serializeCache
init|=
operator|new
name|BytesRefArrayWritable
argument_list|()
decl_stmt|;
name|BytesRefWritable
name|field
index|[]
decl_stmt|;
name|ByteStream
operator|.
name|Output
name|serializeStream
init|=
operator|new
name|ByteStream
operator|.
name|Output
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|blob
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
operator|!
operator|(
name|blob
operator|instanceof
name|BytesRefArrayWritable
operator|)
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|getClass
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|": expects BytesRefArrayWritable!"
argument_list|)
throw|;
block|}
name|BytesRefArrayWritable
name|cols
init|=
operator|(
name|BytesRefArrayWritable
operator|)
name|blob
decl_stmt|;
name|cachedLazyStruct
operator|.
name|init
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|lastOperationSerialize
operator|=
literal|false
expr_stmt|;
name|lastOperationDeserialize
operator|=
literal|true
expr_stmt|;
return|return
name|cachedLazyStruct
return|;
block|}
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getSerDeStats
parameter_list|()
block|{
comment|// must be different
assert|assert
operator|(
name|lastOperationSerialize
operator|!=
name|lastOperationDeserialize
operator|)
assert|;
if|if
condition|(
name|lastOperationSerialize
condition|)
block|{
name|stats
operator|.
name|setRawDataSize
argument_list|(
name|serializedSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|stats
operator|.
name|setRawDataSize
argument_list|(
name|cachedLazyStruct
operator|.
name|getRawDataSerializedSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|stats
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|getSerializedClass
parameter_list|()
block|{
return|return
name|BytesRefArrayWritable
operator|.
name|class
return|;
block|}
specifier|protected
name|void
name|initialize
parameter_list|(
name|int
name|size
parameter_list|)
throws|throws
name|SerDeException
block|{
name|field
operator|=
operator|new
name|BytesRefWritable
index|[
name|size
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|field
index|[
name|i
index|]
operator|=
operator|new
name|BytesRefWritable
argument_list|()
expr_stmt|;
name|serializeCache
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|field
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|serializedSize
operator|=
literal|0
expr_stmt|;
name|stats
operator|=
operator|new
name|SerDeStats
argument_list|()
expr_stmt|;
name|lastOperationSerialize
operator|=
literal|false
expr_stmt|;
name|lastOperationDeserialize
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
throws|throws
name|SerDeException
block|{
return|return
name|cachedObjectInspector
return|;
block|}
block|}
end_class

end_unit

