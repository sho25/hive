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
name|sql
operator|.
name|Statement
import|;
end_import

begin_import
import|import
name|sun
operator|.
name|misc
operator|.
name|Signal
import|;
end_import

begin_import
import|import
name|sun
operator|.
name|misc
operator|.
name|SignalHandler
import|;
end_import

begin_class
specifier|public
class|class
name|SunSignalHandler
implements|implements
name|BeeLineSignalHandler
implements|,
name|SignalHandler
block|{
specifier|private
name|Statement
name|stmt
init|=
literal|null
decl_stmt|;
name|SunSignalHandler
parameter_list|()
block|{
comment|// Interpret Ctrl+C as a request to cancel the currently
comment|// executing query.
name|Signal
operator|.
name|handle
argument_list|(
operator|new
name|Signal
argument_list|(
literal|"INT"
argument_list|)
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setStatement
parameter_list|(
name|Statement
name|stmt
parameter_list|)
block|{
name|this
operator|.
name|stmt
operator|=
name|stmt
expr_stmt|;
block|}
specifier|public
name|void
name|handle
parameter_list|(
name|Signal
name|signal
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|stmt
operator|!=
literal|null
condition|)
block|{
name|stmt
operator|.
name|cancel
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SQLException
name|ex
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
end_class

end_unit

