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
name|llap
operator|.
name|io
operator|.
name|metadata
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|io
operator|.
name|orc
operator|.
name|OrcProto
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
name|io
operator|.
name|orc
operator|.
name|OrcProto
operator|.
name|ColumnEncoding
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
name|io
operator|.
name|orc
operator|.
name|OrcProto
operator|.
name|RowIndex
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
name|io
operator|.
name|orc
operator|.
name|OrcProto
operator|.
name|Stream
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
name|io
operator|.
name|orc
operator|.
name|RecordReader
import|;
end_import

begin_class
specifier|public
class|class
name|OrcStripeMetadata
block|{
name|List
argument_list|<
name|ColumnEncoding
argument_list|>
name|encodings
decl_stmt|;
name|List
argument_list|<
name|Stream
argument_list|>
name|streams
decl_stmt|;
name|RowIndex
index|[]
name|rowIndexes
decl_stmt|;
specifier|public
name|OrcStripeMetadata
parameter_list|(
name|RecordReader
name|reader
parameter_list|,
name|boolean
index|[]
name|includes
parameter_list|)
throws|throws
name|IOException
block|{
name|rowIndexes
operator|=
operator|new
name|OrcProto
operator|.
name|RowIndex
index|[
name|includes
operator|.
name|length
index|]
expr_stmt|;
name|reader
operator|.
name|getCurrentRowIndexEntries
argument_list|(
name|includes
argument_list|,
name|rowIndexes
argument_list|)
expr_stmt|;
name|streams
operator|=
name|reader
operator|.
name|getCurrentStreams
argument_list|()
expr_stmt|;
name|encodings
operator|=
name|reader
operator|.
name|getCurrentColumnEncodings
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasAllIndexes
parameter_list|(
name|boolean
index|[]
name|includes
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|includes
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|includes
index|[
name|i
index|]
operator|&&
name|rowIndexes
index|[
name|i
index|]
operator|==
literal|null
condition|)
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|void
name|loadMissingIndexes
parameter_list|(
name|RecordReader
name|reader
parameter_list|,
name|boolean
index|[]
name|includes
parameter_list|)
throws|throws
name|IOException
block|{
name|reader
operator|.
name|getCurrentRowIndexEntries
argument_list|(
name|includes
argument_list|,
name|rowIndexes
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RowIndex
index|[]
name|getRowIndexes
parameter_list|()
block|{
return|return
name|rowIndexes
return|;
block|}
specifier|public
name|void
name|setRowIndexes
parameter_list|(
name|RowIndex
index|[]
name|rowIndexes
parameter_list|)
block|{
name|this
operator|.
name|rowIndexes
operator|=
name|rowIndexes
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|ColumnEncoding
argument_list|>
name|getEncodings
parameter_list|()
block|{
return|return
name|encodings
return|;
block|}
specifier|public
name|void
name|setEncodings
parameter_list|(
name|List
argument_list|<
name|ColumnEncoding
argument_list|>
name|encodings
parameter_list|)
block|{
name|this
operator|.
name|encodings
operator|=
name|encodings
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Stream
argument_list|>
name|getStreams
parameter_list|()
block|{
return|return
name|streams
return|;
block|}
specifier|public
name|void
name|setStreams
parameter_list|(
name|List
argument_list|<
name|Stream
argument_list|>
name|streams
parameter_list|)
block|{
name|this
operator|.
name|streams
operator|=
name|streams
expr_stmt|;
block|}
block|}
end_class

end_unit

