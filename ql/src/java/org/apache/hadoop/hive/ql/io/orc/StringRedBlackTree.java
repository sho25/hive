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
name|ql
operator|.
name|io
operator|.
name|orc
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
name|io
operator|.
name|Text
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

begin_comment
comment|/**  * A red-black tree that stores strings. The strings are stored as UTF-8 bytes  * and an offset for each entry.  */
end_comment

begin_class
class|class
name|StringRedBlackTree
extends|extends
name|RedBlackTree
block|{
specifier|private
specifier|final
name|DynamicByteArray
name|byteArray
init|=
operator|new
name|DynamicByteArray
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|DynamicIntArray
name|keyOffsets
decl_stmt|;
specifier|private
specifier|final
name|Text
name|newKey
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
specifier|public
name|StringRedBlackTree
parameter_list|(
name|int
name|initialCapacity
parameter_list|)
block|{
name|super
argument_list|(
name|initialCapacity
argument_list|)
expr_stmt|;
name|keyOffsets
operator|=
operator|new
name|DynamicIntArray
argument_list|(
name|initialCapacity
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|add
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|newKey
operator|.
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
comment|// if the key is new, add it to our byteArray and store the offset& length
if|if
condition|(
name|add
argument_list|()
condition|)
block|{
name|int
name|len
init|=
name|newKey
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|keyOffsets
operator|.
name|add
argument_list|(
name|byteArray
operator|.
name|add
argument_list|(
name|newKey
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|lastAdd
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|compareValue
parameter_list|(
name|int
name|position
parameter_list|)
block|{
name|int
name|start
init|=
name|keyOffsets
operator|.
name|get
argument_list|(
name|position
argument_list|)
decl_stmt|;
name|int
name|end
decl_stmt|;
if|if
condition|(
name|position
operator|+
literal|1
operator|==
name|keyOffsets
operator|.
name|size
argument_list|()
condition|)
block|{
name|end
operator|=
name|byteArray
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|end
operator|=
name|keyOffsets
operator|.
name|get
argument_list|(
name|position
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|byteArray
operator|.
name|compare
argument_list|(
name|newKey
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|newKey
operator|.
name|getLength
argument_list|()
argument_list|,
name|start
argument_list|,
name|end
operator|-
name|start
argument_list|)
return|;
block|}
comment|/**    * The information about each node.    */
specifier|public
interface|interface
name|VisitorContext
block|{
comment|/**      * Get the position where the key was originally added.      * @return the number returned by add.      */
name|int
name|getOriginalPosition
parameter_list|()
function_decl|;
comment|/**      * Write the bytes for the string to the given output stream.      * @param out the stream to write to.      * @throws IOException      */
name|void
name|writeBytes
parameter_list|(
name|OutputStream
name|out
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Get the original string.      * @return the string      */
name|Text
name|getText
parameter_list|()
function_decl|;
comment|/**      * Get the number of bytes.      * @return the string's length in bytes      */
name|int
name|getLength
parameter_list|()
function_decl|;
block|}
comment|/**    * The interface for visitors.    */
specifier|public
interface|interface
name|Visitor
block|{
comment|/**      * Called once for each node of the tree in sort order.      * @param context the information about each node      * @throws IOException      */
name|void
name|visit
parameter_list|(
name|VisitorContext
name|context
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|private
class|class
name|VisitorContextImpl
implements|implements
name|VisitorContext
block|{
specifier|private
name|int
name|originalPosition
decl_stmt|;
specifier|private
name|int
name|start
decl_stmt|;
specifier|private
name|int
name|end
decl_stmt|;
specifier|private
specifier|final
name|Text
name|text
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
specifier|public
name|int
name|getOriginalPosition
parameter_list|()
block|{
return|return
name|originalPosition
return|;
block|}
specifier|public
name|Text
name|getText
parameter_list|()
block|{
name|byteArray
operator|.
name|setText
argument_list|(
name|text
argument_list|,
name|start
argument_list|,
name|end
operator|-
name|start
argument_list|)
expr_stmt|;
return|return
name|text
return|;
block|}
specifier|public
name|void
name|writeBytes
parameter_list|(
name|OutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|byteArray
operator|.
name|write
argument_list|(
name|out
argument_list|,
name|start
argument_list|,
name|end
operator|-
name|start
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getLength
parameter_list|()
block|{
return|return
name|end
operator|-
name|start
return|;
block|}
name|void
name|setPosition
parameter_list|(
name|int
name|position
parameter_list|)
block|{
name|originalPosition
operator|=
name|position
expr_stmt|;
name|start
operator|=
name|keyOffsets
operator|.
name|get
argument_list|(
name|originalPosition
argument_list|)
expr_stmt|;
if|if
condition|(
name|position
operator|+
literal|1
operator|==
name|keyOffsets
operator|.
name|size
argument_list|()
condition|)
block|{
name|end
operator|=
name|byteArray
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|end
operator|=
name|keyOffsets
operator|.
name|get
argument_list|(
name|originalPosition
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|recurse
parameter_list|(
name|int
name|node
parameter_list|,
name|Visitor
name|visitor
parameter_list|,
name|VisitorContextImpl
name|context
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|node
operator|!=
name|NULL
condition|)
block|{
name|recurse
argument_list|(
name|getLeft
argument_list|(
name|node
argument_list|)
argument_list|,
name|visitor
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|context
operator|.
name|setPosition
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|visitor
operator|.
name|visit
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|recurse
argument_list|(
name|getRight
argument_list|(
name|node
argument_list|)
argument_list|,
name|visitor
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Visit all of the nodes in the tree in sorted order.    * @param visitor the action to be applied to each ndoe    * @throws IOException    */
specifier|public
name|void
name|visit
parameter_list|(
name|Visitor
name|visitor
parameter_list|)
throws|throws
name|IOException
block|{
name|recurse
argument_list|(
name|root
argument_list|,
name|visitor
argument_list|,
operator|new
name|VisitorContextImpl
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Reset the table to empty.    */
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|super
operator|.
name|clear
argument_list|()
expr_stmt|;
name|byteArray
operator|.
name|clear
argument_list|()
expr_stmt|;
name|keyOffsets
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|/**    * Get the size of the character data in the table.    * @return the bytes used by the table    */
specifier|public
name|int
name|getCharacterSize
parameter_list|()
block|{
return|return
name|byteArray
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * Calculate the approximate size in memory.    * @return the number of bytes used in storing the tree.    */
specifier|public
name|long
name|getSizeInBytes
parameter_list|()
block|{
return|return
name|byteArray
operator|.
name|getSizeInBytes
argument_list|()
operator|+
name|keyOffsets
operator|.
name|getSizeInBytes
argument_list|()
operator|+
name|super
operator|.
name|getSizeInBytes
argument_list|()
return|;
block|}
block|}
end_class

end_unit

