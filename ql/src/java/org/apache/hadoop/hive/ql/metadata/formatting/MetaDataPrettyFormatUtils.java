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
name|metadata
operator|.
name|formatting
package|;
end_package

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
name|util
operator|.
name|StringTokenizer
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
name|StringEscapeUtils
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
name|StringUtils
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
import|;
end_import

begin_comment
comment|/**  * This class provides methods to format the output of DESCRIBE PRETTY  * in a human-readable way.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|MetaDataPrettyFormatUtils
block|{
specifier|public
specifier|static
specifier|final
name|int
name|PRETTY_MAX_INTERCOL_SPACING
init|=
literal|4
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|PRETTY_ALIGNMENT
init|=
literal|10
decl_stmt|;
comment|/**    * Minimum length of the comment column. This is relevant only when the terminal width    * or hive.cli.pretty.output.num.cols is too small, or when there are very large column    * names.    * 10 was arbitrarily chosen.    */
specifier|private
specifier|static
specifier|final
name|int
name|MIN_COMMENT_COLUMN_LEN
init|=
literal|10
decl_stmt|;
specifier|private
name|MetaDataPrettyFormatUtils
parameter_list|()
block|{   }
comment|/**    * @param prettyOutputNumCols The pretty output is formatted to fit within    * these many columns.    */
specifier|public
specifier|static
name|String
name|getAllColumnsInformation
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
parameter_list|,
name|int
name|prettyOutputNumCols
parameter_list|)
block|{
name|StringBuilder
name|columnInformation
init|=
operator|new
name|StringBuilder
argument_list|(
name|MetaDataFormatUtils
operator|.
name|DEFAULT_STRINGBUILDER_SIZE
argument_list|)
decl_stmt|;
name|int
name|maxColNameLen
init|=
name|findMaxColumnNameLen
argument_list|(
name|cols
argument_list|)
decl_stmt|;
name|formatColumnsHeaderPretty
argument_list|(
name|columnInformation
argument_list|,
name|maxColNameLen
argument_list|,
name|prettyOutputNumCols
argument_list|)
expr_stmt|;
name|formatAllFieldsPretty
argument_list|(
name|columnInformation
argument_list|,
name|cols
argument_list|,
name|maxColNameLen
argument_list|,
name|prettyOutputNumCols
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|partCols
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|partCols
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|columnInformation
operator|.
name|append
argument_list|(
name|MetaDataFormatUtils
operator|.
name|LINE_DELIM
argument_list|)
operator|.
name|append
argument_list|(
literal|"# Partition Information"
argument_list|)
operator|.
name|append
argument_list|(
name|MetaDataFormatUtils
operator|.
name|LINE_DELIM
argument_list|)
expr_stmt|;
name|formatColumnsHeaderPretty
argument_list|(
name|columnInformation
argument_list|,
name|maxColNameLen
argument_list|,
name|prettyOutputNumCols
argument_list|)
expr_stmt|;
name|formatAllFieldsPretty
argument_list|(
name|columnInformation
argument_list|,
name|partCols
argument_list|,
name|maxColNameLen
argument_list|,
name|prettyOutputNumCols
argument_list|)
expr_stmt|;
block|}
return|return
name|columnInformation
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Find the length of the largest column name.    */
specifier|private
specifier|static
name|int
name|findMaxColumnNameLen
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|)
block|{
name|int
name|maxLen
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|FieldSchema
name|col
range|:
name|cols
control|)
block|{
name|int
name|colNameLen
init|=
name|col
operator|.
name|getName
argument_list|()
operator|.
name|length
argument_list|()
decl_stmt|;
if|if
condition|(
name|colNameLen
operator|>
name|maxLen
condition|)
block|{
name|maxLen
operator|=
name|colNameLen
expr_stmt|;
block|}
block|}
return|return
name|maxLen
return|;
block|}
comment|/**    * @param maxColNameLen The length of the largest column name    */
specifier|private
specifier|static
name|void
name|formatColumnsHeaderPretty
parameter_list|(
name|StringBuilder
name|columnInformation
parameter_list|,
name|int
name|maxColNameLen
parameter_list|,
name|int
name|prettyOutputNumCols
parameter_list|)
block|{
name|String
name|columnHeaders
index|[]
init|=
name|MetaDataFormatUtils
operator|.
name|getColumnsHeader
argument_list|()
decl_stmt|;
name|formatOutputPretty
argument_list|(
name|columnHeaders
index|[
literal|0
index|]
argument_list|,
name|columnHeaders
index|[
literal|1
index|]
argument_list|,
name|columnHeaders
index|[
literal|2
index|]
argument_list|,
name|columnInformation
argument_list|,
name|maxColNameLen
argument_list|,
name|prettyOutputNumCols
argument_list|)
expr_stmt|;
name|columnInformation
operator|.
name|append
argument_list|(
name|MetaDataFormatUtils
operator|.
name|LINE_DELIM
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|formatAllFieldsPretty
parameter_list|(
name|StringBuilder
name|tableInfo
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|,
name|int
name|maxColNameLen
parameter_list|,
name|int
name|prettyOutputNumCols
parameter_list|)
block|{
for|for
control|(
name|FieldSchema
name|col
range|:
name|cols
control|)
block|{
name|formatOutputPretty
argument_list|(
name|col
operator|.
name|getName
argument_list|()
argument_list|,
name|col
operator|.
name|getType
argument_list|()
argument_list|,
name|MetaDataFormatUtils
operator|.
name|getComment
argument_list|(
name|col
argument_list|)
argument_list|,
name|tableInfo
argument_list|,
name|maxColNameLen
argument_list|,
name|prettyOutputNumCols
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * If the specified comment is too long, add line breaks at appropriate    * locations.  Note that the comment may already include line-breaks    * specified by the user at table creation time.    * @param columnsAlreadyConsumed The number of columns on the current line    * that have already been consumed by the column name, column type and    * and the surrounding delimiters.    * @return The comment with line breaks added at appropriate locations.    */
specifier|private
specifier|static
name|String
name|breakCommentIntoMultipleLines
parameter_list|(
name|String
name|comment
parameter_list|,
name|int
name|columnsAlreadyConsumed
parameter_list|,
name|int
name|prettyOutputNumCols
parameter_list|)
block|{
if|if
condition|(
name|prettyOutputNumCols
operator|==
operator|-
literal|1
condition|)
block|{
comment|// XXX fixed to 80 to remove jline dep
name|prettyOutputNumCols
operator|=
literal|80
operator|-
literal|1
expr_stmt|;
block|}
name|int
name|commentNumCols
init|=
name|prettyOutputNumCols
operator|-
name|columnsAlreadyConsumed
decl_stmt|;
if|if
condition|(
name|commentNumCols
operator|<
name|MIN_COMMENT_COLUMN_LEN
condition|)
block|{
name|commentNumCols
operator|=
name|MIN_COMMENT_COLUMN_LEN
expr_stmt|;
block|}
comment|// Track the number of columns allocated for the comment that have
comment|// already been consumed on the current line.
name|int
name|commentNumColsConsumed
init|=
literal|0
decl_stmt|;
name|StringTokenizer
name|st
init|=
operator|new
name|StringTokenizer
argument_list|(
name|comment
argument_list|,
literal|" \t\n\r\f"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// We use a StringTokenizer instead of a BreakIterator, because
comment|// table comments often contain text that looks like code. For eg:
comment|// 'Type0' => 0, // This is Type 0
comment|// 'Type1' => 1, // This is Type 1
comment|// BreakIterator is meant for regular text, and was found to give
comment|// bad line breaks when we tried it out.
name|StringBuilder
name|commentBuilder
init|=
operator|new
name|StringBuilder
argument_list|(
name|comment
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
while|while
condition|(
name|st
operator|.
name|hasMoreTokens
argument_list|()
condition|)
block|{
name|String
name|currWord
init|=
name|st
operator|.
name|nextToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|currWord
operator|.
name|equals
argument_list|(
literal|"\n"
argument_list|)
operator|||
name|currWord
operator|.
name|equals
argument_list|(
literal|"\r"
argument_list|)
operator|||
name|currWord
operator|.
name|equals
argument_list|(
literal|"\f"
argument_list|)
condition|)
block|{
name|commentBuilder
operator|.
name|append
argument_list|(
name|currWord
argument_list|)
expr_stmt|;
name|commentNumColsConsumed
operator|=
literal|0
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|commentNumColsConsumed
operator|+
name|currWord
operator|.
name|length
argument_list|()
operator|>
name|commentNumCols
condition|)
block|{
comment|// currWord won't fit on the current line
if|if
condition|(
name|currWord
operator|.
name|length
argument_list|()
operator|>
name|commentNumCols
condition|)
block|{
comment|// currWord is too long to split on a line even all by itself.
comment|// Hence we have no option but to split it.  The first chunk
comment|// will go to the end of the current line.  Subsequent chunks
comment|// will be of length commentNumCols.  The last chunk
comment|// may be smaller.
while|while
condition|(
name|currWord
operator|.
name|length
argument_list|()
operator|>
name|commentNumCols
condition|)
block|{
name|int
name|remainingLineLen
init|=
name|commentNumCols
operator|-
name|commentNumColsConsumed
decl_stmt|;
name|String
name|wordChunk
init|=
name|currWord
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|remainingLineLen
argument_list|)
decl_stmt|;
name|commentBuilder
operator|.
name|append
argument_list|(
name|wordChunk
argument_list|)
expr_stmt|;
name|commentBuilder
operator|.
name|append
argument_list|(
name|MetaDataFormatUtils
operator|.
name|LINE_DELIM
argument_list|)
expr_stmt|;
name|commentNumColsConsumed
operator|=
literal|0
expr_stmt|;
name|currWord
operator|=
name|currWord
operator|.
name|substring
argument_list|(
name|remainingLineLen
argument_list|)
expr_stmt|;
block|}
comment|// Handle the last chunk
if|if
condition|(
name|currWord
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|commentBuilder
operator|.
name|append
argument_list|(
name|currWord
argument_list|)
expr_stmt|;
name|commentNumColsConsumed
operator|=
name|currWord
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Start on a new line
name|commentBuilder
operator|.
name|append
argument_list|(
name|MetaDataFormatUtils
operator|.
name|LINE_DELIM
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|currWord
operator|.
name|equals
argument_list|(
literal|" "
argument_list|)
condition|)
block|{
comment|// When starting a new line, do not start with a space.
name|commentBuilder
operator|.
name|append
argument_list|(
name|currWord
argument_list|)
expr_stmt|;
name|commentNumColsConsumed
operator|=
name|currWord
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|commentNumColsConsumed
operator|=
literal|0
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|commentBuilder
operator|.
name|append
argument_list|(
name|currWord
argument_list|)
expr_stmt|;
name|commentNumColsConsumed
operator|+=
name|currWord
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|commentBuilder
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Appends the specified text with alignment to sb.    * Also appends an appopriately sized delimiter.    * @return The number of columns consumed by the aligned string and the    * delimiter.    */
specifier|private
specifier|static
name|int
name|appendFormattedColumn
parameter_list|(
name|StringBuilder
name|sb
parameter_list|,
name|String
name|text
parameter_list|,
name|int
name|alignment
parameter_list|)
block|{
name|String
name|paddedText
init|=
name|String
operator|.
name|format
argument_list|(
literal|"%-"
operator|+
name|alignment
operator|+
literal|"s"
argument_list|,
name|text
argument_list|)
decl_stmt|;
name|int
name|delimCount
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|paddedText
operator|.
name|length
argument_list|()
operator|<
name|alignment
operator|+
name|PRETTY_MAX_INTERCOL_SPACING
condition|)
block|{
name|delimCount
operator|=
operator|(
name|alignment
operator|+
name|PRETTY_MAX_INTERCOL_SPACING
operator|)
operator|-
name|paddedText
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|delimCount
operator|=
name|PRETTY_MAX_INTERCOL_SPACING
expr_stmt|;
block|}
name|String
name|delim
init|=
name|StringUtils
operator|.
name|repeat
argument_list|(
literal|" "
argument_list|,
name|delimCount
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|paddedText
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|delim
argument_list|)
expr_stmt|;
return|return
name|paddedText
operator|.
name|length
argument_list|()
operator|+
name|delim
operator|.
name|length
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|void
name|formatOutputPretty
parameter_list|(
name|String
name|colName
parameter_list|,
name|String
name|colType
parameter_list|,
name|String
name|colComment
parameter_list|,
name|StringBuilder
name|tableInfo
parameter_list|,
name|int
name|maxColNameLength
parameter_list|,
name|int
name|prettyOutputNumCols
parameter_list|)
block|{
name|int
name|colsConsumed
init|=
literal|0
decl_stmt|;
name|colsConsumed
operator|+=
name|appendFormattedColumn
argument_list|(
name|tableInfo
argument_list|,
name|colName
argument_list|,
name|maxColNameLength
operator|+
literal|1
argument_list|)
expr_stmt|;
name|colsConsumed
operator|+=
name|appendFormattedColumn
argument_list|(
name|tableInfo
argument_list|,
name|colType
argument_list|,
name|PRETTY_ALIGNMENT
argument_list|)
expr_stmt|;
name|colComment
operator|=
name|breakCommentIntoMultipleLines
argument_list|(
name|colComment
argument_list|,
name|colsConsumed
argument_list|,
name|prettyOutputNumCols
argument_list|)
expr_stmt|;
comment|/* Comment indent processing for multi-line comments.      * Comments should be indented the same amount on each line      * if the first line comment starts indented by k,      * the following line comments should also be indented by k.      */
name|String
index|[]
name|commentSegments
init|=
name|colComment
operator|.
name|split
argument_list|(
literal|"\n|\r|\r\n"
argument_list|)
decl_stmt|;
name|tableInfo
operator|.
name|append
argument_list|(
name|trimTrailingWS
argument_list|(
name|commentSegments
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|tableInfo
operator|.
name|append
argument_list|(
name|MetaDataFormatUtils
operator|.
name|LINE_DELIM
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|commentSegments
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|tableInfo
operator|.
name|append
argument_list|(
name|StringUtils
operator|.
name|repeat
argument_list|(
literal|" "
argument_list|,
name|colsConsumed
argument_list|)
argument_list|)
expr_stmt|;
name|tableInfo
operator|.
name|append
argument_list|(
name|trimTrailingWS
argument_list|(
name|commentSegments
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|tableInfo
operator|.
name|append
argument_list|(
name|MetaDataFormatUtils
operator|.
name|LINE_DELIM
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|String
name|trimTrailingWS
parameter_list|(
name|String
name|str
parameter_list|)
block|{
return|return
name|str
operator|.
name|replaceAll
argument_list|(
literal|"\\s+$"
argument_list|,
literal|""
argument_list|)
return|;
block|}
block|}
end_class

end_unit

