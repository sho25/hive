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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|WritableUtils
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
name|record
operator|.
name|Buffer
import|;
end_import

begin_comment
comment|/**  * Provides functionality for writing typed bytes.  */
end_comment

begin_class
specifier|public
class|class
name|TypedBytesOutput
block|{
specifier|private
name|DataOutput
name|out
decl_stmt|;
specifier|private
name|TypedBytesOutput
parameter_list|()
block|{   }
specifier|private
name|void
name|setDataOutput
parameter_list|(
name|DataOutput
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
name|TypedBytesOutput
argument_list|()
return|;
block|}
block|}
decl_stmt|;
comment|/**    * Get a thread-local typed bytes output for the supplied {@link DataOutput}.    *    * @param out    *          data output object    * @return typed bytes output corresponding to the supplied {@link DataOutput}    *         .    */
specifier|public
specifier|static
name|TypedBytesOutput
name|get
parameter_list|(
name|DataOutput
name|out
parameter_list|)
block|{
name|TypedBytesOutput
name|bout
init|=
operator|(
name|TypedBytesOutput
operator|)
name|tbOut
operator|.
name|get
argument_list|()
decl_stmt|;
name|bout
operator|.
name|setDataOutput
argument_list|(
name|out
argument_list|)
expr_stmt|;
return|return
name|bout
return|;
block|}
comment|/** Creates a new instance of TypedBytesOutput. */
specifier|public
name|TypedBytesOutput
parameter_list|(
name|DataOutput
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
comment|/**    * Writes a Java object as a typed bytes sequence.    *    * @param obj    *          the object to be written    * @throws IOException    */
specifier|public
name|void
name|write
parameter_list|(
name|Object
name|obj
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|obj
operator|instanceof
name|Buffer
condition|)
block|{
name|writeBytes
argument_list|(
operator|(
operator|(
name|Buffer
operator|)
name|obj
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|obj
operator|instanceof
name|Byte
condition|)
block|{
name|writeByte
argument_list|(
operator|(
name|Byte
operator|)
name|obj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|obj
operator|instanceof
name|Boolean
condition|)
block|{
name|writeBool
argument_list|(
operator|(
name|Boolean
operator|)
name|obj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|obj
operator|instanceof
name|Integer
condition|)
block|{
name|writeInt
argument_list|(
operator|(
name|Integer
operator|)
name|obj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|obj
operator|instanceof
name|Long
condition|)
block|{
name|writeLong
argument_list|(
operator|(
name|Long
operator|)
name|obj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|obj
operator|instanceof
name|Float
condition|)
block|{
name|writeFloat
argument_list|(
operator|(
name|Float
operator|)
name|obj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|obj
operator|instanceof
name|Double
condition|)
block|{
name|writeDouble
argument_list|(
operator|(
name|Double
operator|)
name|obj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|obj
operator|instanceof
name|String
condition|)
block|{
name|writeString
argument_list|(
operator|(
name|String
operator|)
name|obj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|obj
operator|instanceof
name|ArrayList
condition|)
block|{
name|writeVector
argument_list|(
operator|(
name|ArrayList
operator|)
name|obj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|obj
operator|instanceof
name|List
condition|)
block|{
name|writeList
argument_list|(
operator|(
name|List
operator|)
name|obj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|obj
operator|instanceof
name|Map
condition|)
block|{
name|writeMap
argument_list|(
operator|(
name|Map
operator|)
name|obj
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"cannot write objects of this type"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Writes a raw sequence of typed bytes.    *    * @param bytes    *          the bytes to be written    * @throws IOException    */
specifier|public
name|void
name|writeRaw
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes a raw sequence of typed bytes.    *    * @param bytes    *          the bytes to be written    * @param offset    *          an offset in the given array    * @param length    *          number of bytes from the given array to write    * @throws IOException    */
specifier|public
name|void
name|writeRaw
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes a bytes array as a typed bytes sequence, using a given typecode.    *    * @param bytes    *          the bytes array to be written    * @param code    *          the typecode to use    * @throws IOException    */
specifier|public
name|void
name|writeBytes
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|code
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|code
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|bytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes a bytes array as a typed bytes sequence.    *    * @param bytes    *          the bytes array to be written    * @throws IOException    */
specifier|public
name|void
name|writeBytes
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|IOException
block|{
name|writeBytes
argument_list|(
name|bytes
argument_list|,
name|Type
operator|.
name|BYTES
operator|.
name|code
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes a byte as a typed bytes sequence.    *    * @param b    *          the byte to be written    * @throws IOException    */
specifier|public
name|void
name|writeByte
parameter_list|(
name|byte
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|Type
operator|.
name|BYTE
operator|.
name|code
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes a boolean as a typed bytes sequence.    *    * @param b    *          the boolean to be written    * @throws IOException    */
specifier|public
name|void
name|writeBool
parameter_list|(
name|boolean
name|b
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|Type
operator|.
name|BOOL
operator|.
name|code
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes an integer as a typed bytes sequence.    *    * @param i    *          the integer to be written    * @throws IOException    */
specifier|public
name|void
name|writeInt
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|Type
operator|.
name|INT
operator|.
name|code
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes a long as a typed bytes sequence.    *    * @param l    *          the long to be written    * @throws IOException    */
specifier|public
name|void
name|writeLong
parameter_list|(
name|long
name|l
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|Type
operator|.
name|LONG
operator|.
name|code
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|l
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes a float as a typed bytes sequence.    *    * @param f    *          the float to be written    * @throws IOException    */
specifier|public
name|void
name|writeFloat
parameter_list|(
name|float
name|f
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|Type
operator|.
name|FLOAT
operator|.
name|code
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeFloat
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes a double as a typed bytes sequence.    *    * @param d    *          the double to be written    * @throws IOException    */
specifier|public
name|void
name|writeDouble
parameter_list|(
name|double
name|d
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|Type
operator|.
name|DOUBLE
operator|.
name|code
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes a short as a typed bytes sequence.    *    * @param s    *          the short to be written    * @throws IOException    */
specifier|public
name|void
name|writeShort
parameter_list|(
name|short
name|s
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|Type
operator|.
name|SHORT
operator|.
name|code
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeShort
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes a string as a typed bytes sequence.    *    * @param s    *          the string to be written    * @throws IOException    */
specifier|public
name|void
name|writeString
parameter_list|(
name|String
name|s
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|Type
operator|.
name|STRING
operator|.
name|code
argument_list|)
expr_stmt|;
name|WritableUtils
operator|.
name|writeString
argument_list|(
name|out
argument_list|,
name|s
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes a vector as a typed bytes sequence.    *    * @param vector    *          the vector to be written    * @throws IOException    */
specifier|public
name|void
name|writeVector
parameter_list|(
name|ArrayList
name|vector
parameter_list|)
throws|throws
name|IOException
block|{
name|writeVectorHeader
argument_list|(
name|vector
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Object
name|obj
range|:
name|vector
control|)
block|{
name|write
argument_list|(
name|obj
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Writes a vector header.    *    * @param length    *          the number of elements in the vector    * @throws IOException    */
specifier|public
name|void
name|writeVectorHeader
parameter_list|(
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|Type
operator|.
name|VECTOR
operator|.
name|code
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|length
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes a list as a typed bytes sequence.    *    * @param list    *          the list to be written    * @throws IOException    */
specifier|public
name|void
name|writeList
parameter_list|(
name|List
name|list
parameter_list|)
throws|throws
name|IOException
block|{
name|writeListHeader
argument_list|()
expr_stmt|;
for|for
control|(
name|Object
name|obj
range|:
name|list
control|)
block|{
name|write
argument_list|(
name|obj
argument_list|)
expr_stmt|;
block|}
name|writeListFooter
argument_list|()
expr_stmt|;
block|}
comment|/**    * Writes a list header.    *    * @throws IOException    */
specifier|public
name|void
name|writeListHeader
parameter_list|()
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|Type
operator|.
name|LIST
operator|.
name|code
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes a list footer.    *    * @throws IOException    */
specifier|public
name|void
name|writeListFooter
parameter_list|()
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|Type
operator|.
name|MARKER
operator|.
name|code
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes a map as a typed bytes sequence.    *    * @param map    *          the map to be written    * @throws IOException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|void
name|writeMap
parameter_list|(
name|Map
name|map
parameter_list|)
throws|throws
name|IOException
block|{
name|writeMapHeader
argument_list|(
name|map
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|Entry
argument_list|>
name|entries
init|=
name|map
operator|.
name|entrySet
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|write
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|write
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Writes a map header.    *    * @param length    *          the number of key-value pairs in the map    * @throws IOException    */
specifier|public
name|void
name|writeMapHeader
parameter_list|(
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|Type
operator|.
name|MAP
operator|.
name|code
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|length
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeEndOfRecord
parameter_list|()
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|Type
operator|.
name|ENDOFRECORD
operator|.
name|code
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes a<tt>NULL</tt> type marker to the output.    *    * @throws IOException    */
specifier|public
name|void
name|writeNull
parameter_list|()
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|Type
operator|.
name|NULL
operator|.
name|code
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

