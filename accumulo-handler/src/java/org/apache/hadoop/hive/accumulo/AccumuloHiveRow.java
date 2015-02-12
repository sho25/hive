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
name|accumulo
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|builder
operator|.
name|HashCodeBuilder
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Holds column tuples for rowID. Each tuple contains column family label, qualifier label, and byte  * array value.  */
end_comment

begin_class
specifier|public
class|class
name|AccumuloHiveRow
implements|implements
name|Writable
block|{
specifier|private
name|String
name|rowId
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ColumnTuple
argument_list|>
name|tuples
init|=
operator|new
name|ArrayList
argument_list|<
name|ColumnTuple
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|AccumuloHiveRow
parameter_list|()
block|{}
specifier|public
name|AccumuloHiveRow
parameter_list|(
name|String
name|rowId
parameter_list|)
block|{
name|this
operator|.
name|rowId
operator|=
name|rowId
expr_stmt|;
block|}
specifier|public
name|void
name|setRowId
parameter_list|(
name|String
name|rowId
parameter_list|)
block|{
name|this
operator|.
name|rowId
operator|=
name|rowId
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|ColumnTuple
argument_list|>
name|getTuples
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|tuples
argument_list|)
return|;
block|}
comment|/**    * @return true if this instance has a tuple containing fam and qual, false otherwise.    */
specifier|public
name|boolean
name|hasFamAndQual
parameter_list|(
name|Text
name|fam
parameter_list|,
name|Text
name|qual
parameter_list|)
block|{
for|for
control|(
name|ColumnTuple
name|tuple
range|:
name|tuples
control|)
block|{
if|if
condition|(
name|tuple
operator|.
name|getCf
argument_list|()
operator|.
name|equals
argument_list|(
name|fam
argument_list|)
operator|&&
name|tuple
operator|.
name|getCq
argument_list|()
operator|.
name|equals
argument_list|(
name|qual
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * @return byte [] value for first tuple containing fam and qual or null if no match.    */
specifier|public
name|byte
index|[]
name|getValue
parameter_list|(
name|Text
name|fam
parameter_list|,
name|Text
name|qual
parameter_list|)
block|{
for|for
control|(
name|ColumnTuple
name|tuple
range|:
name|tuples
control|)
block|{
if|if
condition|(
name|tuple
operator|.
name|getCf
argument_list|()
operator|.
name|equals
argument_list|(
name|fam
argument_list|)
operator|&&
name|tuple
operator|.
name|getCq
argument_list|()
operator|.
name|equals
argument_list|(
name|qual
argument_list|)
condition|)
block|{
return|return
name|tuple
operator|.
name|getValue
argument_list|()
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|String
name|getRowId
parameter_list|()
block|{
return|return
name|rowId
return|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|this
operator|.
name|rowId
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|tuples
operator|=
operator|new
name|ArrayList
argument_list|<
name|ColumnTuple
argument_list|>
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"AccumuloHiveRow{"
argument_list|)
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"rowId='"
argument_list|)
operator|.
name|append
argument_list|(
name|rowId
argument_list|)
operator|.
name|append
argument_list|(
literal|"', tuples: "
argument_list|)
expr_stmt|;
for|for
control|(
name|ColumnTuple
name|tuple
range|:
name|tuples
control|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|tuple
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
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
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|instanceof
name|AccumuloHiveRow
condition|)
block|{
name|AccumuloHiveRow
name|other
init|=
operator|(
name|AccumuloHiveRow
operator|)
name|o
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|rowId
condition|)
block|{
if|if
condition|(
literal|null
operator|!=
name|other
operator|.
name|rowId
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|rowId
operator|.
name|equals
argument_list|(
name|other
operator|.
name|rowId
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|tuples
operator|.
name|equals
argument_list|(
name|other
operator|.
name|tuples
argument_list|)
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|dataOutput
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
literal|null
operator|!=
name|rowId
condition|)
block|{
name|dataOutput
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|dataOutput
operator|.
name|writeUTF
argument_list|(
name|rowId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|dataOutput
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|int
name|size
init|=
name|tuples
operator|.
name|size
argument_list|()
decl_stmt|;
name|dataOutput
operator|.
name|writeInt
argument_list|(
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|ColumnTuple
name|tuple
range|:
name|tuples
control|)
block|{
name|Text
name|cf
init|=
name|tuple
operator|.
name|getCf
argument_list|()
decl_stmt|,
name|cq
init|=
name|tuple
operator|.
name|getCq
argument_list|()
decl_stmt|;
name|dataOutput
operator|.
name|writeInt
argument_list|(
name|cf
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|dataOutput
operator|.
name|write
argument_list|(
name|cf
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|cf
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|dataOutput
operator|.
name|writeInt
argument_list|(
name|cq
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|dataOutput
operator|.
name|write
argument_list|(
name|cq
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|cq
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|value
init|=
name|tuple
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|dataOutput
operator|.
name|writeInt
argument_list|(
name|value
operator|.
name|length
argument_list|)
expr_stmt|;
name|dataOutput
operator|.
name|write
argument_list|(
name|value
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
name|dataInput
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|dataInput
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|rowId
operator|=
name|dataInput
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
name|int
name|size
init|=
name|dataInput
operator|.
name|readInt
argument_list|()
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|int
name|cfLength
init|=
name|dataInput
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|cfData
init|=
operator|new
name|byte
index|[
name|cfLength
index|]
decl_stmt|;
name|dataInput
operator|.
name|readFully
argument_list|(
name|cfData
argument_list|,
literal|0
argument_list|,
name|cfLength
argument_list|)
expr_stmt|;
name|Text
name|cf
init|=
operator|new
name|Text
argument_list|(
name|cfData
argument_list|)
decl_stmt|;
name|int
name|cqLength
init|=
name|dataInput
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|cqData
init|=
operator|new
name|byte
index|[
name|cqLength
index|]
decl_stmt|;
name|dataInput
operator|.
name|readFully
argument_list|(
name|cqData
argument_list|,
literal|0
argument_list|,
name|cqLength
argument_list|)
expr_stmt|;
name|Text
name|cq
init|=
operator|new
name|Text
argument_list|(
name|cqData
argument_list|)
decl_stmt|;
name|int
name|valSize
init|=
name|dataInput
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|val
init|=
operator|new
name|byte
index|[
name|valSize
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|valSize
condition|;
name|j
operator|++
control|)
block|{
name|val
index|[
name|j
index|]
operator|=
name|dataInput
operator|.
name|readByte
argument_list|()
expr_stmt|;
block|}
name|tuples
operator|.
name|add
argument_list|(
operator|new
name|ColumnTuple
argument_list|(
name|cf
argument_list|,
name|cq
argument_list|,
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|add
parameter_list|(
name|String
name|cf
parameter_list|,
name|String
name|qual
parameter_list|,
name|byte
index|[]
name|val
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|cf
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|qual
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|val
argument_list|)
expr_stmt|;
name|add
argument_list|(
operator|new
name|Text
argument_list|(
name|cf
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
name|qual
argument_list|)
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|add
parameter_list|(
name|Text
name|cf
parameter_list|,
name|Text
name|qual
parameter_list|,
name|byte
index|[]
name|val
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|cf
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|qual
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|val
argument_list|)
expr_stmt|;
name|tuples
operator|.
name|add
argument_list|(
operator|new
name|ColumnTuple
argument_list|(
name|cf
argument_list|,
name|qual
argument_list|,
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|ColumnTuple
block|{
specifier|private
specifier|final
name|Text
name|cf
decl_stmt|;
specifier|private
specifier|final
name|Text
name|cq
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|value
decl_stmt|;
specifier|public
name|ColumnTuple
parameter_list|(
name|Text
name|cf
parameter_list|,
name|Text
name|cq
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
name|this
operator|.
name|cf
operator|=
name|cf
expr_stmt|;
name|this
operator|.
name|cq
operator|=
name|cq
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
specifier|public
name|Text
name|getCf
parameter_list|()
block|{
return|return
name|cf
return|;
block|}
specifier|public
name|Text
name|getCq
parameter_list|()
block|{
return|return
name|cq
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|HashCodeBuilder
name|hcb
init|=
operator|new
name|HashCodeBuilder
argument_list|(
literal|9683
argument_list|,
literal|68783
argument_list|)
decl_stmt|;
return|return
name|hcb
operator|.
name|append
argument_list|(
name|cf
argument_list|)
operator|.
name|append
argument_list|(
name|cq
argument_list|)
operator|.
name|append
argument_list|(
name|value
argument_list|)
operator|.
name|toHashCode
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
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|instanceof
name|ColumnTuple
condition|)
block|{
name|ColumnTuple
name|other
init|=
operator|(
name|ColumnTuple
operator|)
name|o
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|cf
condition|)
block|{
if|if
condition|(
literal|null
operator|!=
name|other
operator|.
name|cf
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|cf
operator|.
name|equals
argument_list|(
name|other
operator|.
name|cf
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
literal|null
operator|==
name|cq
condition|)
block|{
if|if
condition|(
literal|null
operator|!=
name|other
operator|.
name|cq
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|cq
operator|.
name|equals
argument_list|(
name|other
operator|.
name|cq
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
literal|null
operator|==
name|value
condition|)
block|{
if|if
condition|(
literal|null
operator|!=
name|other
operator|.
name|value
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
name|Arrays
operator|.
name|equals
argument_list|(
name|value
argument_list|,
name|other
operator|.
name|value
argument_list|)
return|;
block|}
return|return
literal|false
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
name|cf
operator|+
literal|" "
operator|+
name|cq
operator|+
literal|" "
operator|+
operator|new
name|String
argument_list|(
name|value
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

