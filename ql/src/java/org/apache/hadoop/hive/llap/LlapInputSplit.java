begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Schema
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
name|mapred
operator|.
name|InputSplitWithLocationInfo
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
name|mapred
operator|.
name|SplitLocationInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TDeserializer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TSerializer
import|;
end_import

begin_class
specifier|public
class|class
name|LlapInputSplit
implements|implements
name|InputSplitWithLocationInfo
block|{
name|int
name|splitNum
decl_stmt|;
name|byte
index|[]
name|planBytes
decl_stmt|;
name|byte
index|[]
name|fragmentBytes
decl_stmt|;
name|SplitLocationInfo
index|[]
name|locations
decl_stmt|;
name|Schema
name|schema
decl_stmt|;
specifier|public
name|LlapInputSplit
parameter_list|()
block|{   }
specifier|public
name|LlapInputSplit
parameter_list|(
name|int
name|splitNum
parameter_list|,
name|byte
index|[]
name|planBytes
parameter_list|,
name|byte
index|[]
name|fragmentBytes
parameter_list|,
name|SplitLocationInfo
index|[]
name|locations
parameter_list|,
name|Schema
name|schema
parameter_list|)
block|{
name|this
operator|.
name|planBytes
operator|=
name|planBytes
expr_stmt|;
name|this
operator|.
name|fragmentBytes
operator|=
name|fragmentBytes
expr_stmt|;
name|this
operator|.
name|locations
operator|=
name|locations
expr_stmt|;
name|this
operator|.
name|schema
operator|=
name|schema
expr_stmt|;
name|this
operator|.
name|splitNum
operator|=
name|splitNum
expr_stmt|;
block|}
specifier|public
name|Schema
name|getSchema
parameter_list|()
block|{
return|return
name|schema
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLength
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getLocations
parameter_list|()
throws|throws
name|IOException
block|{
name|String
index|[]
name|locs
init|=
operator|new
name|String
index|[
name|locations
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|locations
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|locs
index|[
name|i
index|]
operator|=
name|locations
index|[
name|i
index|]
operator|.
name|getLocation
argument_list|()
expr_stmt|;
block|}
return|return
name|locs
return|;
block|}
specifier|public
name|int
name|getSplitNum
parameter_list|()
block|{
return|return
name|splitNum
return|;
block|}
specifier|public
name|byte
index|[]
name|getPlanBytes
parameter_list|()
block|{
return|return
name|planBytes
return|;
block|}
specifier|public
name|byte
index|[]
name|getFragmentBytes
parameter_list|()
block|{
return|return
name|fragmentBytes
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|splitNum
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|planBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|planBytes
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|fragmentBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|fragmentBytes
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|locations
operator|.
name|length
argument_list|)
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
name|locations
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|locations
index|[
name|i
index|]
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|binarySchema
decl_stmt|;
try|try
block|{
name|TSerializer
name|serializer
init|=
operator|new
name|TSerializer
argument_list|()
decl_stmt|;
name|byte
index|[]
name|serialzied
init|=
name|serializer
operator|.
name|serialize
argument_list|(
name|schema
argument_list|)
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|serialzied
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|serialzied
argument_list|)
expr_stmt|;
comment|//      AutoExpandingBufferWriteTransport transport = new AutoExpandingBufferWriteTransport(1024, 2d);
comment|//      TProtocol protocol = new TBinaryProtocol(transport);
comment|//      schema.write(protocol);
comment|//      binarySchema = transport.getBuf().array();
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|splitNum
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|int
name|length
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|planBytes
operator|=
operator|new
name|byte
index|[
name|length
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|planBytes
argument_list|)
expr_stmt|;
name|length
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|fragmentBytes
operator|=
operator|new
name|byte
index|[
name|length
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|fragmentBytes
argument_list|)
expr_stmt|;
name|length
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|locations
operator|=
operator|new
name|SplitLocationInfo
index|[
name|length
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
name|length
condition|;
operator|++
name|i
control|)
block|{
name|locations
index|[
name|i
index|]
operator|=
operator|new
name|SplitLocationInfo
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|length
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
try|try
block|{
name|byte
index|[]
name|schemaBytes
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|schemaBytes
argument_list|)
expr_stmt|;
name|TDeserializer
name|tDeserializer
init|=
operator|new
name|TDeserializer
argument_list|()
decl_stmt|;
name|schema
operator|=
operator|new
name|Schema
argument_list|()
expr_stmt|;
name|tDeserializer
operator|.
name|deserialize
argument_list|(
name|schema
argument_list|,
name|schemaBytes
argument_list|)
expr_stmt|;
comment|//      AutoExpandingBufferReadTransport transport = new AutoExpandingBufferReadTransport(length, 2d);
comment|//      AutoExpandingBuffer buf = transport.getBuf();
comment|//      in.readFully(buf.array(), 0, length);
comment|//
comment|//      TProtocol protocol = new TBinaryProtocol(transport);
comment|//      schema = new Schema();
comment|//      schema.read(protocol);
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|SplitLocationInfo
index|[]
name|getLocationInfo
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|locations
return|;
block|}
block|}
end_class

end_unit

