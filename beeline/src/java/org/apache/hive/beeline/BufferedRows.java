begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/*  * This source file is based on code taken from SQLLine 1.0.2  * See SQLLine notice in LICENSE  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|beeline
package|;
end_package

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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Optional
import|;
end_import

begin_comment
comment|/**  * Rows implementation which buffers all rows  */
end_comment

begin_class
class|class
name|BufferedRows
extends|extends
name|Rows
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|Row
argument_list|>
name|list
decl_stmt|;
specifier|private
specifier|final
name|Iterator
argument_list|<
name|Row
argument_list|>
name|iterator
decl_stmt|;
specifier|private
name|int
name|columnCount
decl_stmt|;
specifier|private
name|int
name|maxColumnWidth
decl_stmt|;
name|BufferedRows
parameter_list|(
name|BeeLine
name|beeLine
parameter_list|,
name|ResultSet
name|rs
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
argument_list|(
name|beeLine
argument_list|,
name|rs
argument_list|,
name|Optional
operator|.
expr|<
name|Integer
operator|>
name|absent
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|BufferedRows
parameter_list|(
name|BeeLine
name|beeLine
parameter_list|,
name|ResultSet
name|rs
parameter_list|,
name|Optional
argument_list|<
name|Integer
argument_list|>
name|limit
parameter_list|)
throws|throws
name|SQLException
block|{
name|super
argument_list|(
name|beeLine
argument_list|,
name|rs
argument_list|)
expr_stmt|;
name|list
operator|=
operator|new
name|ArrayList
argument_list|<
name|Row
argument_list|>
argument_list|()
expr_stmt|;
name|columnCount
operator|=
name|rsMeta
operator|.
name|getColumnCount
argument_list|()
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
operator|new
name|Row
argument_list|(
name|columnCount
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|numRowsBuffered
init|=
literal|0
decl_stmt|;
name|int
name|maxRowsBuffered
init|=
name|limit
operator|.
name|or
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
while|while
condition|(
name|numRowsBuffered
operator|++
operator|<
name|maxRowsBuffered
operator|&&
name|rs
operator|.
name|next
argument_list|()
condition|)
block|{
name|this
operator|.
name|list
operator|.
name|add
argument_list|(
operator|new
name|Row
argument_list|(
name|columnCount
argument_list|,
name|rs
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|iterator
operator|=
name|list
operator|.
name|iterator
argument_list|()
expr_stmt|;
name|maxColumnWidth
operator|=
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|getMaxColumnWidth
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|iterator
operator|.
name|hasNext
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|next
parameter_list|()
block|{
return|return
name|iterator
operator|.
name|next
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
name|list
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
name|void
name|normalizeWidths
parameter_list|()
block|{
if|if
condition|(
operator|!
name|list
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|int
index|[]
name|max
init|=
operator|new
name|int
index|[
name|columnCount
index|]
decl_stmt|;
for|for
control|(
name|Row
name|row
range|:
name|list
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|columnCount
condition|;
name|j
operator|++
control|)
block|{
comment|// if the max column width is too large, reset it to max allowed Column width
name|max
index|[
name|j
index|]
operator|=
name|Math
operator|.
name|min
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|max
index|[
name|j
index|]
argument_list|,
name|row
operator|.
name|sizes
index|[
name|j
index|]
operator|+
literal|1
argument_list|)
argument_list|,
name|maxColumnWidth
argument_list|)
expr_stmt|;
block|}
name|row
operator|.
name|sizes
operator|=
name|max
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

