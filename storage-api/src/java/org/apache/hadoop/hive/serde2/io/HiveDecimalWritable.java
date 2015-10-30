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
name|serde2
operator|.
name|io
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
name|java
operator|.
name|math
operator|.
name|BigInteger
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
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|WritableUtils
import|;
end_import

begin_class
specifier|public
class|class
name|HiveDecimalWritable
implements|implements
name|WritableComparable
argument_list|<
name|HiveDecimalWritable
argument_list|>
block|{
specifier|private
name|byte
index|[]
name|internalStorage
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
specifier|private
name|int
name|scale
decl_stmt|;
specifier|public
name|HiveDecimalWritable
parameter_list|()
block|{   }
specifier|public
name|HiveDecimalWritable
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|set
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveDecimalWritable
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
name|set
argument_list|(
name|bytes
argument_list|,
name|scale
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveDecimalWritable
parameter_list|(
name|HiveDecimalWritable
name|writable
parameter_list|)
block|{
name|set
argument_list|(
name|writable
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveDecimalWritable
parameter_list|(
name|HiveDecimal
name|value
parameter_list|)
block|{
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveDecimalWritable
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|set
argument_list|(
operator|(
name|HiveDecimal
operator|.
name|create
argument_list|(
name|value
argument_list|)
operator|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|HiveDecimal
name|value
parameter_list|)
block|{
name|set
argument_list|(
name|value
operator|.
name|unscaledValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|value
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|HiveDecimal
name|value
parameter_list|,
name|int
name|maxPrecision
parameter_list|,
name|int
name|maxScale
parameter_list|)
block|{
name|set
argument_list|(
name|HiveDecimal
operator|.
name|enforcePrecisionScale
argument_list|(
name|value
argument_list|,
name|maxPrecision
argument_list|,
name|maxScale
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|HiveDecimalWritable
name|writable
parameter_list|)
block|{
name|set
argument_list|(
name|writable
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
name|this
operator|.
name|internalStorage
operator|=
name|bytes
expr_stmt|;
name|this
operator|.
name|scale
operator|=
name|scale
expr_stmt|;
block|}
specifier|public
name|HiveDecimal
name|getHiveDecimal
parameter_list|()
block|{
return|return
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|new
name|BigInteger
argument_list|(
name|internalStorage
argument_list|)
argument_list|,
name|scale
argument_list|)
return|;
block|}
comment|/**    * Get a HiveDecimal instance from the writable and constraint it with maximum precision/scale.    *    * @param maxPrecision maximum precision    * @param maxScale maximum scale    * @return HiveDecimal instance    */
specifier|public
name|HiveDecimal
name|getHiveDecimal
parameter_list|(
name|int
name|maxPrecision
parameter_list|,
name|int
name|maxScale
parameter_list|)
block|{
return|return
name|HiveDecimal
operator|.
name|enforcePrecisionScale
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|new
name|BigInteger
argument_list|(
name|internalStorage
argument_list|)
argument_list|,
name|scale
argument_list|)
argument_list|,
name|maxPrecision
argument_list|,
name|maxScale
argument_list|)
return|;
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
name|scale
operator|=
name|WritableUtils
operator|.
name|readVInt
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|byteArrayLen
init|=
name|WritableUtils
operator|.
name|readVInt
argument_list|(
name|in
argument_list|)
decl_stmt|;
if|if
condition|(
name|internalStorage
operator|.
name|length
operator|!=
name|byteArrayLen
condition|)
block|{
name|internalStorage
operator|=
operator|new
name|byte
index|[
name|byteArrayLen
index|]
expr_stmt|;
block|}
name|in
operator|.
name|readFully
argument_list|(
name|internalStorage
argument_list|)
expr_stmt|;
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
name|WritableUtils
operator|.
name|writeVInt
argument_list|(
name|out
argument_list|,
name|scale
argument_list|)
expr_stmt|;
name|WritableUtils
operator|.
name|writeVInt
argument_list|(
name|out
argument_list|,
name|internalStorage
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|internalStorage
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|HiveDecimalWritable
name|that
parameter_list|)
block|{
return|return
name|getHiveDecimal
argument_list|()
operator|.
name|compareTo
argument_list|(
name|that
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getHiveDecimal
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|other
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|other
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|other
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|HiveDecimalWritable
name|bdw
init|=
operator|(
name|HiveDecimalWritable
operator|)
name|other
decl_stmt|;
comment|// 'equals' and 'compareTo' are not compatible with HiveDecimals. We want
comment|// compareTo which returns true iff the numbers are equal (e.g.: 3.14 is
comment|// the same as 3.140). 'Equals' returns true iff equal and the same scale
comment|// is set in the decimals (e.g.: 3.14 is not the same as 3.140)
return|return
name|getHiveDecimal
argument_list|()
operator|.
name|compareTo
argument_list|(
name|bdw
operator|.
name|getHiveDecimal
argument_list|()
argument_list|)
operator|==
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|getHiveDecimal
argument_list|()
operator|.
name|hashCode
argument_list|()
return|;
block|}
comment|/* (non-Javadoc)    * In order to update a Decimal128 fast (w/o allocation) we need to expose access to the    * internal storage bytes and scale.    * @return    */
specifier|public
name|byte
index|[]
name|getInternalStorage
parameter_list|()
block|{
return|return
name|internalStorage
return|;
block|}
comment|/* (non-Javadoc)    * In order to update a Decimal128 fast (w/o allocation) we need to expose access to the    * internal storage bytes and scale.    */
specifier|public
name|int
name|getScale
parameter_list|()
block|{
return|return
name|scale
return|;
block|}
specifier|public
specifier|static
name|HiveDecimalWritable
name|enforcePrecisionScale
parameter_list|(
name|HiveDecimalWritable
name|writable
parameter_list|,
name|int
name|precision
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
if|if
condition|(
name|writable
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|HiveDecimal
name|dec
init|=
name|HiveDecimal
operator|.
name|enforcePrecisionScale
argument_list|(
name|writable
operator|.
name|getHiveDecimal
argument_list|()
argument_list|,
name|precision
argument_list|,
name|scale
argument_list|)
decl_stmt|;
return|return
name|dec
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|HiveDecimalWritable
argument_list|(
name|dec
argument_list|)
return|;
block|}
block|}
end_class

end_unit

