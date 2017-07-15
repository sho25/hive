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
name|io
operator|.
name|DataInputBuffer
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
name|DataOutputBuffer
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|security
operator|.
name|token
operator|.
name|Token
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|ApplicationId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|common
operator|.
name|security
operator|.
name|JobTokenIdentifier
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|common
operator|.
name|security
operator|.
name|JobTokenSecretManager
import|;
end_import

begin_class
specifier|public
class|class
name|SubmitWorkInfo
implements|implements
name|Writable
block|{
specifier|private
name|ApplicationId
name|fakeAppId
decl_stmt|;
specifier|private
name|long
name|creationTime
decl_stmt|;
specifier|private
name|byte
index|[]
name|vertexSpec
decl_stmt|,
name|vertexSpecSignature
decl_stmt|;
comment|// This is used to communicate over the LlapUmbilicalProtocol. Not related to tokens used to
comment|// talk to LLAP daemons itself via the securit work.
specifier|private
name|Token
argument_list|<
name|JobTokenIdentifier
argument_list|>
name|token
decl_stmt|;
specifier|private
name|int
name|vertexParallelism
decl_stmt|;
specifier|public
name|SubmitWorkInfo
parameter_list|(
name|ApplicationId
name|fakeAppId
parameter_list|,
name|long
name|creationTime
parameter_list|,
name|int
name|vertexParallelism
parameter_list|,
name|byte
index|[]
name|vertexSpec
parameter_list|,
name|byte
index|[]
name|vertexSpecSignature
parameter_list|,
name|Token
argument_list|<
name|JobTokenIdentifier
argument_list|>
name|token
parameter_list|)
block|{
name|this
operator|.
name|fakeAppId
operator|=
name|fakeAppId
expr_stmt|;
name|this
operator|.
name|token
operator|=
name|token
expr_stmt|;
name|this
operator|.
name|creationTime
operator|=
name|creationTime
expr_stmt|;
name|this
operator|.
name|vertexSpec
operator|=
name|vertexSpec
expr_stmt|;
name|this
operator|.
name|vertexSpecSignature
operator|=
name|vertexSpecSignature
expr_stmt|;
name|this
operator|.
name|vertexParallelism
operator|=
name|vertexParallelism
expr_stmt|;
block|}
comment|// Empty constructor for writable etc.
specifier|public
name|SubmitWorkInfo
parameter_list|()
block|{   }
specifier|public
name|ApplicationId
name|getFakeAppId
parameter_list|()
block|{
return|return
name|fakeAppId
return|;
block|}
specifier|public
name|String
name|getTokenIdentifier
parameter_list|()
block|{
return|return
name|fakeAppId
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|Token
argument_list|<
name|JobTokenIdentifier
argument_list|>
name|getToken
parameter_list|()
block|{
return|return
name|token
return|;
block|}
specifier|public
name|long
name|getCreationTime
parameter_list|()
block|{
return|return
name|creationTime
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
name|writeLong
argument_list|(
name|fakeAppId
operator|.
name|getClusterTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|fakeAppId
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|token
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|creationTime
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|vertexParallelism
argument_list|)
expr_stmt|;
if|if
condition|(
name|vertexSpec
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|vertexSpec
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|vertexSpec
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|vertexSpecSignature
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|vertexSpecSignature
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|vertexSpecSignature
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
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
name|long
name|appIdTs
init|=
name|in
operator|.
name|readLong
argument_list|()
decl_stmt|;
name|int
name|appIdId
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|fakeAppId
operator|=
name|ApplicationId
operator|.
name|newInstance
argument_list|(
name|appIdTs
argument_list|,
name|appIdId
argument_list|)
expr_stmt|;
name|token
operator|=
operator|new
name|Token
argument_list|<>
argument_list|()
expr_stmt|;
name|token
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|creationTime
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|vertexParallelism
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|int
name|vertexSpecBytes
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|vertexSpecBytes
operator|>
literal|0
condition|)
block|{
name|vertexSpec
operator|=
operator|new
name|byte
index|[
name|vertexSpecBytes
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|vertexSpec
argument_list|)
expr_stmt|;
block|}
name|int
name|vertexSpecSignBytes
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|vertexSpecSignBytes
operator|>
literal|0
condition|)
block|{
name|vertexSpecSignature
operator|=
operator|new
name|byte
index|[
name|vertexSpecSignBytes
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|vertexSpecSignature
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|byte
index|[]
name|toBytes
parameter_list|(
name|SubmitWorkInfo
name|submitWorkInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|DataOutputBuffer
name|dob
init|=
operator|new
name|DataOutputBuffer
argument_list|()
decl_stmt|;
name|submitWorkInfo
operator|.
name|write
argument_list|(
name|dob
argument_list|)
expr_stmt|;
return|return
name|dob
operator|.
name|getData
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|SubmitWorkInfo
name|fromBytes
parameter_list|(
name|byte
index|[]
name|submitWorkInfoBytes
parameter_list|)
throws|throws
name|IOException
block|{
name|DataInputBuffer
name|dib
init|=
operator|new
name|DataInputBuffer
argument_list|()
decl_stmt|;
name|dib
operator|.
name|reset
argument_list|(
name|submitWorkInfoBytes
argument_list|,
literal|0
argument_list|,
name|submitWorkInfoBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|SubmitWorkInfo
name|submitWorkInfo
init|=
operator|new
name|SubmitWorkInfo
argument_list|()
decl_stmt|;
name|submitWorkInfo
operator|.
name|readFields
argument_list|(
name|dib
argument_list|)
expr_stmt|;
return|return
name|submitWorkInfo
return|;
block|}
specifier|public
name|byte
index|[]
name|getVertexBinary
parameter_list|()
block|{
return|return
name|vertexSpec
return|;
block|}
specifier|public
name|byte
index|[]
name|getVertexSignature
parameter_list|()
block|{
return|return
name|vertexSpecSignature
return|;
block|}
specifier|public
name|int
name|getVertexParallelism
parameter_list|()
block|{
return|return
name|vertexParallelism
return|;
block|}
block|}
end_class

end_unit

