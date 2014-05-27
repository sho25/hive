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
name|contrib
operator|.
name|util
operator|.
name|typedbytes
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
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_comment
comment|/**  * Deserialized for records that reads typed bytes.  */
end_comment

begin_class
specifier|public
class|class
name|TypedBytesRecordOutput
block|{
specifier|private
name|TypedBytesOutput
name|out
decl_stmt|;
specifier|private
name|TypedBytesRecordOutput
parameter_list|()
block|{   }
specifier|private
name|void
name|setTypedBytesOutput
parameter_list|(
name|TypedBytesOutput
name|out
parameter_list|)
block|{
name|this
operator|.
name|out
operator|=
name|out
expr_stmt|;
block|}
specifier|private
specifier|static
name|ThreadLocal
name|tbOut
init|=
operator|new
name|ThreadLocal
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
specifier|synchronized
name|Object
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|TypedBytesRecordOutput
argument_list|()
return|;
block|}
block|}
decl_stmt|;
comment|/**    * Get a thread-local typed bytes record input for the supplied    * {@link TypedBytesOutput}.    *    * @param out    *          typed bytes output object    * @return typed bytes record output corresponding to the supplied    *         {@link TypedBytesOutput}.    */
specifier|public
specifier|static
name|TypedBytesRecordOutput
name|get
parameter_list|(
name|TypedBytesOutput
name|out
parameter_list|)
block|{
name|TypedBytesRecordOutput
name|bout
init|=
operator|(
name|TypedBytesRecordOutput
operator|)
name|tbOut
operator|.
name|get
argument_list|()
decl_stmt|;
name|bout
operator|.
name|setTypedBytesOutput
argument_list|(
name|out
argument_list|)
expr_stmt|;
return|return
name|bout
return|;
block|}
comment|/**    * Get a thread-local typed bytes record output for the supplied    * {@link DataOutput}.    *    * @param out    *          data output object    * @return typed bytes record output corresponding to the supplied    *         {@link DataOutput}.    */
specifier|public
specifier|static
name|TypedBytesRecordOutput
name|get
parameter_list|(
name|DataOutput
name|out
parameter_list|)
block|{
return|return
name|get
argument_list|(
name|TypedBytesOutput
operator|.
name|get
argument_list|(
name|out
argument_list|)
argument_list|)
return|;
block|}
comment|/** Creates a new instance of TypedBytesRecordOutput. */
specifier|public
name|TypedBytesRecordOutput
parameter_list|(
name|TypedBytesOutput
name|out
parameter_list|)
block|{
name|this
operator|.
name|out
operator|=
name|out
expr_stmt|;
block|}
comment|/** Creates a new instance of TypedBytesRecordOutput. */
specifier|public
name|TypedBytesRecordOutput
parameter_list|(
name|DataOutput
name|out
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|TypedBytesOutput
argument_list|(
name|out
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeBool
parameter_list|(
name|boolean
name|b
parameter_list|,
name|String
name|tag
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeBool
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeByte
parameter_list|(
name|byte
name|b
parameter_list|,
name|String
name|tag
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeByte
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeDouble
parameter_list|(
name|double
name|d
parameter_list|,
name|String
name|tag
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeDouble
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeFloat
parameter_list|(
name|float
name|f
parameter_list|,
name|String
name|tag
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeFloat
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeInt
parameter_list|(
name|int
name|i
parameter_list|,
name|String
name|tag
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeLong
parameter_list|(
name|long
name|l
parameter_list|,
name|String
name|tag
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeLong
argument_list|(
name|l
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeString
parameter_list|(
name|String
name|s
parameter_list|,
name|String
name|tag
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|startVector
parameter_list|(
name|ArrayList
name|v
parameter_list|,
name|String
name|tag
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVectorHeader
argument_list|(
name|v
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|startMap
parameter_list|(
name|TreeMap
name|m
parameter_list|,
name|String
name|tag
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeMapHeader
argument_list|(
name|m
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|endVector
parameter_list|(
name|ArrayList
name|v
parameter_list|,
name|String
name|tag
parameter_list|)
throws|throws
name|IOException
block|{   }
specifier|public
name|void
name|endMap
parameter_list|(
name|TreeMap
name|m
parameter_list|,
name|String
name|tag
parameter_list|)
throws|throws
name|IOException
block|{   }
block|}
end_class

end_unit

