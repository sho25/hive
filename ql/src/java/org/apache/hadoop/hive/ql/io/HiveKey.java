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
name|BytesWritable
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
comment|/**  * HiveKey is a simple wrapper on Text which allows us to set the hashCode  * easily. hashCode is used for hadoop partitioner.  *   */
end_comment

begin_class
specifier|public
class|class
name|HiveKey
extends|extends
name|BytesWritable
block|{
specifier|private
specifier|static
specifier|final
name|int
name|LENGTH_BYTES
init|=
literal|4
decl_stmt|;
specifier|private
name|int
name|hashCode
decl_stmt|;
specifier|private
name|boolean
name|hashCodeValid
decl_stmt|;
specifier|private
specifier|transient
name|int
name|distKeyLength
decl_stmt|;
specifier|public
name|HiveKey
parameter_list|()
block|{
name|hashCodeValid
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|HiveKey
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|hashcode
parameter_list|)
block|{
name|super
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|hashCode
operator|=
name|hashcode
expr_stmt|;
name|hashCodeValid
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|setHashCode
parameter_list|(
name|int
name|myHashCode
parameter_list|)
block|{
name|hashCodeValid
operator|=
literal|true
expr_stmt|;
name|hashCode
operator|=
name|myHashCode
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
if|if
condition|(
operator|!
name|hashCodeValid
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot get hashCode() from deserialized "
operator|+
name|HiveKey
operator|.
name|class
argument_list|)
throw|;
block|}
return|return
name|hashCode
return|;
block|}
specifier|public
name|void
name|setDistKeyLength
parameter_list|(
name|int
name|distKeyLength
parameter_list|)
block|{
name|this
operator|.
name|distKeyLength
operator|=
name|distKeyLength
expr_stmt|;
block|}
specifier|public
name|int
name|getDistKeyLength
parameter_list|()
block|{
return|return
name|distKeyLength
return|;
block|}
comment|/** A Comparator optimized for HiveKey. */
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
name|HiveKey
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
return|return
name|compareBytes
argument_list|(
name|b1
argument_list|,
name|s1
operator|+
name|LENGTH_BYTES
argument_list|,
name|l1
operator|-
name|LENGTH_BYTES
argument_list|,
name|b2
argument_list|,
name|s2
operator|+
name|LENGTH_BYTES
argument_list|,
name|l2
operator|-
name|LENGTH_BYTES
argument_list|)
return|;
block|}
block|}
static|static
block|{
name|WritableComparator
operator|.
name|define
argument_list|(
name|HiveKey
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

