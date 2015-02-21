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
name|MetadataReader
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
name|OrcProto
operator|.
name|StripeFooter
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
name|RecordReaderImpl
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
name|StripeInformation
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
name|RecordReaderImpl
operator|.
name|Index
name|rowIndex
decl_stmt|;
specifier|public
name|OrcStripeMetadata
parameter_list|(
name|MetadataReader
name|mr
parameter_list|,
name|StripeInformation
name|stripe
parameter_list|,
name|boolean
index|[]
name|includes
parameter_list|,
name|boolean
index|[]
name|sargColumns
parameter_list|)
throws|throws
name|IOException
block|{
name|StripeFooter
name|footer
init|=
name|mr
operator|.
name|readStripeFooter
argument_list|(
name|stripe
argument_list|)
decl_stmt|;
name|streams
operator|=
name|footer
operator|.
name|getStreamsList
argument_list|()
expr_stmt|;
name|encodings
operator|=
name|footer
operator|.
name|getColumnsList
argument_list|()
expr_stmt|;
name|rowIndex
operator|=
name|mr
operator|.
name|readRowIndex
argument_list|(
name|stripe
argument_list|,
name|footer
argument_list|,
name|includes
argument_list|,
literal|null
argument_list|,
name|sargColumns
argument_list|,
literal|null
argument_list|)
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
name|rowIndex
operator|.
name|getRowGroupIndex
argument_list|()
index|[
name|i
index|]
operator|==
literal|null
condition|)
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|void
name|loadMissingIndexes
parameter_list|(
name|MetadataReader
name|mr
parameter_list|,
name|StripeInformation
name|stripe
parameter_list|,
name|boolean
index|[]
name|includes
parameter_list|,
name|boolean
index|[]
name|sargColumns
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO: should we save footer to avoid a read here?
name|rowIndex
operator|=
name|mr
operator|.
name|readRowIndex
argument_list|(
name|stripe
argument_list|,
literal|null
argument_list|,
name|includes
argument_list|,
name|rowIndex
operator|.
name|getRowGroupIndex
argument_list|()
argument_list|,
name|sargColumns
argument_list|,
name|rowIndex
operator|.
name|getBloomFilterIndex
argument_list|()
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
name|rowIndex
operator|.
name|getRowGroupIndex
argument_list|()
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
name|rowIndex
operator|.
name|setRowGroupIndex
argument_list|(
name|rowIndexes
argument_list|)
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

