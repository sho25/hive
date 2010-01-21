begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/**  * JDBM LICENSE v1.00  *  * Redistribution and use of this software and associated documentation  * ("Software"), with or without modification, are permitted provided  * that the following conditions are met:  *  * 1. Redistributions of source code must retain copyright  *    statements and notices.  Redistributions must also contain a  *    copy of this document.  *  * 2. Redistributions in binary form must reproduce the  *    above copyright notice, this list of conditions and the  *    following disclaimer in the documentation and/or other  *    materials provided with the distribution.  *  * 3. The name "JDBM" must not be used to endorse or promote  *    products derived from this Software without prior written  *    permission of Cees de Groot.  For written permission,  *    please contact cg@cdegroot.com.  *  * 4. Products derived from this Software may not be called "JDBM"  *    nor may "JDBM" appear in their names without prior written  *    permission of Cees de Groot.  *  * 5. Due credit should be given to the JDBM Project  *    (http://jdbm.sourceforge.net/).  *  * THIS SOFTWARE IS PROVIDED BY THE JDBM PROJECT AND CONTRIBUTORS  * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT  * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL  * CEES DE GROOT OR ANY CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED  * OF THE POSSIBILITY OF SUCH DAMAGE.  *  * Copyright 2000 (C) Cees de Groot. All Rights Reserved.  * Contributions are Copyright (C) 2000 by their associated contributors.  *  */
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
name|util
operator|.
name|jdbm
operator|.
name|htree
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Externalizable
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
name|ObjectInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectOutput
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

begin_comment
comment|/**  * A bucket is a placeholder for multiple (key, value) pairs. Buckets are used  * to store collisions (same hash value) at all levels of an H*tree.  *   * There are two types of buckets: leaf and non-leaf.  *   * Non-leaf buckets are buckets which hold collisions which happen when the  * H*tree is not fully expanded. Keys in a non-leaf buckets can have different  * hash codes. Non-leaf buckets are limited to an arbitrary size. When this  * limit is reached, the H*tree should create a new Directory page and  * distribute keys of the non-leaf buckets into the newly created Directory.  *   * A leaf bucket is a bucket which contains keys which all have the same  *<code>hashCode()</code>. Leaf buckets stand at the bottom of an H*tree  * because the hashing algorithm cannot further discriminate between different  * keys based on their hash code.  *   * @author<a href="mailto:boisvert@intalio.com">Alex Boisvert</a>  * @version $Id: HashBucket.java,v 1.2 2005/06/25 23:12:32 doomdark Exp $  */
end_comment

begin_class
specifier|final
class|class
name|HashBucket
extends|extends
name|HashNode
implements|implements
name|Externalizable
block|{
specifier|final
specifier|static
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|/**    * The maximum number of elements (key, value) a non-leaf bucket can contain.    */
specifier|public
specifier|static
specifier|final
name|int
name|OVERFLOW_SIZE
init|=
literal|8
decl_stmt|;
comment|/**    * Depth of this bucket.    */
specifier|private
name|int
name|_depth
decl_stmt|;
comment|/**    * Keys in this bucket. Keys are ordered to match their respective value in    *<code>_values</code>.    */
specifier|private
name|ArrayList
name|_keys
decl_stmt|;
comment|/**    * Values in this bucket. Values are ordered to match their respective key in    *<code>_keys</code>.    */
specifier|private
name|ArrayList
name|_values
decl_stmt|;
comment|/**    * Public constructor for serialization.    */
specifier|public
name|HashBucket
parameter_list|()
block|{
comment|// empty
block|}
comment|/**    * Construct a bucket with a given depth level. Depth level is the number of    *<code>HashDirectory</code> above this bucket.    */
specifier|public
name|HashBucket
parameter_list|(
name|int
name|level
parameter_list|)
block|{
if|if
condition|(
name|level
operator|>
name|HashDirectory
operator|.
name|MAX_DEPTH
operator|+
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot create bucket with depth> MAX_DEPTH+1. "
operator|+
literal|"Depth="
operator|+
name|level
argument_list|)
throw|;
block|}
name|_depth
operator|=
name|level
expr_stmt|;
name|_keys
operator|=
operator|new
name|ArrayList
argument_list|(
name|OVERFLOW_SIZE
argument_list|)
expr_stmt|;
name|_values
operator|=
operator|new
name|ArrayList
argument_list|(
name|OVERFLOW_SIZE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns the number of elements contained in this bucket.    */
specifier|public
name|int
name|getElementCount
parameter_list|()
block|{
return|return
name|_keys
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * Returns whether or not this bucket is a "leaf bucket".    */
specifier|public
name|boolean
name|isLeaf
parameter_list|()
block|{
return|return
operator|(
name|_depth
operator|>
name|HashDirectory
operator|.
name|MAX_DEPTH
operator|)
return|;
block|}
comment|/**    * Returns true if bucket can accept at least one more element.    */
specifier|public
name|boolean
name|hasRoom
parameter_list|()
block|{
if|if
condition|(
name|isLeaf
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
comment|// leaf buckets are never full
block|}
else|else
block|{
comment|// non-leaf bucket
return|return
operator|(
name|_keys
operator|.
name|size
argument_list|()
operator|<
name|OVERFLOW_SIZE
operator|)
return|;
block|}
block|}
comment|/**    * Add an element (key, value) to this bucket. If an existing element has the    * same key, it is replaced silently.    *     * @return Object which was previously associated with the given key or    *<code>null</code> if no association existed.    */
specifier|public
name|Object
name|addElement
parameter_list|(
name|Object
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|int
name|existing
init|=
name|_keys
operator|.
name|indexOf
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|existing
operator|!=
operator|-
literal|1
condition|)
block|{
comment|// replace existing element
name|Object
name|before
init|=
name|_values
operator|.
name|get
argument_list|(
name|existing
argument_list|)
decl_stmt|;
name|_values
operator|.
name|set
argument_list|(
name|existing
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|before
return|;
block|}
else|else
block|{
comment|// add new (key, value) pair
name|_keys
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|_values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Remove an element, given a specific key.    *     * @param key    *          Key of the element to remove    *     * @return Removed element value, or<code>null</code> if not found    */
specifier|public
name|Object
name|removeElement
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
name|int
name|existing
init|=
name|_keys
operator|.
name|indexOf
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|existing
operator|!=
operator|-
literal|1
condition|)
block|{
name|Object
name|obj
init|=
name|_values
operator|.
name|get
argument_list|(
name|existing
argument_list|)
decl_stmt|;
name|_keys
operator|.
name|remove
argument_list|(
name|existing
argument_list|)
expr_stmt|;
name|_values
operator|.
name|remove
argument_list|(
name|existing
argument_list|)
expr_stmt|;
return|return
name|obj
return|;
block|}
else|else
block|{
comment|// not found
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Returns the value associated with a given key. If the given key is not    * found in this bucket, returns<code>null</code>.    */
specifier|public
name|Object
name|getValue
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
name|int
name|existing
init|=
name|_keys
operator|.
name|indexOf
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|existing
operator|!=
operator|-
literal|1
condition|)
block|{
return|return
name|_values
operator|.
name|get
argument_list|(
name|existing
argument_list|)
return|;
block|}
else|else
block|{
comment|// key not found
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Obtain keys contained in this buckets. Keys are ordered to match their    * values, which be be obtained by calling<code>getValues()</code>.    *     * As an optimization, the Vector returned is the instance member of this    * class. Please don't modify outside the scope of this class.    */
name|ArrayList
name|getKeys
parameter_list|()
block|{
return|return
name|_keys
return|;
block|}
comment|/**    * Obtain values contained in this buckets. Values are ordered to match their    * keys, which be be obtained by calling<code>getKeys()</code>.    *     * As an optimization, the Vector returned is the instance member of this    * class. Please don't modify outside the scope of this class.    */
name|ArrayList
name|getValues
parameter_list|()
block|{
return|return
name|_values
return|;
block|}
comment|/**    * Implement Externalizable interface.    */
specifier|public
name|void
name|writeExternal
parameter_list|(
name|ObjectOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|_depth
argument_list|)
expr_stmt|;
name|int
name|entries
init|=
name|_keys
operator|.
name|size
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|entries
argument_list|)
expr_stmt|;
comment|// write keys
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|entries
condition|;
name|i
operator|++
control|)
block|{
name|out
operator|.
name|writeObject
argument_list|(
name|_keys
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// write values
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|entries
condition|;
name|i
operator|++
control|)
block|{
name|out
operator|.
name|writeObject
argument_list|(
name|_values
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Implement Externalizable interface.    */
specifier|public
name|void
name|readExternal
parameter_list|(
name|ObjectInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|_depth
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|int
name|entries
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
comment|// prepare array lists
name|int
name|size
init|=
name|Math
operator|.
name|max
argument_list|(
name|entries
argument_list|,
name|OVERFLOW_SIZE
argument_list|)
decl_stmt|;
name|_keys
operator|=
operator|new
name|ArrayList
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|_values
operator|=
operator|new
name|ArrayList
argument_list|(
name|size
argument_list|)
expr_stmt|;
comment|// read keys
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|entries
condition|;
name|i
operator|++
control|)
block|{
name|_keys
operator|.
name|add
argument_list|(
name|in
operator|.
name|readObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// read values
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|entries
condition|;
name|i
operator|++
control|)
block|{
name|_values
operator|.
name|add
argument_list|(
name|in
operator|.
name|readObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuffer
name|buf
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|"HashBucket {depth="
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|_depth
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|", keys="
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|_keys
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|", values="
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|_values
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
return|return
name|buf
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

