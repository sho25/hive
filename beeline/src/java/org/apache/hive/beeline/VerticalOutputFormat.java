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
comment|/**  * OutputFormat for vertical column name: value format.  *  */
end_comment

begin_class
class|class
name|VerticalOutputFormat
implements|implements
name|OutputFormat
block|{
specifier|private
specifier|final
name|BeeLine
name|beeLine
decl_stmt|;
comment|/**    * @param beeLine    */
name|VerticalOutputFormat
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
name|count
init|=
literal|0
decl_stmt|;
name|Rows
operator|.
name|Row
name|header
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
while|while
condition|(
name|rows
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|printRow
argument_list|(
name|rows
argument_list|,
name|header
argument_list|,
operator|(
name|Rows
operator|.
name|Row
operator|)
name|rows
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
return|return
name|count
return|;
block|}
specifier|public
name|void
name|printRow
parameter_list|(
name|Rows
name|rows
parameter_list|,
name|Rows
operator|.
name|Row
name|header
parameter_list|,
name|Rows
operator|.
name|Row
name|row
parameter_list|)
block|{
name|String
index|[]
name|head
init|=
name|header
operator|.
name|values
decl_stmt|;
name|String
index|[]
name|vals
init|=
name|row
operator|.
name|values
decl_stmt|;
name|int
name|headwidth
init|=
literal|0
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
name|head
operator|.
name|length
operator|&&
name|i
operator|<
name|vals
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|headwidth
operator|=
name|Math
operator|.
name|max
argument_list|(
name|headwidth
argument_list|,
name|head
index|[
name|i
index|]
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|headwidth
operator|+=
literal|2
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
name|head
operator|.
name|length
operator|&&
name|i
operator|<
name|vals
operator|.
name|length
condition|;
name|i
operator|++
control|)
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
name|bold
argument_list|(
name|beeLine
operator|.
name|getColorBuffer
argument_list|()
operator|.
name|pad
argument_list|(
name|head
index|[
name|i
index|]
argument_list|,
name|headwidth
argument_list|)
operator|.
name|getMono
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
name|vals
index|[
name|i
index|]
operator|==
literal|null
condition|?
literal|""
else|:
name|vals
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|beeLine
operator|.
name|output
argument_list|(
literal|""
argument_list|)
expr_stmt|;
comment|// spacing
block|}
block|}
end_class

end_unit

