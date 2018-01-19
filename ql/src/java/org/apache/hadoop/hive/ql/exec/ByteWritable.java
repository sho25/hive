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
name|ql
operator|.
name|exec
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
name|WritableComparable
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
name|WritableComparator
import|;
end_import

begin_comment
comment|/**  * ByteWritable.  * Looks like this has been phased out in favor of org.apache.hadoop.hive.serde2.io.ByteWritable.  * This class should eventually be removed.  */
end_comment

begin_class
annotation|@
name|Deprecated
specifier|public
class|class
name|ByteWritable
implements|implements
name|WritableComparable
block|{
specifier|private
name|int
name|value
decl_stmt|;
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
name|writeByte
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
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
name|value
operator|=
name|in
operator|.
name|readByte
argument_list|()
expr_stmt|;
block|}
specifier|public
name|ByteWritable
parameter_list|(
name|int
name|b
parameter_list|)
block|{
name|value
operator|=
name|b
operator|&
literal|0xff
expr_stmt|;
block|}
specifier|public
name|ByteWritable
parameter_list|()
block|{
name|value
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|int
name|b
parameter_list|)
block|{
name|value
operator|=
name|b
operator|&
literal|0xff
expr_stmt|;
block|}
comment|/** Compares two ByteWritables. */
specifier|public
name|int
name|compareTo
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|int
name|thisValue
init|=
name|value
decl_stmt|;
name|int
name|thatValue
init|=
operator|(
operator|(
name|ByteWritable
operator|)
name|o
operator|)
operator|.
name|value
decl_stmt|;
return|return
operator|(
name|thisValue
operator|<
name|thatValue
condition|?
operator|-
literal|1
else|:
operator|(
name|thisValue
operator|==
name|thatValue
condition|?
literal|0
else|:
literal|1
operator|)
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|ByteWritable
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ByteWritable
name|that
init|=
operator|(
name|ByteWritable
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|this
operator|==
name|that
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|value
operator|==
name|that
operator|.
name|value
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
operator|(
name|value
operator|)
return|;
block|}
comment|/** A Comparator optimized for BytesWritable. */
specifier|public
specifier|static
class|class
name|Comparator
extends|extends
name|WritableComparator
block|{
specifier|public
name|Comparator
parameter_list|()
block|{
name|super
argument_list|(
name|ByteWritable
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
comment|/**      * Compare the buffers in serialized form.      */
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|byte
index|[]
name|b1
parameter_list|,
name|int
name|s1
parameter_list|,
name|int
name|l1
parameter_list|,
name|byte
index|[]
name|b2
parameter_list|,
name|int
name|s2
parameter_list|,
name|int
name|l2
parameter_list|)
block|{
comment|/**        * ok - we are implementing a dummy byte int a = b1[s1]& 0xff; int b =        * b2[s1]& 0xff; if(a!=b) return a -b;        */
return|return
literal|0
return|;
block|}
block|}
static|static
block|{
comment|// register this comparator
name|WritableComparator
operator|.
name|define
argument_list|(
name|ByteWritable
operator|.
name|class
argument_list|,
operator|new
name|Comparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

