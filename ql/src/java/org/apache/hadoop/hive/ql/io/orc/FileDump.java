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
name|io
operator|.
name|orc
package|;
end_package

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
name|List
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
name|text
operator|.
name|DecimalFormat
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
name|conf
operator|.
name|Configuration
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|RowIndexEntry
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
name|sarg
operator|.
name|PredicateLeaf
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
name|sarg
operator|.
name|SearchArgument
operator|.
name|TruthValue
import|;
end_import

begin_comment
comment|/**  * A tool for printing out the file structure of ORC files.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|FileDump
block|{
specifier|private
specifier|static
specifier|final
name|String
name|ROWINDEX_PREFIX
init|=
literal|"--rowindex="
decl_stmt|;
comment|// not used
specifier|private
name|FileDump
parameter_list|()
block|{}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|rowIndexCols
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|arg
range|:
name|args
control|)
block|{
if|if
condition|(
name|arg
operator|.
name|startsWith
argument_list|(
literal|"--"
argument_list|)
condition|)
block|{
if|if
condition|(
name|arg
operator|.
name|startsWith
argument_list|(
name|ROWINDEX_PREFIX
argument_list|)
condition|)
block|{
name|String
index|[]
name|colStrs
init|=
name|arg
operator|.
name|substring
argument_list|(
name|ROWINDEX_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|rowIndexCols
operator|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|colStrs
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|colStr
range|:
name|colStrs
control|)
block|{
name|rowIndexCols
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|colStr
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Unknown argument "
operator|+
name|arg
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|files
operator|.
name|add
argument_list|(
name|arg
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|String
name|filename
range|:
name|files
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Structure for "
operator|+
name|filename
argument_list|)
expr_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|filename
argument_list|)
decl_stmt|;
name|Reader
name|reader
init|=
name|OrcFile
operator|.
name|createReader
argument_list|(
name|path
argument_list|,
name|OrcFile
operator|.
name|readerOptions
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|RecordReaderImpl
name|rows
init|=
operator|(
name|RecordReaderImpl
operator|)
name|reader
operator|.
name|rows
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Rows: "
operator|+
name|reader
operator|.
name|getNumberOfRows
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Compression: "
operator|+
name|reader
operator|.
name|getCompression
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|reader
operator|.
name|getCompression
argument_list|()
operator|!=
name|CompressionKind
operator|.
name|NONE
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Compression size: "
operator|+
name|reader
operator|.
name|getCompressionSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Type: "
operator|+
name|reader
operator|.
name|getObjectInspector
argument_list|()
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\nStripe Statistics:"
argument_list|)
expr_stmt|;
name|Metadata
name|metadata
init|=
name|reader
operator|.
name|getMetadata
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|n
init|=
literal|0
init|;
name|n
operator|<
name|metadata
operator|.
name|getStripeStatistics
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|n
operator|++
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"  Stripe "
operator|+
operator|(
name|n
operator|+
literal|1
operator|)
operator|+
literal|":"
argument_list|)
expr_stmt|;
name|StripeStatistics
name|ss
init|=
name|metadata
operator|.
name|getStripeStatistics
argument_list|()
operator|.
name|get
argument_list|(
name|n
argument_list|)
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
name|ss
operator|.
name|getColumnStatistics
argument_list|()
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"    Column "
operator|+
name|i
operator|+
literal|": "
operator|+
name|ss
operator|.
name|getColumnStatistics
argument_list|()
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|ColumnStatistics
index|[]
name|stats
init|=
name|reader
operator|.
name|getStatistics
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\nFile Statistics:"
argument_list|)
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
name|stats
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"  Column "
operator|+
name|i
operator|+
literal|": "
operator|+
name|stats
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\nStripes:"
argument_list|)
expr_stmt|;
name|int
name|stripeIx
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|StripeInformation
name|stripe
range|:
name|reader
operator|.
name|getStripes
argument_list|()
control|)
block|{
operator|++
name|stripeIx
expr_stmt|;
name|long
name|stripeStart
init|=
name|stripe
operator|.
name|getOffset
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"  Stripe: "
operator|+
name|stripe
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|OrcProto
operator|.
name|StripeFooter
name|footer
init|=
name|rows
operator|.
name|readStripeFooter
argument_list|(
name|stripe
argument_list|)
decl_stmt|;
name|long
name|sectionStart
init|=
name|stripeStart
decl_stmt|;
for|for
control|(
name|OrcProto
operator|.
name|Stream
name|section
range|:
name|footer
operator|.
name|getStreamsList
argument_list|()
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"    Stream: column "
operator|+
name|section
operator|.
name|getColumn
argument_list|()
operator|+
literal|" section "
operator|+
name|section
operator|.
name|getKind
argument_list|()
operator|+
literal|" start: "
operator|+
name|sectionStart
operator|+
literal|" length "
operator|+
name|section
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|sectionStart
operator|+=
name|section
operator|.
name|getLength
argument_list|()
expr_stmt|;
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
name|footer
operator|.
name|getColumnsCount
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|OrcProto
operator|.
name|ColumnEncoding
name|encoding
init|=
name|footer
operator|.
name|getColumns
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|"    Encoding column "
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|": "
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|encoding
operator|.
name|getKind
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|encoding
operator|.
name|getKind
argument_list|()
operator|==
name|OrcProto
operator|.
name|ColumnEncoding
operator|.
name|Kind
operator|.
name|DICTIONARY
operator|||
name|encoding
operator|.
name|getKind
argument_list|()
operator|==
name|OrcProto
operator|.
name|ColumnEncoding
operator|.
name|Kind
operator|.
name|DICTIONARY_V2
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|encoding
operator|.
name|getDictionarySize
argument_list|()
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|buf
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rowIndexCols
operator|!=
literal|null
condition|)
block|{
name|RowIndex
index|[]
name|indices
init|=
name|rows
operator|.
name|readRowIndex
argument_list|(
name|stripeIx
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|col
range|:
name|rowIndexCols
control|)
block|{
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|"    Row group index column "
argument_list|)
operator|.
name|append
argument_list|(
name|col
argument_list|)
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
name|RowIndex
name|index
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|(
name|col
operator|>=
name|indices
operator|.
name|length
operator|)
operator|||
operator|(
operator|(
name|index
operator|=
name|indices
index|[
name|col
index|]
operator|)
operator|==
literal|null
operator|)
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|" not found\n"
argument_list|)
expr_stmt|;
continue|continue;
block|}
for|for
control|(
name|int
name|entryIx
init|=
literal|0
init|;
name|entryIx
operator|<
name|index
operator|.
name|getEntryCount
argument_list|()
condition|;
operator|++
name|entryIx
control|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|"\n      Entry "
argument_list|)
operator|.
name|append
argument_list|(
name|entryIx
argument_list|)
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
name|RowIndexEntry
name|entry
init|=
name|index
operator|.
name|getEntry
argument_list|(
name|entryIx
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|"unknown\n"
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|OrcProto
operator|.
name|ColumnStatistics
name|colStats
init|=
name|entry
operator|.
name|getStatistics
argument_list|()
decl_stmt|;
if|if
condition|(
name|colStats
operator|==
literal|null
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|"no stats at "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ColumnStatistics
name|cs
init|=
name|ColumnStatisticsImpl
operator|.
name|deserialize
argument_list|(
name|colStats
argument_list|)
decl_stmt|;
name|Object
name|min
init|=
name|RecordReaderImpl
operator|.
name|getMin
argument_list|(
name|cs
argument_list|)
decl_stmt|,
name|max
init|=
name|RecordReaderImpl
operator|.
name|getMax
argument_list|(
name|cs
argument_list|)
decl_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|" count: "
argument_list|)
operator|.
name|append
argument_list|(
name|cs
operator|.
name|getNumberOfValues
argument_list|()
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|" min: "
argument_list|)
operator|.
name|append
argument_list|(
name|min
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|" max: "
argument_list|)
operator|.
name|append
argument_list|(
name|max
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|append
argument_list|(
literal|" positions: "
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|posIx
init|=
literal|0
init|;
name|posIx
operator|<
name|entry
operator|.
name|getPositionsCount
argument_list|()
condition|;
operator|++
name|posIx
control|)
block|{
if|if
condition|(
name|posIx
operator|!=
literal|0
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getPositions
argument_list|(
name|posIx
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|buf
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|long
name|fileLen
init|=
name|fs
operator|.
name|getContentSummary
argument_list|(
name|path
argument_list|)
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|long
name|paddedBytes
init|=
name|getTotalPaddingSize
argument_list|(
name|reader
argument_list|)
decl_stmt|;
comment|// empty ORC file is ~45 bytes. Assumption here is file length always>0
name|double
name|percentPadding
init|=
operator|(
operator|(
name|double
operator|)
name|paddedBytes
operator|/
operator|(
name|double
operator|)
name|fileLen
operator|)
operator|*
literal|100
decl_stmt|;
name|DecimalFormat
name|format
init|=
operator|new
name|DecimalFormat
argument_list|(
literal|"##.##"
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\nFile length: "
operator|+
name|fileLen
operator|+
literal|" bytes"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Padding length: "
operator|+
name|paddedBytes
operator|+
literal|" bytes"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Padding ratio: "
operator|+
name|format
operator|.
name|format
argument_list|(
name|percentPadding
argument_list|)
operator|+
literal|"%"
argument_list|)
expr_stmt|;
name|rows
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|long
name|getTotalPaddingSize
parameter_list|(
name|Reader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|paddedBytes
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
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
argument_list|>
name|stripes
init|=
name|reader
operator|.
name|getStripes
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|stripes
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|long
name|prevStripeOffset
init|=
name|stripes
operator|.
name|get
argument_list|(
name|i
operator|-
literal|1
argument_list|)
operator|.
name|getOffset
argument_list|()
decl_stmt|;
name|long
name|prevStripeLen
init|=
name|stripes
operator|.
name|get
argument_list|(
name|i
operator|-
literal|1
argument_list|)
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|paddedBytes
operator|+=
name|stripes
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getOffset
argument_list|()
operator|-
operator|(
name|prevStripeOffset
operator|+
name|prevStripeLen
operator|)
expr_stmt|;
block|}
return|return
name|paddedBytes
return|;
block|}
block|}
end_class

end_unit

