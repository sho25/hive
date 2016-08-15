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
name|HiveBaseChar
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
name|HiveVarchar
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

begin_class
specifier|public
class|class
name|HiveVarcharWritable
extends|extends
name|HiveBaseCharWritable
implements|implements
name|WritableComparable
argument_list|<
name|HiveVarcharWritable
argument_list|>
block|{
specifier|public
name|HiveVarcharWritable
parameter_list|()
block|{   }
specifier|public
name|HiveVarcharWritable
parameter_list|(
name|HiveVarchar
name|hc
parameter_list|)
block|{
name|set
argument_list|(
name|hc
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveVarcharWritable
parameter_list|(
name|HiveVarcharWritable
name|hcw
parameter_list|)
block|{
name|set
argument_list|(
name|hcw
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|HiveVarchar
name|val
parameter_list|)
block|{
name|set
argument_list|(
name|val
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|String
name|val
parameter_list|)
block|{
name|set
argument_list|(
name|val
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// copy entire string value
block|}
specifier|public
name|void
name|set
parameter_list|(
name|HiveVarcharWritable
name|val
parameter_list|)
block|{
name|value
operator|.
name|set
argument_list|(
name|val
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|HiveVarcharWritable
name|val
parameter_list|,
name|int
name|maxLength
parameter_list|)
block|{
name|set
argument_list|(
name|val
operator|.
name|getHiveVarchar
argument_list|()
argument_list|,
name|maxLength
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|HiveVarchar
name|val
parameter_list|,
name|int
name|len
parameter_list|)
block|{
name|set
argument_list|(
name|val
operator|.
name|getValue
argument_list|()
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|String
name|val
parameter_list|,
name|int
name|maxLength
parameter_list|)
block|{
name|value
operator|.
name|set
argument_list|(
name|HiveBaseChar
operator|.
name|enforceMaxLength
argument_list|(
name|val
argument_list|,
name|maxLength
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveVarchar
name|getHiveVarchar
parameter_list|()
block|{
return|return
operator|new
name|HiveVarchar
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
specifier|public
name|void
name|enforceMaxLength
parameter_list|(
name|int
name|maxLength
parameter_list|)
block|{
comment|// Might be possible to truncate the existing Text value, for now just do something simple.
if|if
condition|(
name|value
operator|.
name|getLength
argument_list|()
operator|>
name|maxLength
operator|&&
name|getCharacterLength
argument_list|()
operator|>
name|maxLength
condition|)
name|set
argument_list|(
name|getHiveVarchar
argument_list|()
argument_list|,
name|maxLength
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|HiveVarcharWritable
name|rhs
parameter_list|)
block|{
return|return
name|value
operator|.
name|compareTo
argument_list|(
name|rhs
operator|.
name|value
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
name|value
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

