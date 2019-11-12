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
name|hive
operator|.
name|storage
operator|.
name|jdbc
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
name|sql
operator|.
name|PreparedStatement
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|ResultSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
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

begin_comment
comment|/**  * DBRecordWritable writes serialized row by row data to the underlying database.  */
end_comment

begin_class
specifier|public
class|class
name|DBRecordWritable
implements|implements
name|Writable
implements|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|lib
operator|.
name|db
operator|.
name|DBWritable
block|{
specifier|private
name|Object
index|[]
name|columnValues
decl_stmt|;
specifier|public
name|DBRecordWritable
parameter_list|()
block|{   }
specifier|public
name|DBRecordWritable
parameter_list|(
name|int
name|numColumns
parameter_list|)
block|{
name|this
operator|.
name|columnValues
operator|=
operator|new
name|Object
index|[
name|numColumns
index|]
expr_stmt|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|columnValues
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|int
name|i
parameter_list|,
name|Object
name|columnObject
parameter_list|)
block|{
name|columnValues
index|[
name|i
index|]
operator|=
name|columnObject
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|ResultSet
name|rs
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// do nothing
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|PreparedStatement
name|statement
parameter_list|)
throws|throws
name|SQLException
block|{
if|if
condition|(
name|columnValues
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"No data available to be written"
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|columnValues
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|statement
operator|.
name|setObject
argument_list|(
name|i
operator|+
literal|1
argument_list|,
name|columnValues
index|[
name|i
index|]
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
name|in
parameter_list|)
throws|throws
name|IOException
block|{
comment|// do nothing
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
comment|// do nothing
block|}
block|}
end_class

end_unit

