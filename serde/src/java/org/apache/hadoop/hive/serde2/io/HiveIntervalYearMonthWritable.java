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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|HiveIntervalYearMonth
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
name|ByteStream
operator|.
name|RandomAccessOutput
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
name|lazybinary
operator|.
name|LazyBinaryUtils
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
name|lazybinary
operator|.
name|LazyBinaryUtils
operator|.
name|VInt
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
name|HiveIntervalYearMonthWritable
implements|implements
name|WritableComparable
argument_list|<
name|HiveIntervalYearMonthWritable
argument_list|>
block|{
specifier|static
specifier|final
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HiveIntervalYearMonthWritable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|HiveIntervalYearMonth
name|intervalValue
init|=
operator|new
name|HiveIntervalYearMonth
argument_list|()
decl_stmt|;
specifier|public
name|HiveIntervalYearMonthWritable
parameter_list|()
block|{   }
specifier|public
name|HiveIntervalYearMonthWritable
parameter_list|(
name|HiveIntervalYearMonth
name|hiveInterval
parameter_list|)
block|{
name|intervalValue
operator|.
name|set
argument_list|(
name|hiveInterval
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveIntervalYearMonthWritable
parameter_list|(
name|HiveIntervalYearMonthWritable
name|hiveIntervalWritable
parameter_list|)
block|{
name|intervalValue
operator|.
name|set
argument_list|(
name|hiveIntervalWritable
operator|.
name|intervalValue
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|int
name|years
parameter_list|,
name|int
name|months
parameter_list|)
block|{
name|intervalValue
operator|.
name|set
argument_list|(
name|years
argument_list|,
name|months
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|HiveIntervalYearMonth
name|hiveInterval
parameter_list|)
block|{
name|intervalValue
operator|.
name|set
argument_list|(
name|hiveInterval
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|HiveIntervalYearMonthWritable
name|hiveIntervalWritable
parameter_list|)
block|{
name|intervalValue
operator|.
name|set
argument_list|(
name|hiveIntervalWritable
operator|.
name|intervalValue
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|int
name|totalMonths
parameter_list|)
block|{
name|intervalValue
operator|.
name|set
argument_list|(
name|totalMonths
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveIntervalYearMonth
name|getHiveIntervalYearMonth
parameter_list|()
block|{
return|return
operator|new
name|HiveIntervalYearMonth
argument_list|(
name|intervalValue
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
comment|// read totalMonths from DataInput
name|set
argument_list|(
name|WritableUtils
operator|.
name|readVInt
argument_list|(
name|in
argument_list|)
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
comment|// write totalMonths to DataOutput
name|WritableUtils
operator|.
name|writeVInt
argument_list|(
name|out
argument_list|,
name|intervalValue
operator|.
name|getTotalMonths
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|writeToByteStream
parameter_list|(
name|RandomAccessOutput
name|byteStream
parameter_list|)
block|{
name|LazyBinaryUtils
operator|.
name|writeVInt
argument_list|(
name|byteStream
argument_list|,
name|intervalValue
operator|.
name|getTotalMonths
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setFromBytes
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
parameter_list|,
name|VInt
name|vInt
parameter_list|)
block|{
name|LazyBinaryUtils
operator|.
name|readVInt
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|vInt
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|length
operator|==
name|vInt
operator|.
name|length
operator|)
assert|;
name|set
argument_list|(
name|vInt
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|HiveIntervalYearMonthWritable
name|other
parameter_list|)
block|{
return|return
name|this
operator|.
name|intervalValue
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|intervalValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|HiveIntervalYearMonthWritable
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|0
operator|==
name|compareTo
argument_list|(
operator|(
name|HiveIntervalYearMonthWritable
operator|)
name|obj
argument_list|)
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
name|intervalValue
operator|.
name|hashCode
argument_list|()
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
name|intervalValue
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

