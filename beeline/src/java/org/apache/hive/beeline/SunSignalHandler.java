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

