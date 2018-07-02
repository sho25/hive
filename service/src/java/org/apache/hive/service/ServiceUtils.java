begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|conf
operator|.
name|HiveConf
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

begin_class
specifier|public
class|class
name|ServiceUtils
block|{
comment|/*    * Get the index separating the user name from domain name (the user's name up    * to the first '/' or '@').    *    * @param userName full user name.    * @return index of domain match or -1 if not found    */
specifier|public
specifier|static
name|int
name|indexOfDomainMatch
parameter_list|(
name|String
name|userName
parameter_list|)
block|{
if|if
condition|(
name|userName
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|int
name|idx
init|=
name|userName
operator|.
name|indexOf
argument_list|(
literal|'/'
argument_list|)
decl_stmt|;
name|int
name|idx2
init|=
name|userName
operator|.
name|indexOf
argument_list|(
literal|'@'
argument_list|)
decl_stmt|;
name|int
name|endIdx
init|=
name|Math
operator|.
name|min
argument_list|(
name|idx
argument_list|,
name|idx2
argument_list|)
decl_stmt|;
comment|// Use the earlier match.
comment|// Unless at least one of '/' or '@' was not found, in
comment|// which case, user the latter match.
if|if
condition|(
name|endIdx
operator|==
operator|-
literal|1
condition|)
block|{
name|endIdx
operator|=
name|Math
operator|.
name|max
argument_list|(
name|idx
argument_list|,
name|idx2
argument_list|)
expr_stmt|;
block|}
return|return
name|endIdx
return|;
block|}
comment|/**    * Close the Closeable objects and<b>ignore</b> any {@link IOException} or    * null pointers. Must only be used for cleanup in exception handlers.    *    * @param log the log to record problems to at debug level. Can be null.    * @param closeables the objects to close    */
specifier|public
specifier|static
name|void
name|cleanup
parameter_list|(
name|Logger
name|log
parameter_list|,
name|java
operator|.
name|io
operator|.
name|Closeable
modifier|...
name|closeables
parameter_list|)
block|{
for|for
control|(
name|java
operator|.
name|io
operator|.
name|Closeable
name|c
range|:
name|closeables
control|)
block|{
if|if
condition|(
name|c
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|c
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|log
operator|!=
literal|null
operator|&&
name|log
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|log
operator|.
name|debug
argument_list|(
literal|"Exception in closing "
operator|+
name|c
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
specifier|public
specifier|static
name|boolean
name|canProvideProgressLog
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
return|return
operator|(
literal|"tez"
operator|.
name|equals
argument_list|(
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
argument_list|)
operator|||
literal|"spark"
operator|.
name|equals
argument_list|(
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
argument_list|)
operator|)
operator|&&
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_INPLACE_PROGRESS
argument_list|)
return|;
block|}
block|}
end_class

end_unit

