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
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|jline
operator|.
name|console
operator|.
name|completer
operator|.
name|Completer
import|;
end_import

begin_comment
comment|/**  * Completor for BeeLine. It dispatches to sub-completors based on the  * current arguments.  *  */
end_comment

begin_class
class|class
name|BeeLineCompleter
implements|implements
name|Completer
block|{
specifier|private
specifier|final
name|BeeLine
name|beeLine
decl_stmt|;
comment|/**    * @param beeLine    */
name|BeeLineCompleter
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
name|complete
parameter_list|(
name|String
name|buf
parameter_list|,
name|int
name|pos
parameter_list|,
name|List
name|cand
parameter_list|)
block|{
if|if
condition|(
name|buf
operator|!=
literal|null
operator|&&
name|buf
operator|.
name|startsWith
argument_list|(
name|BeeLine
operator|.
name|COMMAND_PREFIX
argument_list|)
operator|&&
operator|!
name|buf
operator|.
name|startsWith
argument_list|(
name|BeeLine
operator|.
name|COMMAND_PREFIX
operator|+
literal|"all"
argument_list|)
operator|&&
operator|!
name|buf
operator|.
name|startsWith
argument_list|(
name|BeeLine
operator|.
name|COMMAND_PREFIX
operator|+
literal|"sql"
argument_list|)
condition|)
block|{
return|return
name|beeLine
operator|.
name|getCommandCompletor
argument_list|()
operator|.
name|complete
argument_list|(
name|buf
argument_list|,
name|pos
argument_list|,
name|cand
argument_list|)
return|;
block|}
else|else
block|{
if|if
condition|(
name|beeLine
operator|.
name|getDatabaseConnection
argument_list|()
operator|!=
literal|null
operator|&&
name|beeLine
operator|.
name|getDatabaseConnection
argument_list|()
operator|.
name|getSQLCompleter
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|beeLine
operator|.
name|getDatabaseConnection
argument_list|()
operator|.
name|getSQLCompleter
argument_list|()
operator|.
name|complete
argument_list|(
name|buf
argument_list|,
name|pos
argument_list|,
name|cand
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|-
literal|1
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

