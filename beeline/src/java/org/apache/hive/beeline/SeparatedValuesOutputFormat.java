begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
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
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|supercsv
operator|.
name|io
operator|.
name|CsvListWriter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|supercsv
operator|.
name|prefs
operator|.
name|CsvPreference
import|;
end_import

begin_comment
comment|/**  * OutputFormat for values separated by a delimiter.  */
end_comment

begin_class
class|class
name|SeparatedValuesOutputFormat
implements|implements
name|OutputFormat
block|{
specifier|public
specifier|final
specifier|static
name|String
name|DISABLE_QUOTING_FOR_SV
init|=
literal|"disable.quoting.for.sv"
decl_stmt|;
specifier|private
specifier|final
name|BeeLine
name|beeLine
decl_stmt|;
specifier|private
name|CsvPreference
name|quotedCsvPreference
decl_stmt|;
specifier|private
name|CsvPreference
name|unquotedCsvPreference
decl_stmt|;
name|SeparatedValuesOutputFormat
parameter_list|(
name|BeeLine
name|beeLine
parameter_list|,
name|char
name|separator
parameter_list|)
block|{
name|this
operator|.
name|beeLine
operator|=
name|beeLine
expr_stmt|;
name|unquotedCsvPreference
operator|=
operator|new
name|CsvPreference
operator|.
name|Builder
argument_list|(
literal|'\0'
argument_list|,
name|separator
argument_list|,
literal|""
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|quotedCsvPreference
operator|=
operator|new
name|CsvPreference
operator|.
name|Builder
argument_list|(
literal|'"'
argument_list|,
name|separator
argument_list|,
literal|""
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|updateCsvPreference
parameter_list|()
block|{
if|if
condition|(
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|getOutputFormat
argument_list|()
operator|.
name|equals
argument_list|(
literal|"dsv"
argument_list|)
condition|)
block|{
comment|// check whether delimiter changed by user
name|char
name|curDel
init|=
operator|(
name|char
operator|)
name|getCsvPreference
argument_list|()
operator|.
name|getDelimiterChar
argument_list|()
decl_stmt|;
name|char
name|newDel
init|=
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|getDelimiterForDSV
argument_list|()
decl_stmt|;
comment|// if delimiter changed, rebuild the csv preference
if|if
condition|(
name|newDel
operator|!=
name|curDel
condition|)
block|{
comment|// "" is passed as the end of line symbol in following function, as
comment|// beeline itself adds newline
if|if
condition|(
name|isQuotingDisabled
argument_list|()
condition|)
block|{
name|unquotedCsvPreference
operator|=
operator|new
name|CsvPreference
operator|.
name|Builder
argument_list|(
literal|'\0'
argument_list|,
name|newDel
argument_list|,
literal|""
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|quotedCsvPreference
operator|=
operator|new
name|CsvPreference
operator|.
name|Builder
argument_list|(
literal|'"'
argument_list|,
name|newDel
argument_list|,
literal|""
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|print
parameter_list|(
name|Rows
name|rows
parameter_list|)
block|{
name|updateCsvPreference
argument_list|()
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|rows
operator|.
name|hasNext
argument_list|()
condition|)
block|{
if|if
condition|(
name|count
operator|==
literal|0
operator|&&
operator|!
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|getShowHeader
argument_list|()
condition|)
block|{
name|rows
operator|.
name|next
argument_list|()
expr_stmt|;
name|count
operator|++
expr_stmt|;
continue|continue;
block|}
name|printRow
argument_list|(
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
operator|-
literal|1
return|;
comment|// sans header row
block|}
specifier|private
name|String
name|getFormattedStr
parameter_list|(
name|String
index|[]
name|vals
parameter_list|)
block|{
name|StringWriter
name|strWriter
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|CsvListWriter
name|writer
init|=
operator|new
name|CsvListWriter
argument_list|(
name|strWriter
argument_list|,
name|getCsvPreference
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|vals
operator|.
name|length
operator|>
literal|0
condition|)
block|{
try|try
block|{
name|writer
operator|.
name|write
argument_list|(
name|vals
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|beeLine
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|writer
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|strWriter
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|void
name|printRow
parameter_list|(
name|Rows
operator|.
name|Row
name|row
parameter_list|)
block|{
name|String
index|[]
name|vals
init|=
name|row
operator|.
name|values
decl_stmt|;
name|String
name|formattedStr
init|=
name|getFormattedStr
argument_list|(
name|vals
argument_list|)
decl_stmt|;
name|beeLine
operator|.
name|output
argument_list|(
name|formattedStr
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|isQuotingDisabled
parameter_list|()
block|{
name|String
name|quotingDisabledStr
init|=
name|System
operator|.
name|getProperty
argument_list|(
name|SeparatedValuesOutputFormat
operator|.
name|DISABLE_QUOTING_FOR_SV
argument_list|)
decl_stmt|;
if|if
condition|(
name|quotingDisabledStr
operator|==
literal|null
operator|||
name|quotingDisabledStr
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// default is disabling the double quoting for separated value
return|return
literal|true
return|;
block|}
name|String
name|parsedOptionStr
init|=
name|quotingDisabledStr
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
if|if
condition|(
name|parsedOptionStr
operator|.
name|equals
argument_list|(
literal|"false"
argument_list|)
operator|||
name|parsedOptionStr
operator|.
name|equals
argument_list|(
literal|"true"
argument_list|)
condition|)
block|{
return|return
name|Boolean
operator|.
name|valueOf
argument_list|(
name|parsedOptionStr
argument_list|)
return|;
block|}
else|else
block|{
name|beeLine
operator|.
name|error
argument_list|(
literal|"System Property disable.quoting.for.sv is now "
operator|+
name|parsedOptionStr
operator|+
literal|" which only accepts boolean value"
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
specifier|private
name|CsvPreference
name|getCsvPreference
parameter_list|()
block|{
if|if
condition|(
name|isQuotingDisabled
argument_list|()
condition|)
block|{
return|return
name|unquotedCsvPreference
return|;
block|}
else|else
block|{
return|return
name|quotedCsvPreference
return|;
block|}
block|}
block|}
end_class

end_unit

