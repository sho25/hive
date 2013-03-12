begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  Copyright (c) 2002,2003,2004,2005 Marc Prud'hommeaux  *  All rights reserved.  *  *  *  Redistribution and use in source and binary forms,  *  with or without modification, are permitted provided  *  that the following conditions are met:  *  *  Redistributions of source code must retain the above  *  copyright notice, this list of conditions and the following  *  disclaimer.  *  Redistributions in binary form must reproduce the above  *  copyright notice, this list of conditions and the following  *  disclaimer in the documentation and/or other materials  *  provided with the distribution.  *  Neither the name of the<ORGANIZATION> nor the names  *  of its contributors may be used to endorse or promote  *  products derived from this software without specific  *  prior written permission.  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS  *  AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED  *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR  *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT  *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,  *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE  *  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR  *  BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY  *  OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,  *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING  *  IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  *  *  This software is hosted by SourceForge.  *  SourceForge is a trademark of VA Linux Systems, Inc.  */
end_comment

begin_comment
comment|/*  * This source file is based on code taken from SQLLine 1.0.2  * The license above originally appeared in src/sqlline/SqlLine.java  * http://sqlline.sourceforge.net/  */
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

begin_comment
comment|/**  * OutputFormat for a pretty, table-like format.  *  */
end_comment

begin_class
class|class
name|TableOutputFormat
implements|implements
name|OutputFormat
block|{
specifier|private
specifier|final
name|BeeLine
name|beeLine
decl_stmt|;
specifier|private
specifier|final
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
comment|/**    * @param beeLine    */
name|TableOutputFormat
parameter_list|(
name|BeeLine
name|beeLine
parameter_list|)
block|{
name|this
operator|.
name|beeLine
operator|=
name|beeLine
expr_stmt|;
block|}
specifier|public
name|int
name|print
parameter_list|(
name|Rows
name|rows
parameter_list|)
block|{
name|int
name|index
init|=
literal|0
decl_stmt|;
name|ColorBuffer
name|header
init|=
literal|null
decl_stmt|;
name|ColorBuffer
name|headerCols
init|=
literal|null
decl_stmt|;
specifier|final
name|int
name|width
init|=
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|getMaxWidth
argument_list|()
operator|-
literal|4
decl_stmt|;
comment|// normalize the columns sizes
name|rows
operator|.
name|normalizeWidths
argument_list|()
expr_stmt|;
for|for
control|(
init|;
name|rows
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Rows
operator|.
name|Row
name|row
init|=
operator|(
name|Rows
operator|.
name|Row
operator|)
name|rows
operator|.
name|next
argument_list|()
decl_stmt|;
name|ColorBuffer
name|cbuf
init|=
name|getOutputString
argument_list|(
name|rows
argument_list|,
name|row
argument_list|)
decl_stmt|;
name|cbuf
operator|=
name|cbuf
operator|.
name|truncate
argument_list|(
name|width
argument_list|)
expr_stmt|;
if|if
condition|(
name|index
operator|==
literal|0
condition|)
block|{
name|sb
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|row
operator|.
name|sizes
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|row
operator|.
name|sizes
index|[
name|j
index|]
condition|;
name|k
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|'-'
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"-+-"
argument_list|)
expr_stmt|;
block|}
name|headerCols
operator|=
name|cbuf
expr_stmt|;
name|header
operator|=
name|beeLine
operator|.
name|getColorBuffer
argument_list|()
operator|.
name|green
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|truncate
argument_list|(
name|headerCols
operator|.
name|getVisibleLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|index
operator|==
literal|0
operator|||
operator|(
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|getHeaderInterval
argument_list|()
operator|>
literal|0
operator|&&
name|index
operator|%
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|getHeaderInterval
argument_list|()
operator|==
literal|0
operator|&&
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|getShowHeader
argument_list|()
operator|)
condition|)
block|{
name|printRow
argument_list|(
name|header
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|printRow
argument_list|(
name|headerCols
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|printRow
argument_list|(
name|header
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|index
operator|!=
literal|0
condition|)
block|{
name|printRow
argument_list|(
name|cbuf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|index
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|header
operator|!=
literal|null
operator|&&
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|getShowHeader
argument_list|()
condition|)
block|{
name|printRow
argument_list|(
name|header
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
return|return
name|index
operator|-
literal|1
return|;
block|}
name|void
name|printRow
parameter_list|(
name|ColorBuffer
name|cbuff
parameter_list|,
name|boolean
name|header
parameter_list|)
block|{
if|if
condition|(
name|header
condition|)
block|{
name|beeLine
operator|.
name|output
argument_list|(
name|beeLine
operator|.
name|getColorBuffer
argument_list|()
operator|.
name|green
argument_list|(
literal|"+-"
argument_list|)
operator|.
name|append
argument_list|(
name|cbuff
argument_list|)
operator|.
name|green
argument_list|(
literal|"-+"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|beeLine
operator|.
name|output
argument_list|(
name|beeLine
operator|.
name|getColorBuffer
argument_list|()
operator|.
name|green
argument_list|(
literal|"| "
argument_list|)
operator|.
name|append
argument_list|(
name|cbuff
argument_list|)
operator|.
name|green
argument_list|(
literal|" |"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|ColorBuffer
name|getOutputString
parameter_list|(
name|Rows
name|rows
parameter_list|,
name|Rows
operator|.
name|Row
name|row
parameter_list|)
block|{
return|return
name|getOutputString
argument_list|(
name|rows
argument_list|,
name|row
argument_list|,
literal|" | "
argument_list|)
return|;
block|}
name|ColorBuffer
name|getOutputString
parameter_list|(
name|Rows
name|rows
parameter_list|,
name|Rows
operator|.
name|Row
name|row
parameter_list|,
name|String
name|delim
parameter_list|)
block|{
name|ColorBuffer
name|buf
init|=
name|beeLine
operator|.
name|getColorBuffer
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
name|row
operator|.
name|values
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|buf
operator|.
name|getVisibleLength
argument_list|()
operator|>
literal|0
condition|)
block|{
name|buf
operator|.
name|green
argument_list|(
name|delim
argument_list|)
expr_stmt|;
block|}
name|ColorBuffer
name|v
decl_stmt|;
if|if
condition|(
name|row
operator|.
name|isMeta
condition|)
block|{
name|v
operator|=
name|beeLine
operator|.
name|getColorBuffer
argument_list|()
operator|.
name|center
argument_list|(
name|row
operator|.
name|values
index|[
name|i
index|]
argument_list|,
name|row
operator|.
name|sizes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|rows
operator|.
name|isPrimaryKey
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|buf
operator|.
name|cyan
argument_list|(
name|v
operator|.
name|getMono
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|buf
operator|.
name|bold
argument_list|(
name|v
operator|.
name|getMono
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|v
operator|=
name|beeLine
operator|.
name|getColorBuffer
argument_list|()
operator|.
name|pad
argument_list|(
name|row
operator|.
name|values
index|[
name|i
index|]
argument_list|,
name|row
operator|.
name|sizes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|rows
operator|.
name|isPrimaryKey
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|buf
operator|.
name|cyan
argument_list|(
name|v
operator|.
name|getMono
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|buf
operator|.
name|append
argument_list|(
name|v
operator|.
name|getMono
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|row
operator|.
name|deleted
condition|)
block|{
name|buf
operator|=
name|beeLine
operator|.
name|getColorBuffer
argument_list|()
operator|.
name|red
argument_list|(
name|buf
operator|.
name|getMono
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|row
operator|.
name|updated
condition|)
block|{
name|buf
operator|=
name|beeLine
operator|.
name|getColorBuffer
argument_list|()
operator|.
name|blue
argument_list|(
name|buf
operator|.
name|getMono
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|row
operator|.
name|inserted
condition|)
block|{
name|buf
operator|=
name|beeLine
operator|.
name|getColorBuffer
argument_list|()
operator|.
name|green
argument_list|(
name|buf
operator|.
name|getMono
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|buf
return|;
block|}
block|}
end_class

end_unit

