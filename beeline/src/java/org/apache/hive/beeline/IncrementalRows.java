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
name|NoSuchElementException
import|;
end_import

begin_comment
comment|/**  * Rows implementation which returns rows incrementally from result set  * without any buffering.  */
end_comment

begin_class
specifier|public
class|class
name|IncrementalRows
extends|extends
name|Rows
block|{
specifier|protected
specifier|final
name|ResultSet
name|rs
decl_stmt|;
specifier|private
specifier|final
name|Row
name|labelRow
decl_stmt|;
specifier|private
specifier|final
name|Row
name|maxRow
decl_stmt|;
specifier|private
name|Row
name|nextRow
decl_stmt|;
specifier|private
name|boolean
name|endOfResult
decl_stmt|;
specifier|protected
name|boolean
name|normalizingWidths
decl_stmt|;
name|IncrementalRows
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
name|super
argument_list|(
name|beeLine
argument_list|,
name|rs
argument_list|)
expr_stmt|;
name|this
operator|.
name|rs
operator|=
name|rs
expr_stmt|;
name|labelRow
operator|=
operator|new
name|Row
argument_list|(
name|rsMeta
operator|.
name|getColumnCount
argument_list|()
argument_list|)
expr_stmt|;
name|maxRow
operator|=
operator|new
name|Row
argument_list|(
name|rsMeta
operator|.
name|getColumnCount
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|maxWidth
init|=
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|getMaxColumnWidth
argument_list|()
decl_stmt|;
comment|// pre-compute normalization so we don't have to deal
comment|// with SQLExceptions later
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|maxRow
operator|.
name|sizes
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
comment|// normalized display width is based on maximum of display size
comment|// and label size
name|maxRow
operator|.
name|sizes
index|[
name|i
index|]
operator|=
name|Math
operator|.
name|max
argument_list|(
name|maxRow
operator|.
name|sizes
index|[
name|i
index|]
argument_list|,
name|rsMeta
operator|.
name|getColumnDisplaySize
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|maxRow
operator|.
name|sizes
index|[
name|i
index|]
operator|=
name|Math
operator|.
name|min
argument_list|(
name|maxWidth
argument_list|,
name|maxRow
operator|.
name|sizes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|nextRow
operator|=
name|labelRow
expr_stmt|;
name|endOfResult
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
if|if
condition|(
name|endOfResult
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|nextRow
operator|==
literal|null
condition|)
block|{
try|try
block|{
if|if
condition|(
name|rs
operator|.
name|next
argument_list|()
condition|)
block|{
name|nextRow
operator|=
operator|new
name|Row
argument_list|(
name|labelRow
operator|.
name|sizes
operator|.
name|length
argument_list|,
name|rs
argument_list|)
expr_stmt|;
if|if
condition|(
name|normalizingWidths
condition|)
block|{
comment|// perform incremental normalization
name|nextRow
operator|.
name|sizes
operator|=
name|labelRow
operator|.
name|sizes
expr_stmt|;
block|}
block|}
else|else
block|{
name|endOfResult
operator|=
literal|true
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SQLException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ex
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
return|return
operator|(
name|nextRow
operator|!=
literal|null
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|next
parameter_list|()
block|{
if|if
condition|(
operator|!
name|hasNext
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|()
throw|;
block|}
name|Object
name|ret
init|=
name|nextRow
decl_stmt|;
name|nextRow
operator|=
literal|null
expr_stmt|;
return|return
name|ret
return|;
block|}
annotation|@
name|Override
name|void
name|normalizeWidths
parameter_list|()
block|{
comment|// normalize label row
name|labelRow
operator|.
name|sizes
operator|=
name|maxRow
operator|.
name|sizes
expr_stmt|;
comment|// and remind ourselves to perform incremental normalization
comment|// for each row as it is produced
name|normalizingWidths
operator|=
literal|true
expr_stmt|;
block|}
block|}
end_class

end_unit

