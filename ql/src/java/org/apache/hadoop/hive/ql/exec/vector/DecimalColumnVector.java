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
name|exec
operator|.
name|vector
package|;
end_package

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
name|common
operator|.
name|type
operator|.
name|Decimal128
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
name|serde2
operator|.
name|io
operator|.
name|HiveDecimalWritable
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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|VectorizedRowBatch
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
name|NullWritable
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
class|class
name|DecimalColumnVector
extends|extends
name|ColumnVector
block|{
comment|/**    * A vector of HiveDecimalWritable objects.    *    * For high performance and easy access to this low-level structure,    * the fields are public by design (as they are in other ColumnVector    * types).    */
specifier|public
name|HiveDecimalWritable
index|[]
name|vector
decl_stmt|;
specifier|public
name|short
name|scale
decl_stmt|;
specifier|public
name|short
name|precision
decl_stmt|;
specifier|private
specifier|final
name|HiveDecimalWritable
name|writableObj
init|=
operator|new
name|HiveDecimalWritable
argument_list|()
decl_stmt|;
specifier|public
name|DecimalColumnVector
parameter_list|(
name|int
name|precision
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
name|this
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|,
name|precision
argument_list|,
name|scale
argument_list|)
expr_stmt|;
block|}
specifier|public
name|DecimalColumnVector
parameter_list|(
name|int
name|size
parameter_list|,
name|int
name|precision
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
name|super
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|this
operator|.
name|precision
operator|=
operator|(
name|short
operator|)
name|precision
expr_stmt|;
name|this
operator|.
name|scale
operator|=
operator|(
name|short
operator|)
name|scale
expr_stmt|;
specifier|final
name|int
name|len
init|=
name|size
decl_stmt|;
name|vector
operator|=
operator|new
name|HiveDecimalWritable
index|[
name|len
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
name|len
condition|;
name|i
operator|++
control|)
block|{
name|vector
index|[
name|i
index|]
operator|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|ZERO
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Writable
name|getWritableObject
parameter_list|(
name|int
name|index
parameter_list|)
block|{
if|if
condition|(
name|isRepeating
condition|)
block|{
name|index
operator|=
literal|0
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|noNulls
operator|&&
name|isNull
index|[
name|index
index|]
condition|)
block|{
return|return
name|NullWritable
operator|.
name|get
argument_list|()
return|;
block|}
else|else
block|{
name|writableObj
operator|.
name|set
argument_list|(
name|vector
index|[
name|index
index|]
argument_list|)
expr_stmt|;
return|return
name|writableObj
return|;
block|}
block|}
comment|// Fill the all the vector entries with provided value
specifier|public
name|void
name|fill
parameter_list|(
name|Decimal128
name|value
parameter_list|)
block|{
name|noNulls
operator|=
literal|true
expr_stmt|;
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|vector
index|[
literal|0
index|]
operator|=
name|value
expr_stmt|;
block|}
comment|// Fill the column vector with nulls
specifier|public
name|void
name|fillWithNulls
parameter_list|()
block|{
name|noNulls
operator|=
literal|false
expr_stmt|;
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|vector
index|[
literal|0
index|]
operator|=
literal|null
expr_stmt|;
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|flatten
parameter_list|(
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|size
parameter_list|)
block|{
comment|// TODO Auto-generated method stub
block|}
annotation|@
name|Override
specifier|public
name|void
name|setElement
parameter_list|(
name|int
name|outElementNum
parameter_list|,
name|int
name|inputElementNum
parameter_list|,
name|ColumnVector
name|inputVector
parameter_list|)
block|{
name|HiveDecimal
name|hiveDec
init|=
operator|(
operator|(
name|DecimalColumnVector
operator|)
name|inputVector
operator|)
operator|.
name|vector
index|[
name|inputElementNum
index|]
operator|.
name|getHiveDecimal
argument_list|(
name|precision
argument_list|,
name|scale
argument_list|)
decl_stmt|;
if|if
condition|(
name|hiveDec
operator|==
literal|null
condition|)
block|{
name|noNulls
operator|=
literal|false
expr_stmt|;
name|isNull
index|[
name|outElementNum
index|]
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|vector
index|[
name|outElementNum
index|]
operator|.
name|set
argument_list|(
name|hiveDec
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|set
parameter_list|(
name|int
name|elementNum
parameter_list|,
name|HiveDecimalWritable
name|writeable
parameter_list|)
block|{
name|HiveDecimal
name|hiveDec
init|=
name|writeable
operator|.
name|getHiveDecimal
argument_list|(
name|precision
argument_list|,
name|scale
argument_list|)
decl_stmt|;
if|if
condition|(
name|hiveDec
operator|==
literal|null
condition|)
block|{
name|noNulls
operator|=
literal|false
expr_stmt|;
name|isNull
index|[
name|elementNum
index|]
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|vector
index|[
name|elementNum
index|]
operator|.
name|set
argument_list|(
name|hiveDec
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|set
parameter_list|(
name|int
name|elementNum
parameter_list|,
name|HiveDecimal
name|hiveDec
parameter_list|)
block|{
name|HiveDecimal
name|checkedDec
init|=
name|HiveDecimal
operator|.
name|enforcePrecisionScale
argument_list|(
name|hiveDec
argument_list|,
name|precision
argument_list|,
name|scale
argument_list|)
decl_stmt|;
if|if
condition|(
name|checkedDec
operator|==
literal|null
condition|)
block|{
name|noNulls
operator|=
literal|false
expr_stmt|;
name|isNull
index|[
name|elementNum
index|]
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|vector
index|[
name|elementNum
index|]
operator|.
name|set
argument_list|(
name|checkedDec
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|visit
parameter_list|(
name|ColumnVectorVisitor
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|v
operator|.
name|visit
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setNullDataValue
parameter_list|(
name|int
name|elementNum
parameter_list|)
block|{
comment|// E.g. For scale 2 the minimum is "0.01"
name|HiveDecimal
name|minimumNonZeroValue
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
name|BigInteger
operator|.
name|ONE
argument_list|,
name|scale
argument_list|)
decl_stmt|;
name|vector
index|[
name|elementNum
index|]
operator|.
name|set
argument_list|(
name|minimumNonZeroValue
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

