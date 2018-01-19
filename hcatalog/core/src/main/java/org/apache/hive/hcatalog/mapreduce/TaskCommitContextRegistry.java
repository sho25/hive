begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|mapreduce
package|;
end_package

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
name|mapreduce
operator|.
name|TaskAttemptContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|util
operator|.
name|HashMap
import|;
end_import

begin_comment
comment|/**  * Singleton Registry to track the commit of TaskAttempts.  * Used to manage commits for Tasks that create dynamic-partitions.  */
end_comment

begin_class
specifier|public
class|class
name|TaskCommitContextRegistry
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TaskCommitContextRegistry
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|TaskCommitContextRegistry
name|ourInstance
init|=
operator|new
name|TaskCommitContextRegistry
argument_list|()
decl_stmt|;
comment|/**    * Singleton instance getter.    */
specifier|public
specifier|static
name|TaskCommitContextRegistry
name|getInstance
parameter_list|()
block|{
return|return
name|ourInstance
return|;
block|}
comment|/**    * Implement this interface to register call-backs for committing TaskAttempts.    */
specifier|public
specifier|static
interface|interface
name|TaskCommitterProxy
block|{
comment|/**      * Call-back for Committer's abortTask().      */
specifier|public
name|void
name|abortTask
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Call-back for Committer's abortTask().      */
specifier|public
name|void
name|commitTask
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|TaskCommitterProxy
argument_list|>
name|taskCommitters
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|TaskCommitterProxy
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Trigger commit for TaskAttempt, as specified by the TaskAttemptContext argument.    */
specifier|public
specifier|synchronized
name|void
name|commitTask
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|key
init|=
name|generateKey
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|taskCommitters
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No callback registered for TaskAttemptID:"
operator|+
name|key
operator|+
literal|". Skipping."
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Committing TaskAttempt:"
operator|+
name|key
argument_list|)
expr_stmt|;
name|taskCommitters
operator|.
name|get
argument_list|(
name|key
argument_list|)
operator|.
name|commitTask
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not clean up TaskAttemptID:"
operator|+
name|key
argument_list|,
name|t
argument_list|)
throw|;
block|}
block|}
specifier|private
name|String
name|generateKey
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|jobInfoString
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_OUTPUT_INFO
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|jobInfoString
argument_list|)
condition|)
block|{
comment|// Avoid the NPE.
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not retrieve OutputJobInfo for TaskAttempt "
operator|+
name|context
operator|.
name|getTaskAttemptID
argument_list|()
argument_list|)
throw|;
block|}
name|OutputJobInfo
name|jobInfo
init|=
operator|(
name|OutputJobInfo
operator|)
name|HCatUtil
operator|.
name|deserialize
argument_list|(
name|jobInfoString
argument_list|)
decl_stmt|;
return|return
name|context
operator|.
name|getTaskAttemptID
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|"@"
operator|+
name|jobInfo
operator|.
name|getLocation
argument_list|()
return|;
block|}
comment|/**    * Trigger abort for TaskAttempt, as specified by the TaskAttemptContext argument.    */
specifier|public
specifier|synchronized
name|void
name|abortTask
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|key
init|=
name|generateKey
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|taskCommitters
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No callback registered for TaskAttemptID:"
operator|+
name|key
operator|+
literal|". Skipping."
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Aborting TaskAttempt:"
operator|+
name|key
argument_list|)
expr_stmt|;
name|taskCommitters
operator|.
name|get
argument_list|(
name|key
argument_list|)
operator|.
name|abortTask
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not clean up TaskAttemptID:"
operator|+
name|key
argument_list|,
name|t
argument_list|)
throw|;
block|}
block|}
comment|/**    * Method to register call-backs to control commits and aborts of TaskAttempts.    * @param context The TaskAttemptContext instance for the task-attempt, identifying the output.    * @param committer Instance of TaskCommitterProxy, to commit/abort a TaskAttempt.    * @throws java.io.IOException On failure.    */
specifier|public
specifier|synchronized
name|void
name|register
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|,
name|TaskCommitterProxy
name|committer
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|key
init|=
name|generateKey
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Registering committer for TaskAttemptID:"
operator|+
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
name|taskCommitters
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Replacing previous committer:"
operator|+
name|committer
argument_list|)
expr_stmt|;
block|}
name|taskCommitters
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|committer
argument_list|)
expr_stmt|;
block|}
comment|/**    * Method to discard the committer call-backs for a specified TaskAttemptID.    * @param context The TaskAttemptContext instance for the task-attempt, identifying the output.    * @throws java.io.IOException On failure.    */
specifier|public
specifier|synchronized
name|void
name|discardCleanupFor
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|key
init|=
name|generateKey
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Discarding all cleanup for TaskAttemptID:"
operator|+
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|taskCommitters
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No committer registered for TaskAttemptID:"
operator|+
name|key
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|taskCommitters
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Hide constructor, for make benefit glorious Singleton.
specifier|private
name|TaskCommitContextRegistry
parameter_list|()
block|{   }
block|}
end_class

end_unit

